package git.jbredwards.well.common.init;

import git.jbredwards.well.common.block.BlockWell;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumDyeColor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public final class ModBlocks
{
    @Nonnull public static final List<BlockWell> INIT = new ArrayList<>();

    @Nonnull public static final BlockWell WELL = register("well", new BlockWell(Material.ROCK, MapColor.RED));
    @Nonnull public static final BlockWell WHITE_WELL      = register(0);
    @Nonnull public static final BlockWell ORANGE_WELL     = register(1);
    @Nonnull public static final BlockWell MAGENTA_WELL    = register(2);
    @Nonnull public static final BlockWell LIGHT_BLUE_WELL = register(3);
    @Nonnull public static final BlockWell YELLOW_WELL     = register(4);
    @Nonnull public static final BlockWell LIME_WELL       = register(5);
    @Nonnull public static final BlockWell PINK_WELL       = register(6);
    @Nonnull public static final BlockWell GRAY_WELL       = register(7);
    @Nonnull public static final BlockWell SILVER_WELL     = register(8);
    @Nonnull public static final BlockWell CYAN_WELL       = register(9);
    @Nonnull public static final BlockWell PURPLE_WELL     = register(10);
    @Nonnull public static final BlockWell BLUE_WELL       = register(11);
    @Nonnull public static final BlockWell BROWN_WELL      = register(12);
    @Nonnull public static final BlockWell GREEN_WELL      = register(13);
    @Nonnull public static final BlockWell RED_WELL        = register(14);
    @Nonnull public static final BlockWell BLACK_WELL      = register(15);

    @Nonnull
    static BlockWell register(@Nonnull String name, @Nonnull BlockWell block) {
        block.setRegistryName("well", name).setTranslationKey("well." + name);
        INIT.add(block);
        return block;
    }

    @Nonnull
    static BlockWell register(int colorIndex) {
        final EnumDyeColor color = EnumDyeColor.byMetadata(colorIndex);
        return register(color.getName() + "_well", new BlockWell(Material.ROCK, MapColor.getBlockColor(color)));
    }
}
