package git.jbredwards.well.common.block;

import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public enum FluidUnlistedProperty implements IUnlistedProperty<FluidStack>
{
    INSTANCE;

    @Nonnull
    @Override
    public String getName() { return "fluid"; }

    @Override
    public boolean isValid(@Nullable FluidStack value) { return true; }

    @Nonnull
    @Override
    public Class<FluidStack> getType() { return FluidStack.class; }

    @Nonnull
    @Override
    public String valueToString(@Nullable FluidStack value) {
        return value == null ? "EMPTY" : String.format(
                "(fluid=%s, amount=%s, tag=%s)",
                value.getFluid(), value.amount, value.tag);
    }
}
