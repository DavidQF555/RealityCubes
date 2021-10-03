package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.entity;

import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;
import java.util.function.Supplier;

public enum GenericEntityTickerType implements Consumer<Entity>, Supplier<GenericEntityTicker> {

    FIRE(entity -> {
        if (entity.level.getRandom().nextDouble() < 0.05) {
            entity.setSecondsOnFire(2);
        }
    });

    private final Consumer<Entity> effect;

    GenericEntityTickerType(Consumer<Entity> effect) {
        this.effect = effect;
    }

    @Override
    public void accept(Entity entity) {
        effect.accept(entity);
    }

    @Override
    public GenericEntityTicker get() {
        return new GenericEntityTicker(this);
    }
}
