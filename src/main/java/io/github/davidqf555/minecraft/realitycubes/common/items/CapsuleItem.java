package io.github.davidqf555.minecraft.realitycubes.common.items;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class CapsuleItem extends Item {

    private static final String PROGRESS = "item." + RealityCubes.MOD_ID + ".capsule.progress";
    private final CapsuleType type;

    public CapsuleItem(CapsuleType type) {
        super(new Properties().tab(RealityCubes.TAB).stacksTo(1));
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> components, TooltipFlag flag) {
        components.add(new TranslatableComponent(PROGRESS, progressAsPercent(stack, 2)).withStyle(ChatFormatting.DARK_GREEN));
    }

    private String progressAsPercent(ItemStack item, int digits) {
        double val = getProgress(item, getDominantMemory(item));
        double pow = Math.pow(10, digits);
        return (int) (val * pow * 100) / pow + "";
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean held) {
        super.inventoryTick(stack, world, entity, slot, held);
        CapsuleType.MemoryType memory = getDominantMemory(stack);
        if (getProgress(stack, memory) >= 1) {
            IItemHandler inventory = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(ItemStackHandler::new);
            inventory.extractItem(slot, getItemStackLimit(stack), false);
            inventory.insertItem(slot, memory.getItem().getDefaultInstance(), false);
        }
    }

    public CapsuleType getType() {
        return type;
    }

    public CapsuleType.MemoryType getDominantMemory(ItemStack item) {
        CapsuleType type = getType();
        CapsuleType.MemoryType max = null;
        double maxVal = 0;
        for (CapsuleType.MemoryType memory : type.getMemories()) {
            double val = getProgress(item, memory);
            if (max == null || val > maxVal) {
                max = memory;
                maxVal = val;
            }
        }
        if (max == null) {
            throw new RuntimeException("No Possible Memory of type " + type);
        }
        return max;
    }

    public double getProgress(ItemStack item, CapsuleType.MemoryType memory) {
        CompoundTag tag = item.getOrCreateTagElement(RealityCubes.MOD_ID);
        if (tag.contains("Progress", Constants.NBT.TAG_COMPOUND)) {
            CompoundTag progress = tag.getCompound("Progress");
            return progress.contains(memory.getName(), Constants.NBT.TAG_DOUBLE) ? progress.getDouble(memory.getName()) : 0;
        }
        return 0;
    }

    public void increaseProgress(ItemStack item, CapsuleType.MemoryType memory, double value) {
        CompoundTag tag = item.getOrCreateTagElement(RealityCubes.MOD_ID);
        if (tag.contains("Progress", Constants.NBT.TAG_COMPOUND)) {
            CompoundTag progress = tag.getCompound("Progress");
            double initial = progress.contains(memory.getName(), Constants.NBT.TAG_DOUBLE) ? progress.getDouble(memory.getName()) : 0;
            progress.putDouble(memory.getName(), Math.min(1, initial + value));
            for (String key : progress.getAllKeys()) {
                if (progress.contains(key, Constants.NBT.TAG_DOUBLE) && !key.equals(memory.getName())) {
                    progress.putDouble(key, Math.max(0, progress.getDouble(key) - value / 2));
                }
            }
        } else {
            CompoundTag progress = new CompoundTag();
            progress.putDouble(memory.getName(), value);
            tag.put("Progress", progress);
        }
    }

}
