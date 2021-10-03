package io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes;

import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;

public class FlatShape implements HeightGenerationShape {

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
        return (getMaxY(settings) - getMinY(settings)) / 2;
    }

    @Override
    public int getTopHeight(RealityCubeSettings settings, int x, int z) {
        return 0;
    }

    @Override
    public String getName() {
        return "flat";
    }
}
