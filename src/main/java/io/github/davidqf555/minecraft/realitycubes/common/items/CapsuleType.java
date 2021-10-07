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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public enum CapsuleType {

    SHAPE(
            new MemoryType("flat", 0xFF00FF00, settings -> settings.setShape(ShapesHelper.getShape("flat")), new BiomePeriodicCriteria(ImmutableList.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS))),
            new MemoryType("peak", 0xFF00FF00, settings -> settings.setShape(ShapesHelper.getShape("peak")), new WorldPeriodicCriteria(instance -> instance.player.position().y() > 100))
    ),
    BIOME(
            new MemoryType("plains", 0xFF00FF00, settings -> settings.setBiome(Biomes.PLAINS), new BiomePeriodicCriteria(ImmutableList.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS)))
    ),
    PRESET(
            new MemoryType("amnesia", 0xFF8888FF, settings -> settings.applyPreset(Preset.getPreset("default")), new BlockBreakCriteria(ImmutableList.of()))
    ),
    BLOCK(
            new MemoryType("stone", 0xFF888888, settings -> settings.setDefaultBlockType(DefaultBlockType.STONE), new BlockBreakCriteria(ImmutableList.of(Blocks.STONE)))
    ),
    FLUID(
            new MemoryType("water", 0xFF0000FF, settings -> settings.setDefaultFluidType(DefaultFluidType.WATER), new WorldPeriodicCriteria(instance -> instance.world.getBlockState(instance.player.blockPosition()).getBlock().equals(Blocks.WATER)))
    ),
    TIME(
            new MemoryType("midnight", 0xFF222222, settings -> settings.setTime(18000), new WorldPeriodicCriteria(instance -> instance.world.getDayTime() > 16000 && instance.world.getDayTime() < 20000))
    );

    private final CapsuleItem capsule;
    private final MemoryType[] memories;

    CapsuleType(MemoryType... memories) {
        this.memories = memories;
        capsule = (CapsuleItem) new CapsuleItem(this).setRegistryName(RealityCubes.MOD_ID, name().toLowerCase() + "_capsule");
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

    public MemoryType[] getMemories() {
        return memories;
    }

    public CapsuleItem getCapsule() {
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
        private final SettingsEditorItem item;
        private final String name;
        private final Criteria criteria;

        private MemoryType(String name, int color, Consumer<RealityCubeSettings> effect, Criteria criteria) {
            this.color = color;
            this.name = name;
            item = (SettingsEditorItem) new SettingsEditorItem(effect).setRegistryName(RealityCubes.MOD_ID, name + "_memory");
            this.criteria = criteria;
        }

        public SettingsEditorItem getItem() {
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
