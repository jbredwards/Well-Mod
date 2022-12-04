package git.jbredwards.well.common.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class ModSounds
{
    @Nonnull
    public static final SoundEvent CRANK = new SoundEvent(new ResourceLocation("well", "block.well.crank")).setRegistryName("well", "block.well.crank");
}
