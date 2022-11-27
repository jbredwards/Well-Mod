package git.jbredwards.well.common.config;

import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
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
    @Nonnull static final Map<Biome, WellFluidData> downWellData = new HashMap<>();
    @Nonnull static final Map<Biome, WellFluidData> upWellData = new HashMap<>();

    public static void initData() {
        //reset defaults
        WellFluidData.DOWN_DEFAULT.reset();
        WellFluidData.UP_DEFAULT.reset();
        downWellData.clear();
        upWellData.clear();

        try {
            final NBTTagCompound nbt = JsonToNBT.getTagFromJson(data);
            //set default fluid
            if(nbt.hasKey("DefaultFluid", NBT.TAG_COMPOUND)) {
                final FluidStack defaultFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("DefaultFluid"));
                if(defaultFluid != null) {
                    WellFluidData.DOWN_DEFAULT.fluid = defaultFluid;
                    WellFluidData.UP_DEFAULT.fluid = defaultFluid;
                }
            }
            //set default min fill ticks
            if(nbt.hasKey("DefaultMinTicks", NBT.TAG_INT)) {
                WellFluidData.DOWN_DEFAULT.minToFill = Math.max(0, nbt.getInteger("DefaultMinTicks"));
                WellFluidData.UP_DEFAULT.minToFill = Math.max(0, nbt.getInteger("DefaultMinTicks"));
            }
            //set default max fill ticks
            if(nbt.hasKey("DefaultMaxTicks", NBT.TAG_INT)) {
                WellFluidData.DOWN_DEFAULT.maxToFill = Math.max(0, nbt.getInteger("DefaultMaxTicks"));
                WellFluidData.UP_DEFAULT.maxToFill = Math.max(0, nbt.getInteger("DefaultMaxTicks"));
            }

            //set default down fluid
            if(nbt.hasKey("DefaultDownFluid", NBT.TAG_COMPOUND)) {
                final FluidStack defaultFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("DefaultDownFluid"));
                if(defaultFluid != null) WellFluidData.DOWN_DEFAULT.fluid = defaultFluid;
            }
            //set default down min fill ticks
            if(nbt.hasKey("DefaultDownMinTicks", NBT.TAG_INT))
                WellFluidData.DOWN_DEFAULT.minToFill = Math.max(0, nbt.getInteger("DefaultDownMinTicks"));
            //set default down max fill ticks
            if(nbt.hasKey("DefaultDownMaxTicks", NBT.TAG_INT))
                WellFluidData.DOWN_DEFAULT.maxToFill = Math.max(0, nbt.getInteger("DefaultDownMaxTicks"));

            //set default up fluid
            if(nbt.hasKey("DefaultUpFluid", NBT.TAG_COMPOUND)) {
                final FluidStack defaultFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("DefaultUpFluid"));
                if(defaultFluid != null) WellFluidData.UP_DEFAULT.fluid = defaultFluid;
            }
            //set default up min fill ticks
            if(nbt.hasKey("DefaultUpMinTicks", NBT.TAG_INT))
                WellFluidData.UP_DEFAULT.minToFill = Math.max(0, nbt.getInteger("DefaultUpMinTicks"));
            //set default up max fill ticks
            if(nbt.hasKey("DefaultUpMaxTicks", NBT.TAG_INT))
                WellFluidData.UP_DEFAULT.maxToFill = Math.max(0, nbt.getInteger("DefaultUpMaxTicks"));

            //handle per-biome data
            if(nbt.hasKey("Data", NBT.TAG_LIST)) {
                final NBTTagList dataList = nbt.getTagList("Data", NBT.TAG_COMPOUND);
                for(int i = 0; i < dataList.tagCount(); i++) {
                    final NBTTagCompound data = dataList.getCompoundTagAt(i);
                    if(data.hasKey("Fluid", NBT.TAG_COMPOUND)) {
                        final FluidStack fluid = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("Fluid"));
                        final int minToFill = (fluid != null && data.hasKey("MinTicks", NBT.TAG_INT) ? Math.max(0, data.getInteger("MinTicks")) : 0);
                        final int maxToFill = (fluid != null && data.hasKey("MaxTicks", NBT.TAG_INT) ? Math.max(0, data.getInteger("MaxTicks")) : 0);
                        final WellFluidData wellData = new WellFluidData(fluid, minToFill, maxToFill);
                        //handle biome tags
                        data.getTagList("BiomeTags", NBT.TAG_STRING).forEach(biomeTagNbt -> {
                            final BiomeDictionary.Type biomeTag = BiomeDictionary.Type.getType(((NBTTagString)biomeTagNbt).getString());
                            for(Biome biome : BiomeDictionary.getBiomes(biomeTag)) {
                                //remove existing data
                                if(fluid == null || maxToFill < minToFill || maxToFill <= 0) {
                                    downWellData.remove(biome);
                                    upWellData.remove(biome);
                                }
                                //add new data
                                else {
                                    if(wellData.fluid.getFluid().isLighterThanAir()) upWellData.put(biome, wellData);
                                    else downWellData.put(biome, wellData);
                                }
                            }
                        });
                        //handle biomes
                        data.getTagList("Biomes", NBT.TAG_STRING).forEach(biomeNbt -> {
                            final Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(((NBTTagString)biomeNbt).getString()));
                            if(biome != null) {
                                //remove existing data
                                if(fluid == null || maxToFill < minToFill || maxToFill <= 0) {
                                    downWellData.remove(biome);
                                    upWellData.remove(biome);
                                }
                                //add new data
                                else {
                                    if(wellData.fluid.getFluid().isLighterThanAir()) upWellData.put(biome, wellData);
                                    else downWellData.put(biome, wellData);
                                }
                            }
                        });
                    }
                }
            }
        }

        //malformed config
        catch(NBTException ignored) {}
    }

    @Nullable
    public static FluidStack getFillFluid(@Nonnull Biome biome, @Nonnull World world, boolean upsideDown, int nearbyWells) {
        final WellFluidData data = upsideDown
                ? upWellData.getOrDefault(biome, WellFluidData.UP_DEFAULT)
                : downWellData.getOrDefault(biome, WellFluidData.DOWN_DEFAULT);

        if(data.fluid.amount <= 0 || world.provider.doesWaterVaporize() && data.fluid.getFluid().doesVaporize(data.fluid))
            return null;

        final FluidStack fluid = data.fluid.copy();
        fluid.amount /= nearbyWells;
        return fluid;
    }

    public static int getFillDelay(@Nonnull Biome biome, @Nonnull Random rand, boolean upsideDown) {
        final WellFluidData data = upsideDown
                ? upWellData.getOrDefault(biome, WellFluidData.UP_DEFAULT)
                : downWellData.getOrDefault(biome, WellFluidData.DOWN_DEFAULT);

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
        @Nonnull static final WellFluidData DOWN_DEFAULT = new WellFluidData(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), 160, 200);
        @Nonnull static final WellFluidData UP_DEFAULT = new WellFluidData(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), 160, 200);

        FluidStack fluid;
        int minToFill;
        int maxToFill;

        WellFluidData(@Nullable FluidStack fluidIn, int minIn, int maxIn) {
            fluid = fluidIn;
            minToFill = minIn;
            maxToFill = maxIn;
        }

        void reset() {
            fluid = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
            minToFill = 160;
            maxToFill = 200;
        }
    }
}
