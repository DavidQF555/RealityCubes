package io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.spawner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.Ticker;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.TickerType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
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
import java.util.*;

public class SpawnerTicker implements Ticker {

    private final Map<EntityType<?>, Integer> entities;
    private final List<SpawnCondition> conditions;
    private SpawnPlacements.Type type;
    private long period;

    public SpawnerTicker() {
        this(SpawnPlacements.Type.NO_RESTRICTIONS, ImmutableMap.of(), 400, ImmutableList.of());
    }

    public SpawnerTicker(SpawnPlacements.Type type, Map<EntityType<?>, Integer> entities, long period, List<SpawnCondition> conditions) {
        this.type = type;
        this.entities = new HashMap<>(entities);
        this.period = period;
        this.conditions = new ArrayList<>(conditions);
    }

    @Override
    public void onTick(ServerLevel world) {
        if (world.getGameTime() % period == 0 && world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            ServerChunkCache cache = world.getChunkSource();
            cache.chunkMap.getChunks().forEach(holder -> {
                Optional<LevelChunk> optional = holder.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
                if (optional.isPresent()) {
                    LevelChunk chunk = optional.get();
                    ChunkPos pos = chunk.getPos();
                    if (world.getWorldBorder().isWithinBounds(pos) && world.isPositionEntityTicking(pos) && !cache.chunkMap.noPlayersCloseForSpawning(pos)) {
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
                                world.addFreshEntity(entity);
                            }
                        }
                    }
                }
            });
        }
    }

    private boolean canSpawn(Entity entity, ServerLevelAccessor level, BlockPos pos) {
        boolean val = type.canSpawnAt(level, pos, entity.getType());
        for (SpawnCondition condition : conditions) {
            val = val && condition.canSpawn(level, pos);
        }
        return val;
    }

    @Nullable
    private Entity randomEntity(Level level) {
        int sum = entities.values().stream().reduce(0, Integer::sum);
        EntityType<?> type = null;
        double acc = 0;
        double rand = level.getRandom().nextDouble();
        for (Map.Entry<EntityType<?>, Integer> entry : entities.entrySet()) {
            acc += entry.getValue() * 1.0 / sum;
            if (acc < rand) {
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
        tag.putLong("Period", period);
        ListTag conditions = new ListTag();
        for (SpawnCondition condition : this.conditions) {
            conditions.add(StringTag.valueOf(condition.name()));
        }
        tag.put("Conditions", conditions);
        return tag;
    }

    @Override
    public void readAdditional(CompoundTag nbt) {
        if (nbt.contains("Type", Constants.NBT.TAG_STRING)) {
            type = SpawnPlacements.Type.valueOf(nbt.getString("Type"));
        }
        if (nbt.contains("Entities", Constants.NBT.TAG_COMPOUND)) {
            nbt.getCompound("Entities").getAllKeys().forEach(name -> entities.put(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name)), nbt.getInt(name)));
        }
        if (nbt.contains("Period", Constants.NBT.TAG_LONG)) {
            period = nbt.getLong("Period");
        }
        if (nbt.contains("Conditions", Constants.NBT.TAG_LIST)) {
            for (Tag tag : nbt.getList("Conditions", Constants.NBT.TAG_STRING)) {
                conditions.add(SpawnCondition.valueOf(tag.getAsString()));
            }
        }
    }

    @Override
    public TickerType getType() {
        return TickerType.SPAWNER;
    }
}
