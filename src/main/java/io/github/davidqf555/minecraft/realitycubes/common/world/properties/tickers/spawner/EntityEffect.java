package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.function.Consumer;

public enum EntityEffect implements Consumer<Entity> {

    RANDOM_LAUNCH(entity -> {
        double mag = 2;
        Random rand = entity.level.getRandom();
        Vec3 push = new Vec3(0, 1, 0).xRot(rand.nextFloat() * 2 * (float) Math.PI).yRot(rand.nextFloat() * 2 * (float) Math.PI).zRot(rand.nextFloat() * 2 * (float) Math.PI).scale(mag);
        entity.push(push.x(), push.y(), push.z());
    }),
    LOWER_ARROW_LIFE(entity -> {
        if (entity instanceof AbstractArrow) {
            ((AbstractArrow) entity).life = 1100;
        }
    });

    private final Consumer<Entity> effect;

    EntityEffect(Consumer<Entity> effect) {
        this.effect = effect;
    }

    @Override
    public void accept(Entity entity) {
        effect.accept(entity);
    }
}
