package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.entity;

import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.Ticker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public interface EntityTicker extends Ticker, Consumer<Entity> {

    @Override
    default void onTick(ServerLevel world) {
        for (Entity entity : world.getAllEntities()) {
            accept(entity);
        }
    }

}
