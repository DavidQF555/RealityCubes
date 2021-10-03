package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.function.BiPredicate;

public enum SpawnCondition {

    DARK((level, pos) -> Monster.isDarkEnoughToSpawn(level, pos, level.getRandom()));

    private final BiPredicate<ServerLevelAccessor, BlockPos> condition;

    SpawnCondition(BiPredicate<ServerLevelAccessor, BlockPos> condition) {
        this.condition = condition;
    }

    public boolean canSpawn(ServerLevelAccessor level, BlockPos pos) {
        return condition.test(level, pos);
    }
}
