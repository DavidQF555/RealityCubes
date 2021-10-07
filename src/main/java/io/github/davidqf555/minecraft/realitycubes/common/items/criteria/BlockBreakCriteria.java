package io.github.davidqf555.minecraft.realitycubes.common.items.criteria;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockBreakCriteria implements Criteria {

    private final List<Block> blocks;

    public BlockBreakCriteria(List<Block> blocks) {
        this.blocks = blocks;
    }

    @Override
    public boolean checkInstance(Criteria.Instance instance) {
        return instance instanceof Instance && blocks.contains(((Instance) instance).block);
    }

    @Override
    public double increaseAmount(Criteria.Instance instance) {
        return 0.005;
    }

    public static class Instance extends Criteria.Instance {

        private final Block block;

        public Instance(Player player, Block block) {
            super(player);
            this.block = block;
        }
    }
}
