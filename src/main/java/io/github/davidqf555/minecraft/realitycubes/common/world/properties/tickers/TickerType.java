package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers;

import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.entity.GenericEntityTicker;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.entity.MobEffectTicker;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner.SpawnerTicker;

import java.util.function.Supplier;

public enum TickerType implements Supplier<Ticker> {

    GENERIC_ENTITY(GenericEntityTicker::new),
    MOB_EFFECT(MobEffectTicker::new),
    SPAWNER(SpawnerTicker::new);

    private final Supplier<Ticker> empty;

    TickerType(Supplier<Ticker> empty) {
        this.empty = empty;
    }

    @Override
    public Ticker get() {
        return empty.get();
    }

}
