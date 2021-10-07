package io.github.davidqf555.minecraft.realitycubes.common.items.criteria;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class WorldPeriodicCriteria implements Criteria {

    private final Predicate<Instance> condition;

    public WorldPeriodicCriteria(Predicate<Instance> condition) {
        this.condition = condition;
    }

    @Override
    public boolean checkInstance(Criteria.Instance instance) {
        return instance instanceof Instance && condition.test((Instance) instance);
    }

    @Override
    public double increaseAmount(Criteria.Instance instance) {
        return 0.001;
    }

    public static class Instance extends Criteria.Instance {

        public final Level world;

        public Instance(Player player, Level world) {
            super(player);
            this.world = world;
        }
    }
}
