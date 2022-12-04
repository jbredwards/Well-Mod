package git.jbredwards.well.common.block;

import git.jbredwards.well.common.config.ConfigHandler;
import git.jbredwards.well.common.tileentity.TileEntityWell;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;

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
    @Nonnull public static final PropertyBool UPSIDE_DOWN = PropertyBool.create("upside_down");

    @Nullable
    protected FluidStack cachedFluid;
    public BlockWell(@Nonnull Material materialIn) { this(materialIn, materialIn.getMaterialMapColor()); }
    public BlockWell(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn) {
        super(materialIn, mapColorIn);
        setCreativeTab(CreativeTabs.DECORATIONS).setHardness(3f).setResistance(1.5f).setHarvestLevel("pickaxe", 0);
        setDefaultState(getDefaultState().withProperty(IS_BOTTOM, true));
        useNeighborBrightness = true;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this)
                .add(FluidUnlistedProperty.INSTANCE)
                .add(AXIS, IS_BOTTOM, UPSIDE_DOWN)
                .build();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(IS_BOTTOM, (meta & 1) == 1)
                .withProperty(UPSIDE_DOWN, (meta >> 1 & 1) == 1)
                .withProperty(AXIS, EnumFacing.getFacingFromVector((meta >> 2 & 1) ^ 1, 0, meta >> 2 & 1).getAxis());
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return (state.getValue(IS_BOTTOM) ? 1 : 0)
                | (state.getValue(UPSIDE_DOWN) ? 2 : 0)
                | state.getValue(AXIS).ordinal() << 1;
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
        return super.canPlaceBlockAt(worldIn, pos) && (super.canPlaceBlockAt(worldIn, pos.up()) || super.canPlaceBlockAt(worldIn, pos.down()));
    }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
        final EnumFacing.Axis axis = placer.isSneaking() ? placer.getHorizontalFacing().rotateY().getAxis() : placer.getHorizontalFacing().getAxis();
        return getDefaultState().withProperty(AXIS, axis).withProperty(UPSIDE_DOWN, !super.canPlaceBlockAt(worldIn, pos.up()));
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        final int verticalDir = state.getValue(UPSIDE_DOWN) ? -1 : 1;
        worldIn.setBlockState(pos.up(verticalDir), state.withProperty(IS_BOTTOM, false), Constants.BlockFlags.SEND_TO_CLIENTS);
        //warn placer if only one well can function in the area
        if(ConfigHandler.onlyOnePerChunk && placer instanceof EntityPlayerMP) {
            final @Nullable TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof TileEntityWell && ((TileEntityWell)tile).nearbyWells > 1)
                sendWarning((EntityPlayerMP)placer, verticalDir == -1);
        }
    }

    protected void sendWarning(@Nonnull EntityPlayerMP player, boolean isUpsideDown) {
        player.connection.sendPacket(new SPacketTitle(SPacketTitle.Type.ACTIONBAR,
                new TextComponentTranslation(isUpsideDown ? "warn.well.onePerChunkFlipped" : "warn.well.onePerChunk")));
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
        final int verticalDir = state.getValue(UPSIDE_DOWN) ? -1 : 1;
        if(pos.equals(fromPos.down(verticalDir)) && state.getValue(IS_BOTTOM) && worldIn.getBlockState(fromPos).getBlock() != this)
            worldIn.destroyBlock(pos, false);

        else if(pos.equals(fromPos.up(verticalDir)) && !state.getValue(IS_BOTTOM) && worldIn.getBlockState(fromPos).getBlock() != this)
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
                        if(FMLCommonHandler.instance().getSide().isClient() && canRenderFluid())
                            return Math.max(state.getLightValue(), (int)baseFluidLight);

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
        if(canRenderFluid()) {
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
        final RayTraceResult[] collidingBoxes = WellCollisions.getTraceBoxList(blockState).stream()
                .map(aabb -> rayTrace(pos, start, end, aabb))
                .filter(Objects::nonNull)
                .toArray(RayTraceResult[]::new);

        if(collidingBoxes.length == 0) return null;
        RayTraceResult furthest = null;
        double dist = -1;

        for(RayTraceResult trace : collidingBoxes) {
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
        final boolean isBottom = state.getValue(IS_BOTTOM) != state.getValue(UPSIDE_DOWN);
        return new AxisAlignedBB(0, isBottom ? 0 : -1, 0, 1, isBottom ? 2 : 1, 1).offset(pos);
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entity, double yToTest, @Nonnull Material materialIn, boolean testingHead) {
        if(!testingHead) yToTest = entity.posY;
        final @Nullable TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityWell) {
            final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
            if(fluid != null && fluid.getFluid().canBePlacedInWorld())
                if(fluid.getFluid().getBlock().getDefaultState().getMaterial() == materialIn)
                    return state.getValue(UPSIDE_DOWN)
                            ? yToTest >= pos.getY() - ConfigHandler.getRenderedFluidHeight(fluid, true)
                            : yToTest <= pos.getY() + ConfigHandler.getRenderedFluidHeight(fluid, false);
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

        if(fluid == null) return false;
        if(world.getBlockState(pos).getValue(UPSIDE_DOWN))
            return boundingBox.minY >= pos.getY() - ConfigHandler.getRenderedFluidHeight(fluid, true) ? true : null;
        return boundingBox.minY <= pos.getY() + ConfigHandler.getRenderedFluidHeight(fluid, false) ? true : null;
    }

    @Override
    public float getBlockLiquidHeight(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Material material) {
        final @Nullable TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityWell) {
            final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
            if(fluid != null && fluid.getFluid().canBePlacedInWorld())
                if(fluid.getFluid().getBlock().getDefaultState().getMaterial() == material)
                    return ConfigHandler.getRenderedFluidHeight(fluid, false);
        }

        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        final @Nullable TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityWell) {
            final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
            if(fluid != null && fluid.getFluid().canBePlacedInWorld()) {
                final float height = ConfigHandler.getRenderedFluidHeight(fluid, false);
                final IBlockState fluidState = fluid.getFluid().getBlock().getDefaultState()
                        .withProperty(BlockLiquid.LEVEL, 8 - (int)(height * 8));
                fluidState.getBlock().randomDisplayTick(fluidState, worldIn, pos, rand);

                //get around lava particle check
                if(fluid.getFluid() == FluidRegistry.LAVA) {
                    if(rand.nextInt(100) == 0) {
                        final double x = pos.getX() + rand.nextFloat();
                        final double y = pos.getY() + height;
                        final double z = pos.getZ() + rand.nextFloat();
                        worldIn.spawnParticle(EnumParticleTypes.LAVA, x, y, z, 0, 0, 0);
                        worldIn.playSound(x, y, z, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2f + rand.nextFloat() * 0.2f, 0.9f + rand.nextFloat() * 0.15f, false);
                    }

                    if(rand.nextInt(200) == 0) {
                        final double x = pos.getX() + 0.5;
                        final double y = pos.getY() + height / 2;
                        final double z = pos.getZ() + 0.5;
                        worldIn.playSound(x, y, z, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2f + rand.nextFloat() * 0.2f, 0.9f + rand.nextFloat() * 0.15f, false);
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public SoundType getSoundType(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        //improve the roof sound if possible (some mods change the soundType of bricks to be better)
        return state.getValue(IS_BOTTOM) ? getSoundType() : Blocks.BRICK_BLOCK.getSoundType();
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

    public boolean canRenderFluid() { return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT; }

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
