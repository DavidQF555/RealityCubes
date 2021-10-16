package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.function.BiPredicate;

public enum SpawnPredicate implements BiPredicate<ServerLevelAccessor, BlockPos> {

    AIR((level, pos) -> level.getBlockState(pos).isAir()),
    DARK((level, pos) -> Monster.isDarkEnoughToSpawn(level, pos, level.getRandom()));

    private final BiPredicate<ServerLevelAccessor, BlockPos> condition;

    SpawnPredicate(BiPredicate<ServerLevelAccessor, BlockPos> condition) {
        this.condition = condition;
    }

    @Override
    public boolean test(ServerLevelAccessor serverLevelAccessor, BlockPos pos) {
        return condition.test(serverLevelAccessor, pos);
    }
}
