package io.github.davidqf555.minecraft.realitycubes.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReturnData implements INBTSerializable<CompoundTag> {

    private boolean exists;
    private Vec3 loc;
    private float xRot;
    private float yRot;

    public ReturnData() {
        exists = true;
        loc = null;
        xRot = 0;
        yRot = 0;
    }

    public static ReturnData get(Entity entity) {
        return entity.getCapability(Provider.capability).orElseThrow(NullPointerException::new);
    }

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    @Nullable
    public Vec3 getReturnLocation() {
        return loc;
    }

    public void setReturnLocation(@Nullable Vec3 loc) {
        this.loc = loc;
    }

    public float getXRotation() {
        return xRot;
    }

    public void setXRotation(float xRot) {
        this.xRot = xRot;
    }

    public float getYRotation() {
        return yRot;
    }

    public void setYRotation(float yRot) {
        this.yRot = yRot;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        Vec3 loc = getReturnLocation();
        if (loc != null) {
            tag.putDouble("X", loc.x());
            tag.putDouble("Y", loc.y());
            tag.putDouble("Z", loc.z());
        }
        tag.putBoolean("Exists", exists());
        tag.putFloat("xRot", getXRotation());
        tag.putFloat("yRot", getYRotation());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("X", Constants.NBT.TAG_DOUBLE) && nbt.contains("Y", Constants.NBT.TAG_DOUBLE) && nbt.contains("Z", Constants.NBT.TAG_DOUBLE)) {
            setReturnLocation(new Vec3(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z")));
        }
        if (nbt.contains("Exists", Constants.NBT.TAG_BYTE)) {
            setExists(nbt.getBoolean("Exists"));
        }
        if (nbt.contains("xRot", Constants.NBT.TAG_FLOAT)) {
            setXRotation(nbt.getFloat("XRot"));
        }
        if (nbt.contains("yRot", Constants.NBT.TAG_FLOAT)) {
            setYRotation(nbt.getFloat("YRot"));
        }
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        @CapabilityInject(ReturnData.class)
        public static Capability<ReturnData> capability = null;
        private final LazyOptional<ReturnData> instance;

        public Provider() {
            ReturnData equips = new ReturnData();
            instance = LazyOptional.of(() -> equips);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return cap == capability ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(NullPointerException::new).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            instance.orElseThrow(NullPointerException::new).deserializeNBT(tag);
        }
    }
}
