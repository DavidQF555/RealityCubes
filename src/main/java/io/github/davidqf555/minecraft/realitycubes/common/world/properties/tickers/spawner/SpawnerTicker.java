package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.ReturnData;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.Ticker;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.TickerType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerTicker implements Ticker {

    private final Map<EntityType<?>, Integer> entities;
    private final List<SpawnPredicate> conditions;
    private final List<EntityEffect> effects;
    private SpawnPlacements.Type type;
    private double period;

    public SpawnerTicker() {
        this(SpawnPlacements.Type.NO_RESTRICTIONS, ImmutableMap.of(), 400, ImmutableList.of(), ImmutableList.of());
    }

    public SpawnerTicker(SpawnPlacements.Type type, Map<EntityType<?>, Integer> entities, double period, List<SpawnPredicate> conditions, List<EntityEffect> effects) {
        this.type = type;
        this.entities = new HashMap<>(entities);
        this.period = period;
        this.conditions = new ArrayList<>(conditions);
        this.effects = new ArrayList<>(effects);
    }

    @Override
    public void onTick(ServerLevel world, RealityCubeSettings settings) {
        if ((period < 2 || world.getGameTime() % (int) period == 0) && world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            ServerChunkCache cache = world.getChunkSource();
            int radius = settings.getChunkRadius();
            for (int cX = -radius; cX < radius; cX++) {
                for (int cZ = -radius; cZ < radius; cZ++) {
                    LevelChunk chunk = cache.getChunk(cX, cZ, false);
                    if (chunk != null) {
                        ChunkPos pos = chunk.getPos();
                        if (world.isPositionEntityTicking(pos)) {
                            int count = Math.max(1, (int) (1 / period));
                            for (int i = 0; i < count; i++) {
                                Entity entity = randomEntity(world);
                                if (entity != null) {
                                    List<BlockPos> possible = new ArrayList<>();
                                    int x = pos.getMinBlockX();
                                    int z = pos.getMinBlockZ();
                                    for (int dX = 0; dX < 16; dX++) {
                                        for (int dZ = 0; dZ < 16; dZ++) {
                                            for (int y = world.getMinBuildHeight(); y < world.getMaxBuildHeight(); y++) {
                                                BlockPos spawn = new BlockPos(x + dX, y, z + dZ);
                                                if (canSpawn(entity, world, spawn)) {
                                                    possible.add(spawn);
                                                }
                                            }
                                        }
                                    }
                                    if (!possible.isEmpty()) {
                                        BlockPos spawn = possible.get(world.random.nextInt(possible.size()));
                                        entity.setPos(spawn.getX(), spawn.getY(), spawn.getZ());
                                        applyEffects(entity);
                                        ReturnData.get(entity).setExists(false);
                                        world.addFreshEntity(entity);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean canSpawn(Entity entity, ServerLevelAccessor level, BlockPos pos) {
        boolean val = type.canSpawnAt(level, pos, entity.getType());
        if (val) {
            for (SpawnPredicate condition : conditions) {
                if (!condition.test(level, pos)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void applyEffects(Entity entity) {
        for (EntityEffect effect : effects) {
            effect.accept(entity);
        }
    }

    @Nullable
    private Entity randomEntity(Level level) {
        int sum = entities.values().stream().reduce(0, Integer::sum);
        EntityType<?> type = null;
        double acc = 0;
        double rand = level.getRandom().nextDouble();
        for (Map.Entry<EntityType<?>, Integer> entry : entities.entrySet()) {
            acc += entry.getValue() * 1.0 / sum;
            if (rand < acc) {
                type = entry.getKey();
                break;
            }
        }
        return type == null ? null : type.create(level);
    }

    @Override
    public CompoundTag writeAdditional() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type.name());
        CompoundTag entities = new CompoundTag();
        this.entities.forEach((type, weight) -> entities.putInt(type.getRegistryName().toString(), weight));
        tag.put("Entities", entities);
        tag.putDouble("Period", period);
        ListTag conditions = new ListTag();
        for (SpawnPredicate condition : this.conditions) {
            conditions.add(StringTag.valueOf(condition.name()));
        }
        tag.put("Conditions", conditions);
        ListTag effects = new ListTag();
        for (EntityEffect effect : this.effects) {
            effects.add(StringTag.valueOf(effect.name()));
        }
        tag.put("Effects", effects);
        return tag;
    }

    @Override
    public void readAdditional(CompoundTag nbt) {
        if (nbt.contains("Type", Constants.NBT.TAG_STRING)) {
            type = SpawnPlacements.Type.valueOf(nbt.getString("Type"));
        }
        if (nbt.contains("Entities", Constants.NBT.TAG_COMPOUND)) {
            CompoundTag entities = nbt.getCompound("Entities");
            for (String key : entities.getAllKeys()) {
                if (entities.contains(key, Constants.NBT.TAG_INT)) {
                    this.entities.put(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(key)), entities.getInt(key));
                }
            }
        }
        if (nbt.contains("Period", Constants.NBT.TAG_DOUBLE)) {
            period = nbt.getDouble("Period");
        }
        if (nbt.contains("Conditions", Constants.NBT.TAG_LIST)) {
            for (Tag tag : nbt.getList("Conditions", Constants.NBT.TAG_STRING)) {
                conditions.add(SpawnPredicate.valueOf(tag.getAsString()));
            }
        }
        if (nbt.contains("Effects", Constants.NBT.TAG_LIST)) {
            for (Tag tag : nbt.getList("Effects", Constants.NBT.TAG_STRING)) {
                effects.add(EntityEffect.valueOf(tag.getAsString()));
            }
        }
    }

    @Override
    public TickerType getType() {
        return TickerType.SPAWNER;
    }
}
