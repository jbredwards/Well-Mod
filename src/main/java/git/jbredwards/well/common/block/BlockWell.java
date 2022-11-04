package git.jbredwards.well.common.block;

import git.jbredwards.well.Main;
import git.jbredwards.well.common.config.ConfigHandler;
import git.jbredwards.well.common.tileentity.TileEntityWell;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("deprecation")
public class BlockWell extends Block implements ITileEntityProvider
{
    @Nonnull public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class, EnumFacing.Axis::isHorizontal);
    @Nonnull public static final PropertyBool IS_BOTTOM = PropertyBool.create("is_bottom");

    @Nullable
    protected FluidStack cachedFluid;
    public BlockWell(@Nonnull Material materialIn) { this(materialIn, materialIn.getMaterialMapColor()); }
    public BlockWell(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        setDefaultState(getDefaultState().withProperty(IS_BOTTOM, true));
        useNeighborBrightness = true;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this)
                .add(FluidUnlistedProperty.INSTANCE)
                .add(AXIS, IS_BOTTOM)
                .build();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(IS_BOTTOM, (meta & 1) == 1)
                .withProperty(AXIS, EnumFacing.Axis.values()[MathHelper.clamp(meta >> 1, 0, 2)]);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(AXIS).ordinal() << 1 | (state.getValue(IS_BOTTOM) ? 1 : 0);
    }

    @Override
    public boolean hasTileEntity(@Nonnull IBlockState state) { return state.getValue(IS_BOTTOM); }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) { return new TileEntityWell(); }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return hasTileEntity(state) ? createNewTileEntity(world, 0) : null;
    }

    @Override
    public boolean canPlaceBlockAt(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos.up());
    }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        return getDefaultState().withProperty(AXIS, placer.getHorizontalFacing().getAxis());
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        worldIn.setBlockState(pos.up(), state.withProperty(IS_BOTTOM, false), Constants.BlockFlags.SEND_TO_CLIENTS);
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        final @Nullable TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityWell) {
            ((TileEntityWell)tile).countNearbyWells(te -> te.nearbyWells--);
            worldIn.removeTileEntity(pos);
        }
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        if(pos.equals(fromPos.down()) && state.getValue(IS_BOTTOM) && worldIn.getBlockState(fromPos).getBlock() != this)
            worldIn.destroyBlock(pos, false);

        else if(pos.equals(fromPos.up()) && !state.getValue(IS_BOTTOM) && worldIn.getBlockState(fromPos).getBlock() != this)
            worldIn.destroyBlock(pos, false);
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        final @Nullable TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityWell && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
            final @Nullable IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
            return handler != null && FluidUtil.interactWithFluidHandler(playerIn, hand, handler);
        }

        return false;
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        if(state.getValue(IS_BOTTOM)) {
            final @Nullable TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileEntityWell) {
                final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
                if(fluid != null && fluid.getFluid().canBePlacedInWorld()) {
                    final float baseFluidLight = fluid.getFluid().getBlock().getDefaultState().getLightValue();
                    if(baseFluidLight > 0) {
                        if(Main.proxy.isTranslucentActive()) return Math.max(state.getLightValue(), (int)baseFluidLight);
                        final int fluidLight = MathHelper.clamp((int)(baseFluidLight * fluid.amount / ConfigHandler.tankCapacity + 0.5), 1, 15);
                        return Math.max(state.getLightValue(), fluidLight);
                    }
                }
            }
        }

        return state.getLightValue();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getPackedLightmapCoords(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        if(Main.proxy.isTranslucentActive()) {
            final @Nullable TileEntity tile = source.getTileEntity(pos);
            if(tile instanceof TileEntityWell) {
                final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
                if(fluid != null && fluid.getFluid().canBePlacedInWorld()) {
                    final int fluidLight = fluid.getFluid().getBlock().getDefaultState().getLightValue();
                    if(fluidLight > 0) return source.getCombinedLight(pos, Math.max(fluidLight, state.getLightValue()));
                }
            }
        }

        return super.getPackedLightmapCoords(state, source, pos);
    }

    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        WellCollisions.getCollisionBoxList(state).forEach(aabb -> addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb));
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        final List<RayTraceResult> list = WellCollisions.getTraceBoxList(blockState).stream()
                .map(aabb -> rayTrace(pos, start, end, aabb))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if(list.isEmpty()) return null;
        RayTraceResult furthest = null;
        double dist = -1;

        for(RayTraceResult trace : list) {
            final double newDist = trace.hitVec.squareDistanceTo(end);
            if(newDist > dist) {
                furthest = trace;
                dist = newDist;
            }
        }

        return furthest;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        final boolean isBottom = state.getValue(IS_BOTTOM);
        return new AxisAlignedBB(0, isBottom ? 0 : -1, 0, 1, isBottom ? 2 : 1, 1).offset(pos);
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState iblockstate, @Nonnull Entity entity, double yToTest, @Nonnull Material materialIn, boolean testingHead) {
        if(!testingHead) yToTest = entity.posY;
        final @Nullable TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityWell) {
            final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
            if(fluid != null && fluid.getFluid().canBePlacedInWorld())
                if(fluid.getFluid().getBlock().getDefaultState().getMaterial() == materialIn)
                    return yToTest < pos.getY() + ConfigHandler.getRenderedFluidHeight(fluid);
        }

        return null;
    }

    @Nullable
    @Override
    public Boolean isAABBInsideMaterial(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB boundingBox, @Nonnull Material materialIn) {
        final @Nullable TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityWell) {
            final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
            if(fluid != null && fluid.getFluid().canBePlacedInWorld()) {
                if(fluid.getFluid().getBlock().getDefaultState().getMaterial() == materialIn) {
                    cachedFluid = fluid;
                    return isAABBInsideLiquid(world, pos, boundingBox);
                }
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Boolean isAABBInsideLiquid(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB boundingBox) {
        if(cachedFluid == null) {
            final @Nullable TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileEntityWell) {
                final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
                if(fluid != null && fluid.getFluid().canBePlacedInWorld()) cachedFluid = fluid;
            }
        }

        final @Nullable FluidStack fluid = cachedFluid;
        cachedFluid = null;

        return fluid != null && boundingBox.minY < pos.getY()
                + ConfigHandler.getRenderedFluidHeight(fluid)
                ? true : null;
    }

    @Override
    public float getBlockLiquidHeight(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Material material) {
        final @Nullable TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityWell) {
            final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
            if(fluid != null && fluid.getFluid().canBePlacedInWorld())
                if(fluid.getFluid().getBlock().getDefaultState().getMaterial() == material)
                    return ConfigHandler.getRenderedFluidHeight(fluid);
        }

        return 0;
    }

    @Nonnull
    @Override
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) { return EnumPushReaction.BLOCK; }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) { return false; }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) { return false; }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return state.getValue(IS_BOTTOM) && face != EnumFacing.UP ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isSideSolid(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return state.getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID;
    }

    @Override
    public boolean doesSideBlockRendering(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return state.isSideSolid(world, pos, face);
    }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || state.getValue(IS_BOTTOM) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        if(state instanceof IExtendedBlockState) {
            final @Nullable TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileEntityWell) state = ((IExtendedBlockState)state)
                    .withProperty(FluidUnlistedProperty.INSTANCE, ((TileEntityWell)tile).tank.getFluid());
        }

        return state;
    }
}
