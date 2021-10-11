package io.github.davidqf555.minecraft.realitycubes.client;

import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

public class CapsuleColor implements ItemColor {
    @Override
    public int getColor(ItemStack stack, int tint) {
        return tint > 0 ? -1 : ((CapsuleItem) stack.getItem()).getDominantMemory(stack).getColor();
    }
}
