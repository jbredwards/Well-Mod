package git.jbredwards.well.common.tileentity;

import git.jbredwards.well.common.init.RegistryHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class TileEntityWell extends TileEntity implements ITickable
{
    @Nonnull
    public final FluidTankSynced tank = new FluidTankSynced(this, 100000);
    public long fillTick = 0;

    @Override
    public void update() {
        if(hasWorld() && !world.isRemote && !world.provider.doesWaterVaporize() && fillTick <= world.getTotalWorldTime()) {
            tank.fill(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), true);
            initFillTick();
        }
    }

    @Override
    public void onLoad() {
        if(fillTick == 0) initFillTick();
        if(tank.updateLight(tank.getFluid()))
            world.markBlockRangeForRenderUpdate(pos, pos);
    }

    protected void initFillTick() { fillTick = world.getTotalWorldTime() + 25 + world.rand.nextInt(50); }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 0, getUpdateTag()); }

    @Override
    public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
        final @Nullable FluidStack oldFluid = tank.getFluid();
        handleUpdateTag(pkt.getNbtCompound());
        final @Nullable FluidStack newFluid = tank.getFluid();

        final boolean wasEmpty = newFluid != null && oldFluid == null;
        final boolean wasFull = newFluid == null && oldFluid != null;

        //update renderer and light level if needed
        if(wasEmpty || wasFull || newFluid != null && newFluid.amount != oldFluid.amount) {
            world.markBlockRangeForRenderUpdate(pos, pos);
            if(newFluid != null) tank.updateLight(newFluid);
            else tank.updateLight(oldFluid);
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag);
        tank.readFromNBT(tag);
        fillTick = tag.getLong("fillTick");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tank.writeToNBT(tag);
        tag.setLong("fillTick", fillTick);
        return tag;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T)tank;
        return super.getCapability(capability, facing);
    }

    public static class FluidTankSynced extends FluidTank
    {
        public FluidTankSynced(@Nonnull TileEntity tile, int capacity) {
            super(capacity);
            setTileEntity(tile);
        }

        @Override
        public boolean canFillFluidType(@Nonnull FluidStack fluid) {
            if(fluid.getFluid().isLighterThanAir()) return false;
            if(tile.getWorld().provider.doesWaterVaporize() && fluid.getFluid().doesVaporize(fluid)) return false;
            return canFill();
        }

        @Override
        public int fillInternal(@Nullable FluidStack resource, boolean doFill) {
            final int fill = super.fillInternal(resource, doFill);
            if(doFill && fill > 0) {
                final IBlockState state = RegistryHandler.WELL_BLOCK.getDefaultState();
                tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, Constants.BlockFlags.DEFAULT);
                updateLight(resource);
            }

            return fill;
        }

        @Nullable
        @Override
        public FluidStack drainInternal(int maxDrain, boolean doDrain) {
            final @Nullable FluidStack resource = super.drainInternal(maxDrain, doDrain);
            if(resource != null && doDrain) {
                final IBlockState state = RegistryHandler.WELL_BLOCK.getDefaultState();
                tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, Constants.BlockFlags.DEFAULT);
                updateLight(resource);
            }

            return resource;
        }

        protected boolean updateLight(@Nullable FluidStack resource) {
            if(resource != null && resource.getFluid().canBePlacedInWorld()) {
                if(resource.getFluid().getBlock().getDefaultState().getLightValue() > 0)
                    return tile.getWorld().checkLight(tile.getPos());
            }

            return false;
        }
    }
}
