package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public interface Ticker extends INBTSerializable<CompoundTag> {

    static Ticker deserialize(CompoundTag tag) {
        Ticker ticker = TickerType.valueOf(tag.getString("Type")).get();
        ticker.deserializeNBT(tag);
        return ticker;
    }

    void onTick(ServerLevel world);

    TickerType getType();

    default CompoundTag writeAdditional() {
        return new CompoundTag();
    }

    default void readAdditional(CompoundTag nbt) {
    }

    @Override
    default CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", getType().name());
        tag.put("Data", writeAdditional());
        return tag;
    }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("Data", Constants.NBT.TAG_COMPOUND)) {
            readAdditional(nbt.getCompound("Data"));
        }
    }
}
