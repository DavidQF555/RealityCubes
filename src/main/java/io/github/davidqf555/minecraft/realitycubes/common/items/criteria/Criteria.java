package io.github.davidqf555.minecraft.realitycubes.common.items.criteria;

import net.minecraft.world.entity.player.Player;

public interface Criteria {

    boolean checkInstance(Instance instance);

    double increaseAmount(Instance instance);

    class Instance {

        public final Player player;

        public Instance(Player player) {
            this.player = player;
        }
    }
}
