package io.github.davidqf555.minecraft.realitycubes.common.items;

import com.google.common.collect.ImmutableList;
import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.items.criteria.BiomePeriodicCriteria;
import io.github.davidqf555.minecraft.realitycubes.common.items.criteria.BlockBreakCriteria;
import io.github.davidqf555.minecraft.realitycubes.common.items.criteria.Criteria;
import io.github.davidqf555.minecraft.realitycubes.common.items.criteria.WorldPeriodicCriteria;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.DefaultBlockType;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.DefaultFluidType;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.Preset;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.ShapesHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public enum CapsuleType {

    SHAPE(Tags.Items.OBSIDIAN,
            new MemoryType("flat", 0xFF00FF00, settings -> settings.setShape(ShapesHelper.getShape("flat")), new BiomePeriodicCriteria(ImmutableList.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS))),
            new MemoryType("peak", 0xFF00FF00, settings -> settings.setShape(ShapesHelper.getShape("peak")), new WorldPeriodicCriteria(instance -> instance.player.position().y() > 100))
    ),
    BIOME(Tags.Items.RODS_BLAZE,
            new MemoryType("plains", 0xFF00FF00, settings -> {
                settings.setBiome(Biomes.PLAINS);
                settings.setDefaultBlockType(DefaultBlockType.STONE);
                settings.setDefaultFluidType(DefaultFluidType.WATER);
            }, new BiomePeriodicCriteria(ImmutableList.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS)))
    ),
    PRESET(Tags.Items.GEMS_DIAMOND,
            new MemoryType("amnesia", 0xFF8888FF, settings -> settings.applyPreset(Preset.getPreset("default")), new BlockBreakCriteria(ImmutableList.of()))
    ),
    TIME(Tags.Items.INGOTS_GOLD,
            new MemoryType("midnight", 0xFF222222, settings -> settings.setTime(18000), new WorldPeriodicCriteria(instance -> instance.world.getDayTime() > 16000 && instance.world.getDayTime() < 20000))
    );
    private final Tag<Item> recipe;
    private final RegistryObject<CapsuleItem> capsule;
    private final Supplier<Item> factory;
    private final MemoryType[] memories;

    CapsuleType(Tag<Item> recipe, MemoryType... memories) {
        this.recipe = recipe;
        this.memories = memories;
        ResourceLocation loc = new ResourceLocation(RealityCubes.MOD_ID, name().toLowerCase() + "_capsule");
        capsule = RegistryObject.of(loc, ForgeRegistries.ITEMS);
        factory = () -> new CapsuleItem(this).setRegistryName(loc);
    }

    public static void processCriteriaInstance(Criteria.Instance instance) {
        for (CapsuleType type : values()) {
            for (MemoryType memory : type.getMemories()) {
                if (memory.criteria.checkInstance(instance)) {
                    for (ItemStack item : type.getFromInventoryCapsules(instance.player)) {
                        double value = memory.criteria.increaseAmount(instance);
                        ((CapsuleItem) item.getItem()).increaseProgress(item, memory, value);
                    }
                }
            }
        }
    }

    public Tag<Item> getRecipe() {
        return recipe;
    }

    public Item[] createAll() {
        Item[] all = new Item[memories.length + 1];
        all[0] = factory.get();
        for (int i = 0; i < memories.length; i++) {
            all[i + 1] = memories[i].factory.get();
        }
        return all;
    }

    public MemoryType[] getMemories() {
        return memories;
    }

    public RegistryObject<CapsuleItem> getCapsule() {
        return capsule;
    }

    public List<ItemStack> getFromInventoryCapsules(Player player) {
        List<ItemStack> capsules = new ArrayList<>();
        IItemHandler inventory = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(ItemStackHandler::new);
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem().equals(getCapsule())) {
                capsules.add(stack);
            }
        }
        return capsules;
    }

    public static class MemoryType {

        private final int color;
        private final RegistryObject<SettingsEditorItem> item;
        private final Supplier<Item> factory;
        private final String name;
        private final Criteria criteria;

        private MemoryType(String name, int color, Consumer<RealityCubeSettings> effect, Criteria criteria) {
            this.color = color;
            this.name = name;
            this.criteria = criteria;
            ResourceLocation loc = new ResourceLocation(RealityCubes.MOD_ID, name.toLowerCase() + "_memory");
            item = RegistryObject.of(loc, ForgeRegistries.ITEMS);
            factory = () -> new SettingsEditorItem(effect).setRegistryName(loc);
        }

        public RegistryObject<SettingsEditorItem> getItem() {
            return item;
        }

        public int getColor() {
            return color;
        }

        public String getName() {
            return name;
        }
    }

}
