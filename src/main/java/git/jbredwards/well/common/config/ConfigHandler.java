package git.jbredwards.well.common.config;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

    @Config.LangKey("config.well.data")
    @Nonnull public static String data = "{}";
    @Nonnull static final Map<Biome, WellFluidData> wellData = new HashMap<>();

    public static void initData() {
        //reset defaults
        WellFluidData.DEFAULT.fluid = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
        WellFluidData.DEFAULT.minToFill = 160;
        WellFluidData.DEFAULT.maxToFill = 200;
        wellData.clear();

        try {
            final NBTTagCompound nbt = JsonToNBT.getTagFromJson(data);
            //set default fluid
            if(nbt.hasKey("DefaultFluid", NBT.TAG_COMPOUND)) {
                final FluidStack defaultFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("DefaultFluid"));
                if(defaultFluid != null) WellFluidData.DEFAULT.fluid = defaultFluid;
            }
            //set default min fill ticks
            if(nbt.hasKey("DefaultMinTicks", NBT.TAG_INT))
                WellFluidData.DEFAULT.minToFill = Math.max(0, nbt.getInteger("DefaultMinTicks"));
            //set default max fill ticks
            if(nbt.hasKey("DefaultMaxTicks", NBT.TAG_INT))
                WellFluidData.DEFAULT.maxToFill = Math.max(0, nbt.getInteger("DefaultMaxTicks"));
        }

        //malformed config
        catch(NBTException ignored) {}
    }

    @Nullable
    public static FluidStack getFillFluid(@Nonnull Biome biome, @Nonnull World world, int nearbyWells) {
        final WellFluidData data = wellData.getOrDefault(biome, WellFluidData.DEFAULT);
        if(data.fluid.amount <= 0 || world.provider.doesWaterVaporize() && data.fluid.getFluid().doesVaporize(data.fluid))
            return null;

        final FluidStack fluid = data.fluid.copy();
        fluid.amount /= nearbyWells;
        return fluid;
    }

    public static int getFillDelay(@Nonnull Biome biome, @Nonnull Random rand) {
        final WellFluidData data = wellData.getOrDefault(biome, WellFluidData.DEFAULT);
        return MathHelper.getInt(rand, data.minToFill, data.maxToFill);
    }

    @SubscribeEvent
    static void sync(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals("well")) {
            ConfigManager.sync("well", Config.Type.INSTANCE);
            initData();
        }
    }

    static class WellFluidData
    {
        @Nonnull
        static final WellFluidData DEFAULT = new WellFluidData(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), 160, 200);

        @Nonnull
        FluidStack fluid;
        int minToFill;
        int maxToFill;

        WellFluidData(@Nonnull FluidStack fluidIn, int minIn, int maxIn) {
            fluid = fluidIn;
            minToFill = minIn;
            maxToFill = maxIn;
        }
    }
}
