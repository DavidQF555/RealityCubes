package io.github.davidqf555.minecraft.realitycubes.common.world.properties;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public enum DefaultBlockType implements Supplier<BlockState> {

    STONE(Blocks.STONE::defaultBlockState),
    NETHERRACK(Blocks.NETHERRACK::defaultBlockState),
    END_STONE(Blocks.END_STONE::defaultBlockState);

    private final Supplier<BlockState> block;

    DefaultBlockType(Supplier<BlockState> block) {
        this.block = block;
    }

    @Override
    public BlockState get() {
        return block.get();
    }
}
