package io.github.davidqf555.minecraft.realitycubes.client;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.packets.UseRealityCubePacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class EventBusSubscriber {

    @Mod.EventBusSubscriber(modid = RealityCubes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBus {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.KeyInputEvent event) {
            if (KeyMappingsList.REALITY_CUBE.isDown()) {
                RealityCubes.CHANNEL.sendToServer(new UseRealityCubePacket());
            }
        }
    }

    @Mod.EventBusSubscriber(modid = RealityCubes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {

        @SubscribeEvent
        public static void onFMLClientSetup(FMLClientSetupEvent event) {
            KeyMappingsList.register();
        }

    }
}
