package git.jbredwards.well.common.init;

import git.jbredwards.well.common.item.ItemBlockColoredWell;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public final class ModItems
{
    @Nonnull public static final List<ItemBlock> INIT = new ArrayList<>();

    @Nonnull public static final ItemBlock WELL = register("well", new ItemBlock(ModBlocks.WELL));
    @Nonnull public static final ItemBlock WHITE_WELL      = register(0);
    @Nonnull public static final ItemBlock ORANGE_WELL     = register(1);
    @Nonnull public static final ItemBlock MAGENTA_WELL    = register(2);
    @Nonnull public static final ItemBlock LIGHT_BLUE_WELL = register(3);
    @Nonnull public static final ItemBlock YELLOW_WELL     = register(4);
    @Nonnull public static final ItemBlock LIME_WELL       = register(5);
    @Nonnull public static final ItemBlock PINK_WELL       = register(6);
    @Nonnull public static final ItemBlock GRAY_WELL       = register(7);
    @Nonnull public static final ItemBlock SILVER_WELL     = register(8);
    @Nonnull public static final ItemBlock CYAN_WELL       = register(9);
    @Nonnull public static final ItemBlock PURPLE_WELL     = register(10);
    @Nonnull public static final ItemBlock BLUE_WELL       = register(11);
    @Nonnull public static final ItemBlock BROWN_WELL      = register(12);
    @Nonnull public static final ItemBlock GREEN_WELL      = register(13);
    @Nonnull public static final ItemBlock RED_WELL        = register(14);
    @Nonnull public static final ItemBlock BLACK_WELL      = register(15);

    @Nonnull
    static ItemBlock register(@Nonnull String name, @Nonnull ItemBlock item) {
        item.setRegistryName("well", name).setTranslationKey("well." + name);
        INIT.add(item);
        return item;
    }

    @Nonnull
    static ItemBlock register(int index) {
        return register(
                EnumDyeColor.byMetadata(index).getName() + "_well",
                new ItemBlockColoredWell(ModBlocks.INIT.get(index + 1))
        );
    }
}
