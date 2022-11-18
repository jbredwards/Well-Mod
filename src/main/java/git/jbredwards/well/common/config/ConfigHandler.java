package git.jbredwards.well.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Config(modid = "well")
@Mod.EventBusSubscriber(modid = "well")
public final class ConfigHandler
{
    @Config.RangeInt(min = 0)
    @Config.RequiresWorldRestart
    @Config.LangKey("config.well.tankCapacity")
    public static int tankCapacity = 100000;
    public static float getRenderedFluidHeight(@Nonnull FluidStack fluid, boolean isUpsideDown) {
        final float normalHeight = fluid.amount * 14.5f / (16 * tankCapacity) + 1.5f / 16;
        return isUpsideDown ? 1 - normalHeight : normalHeight;
    }

    @Config.LangKey("config.well.onlyOnePerChunk")
    public static boolean onlyOnePerChunk = false;
    public static boolean canGenerateFluid(int nearbyWells) { return !onlyOnePerChunk || nearbyWells == 1; }

    @Config.LangKey("config.well.playSound")
    public static boolean playSound = true;

    @SubscribeEvent
    static void sync(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals("well")) ConfigManager.sync("well", Config.Type.INSTANCE);
    }
}
