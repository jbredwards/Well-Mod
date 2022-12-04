package git.jbredwards.well.common.init;

import git.jbredwards.well.client.block.model.ModelWellFluid;
import git.jbredwards.well.common.block.BlockWell;
import git.jbredwards.well.common.tileentity.TileEntityWell;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = "well")
public final class RegistryHandler
{
    @SubscribeEvent
    static void registerBlocks(@Nonnull RegistryEvent.Register<Block> event) {
        ModBlocks.INIT.forEach(event.getRegistry()::register);
        TileEntity.register("well:well", TileEntityWell.class);
    }

    @SubscribeEvent
    static void registerItems(@Nonnull RegistryEvent.Register<Item> event) {
        ModItems.INIT.forEach(event.getRegistry()::register);
        ModItems.INIT.forEach(item -> OreDictionary.registerOre("blockWell", item));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerModels(@Nonnull ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(ModelWellFluid.Loader.INSTANCE);
        ModItems.INIT.forEach(item -> ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory")));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerBlockColors(@Nonnull ColorHandlerEvent.Block event) {
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
        },
        ModBlocks.INIT.toArray(new BlockWell[0]));
    }
}
