package io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes;

import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public interface GenerationShape {

    default int getMinY(RealityCubeSettings settings) {
        return 64 - SectionPos.sectionToBlockCoord(settings.getChunkRadius());
    }

    default int getMaxY(RealityCubeSettings settings) {
        return 64 + SectionPos.sectionToBlockCoord(settings.getChunkRadius());
    }

    default int getSeaLevel(RealityCubeSettings settings) {
        return 64;
    }

    default int getHeight(RealityCubeSettings settings) {
        return getMaxY(settings) - getMinY(settings);
    }

    ChunkAccess generateChunk(ChunkAccess chunk, RealityCubeSettings settings);

    void generateSurface(WorldGenRegion world, ChunkAccess chunk, RealityCubeSettings settings, int sea);

    void generateBedrock(ChunkAccess chunk, RealityCubeSettings settings);

    int getBaseHeight(int x, int z, Heightmap.Types type, RealityCubeSettings settings);

    String getName();

    boolean hasCeiling();

    boolean hasFloor();

    NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor accessor, RealityCubeSettings settings);
}
