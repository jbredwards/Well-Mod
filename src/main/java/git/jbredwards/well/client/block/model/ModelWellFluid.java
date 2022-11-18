package git.jbredwards.well.client.block.model;

import git.jbredwards.well.common.block.BlockWell;
import git.jbredwards.well.common.block.FluidUnlistedProperty;
import git.jbredwards.well.common.config.ConfigHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public enum ModelWellFluid implements IModel
{
    INSTANCE;

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedModel(bakedTextureGetter, format);
    }

    public static class BakedModel implements IBakedModel
    {
        @Nonnull protected final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
        @Nonnull protected final VertexFormat format;

        public BakedModel(@Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, @Nonnull VertexFormat format) {
            this.bakedTextureGetter = bakedTextureGetter;
            this.format = format;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            if(state instanceof IExtendedBlockState && side == EnumFacing.UP) {
                final @Nullable FluidStack fluid = ((IExtendedBlockState)state).getValue(FluidUnlistedProperty.INSTANCE);
                if(fluid != null) {
                    final boolean isUpsideDown = state.getValue(BlockWell.UPSIDE_DOWN);
                    final float height = ConfigHandler.getRenderedFluidHeight(fluid, isUpsideDown);

                    return Collections.singletonList(ItemTextureQuadConverter.genQuad(format,
                            new TRSRTransformation(new Vector3f(0, height, isUpsideDown ? 1 : 0),
                                    null, null, new Quat4f(1, 0, 0, isUpsideDown ? -1 : 1)),
                            3, 3, 13, 13, 0,
                            bakedTextureGetter.apply(fluid.getFluid().getStill(fluid)),
                            side, fluid.getFluid().getColor(fluid), 0
                    ));
                }
            }

            return Collections.emptyList();
        }

        @Override
        public boolean isAmbientOcclusion() { return false; }

        @Override
        public boolean isGui3d() { return false; }

        @Override
        public boolean isBuiltInRenderer() { return false; }

        @Nonnull
        @Override
        public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }

        @Override
        public boolean isAmbientOcclusion(@Nonnull IBlockState state) { return isAmbientOcclusion(); }

        @Nonnull
        @Override
        public TextureAtlasSprite getParticleTexture() {
            return bakedTextureGetter.apply(FluidRegistry.WATER.getStill());
        }
    }

    public enum Loader implements ICustomModelLoader
    {
        INSTANCE;

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {}

        @Override
        public boolean accepts(@Nonnull ResourceLocation modelLocation) {
            return modelLocation.equals(new ResourceLocation("well", "models/block/fluid"));
        }

        @Nonnull
        @Override
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) { return ModelWellFluid.INSTANCE; }
    }
}
