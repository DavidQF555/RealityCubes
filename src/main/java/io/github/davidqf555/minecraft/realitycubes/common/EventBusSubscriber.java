package io.github.davidqf555.minecraft.realitycubes.common;

import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.ReturnData;
import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleType;
import io.github.davidqf555.minecraft.realitycubes.common.packets.UpdateClientLevelsPacket;
import io.github.davidqf555.minecraft.realitycubes.common.packets.UseRealityCubePacket;
import io.github.davidqf555.minecraft.realitycubes.common.world.RealityCubeHelper;
import io.github.davidqf555.minecraft.realitycubes.common.world.RealityCubeWorldData;
import io.github.davidqf555.minecraft.realitycubes.common.world.ShapeChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventBusSubscriber {

    @Mod.EventBusSubscriber(modid = RealityCubes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBus {

        private static final ResourceLocation REALITY_CUBES = new ResourceLocation(RealityCubes.MOD_ID, "reality_cubes");
        private static final ResourceLocation RETURN = new ResourceLocation(RealityCubes.MOD_ID, "return");
        private static final Map<UUID, RealityCubeSettings> CLONED = new HashMap<>();

        @SubscribeEvent
        public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
            Entity entity = event.getObject();
            event.addCapability(RETURN, new ReturnData.Provider());
            if (entity instanceof Player) {
                event.addCapability(REALITY_CUBES, new RealityCubeSettings.Provider());
            }
        }

        @SubscribeEvent
        public static void onWorldTickEvent(TickEvent.WorldTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel && RealityCubeHelper.getRealityCube(event.world) != null && !((ServerLevel) event.world).players().isEmpty()) {
                RealityCubeWorldData.get((ServerLevel) event.world).onTick((ServerLevel) event.world);
            }
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            RealityCubeCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onClonePlayerEvent(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                ServerPlayer original = (ServerPlayer) event.getOriginal();
                CLONED.put(original.getUUID(), RealityCubeSettings.get(original));
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            RealityCubeSettings cloned = CLONED.get(player.getUUID());
            if (cloned != null) {
                RealityCubeSettings.get(player).deserializeNBT(cloned.serializeNBT());
            }
        }

    }

    @Mod.EventBusSubscriber(modid = RealityCubes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {

        private static int index = 0;

        @SubscribeEvent
        public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(RealityCubes.MOD_ID, "chunk_generator_codec"), ShapeChunkGenerator.CODEC);
                UseRealityCubePacket.register(index++);
                UpdateClientLevelsPacket.register(index++);
            });
        }

        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(RealityCubeSettings.class);
            event.register(ReturnData.class);
        }

        @SubscribeEvent
        public static void onRegisterItems(RegistryEvent.Register<Item> event) {
            IForgeRegistry<Item> registry = event.getRegistry();
            for (CapsuleType type : CapsuleType.values()) {
                registry.register(type.getCapsule());
                for (CapsuleType.MemoryType memory : type.getMemories()) {
                    registry.register(memory.getItem());
                }
            }
        }
    }
}
