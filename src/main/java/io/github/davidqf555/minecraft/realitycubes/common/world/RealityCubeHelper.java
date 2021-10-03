package io.github.davidqf555.minecraft.realitycubes.common.world;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.ReturnData;
import io.github.davidqf555.minecraft.realitycubes.common.packets.UpdateClientLevelsPacket;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.GenerationShape;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.OptionalLong;
import java.util.Random;
import java.util.UUID;

public class RealityCubeHelper {

    public static void decreaseExperiencePoints(ServerPlayer player, int points) {
        while (points > 0 && (player.experienceLevel > 0 || player.totalExperience > 0)) {
            if (points <= player.totalExperience) {
                player.giveExperiencePoints(-points);
                points = 0;
            } else {
                points -= player.totalExperience;
                player.giveExperienceLevels(-1);
                player.giveExperiencePoints(player.getXpNeededForNextLevel() - player.totalExperience);
            }
        }
    }

    public static void kickAll(ServerLevel world) {
        ResourceKey<Level> original = RealityCubeWorldData.get(world).getOriginLevel();
        for (Entity entity : world.getAllEntities()) {
            RealityCubeHelper.returnToWorld(original, entity);
        }
    }

    public static void sendToRealityCube(ServerLevel cube, Entity target, RealityCubeSettings settings, Vec3 center) {
        if (target.canChangeDimensions()) {
            ReturnData data = ReturnData.get(target);
            data.setReturnLocation(target.position());
            data.setXRotation(target.getXRot());
            data.setYRotation(target.getYRot());
            int range = settings.getRange();
            int radius = SectionPos.sectionToBlockCoord(settings.getChunkRadius());
            target.changeDimension(cube, new RealityCubeTeleporter(Mth.clamp((target.getX() - center.x()) * radius / range, -radius, radius), Mth.clamp((target.getZ() - center.z()) * radius / range, -radius, radius)));
        }
    }

    public static void returnToWorld(ResourceKey<Level> key, Entity target) {
        ReturnData data = ReturnData.get(target);
        if (target.canChangeDimensions() && (data.exists() || target instanceof Player)) {
            MinecraftServer server = target.getServer();
            if (server != null) {
                ServerLevel world;
                if (key == null) {
                    if (target instanceof Player) {
                        world = server.overworld();
                    } else {
                        return;
                    }
                } else {
                    world = server.getLevel(key);
                }
                target.changeDimension(world, new ReturnTeleporter());
            }
        }
    }

    @Nullable
    public static UUID getRealityCube(Level world) {
        return getRealityCube(world.dimension());
    }

    @Nullable
    public static UUID getRealityCube(ResourceKey<Level> level) {
        ResourceLocation loc = level.location();
        if (loc.getNamespace().equals(RealityCubes.MOD_ID)) {
            return UUID.fromString(loc.getPath());
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static ServerLevel createRealityCube(MinecraftServer server, UUID id, RealityCubeSettings cube, Vec3 originPos, ResourceKey<Level> originLevel) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        ResourceKey<LevelStem> stemKey = createDimensionKey(id);
        WorldData data = server.getWorldData();
        WorldGenSettings worldGen = data.worldGenSettings();
        ResourceKey<Level> key = createLevelKey(id);
        LevelStem dimension = createRealityCubeDimension(server, key, cube, overworld.getRandom());
        worldGen.dimensions().register(stemKey, dimension, Lifecycle.experimental());
        DerivedLevelData derived = new DerivedLevelData(data, data.overworldData());
        ServerLevel newLevel = new ServerLevel(server, server.executor, server.storageSource, derived, key, dimension.type(), server.progressListenerFactory.create(11), dimension.generator(), worldGen.isDebug(), worldGen.seed(), ImmutableList.of(), false);
        overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newLevel.getWorldBorder()));
        server.forgeGetWorldMap().put(key, newLevel);
        server.markWorldsDirty();
        RealityCubeWorldData.create(newLevel, cube, originPos, originLevel);
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(newLevel));
        RealityCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new UpdateClientLevelsPacket(key));
        return newLevel;
    }

    private static ResourceKey<Level> createLevelKey(UUID id) {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(RealityCubes.MOD_ID, id.toString()));
    }

    private static ResourceKey<LevelStem> createDimensionKey(UUID id) {
        return ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(RealityCubes.MOD_ID, id.toString()));
    }

    private static LevelStem createRealityCubeDimension(MinecraftServer server, ResourceKey<Level> level, RealityCubeSettings data, Random random) {
        ChunkGenerator generator = new ShapeChunkGenerator(data, level, new FixedBiomeSource(() -> server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).get(data.getBiome())), random.nextLong());
        DimensionType type = createDimensionType(data);
        return new LevelStem(() -> type, generator);
    }

    private static DimensionType createDimensionType(RealityCubeSettings data) {
        GenerationShape shape = data.getShape();
        boolean hasCeiling = shape.hasCeiling();
        long time = data.getTime();
        int min = shape.getMinY(data);
        int height = shape.getHeight(data);
        return DimensionType.create(time == -1 ? OptionalLong.empty() : OptionalLong.of(time), !hasCeiling, hasCeiling, false, false, 1, false, false, false, false, false, min, height, height, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), DimensionType.OVERWORLD_EFFECTS, 0);
    }

}
