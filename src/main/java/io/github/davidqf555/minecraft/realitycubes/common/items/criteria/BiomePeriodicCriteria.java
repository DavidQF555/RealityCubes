package io.github.davidqf555.minecraft.realitycubes.common.items.criteria;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public class BiomePeriodicCriteria extends WorldPeriodicCriteria {

    public BiomePeriodicCriteria(List<ResourceKey<Biome>> biomes) {
        super(instance -> instance.world.getBiomeName(instance.player.blockPosition()).filter(biomes::contains).isPresent());
    }

}
