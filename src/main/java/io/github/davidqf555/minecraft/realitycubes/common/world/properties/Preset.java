package io.github.davidqf555.minecraft.realitycubes.common.world.properties;

import com.google.common.collect.ImmutableList;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.GenerationShape;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.ShapesHelper;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.Ticker;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.entity.GenericEntityTickerType;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.entity.MobEffectTicker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Preset implements Consumer<RealityCubeSettings> {

    private static final Map<String, Preset> PRESETS = new HashMap<>();

    static {
        addPreset(new Preset("default", ImmutableList.of(), ShapesHelper.DEFAULT, Biomes.PLAINS, -1L, DefaultBlockType.STONE, DefaultFluidType.WATER));
        addPreset(new Preset("horror", ImmutableList.of(new MobEffectTicker(MobEffects.BLINDNESS, 1)), null, Biomes.DARK_FOREST, 18000L, null, null));
        addPreset(new Preset("fire", ImmutableList.of(GenericEntityTickerType.FIRE.get()), null, Biomes.NETHER_WASTES, null, DefaultBlockType.NETHERRACK, DefaultFluidType.LAVA));
    }

    private final String name;
    private final Collection<Ticker> tickers;
    private final GenerationShape shape;
    private final ResourceKey<Biome> biome;
    private final Long time;
    private final DefaultBlockType block;
    private final DefaultFluidType fluid;

    public Preset(String name, @Nullable Collection<Ticker> tickers, @Nullable GenerationShape shape, @Nullable ResourceKey<Biome> biome, @Nullable Long time, @Nullable DefaultBlockType block, @Nullable DefaultFluidType fluid) {
        this.name = name;
        this.tickers = tickers;
        this.shape = shape;
        this.biome = biome;
        this.time = time;
        this.block = block;
        this.fluid = fluid;
    }

    public static void addPreset(Preset preset) {
        PRESETS.put(preset.name, preset);
    }

    @Nullable
    public static Preset getPreset(String name) {
        return PRESETS.get(name);
    }

    public static Set<String> getPresets() {
        return PRESETS.keySet();
    }

    @Override
    public void accept(RealityCubeSettings settings) {
        if (tickers != null) {
            settings.setTickers(tickers);
        }
        if (shape != null) {
            settings.setShape(shape);
        }
        if (biome != null) {
            settings.setBiome(biome);
        }
        if (time != null) {
            settings.setTime(time);
        }
        if (block != null) {
            settings.setDefaultBlockType(block);
        }
        if (fluid != null) {
            settings.setDefaultFluidType(fluid);
        }
    }
}
