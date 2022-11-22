package git.jbredwards.well;

import git.jbredwards.well.common.CommonProxy;
import git.jbredwards.well.common.config.ConfigHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("NotNullFieldNotInitialized")
@Mod(modid = "well", name = "Well Mod", version = "1.0")
public final class Main
{
    @Nonnull
    @SidedProxy(clientSide = "git.jbredwards.well.client.ClientProxy", serverSide = "git.jbredwards.well.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    static void init(@Nonnull FMLInitializationEvent event) { ConfigHandler.initData(); }
}
