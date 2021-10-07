package io.github.davidqf555.minecraft.realitycubes.client;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleColor;
import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleItem;
import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleType;
import io.github.davidqf555.minecraft.realitycubes.common.packets.UseRealityCubePacket;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Arrays;

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
            event.enqueueWork(() -> Arrays.stream(CapsuleType.values()).map(CapsuleType::getCapsule).forEach(capsule -> {
                ItemProperties.register(capsule, new ResourceLocation(RealityCubes.MOD_ID, "progress"), (stack, world, entity, value) -> {
                    CapsuleItem item = ((CapsuleItem) stack.getItem());
                    return (float) item.getProgress(stack, item.getDominantMemory(stack));
                });
            }));
        }

        @SubscribeEvent
        public static void onHandleItemColors(ColorHandlerEvent.Item event) {
            CapsuleColor color = new CapsuleColor();
            for (CapsuleType type : CapsuleType.values()) {
                event.getItemColors().register(color, type.getCapsule());
            }
        }
    }
}
