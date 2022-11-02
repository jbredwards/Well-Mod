package git.jbredwards.well.common.init;

import git.jbredwards.well.common.block.BlockWell;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
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
    static void registerBlock(@Nonnull RegistryEvent.Register<Block> event) {
        final BlockWell block = new BlockWell(Material.ROCK);
        block.setRegistryName("well:well").setTranslationKey("well.well").setCreativeTab(CreativeTabs.DECORATIONS)
                .setHardness(1f).setResistance(1.5f).setHarvestLevel("pickaxe", 0);

        event.getRegistry().register(block);
    }

    @SubscribeEvent
    static void registerItem(@Nonnull RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(new ItemBlock(Objects.requireNonNull(Block.getBlockFromName("well:well"))));
    }
}
