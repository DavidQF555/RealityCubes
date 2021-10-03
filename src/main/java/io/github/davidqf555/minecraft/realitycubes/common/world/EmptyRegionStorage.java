package io.github.davidqf555.minecraft.realitycubes.common.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;

@ParametersAreNonnullByDefault
public class EmptyRegionStorage extends RegionFileStorage {

    public EmptyRegionStorage(File file, boolean sync) {
        super(file, sync);
    }

    @Override
    protected void write(ChunkPos pos, @Nullable CompoundTag tag) {
    }

    @Nullable
    @Override
    public CompoundTag read(ChunkPos pos) {
        return null;
    }
}
