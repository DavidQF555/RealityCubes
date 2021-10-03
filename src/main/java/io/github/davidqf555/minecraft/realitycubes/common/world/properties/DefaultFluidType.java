package io.github.davidqf555.minecraft.realitycubes.common.world.properties;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public enum DefaultFluidType implements Supplier<BlockState> {

    WATER(Blocks.WATER::defaultBlockState),
    LAVA(Blocks.LAVA::defaultBlockState),
    AIR(Blocks.AIR::defaultBlockState);

    private final Supplier<BlockState> block;

    DefaultFluidType(Supplier<BlockState> block) {
        this.block = block;
    }

    @Override
    public BlockState get() {
        return block.get();
    }
}
