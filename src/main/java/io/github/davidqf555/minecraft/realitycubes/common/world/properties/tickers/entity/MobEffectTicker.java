package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.entity;

import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.TickerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

public class MobEffectTicker implements EntityTicker {

    private MobEffect effect;
    private int amp;

    public MobEffectTicker(MobEffect effect, int amp) {
        this.effect = effect;
        this.amp = amp;
    }

    public MobEffectTicker() {
        this(MobEffects.REGENERATION, 1);
    }

    @Override
    public TickerType getType() {
        return TickerType.MOB_EFFECT;
    }

    @Override
    public void accept(Entity entity) {
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).addEffect(new MobEffectInstance(effect, 2, amp - 1));
        }
    }

    @Override
    public void readAdditional(CompoundTag nbt) {
        if (nbt.contains("Effect", Constants.NBT.TAG_STRING)) {
            effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(nbt.getString("Effect")));
        }
        if (nbt.contains("Amplification", Constants.NBT.TAG_INT)) {
            amp = nbt.getInt("Amplification");
        }
    }

    @Override
    public CompoundTag writeAdditional() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("Effect", effect.getRegistryName().toString());
        nbt.putInt("Amplification", amp);
        return nbt;
    }
}
