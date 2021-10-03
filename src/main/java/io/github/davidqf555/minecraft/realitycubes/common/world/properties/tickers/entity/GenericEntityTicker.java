package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.entity;

import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.TickerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.Constants;

public class GenericEntityTicker implements EntityTicker {

    private GenericEntityTickerType type;

    public GenericEntityTicker(GenericEntityTickerType type) {
        this.type = type;
    }

    public GenericEntityTicker() {
        this(GenericEntityTickerType.FIRE);
    }

    @Override
    public TickerType getType() {
        return TickerType.GENERIC_ENTITY;
    }

    @Override
    public void accept(Entity entity) {
        type.accept(entity);
    }

    @Override
    public void readAdditional(CompoundTag nbt) {
        if (nbt.contains("Type", Constants.NBT.TAG_STRING)) {
            type = GenericEntityTickerType.valueOf(nbt.getString("Type"));
        }
    }

    @Override
    public CompoundTag writeAdditional() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("Type", type.name());
        return nbt;
    }
}
