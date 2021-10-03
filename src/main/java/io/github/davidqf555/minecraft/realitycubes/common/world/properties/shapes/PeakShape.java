package io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes;

import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import net.minecraft.core.SectionPos;

public class PeakShape implements HeightGenerationShape {

    @Override
    public boolean hasCeiling() {
        return false;
    }

    @Override
    public boolean hasFloor() {
        return true;
    }

    @Override
    public int getBottomHeight(RealityCubeSettings settings, int x, int z) {
        double ratio = Math.sqrt(x * x + z * z) / SectionPos.sectionToBlockCoord(settings.getChunkRadius());
        return (int) (getHeight(settings) / (1 + ratio) / 2);
    }

    @Override
    public int getTopHeight(RealityCubeSettings settings, int x, int z) {
        return 0;
    }

    @Override
    public String getName() {
        return "peak";
    }
}
