package io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes;

import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public interface HeightGenerationShape extends GenerationShape {

    int getBottomHeight(RealityCubeSettings settings, int x, int z);

    int getTopHeight(RealityCubeSettings settings, int x, int z);

    default int getMinSurfaceLevel(RealityCubeSettings settings) {
        return getMinY(settings);
    }

    @Override
    default ChunkAccess generateChunk(ChunkAccess chunk, RealityCubeSettings settings) {
        ChunkPos cPos = chunk.getPos();
        int minY = Math.max(getMinY(settings), chunk.getMinBuildHeight());
        int maxY = Math.min(getMaxY(settings), chunk.getMaxBuildHeight());
        int x = cPos.getMinBlockX();
        int z = cPos.getMinBlockZ();
        BlockState def = settings.getDefaultBlockType().get();
        for (int dX = 0; dX < 16; dX++) {
            for (int dZ = 0; dZ < 16; dZ++) {
                if (hasCeiling()) {
                    int y = maxY - Math.max(0, getTopHeight(settings, x + dX, z + dZ));
                    for (BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x + dX, maxY, z + dZ); pos.getY() >= y; pos.setY(pos.getY() - 1)) {
                        chunk.setBlockState(pos, def, false);
                    }
                }
                if (hasFloor()) {
                    int y = minY + Math.max(0, getBottomHeight(settings, x + dX, z + dZ));
                    for (BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x + dX, minY, z + dZ); pos.getY() <= y; pos.setY(pos.getY() + 1)) {
                        chunk.setBlockState(pos, def, false);
                    }
                }
            }
        }
        return chunk;
    }

    @Override
    default void generateSurface(WorldGenRegion world, ChunkAccess chunk, RealityCubeSettings settings, int sea) {
        ChunkPos cPos = chunk.getPos();
        WorldgenRandom random = new WorldgenRandom();
        random.setBaseChunkSeed(cPos.x, cPos.z);
        int x = cPos.getMinBlockX();
        int z = cPos.getMinBlockZ();
        int minSurfaceLevel = getMinSurfaceLevel(settings);
        long seed = world.getSeed();
        BlockState block = settings.getDefaultBlockType().get();
        BlockState fluid = settings.getDefaultFluidType().get();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dX = 0; dX < 16; dX++) {
            for (int dZ = 0; dZ < 16; dZ++) {
                int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dX, dZ) + 1;
                world.getBiome(pos.set(x + dX, y, z + dZ)).buildSurfaceAt(random, chunk, x + dX, z + dZ, y, 1, block, fluid, sea, minSurfaceLevel, seed);
            }
        }
    }

    @Override
    default void generateBedrock(ChunkAccess chunk, RealityCubeSettings settings) {
        int x = chunk.getPos().getMinBlockX();
        int z = chunk.getPos().getMinBlockZ();
        int minY = Math.max(getMinY(settings), chunk.getMinBuildHeight());
        int maxY = Math.min(getMaxY(settings), chunk.getMaxBuildHeight());
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dX = 0; dX < 16; dX++) {
            for (int dZ = 0; dZ < 16; dZ++) {
                if (hasCeiling()) {
                    chunk.setBlockState(pos.set(x + dX, maxY, z + dZ), bedrock, false);
                }
                if (hasFloor()) {
                    chunk.setBlockState(pos.set(x + dX, minY, z + dZ), bedrock, false);
                }
            }
        }
    }

    @Override
    default int getBaseHeight(int x, int z, Heightmap.Types type, RealityCubeSettings settings) {
        return getMinY(settings) + (hasFloor() ? getBottomHeight(settings, x, z) : 0);
    }

    @Override
    default NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor accessor, RealityCubeSettings settings) {
        BlockState[] blocks = new BlockState[accessor.getMaxBuildHeight() - accessor.getMinBuildHeight() + 1];
        BlockState def = settings.getDefaultBlockType().get();
        if (hasCeiling()) {
            int height = getTopHeight(settings, x, z);
            for (int i = 0; i < height; i++) {
                blocks[blocks.length - i - 1] = def;
            }
        }
        if (hasFloor()) {
            int height = getBottomHeight(settings, x, z);
            for (int i = 0; i < height; i++) {
                blocks[i] = def;
            }
        }
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] == null) {
                blocks[i] = Blocks.AIR.defaultBlockState();
            }
        }
        return new NoiseColumn(getMinY(settings), blocks);
    }
}
