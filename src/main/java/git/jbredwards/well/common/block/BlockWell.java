package git.jbredwards.well.common.block;

import git.jbredwards.well.common.tileentity.TileEntityWell;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class BlockWell extends Block implements ITileEntityProvider
{
    @Nonnull public static final PropertyEnum<EnumFacing.Axis> FACING = PropertyEnum.create("facing", EnumFacing.Axis.class, EnumFacing.Axis::isHorizontal);
    @Nonnull public static final IUnlistedProperty<Integer> LEVEL = new IUnlistedProperty<Integer>() {
        @Nonnull
        @Override
        public String getName() { return "level"; }

        @Override
        public boolean isValid(@Nonnull Integer value) { return value <= 100000 && value >= 0; }

        @Nonnull
        @Override
        public Class<Integer> getType() { return Integer.class; }

        @Nonnull
        @Override
        public String valueToString(@Nonnull Integer value) { return value.toString(); }
    };

    public BlockWell(@Nonnull Material materialIn) { this(materialIn, materialIn.getMaterialMapColor()); }
    public BlockWell(@Nonnull Material materialIn, @Nonnull MapColor mapColorIn) { super(materialIn, mapColorIn); }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(FACING).add(LEVEL).build();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.Axis.values()[MathHelper.clamp(meta, 0, 2)]);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) { return state.getValue(FACING).ordinal(); }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) { return new TileEntityWell(); }
}
