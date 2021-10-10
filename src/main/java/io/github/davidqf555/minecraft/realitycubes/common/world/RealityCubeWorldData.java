package io.github.davidqf555.minecraft.realitycubes.common.world;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.Ticker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class RealityCubeWorldData extends SavedData {

    private static final String NAME = RealityCubes.MOD_ID + "_Data";
    private static final String TITLE = "message." + RealityCubes.MOD_ID + ".name";
    private static final double PARTICLES = 0.025;
    private final RealityCubeSettings settings;
    private final Vec3 originPos;
    private final ResourceKey<Level> originLevel;
    private int count;

    private RealityCubeWorldData(RealityCubeSettings settings, Vec3 originPos, ResourceKey<Level> originLevel) {
        this.settings = new RealityCubeSettings();
        this.settings.deserializeNBT(settings.serializeNBT());
        this.originPos = originPos;
        this.originLevel = originLevel;
        count = 0;
    }

    private RealityCubeWorldData(CompoundTag tag) {
        settings = new RealityCubeSettings();
        if (tag.contains("Settings", Constants.NBT.TAG_COMPOUND)) {
            settings.deserializeNBT(tag.getCompound("Settings"));
        }
        if (tag.contains("Count", Constants.NBT.TAG_INT)) {
            count = tag.getInt("Count");
        } else {
            count = 0;
        }
        if (tag.contains("X", Constants.NBT.TAG_DOUBLE) && tag.contains("Y", Constants.NBT.TAG_DOUBLE) && tag.contains("Z", Constants.NBT.TAG_DOUBLE)) {
            originPos = new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
        } else {
            originPos = Vec3.ZERO;
        }
        if (tag.contains("Level", Constants.NBT.TAG_STRING)) {
            originLevel = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("Level")));
        } else {
            originLevel = Level.OVERWORLD;
        }
    }

    @Nullable
    public static RealityCubeWorldData get(ServerLevel level) {
        return level.getDataStorage().get(RealityCubeWorldData::new, NAME);
    }

    public static void create(ServerLevel level, RealityCubeSettings settings, Vec3 originPos, ResourceKey<Level> originLevel) {
        RealityCubeWorldData data = new RealityCubeWorldData(settings, originPos, originLevel);
        data.setDirty();
        level.getDataStorage().set(NAME, data);
    }

    public void onTick(ServerLevel world) {
        UUID id = RealityCubeHelper.getRealityCube(world);
        Entity owner = world.getEntity(id);
        if (owner != null && (((ServerPlayer) owner).experienceLevel > 0 || ((ServerPlayer) owner).totalExperience > 0)) {
            world.setSpawnSettings(false, false);
            ServerLevel original = world.getServer().getLevel(getOriginLevel());
            RealityCubeSettings settings = getSettings();
            if (original != null) {
                Vec3 pos = getOriginPos();
                int range = settings.getRange();
                Random rand = original.getRandom();
                for (int x = -range; x <= range; x++) {
                    for (int y = -range; y <= range; y++) {
                        for (int z = -range; z <= range; z++) {
                            if (rand.nextDouble() < PARTICLES) {
                                original.sendParticles(ParticleTypes.END_ROD, pos.x() + x, pos.y() + y, pos.z() + z, 1, 0.5f, 0.5f, 0.5f, 0);
                            }
                        }
                    }
                }
                Team team = owner.getTeam();
                Component title = new TranslatableComponent(TITLE, owner.getDisplayName()).withStyle(team == null ? ChatFormatting.WHITE : team.getColor());
                for (Entity target : original.getEntities(null, AABB.ofSize(pos, range, range, range))) {
                    RealityCubeHelper.sendToRealityCube(world, target, settings, pos);
                    if (target instanceof ServerPlayer) {
                        ((ServerPlayer) target).connection.send(new ClientboundSetTitleTextPacket(title));
                    }
                }
            }
            count--;
            if (count <= 0) {
                RealityCubeHelper.decreaseExperiencePoints((ServerPlayer) owner, 1);
                count = (int) (1 / RealityCubeSettings.RATE);
            }
            for (Ticker ticker : settings.getTickers()) {
                ticker.onTick(world, settings);
            }
        } else {
            RealityCubeHelper.kickAll(world);
        }
    }

    public RealityCubeSettings getSettings() {
        return settings;
    }

    public Vec3 getOriginPos() {
        return originPos;
    }

    public ResourceKey<Level> getOriginLevel() {
        return originLevel;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putString("Level", getOriginLevel().location().toString());
        nbt.put("Settings", getSettings().serializeNBT());
        Vec3 pos = getOriginPos();
        nbt.putDouble("X", pos.x());
        nbt.putDouble("Y", pos.y());
        nbt.putDouble("Z", pos.z());
        nbt.putInt("Count", count);
        return nbt;
    }

}
