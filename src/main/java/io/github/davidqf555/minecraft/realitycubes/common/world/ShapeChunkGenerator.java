package io.github.davidqf555.minecraft.realitycubes.common.world;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.GenerationShape;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ShapeChunkGenerator extends ChunkGenerator {

    public static final Codec<ShapeChunkGenerator> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            CompoundTag.CODEC.fieldOf("settings").forGetter(gen -> gen.settings.serializeNBT()),
            ResourceLocation.CODEC.fieldOf("level").forGetter(gen -> gen.level.location()),
            BiomeSource.CODEC.fieldOf("biomes").forGetter(gen -> gen.biomeSource),
            Codec.LONG.fieldOf("seed").forGetter(gen -> gen.seed)
    ).apply(builder, (tag, loc, biomes, seed) -> {
        RealityCubeSettings settings = new RealityCubeSettings();
        settings.deserializeNBT(tag);
        return new ShapeChunkGenerator(settings, ResourceKey.create(Registry.DIMENSION_REGISTRY, loc), biomes, seed);
    }));
    private final ResourceKey<Level> level;
    private final long seed;
    private final RealityCubeSettings settings;

    public ShapeChunkGenerator(RealityCubeSettings settings, ResourceKey<Level> level, BiomeSource biomes, long seed) {
        super(biomes, new StructureSettings(Optional.empty(), ImmutableMap.of()));
        this.level = level;
        this.seed = seed;
        this.settings = settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion world, ChunkAccess chunk) {
        ChunkPos cPos = chunk.getPos();
        if (isBarrierChunk(cPos.x, cPos.z)) {
            generateBarriers(chunk, settings);
        } else if (inRange(cPos.x, cPos.z)) {
            updateSeed(world);
            GenerationShape shape = settings.getShape();
            shape.generateSurface(world, chunk, settings, getSeaLevel());
            shape.generateBedrock(chunk, settings);
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenRegion region, StructureFeatureManager structures) {
        updateSeed(region);
        super.applyBiomeDecoration(region, structures);
    }

    private void updateSeed(WorldGenRegion region) {
        region.seed = seed;
        region.biomeManager = createBiomeManager(region.biomeManager);
    }

    private BiomeManager createBiomeManager(BiomeManager source) {
        return new BiomeManager(source.noiseBiomeSource, BiomeManager.obfuscateSeed(seed), source.zoomer);
    }

    @Override
    public void applyCarvers(long seed, BiomeManager biomes, ChunkAccess chunk, GenerationStep.Carving carving) {
        super.applyCarvers(this.seed, createBiomeManager(biomes), chunk, carving);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        updateSeed(region);
        super.spawnOriginalMobs(region);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, StructureFeatureManager structures, ChunkAccess chunk) {
        ChunkPos cPos = chunk.getPos();
        if (inRange(cPos.x, cPos.z)) {
            GenerationShape shape = settings.getShape();
            return CompletableFuture.supplyAsync(() -> shape.generateChunk(chunk, settings), executor);
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private boolean isBarrierChunk(int x, int z) {
        int radius = settings.getChunkRadius();
        return (x == radius || x + 1 == -radius) && (z < radius && z >= -radius) || (z == radius || z + 1 == -radius) && (x < radius && x >= -radius);
    }

    private void generateBarriers(ChunkAccess chunk, RealityCubeSettings settings) {
        ChunkPos pos = chunk.getPos();
        int radius = settings.getChunkRadius();
        boolean xConst = pos.x == radius || pos.x + 1 == -radius;
        boolean decrease = pos.x + 1 == -radius || pos.z + 1 == -radius;
        int x = pos.x == radius ? pos.getMinBlockX() : pos.getMaxBlockX();
        int z = pos.z == radius ? pos.getMinBlockZ() : pos.getMaxBlockZ();
        BlockState barrier = Blocks.BARRIER.defaultBlockState();
        BlockPos.MutableBlockPos block = new BlockPos.MutableBlockPos();
        for (int y = chunk.getMinBuildHeight(); y <= chunk.getMaxBuildHeight(); y++) {
            for (int d = 0; d < 16; d++) {
                int dif = decrease ? 15 - d : d;
                if (xConst) {
                    chunk.setBlockState(block.set(x, y, z + dif), barrier, false);
                } else {
                    chunk.setBlockState(block.set(x + dif, y, z), barrier, false);
                }
            }
        }
    }

    private boolean inRange(int x, int z) {
        int radius = settings.getChunkRadius();
        return x < radius && x >= -radius && z < radius && z >= -radius;
    }

    @Override
    public int getGenDepth() {
        GenerationShape shape = settings.getShape();
        return shape.getMaxY(settings) - shape.getMinY(settings);
    }

    @Override
    public int getSeaLevel() {
        return settings.getShape().getSeaLevel(settings);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor accessor) {
        GenerationShape shape = settings.getShape();
        return shape.getBaseHeight(x, z, type, settings);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor accessor) {
        GenerationShape shape = settings.getShape();
        return shape.getBaseColumn(x, z, accessor, settings);
    }

    @Override
    public void createStructures(RegistryAccess registry, StructureFeatureManager features, ChunkAccess chunk, StructureManager structures, long seed) {
    }

    @Override
    public boolean hasStronghold(ChunkPos pos) {
        return false;
    }
}
