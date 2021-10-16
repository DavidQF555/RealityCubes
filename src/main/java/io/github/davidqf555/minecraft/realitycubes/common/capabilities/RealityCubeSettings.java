package io.github.davidqf555.minecraft.realitycubes.common.capabilities;

import io.github.davidqf555.minecraft.realitycubes.common.world.properties.DefaultBlockType;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.DefaultFluidType;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.Preset;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.GenerationShape;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.ShapesHelper;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.Ticker;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RealityCubeSettings implements INBTSerializable<CompoundTag> {

    public static final int INITIAL_EXP = 160;
    public static final double RATE = 0.075;
    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 4;
    public static final int MIN_RANGE = 4;
    public static final int MAX_RANGE = 16;
    private final List<Ticker> tickers;
    private GenerationShape shape;
    private long time;
    private int chunks, range;
    private ResourceKey<Biome> biome;
    private DefaultBlockType block;
    private DefaultFluidType fluid;

    public RealityCubeSettings() {
        tickers = new ArrayList<>();
        shape = ShapesHelper.DEFAULT;
        biome = Biomes.PLAINS;
        chunks = 1;
        range = 8;
        block = DefaultBlockType.STONE;
        fluid = DefaultFluidType.WATER;
        time = -1;
    }

    public static RealityCubeSettings get(Player player) {
        return player.getCapability(Provider.capability).orElseThrow(NullPointerException::new);
    }

    public List<Ticker> getTickers() {
        return tickers;
    }

    public void setTickers(Collection<Ticker> tickers) {
        this.tickers.clear();
        this.tickers.addAll(tickers);
    }

    public void addTickers(Ticker... tickers) {
        this.tickers.addAll(Arrays.asList(tickers));
    }

    public GenerationShape getShape() {
        return shape;
    }

    public void setShape(GenerationShape shape) {
        this.shape = shape;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getChunkRadius() {
        return chunks;
    }

    public void setChunkRadius(int chunks) {
        this.chunks = Mth.clamp(chunks, MIN_RADIUS, MAX_RADIUS);
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = Mth.clamp(range, MIN_RANGE, MAX_RANGE);
    }

    public ResourceKey<Biome> getBiome() {
        return biome;
    }

    public void setBiome(ResourceKey<Biome> biome) {
        this.biome = biome;
    }

    public DefaultBlockType getDefaultBlockType() {
        return block;
    }

    public void setDefaultBlockType(DefaultBlockType block) {
        this.block = block;
    }

    public DefaultFluidType getDefaultFluidType() {
        return fluid;
    }

    public void setDefaultFluidType(DefaultFluidType fluid) {
        this.fluid = fluid;
    }

    public void applyPreset(Preset preset) {
        preset.accept(this);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag tickers = new ListTag();
        for (Ticker ticker : getTickers()) {
            tickers.add(ticker.serializeNBT());
        }
        tag.put("Tickers", tickers);
        tag.putLong("Time", getTime());
        tag.putString("Shape", getShape().getName());
        tag.putInt("Chunks", getChunkRadius());
        tag.putInt("Range", getRange());
        tag.putString("Biome", getBiome().location().toString());
        tag.putString("Block", getDefaultBlockType().name());
        tag.putString("Fluid", getDefaultFluidType().name());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("Tickers", Constants.NBT.TAG_LIST)) {
            setTickers(nbt.getList("Tickers", Constants.NBT.TAG_COMPOUND).stream().map(tag -> Ticker.deserialize((CompoundTag) tag)).toList());
        }
        if (nbt.contains("Time", Constants.NBT.TAG_LONG)) {
            setTime(nbt.getLong("Time"));
        }
        if (nbt.contains("Shape", Constants.NBT.TAG_STRING)) {
            setShape(ShapesHelper.getOrDefaultShape(nbt.getString("Shape")));
        }
        if (nbt.contains("Chunks", Constants.NBT.TAG_INT)) {
            setChunkRadius(nbt.getInt("Chunks"));
        }
        if (nbt.contains("Range", Constants.NBT.TAG_INT)) {
            setRange(nbt.getInt("Range"));
        }
        if (nbt.contains("Biome", Constants.NBT.TAG_STRING)) {
            setBiome(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(nbt.getString("Biome"))));
        }
        if (nbt.contains("Block", Constants.NBT.TAG_STRING)) {
            setDefaultBlockType(DefaultBlockType.valueOf(nbt.getString("Block")));
        }
        if (nbt.contains("Fluid", Constants.NBT.TAG_STRING)) {
            setDefaultFluidType(DefaultFluidType.valueOf(nbt.getString("Fluid")));
        }
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        @CapabilityInject(RealityCubeSettings.class)
        private static Capability<RealityCubeSettings> capability = null;
        private final LazyOptional<RealityCubeSettings> instance;

        public Provider() {
            RealityCubeSettings settings = new RealityCubeSettings();
            instance = LazyOptional.of(() -> settings);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return cap == capability ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(NullPointerException::new).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            instance.orElseThrow(NullPointerException::new).deserializeNBT(tag);
        }
    }
}
