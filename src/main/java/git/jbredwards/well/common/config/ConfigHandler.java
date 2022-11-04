package git.jbredwards.well.common.config;

import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class ConfigHandler
{
    public static int tankCapacity = 100000;

    public static float getRenderedFluidHeight(@Nonnull FluidStack fluid) {
        return fluid.amount * 14.5f / (16 * tankCapacity) + 1.5f / 16;
    }
}
