package io.github.davidqf555.minecraft.realitycubes.common.items;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

public class CapsuleColor implements ItemColor {
    @Override
    public int getColor(ItemStack stack, int tint) {
        return ((CapsuleItem) stack.getItem()).getDominantMemory(stack).getColor();
    }
}
