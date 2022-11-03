package git.jbredwards.well.common.init;

import git.jbredwards.well.client.block.model.ModelWellFluid;
import git.jbredwards.well.common.block.BlockWell;
import git.jbredwards.well.common.tileentity.TileEntityWell;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
@GameRegistry.ObjectHolder("well")
@Mod.EventBusSubscriber(modid = "well")
public final class RegistryHandler
{
    @SuppressWarnings("NotNullFieldNotInitialized")
    @GameRegistry.ObjectHolder("well")
    @Nonnull public static Block WELL_BLOCK;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @GameRegistry.ObjectHolder("well")
    @Nonnull public static Item WELL_ITEM;

    @SubscribeEvent
    static void registerBlock(@Nonnull RegistryEvent.Register<Block> event) {
        final BlockWell block = new BlockWell(Material.ROCK);
        block.setRegistryName("well:well").setTranslationKey("well.well").setCreativeTab(CreativeTabs.DECORATIONS)
                .setHardness(1f).setResistance(1.5f).setHarvestLevel("pickaxe", 0);

        event.getRegistry().register(block);
        GameRegistry.registerTileEntity(TileEntityWell.class, Objects.requireNonNull(block.getRegistryName()));
    }

    @SubscribeEvent
    static void registerItem(@Nonnull RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(new ItemBlock(WELL_BLOCK)
                .setRegistryName("well:well")
                .setTranslationKey("well.well"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerModel(@Nonnull ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(ModelWellFluid.Loader.INSTANCE);
        ModelLoader.setCustomModelResourceLocation(WELL_ITEM, 0, new ModelResourceLocation(
                Objects.requireNonNull(WELL_ITEM.getRegistryName()), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerBlockColor(@Nonnull ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
            if(world != null && pos != null) {
                final @Nullable TileEntity tile = world.getTileEntity(pos);
                if(tile instanceof TileEntityWell) {
                    final @Nullable FluidStack fluid = ((TileEntityWell)tile).tank.getFluid();
                    if(fluid != null && fluid.getFluid().canBePlacedInWorld()) {
                        return event.getBlockColors().colorMultiplier(
                                fluid.getFluid().getBlock().getDefaultState(),
                                world, pos, tintIndex);
                    }
                }
            }

            return -1;
        }, WELL_BLOCK);
    }
}
