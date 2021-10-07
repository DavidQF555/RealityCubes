package io.github.davidqf555.minecraft.realitycubes.common.items.criteria;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class CriteriaEventBusSubscriber {

    @Mod.EventBusSubscriber(modid = RealityCubes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBus {

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.START && event.player instanceof ServerPlayer && event.player.level.getGameTime() % 100 == 0) {
                CapsuleType.processCriteriaInstance(new WorldPeriodicCriteria.Instance(event.player, event.player.level));
            }
        }

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            CapsuleType.processCriteriaInstance(new BlockBreakCriteria.Instance(event.getPlayer(), event.getState().getBlock()));
        }

    }
}
