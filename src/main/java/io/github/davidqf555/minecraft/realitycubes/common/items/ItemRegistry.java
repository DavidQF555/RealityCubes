package io.github.davidqf555.minecraft.realitycubes.common.items;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner.EntityEffect;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner.SpawnPredicate;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner.SpawnerTicker;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = RealityCubes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemRegistry {

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (CapsuleType type : CapsuleType.values()) {
            registry.registerAll(type.createAll());
        }
        registry.registerAll(new SettingsEditorItem(settings -> settings.addTickers(new SpawnerTicker(SpawnPlacements.Type.NO_RESTRICTIONS, ImmutableMap.of(EntityType.ARROW, 10, EntityType.SPECTRAL_ARROW, 2, EntityType.TRIDENT, 1), 1, ImmutableList.of(SpawnPredicate.AIR), ImmutableList.of(EntityEffect.RANDOM_LAUNCH, EntityEffect.LOWER_ARROW_LIFE)))).setRegistryName(RealityCubes.MOD_ID, "broken_arrow"),
                new SettingsEditorItem(settings -> settings.addTickers(new SpawnerTicker(SpawnPlacements.Type.ON_GROUND, ImmutableMap.of(EntityType.ZOMBIE, 10, EntityType.HUSK, 2, EntityType.ZOMBIE_VILLAGER, 1, EntityType.ZOMBIFIED_PIGLIN, 1), 100, ImmutableList.of(SpawnPredicate.DARK), ImmutableList.of()))).setRegistryName(RealityCubes.MOD_ID, "infected_flesh")
        );
    }
}
