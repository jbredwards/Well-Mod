package git.jbredwards.well;

import git.jbredwards.well.common.config.ConfigHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = "well", name = "Well Mod", version = "1.0.0")
public final class Main
{
    @Mod.EventHandler
    static void init(@Nonnull FMLInitializationEvent event) { ConfigHandler.initData(); }
}
