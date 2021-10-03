package io.github.davidqf555.minecraft.realitycubes.common;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.DefaultBlockType;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.DefaultFluidType;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.Preset;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.GenerationShape;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes.ShapesHelper;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.Ticker;
import io.github.davidqf555.minecraft.realitycubes.common.world.properties.tickers.TickerType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LocateBiomeCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.server.command.EnumArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RealityCubeCommand {

    private static final DynamicCommandExceptionType INVALID_SHAPE = new DynamicCommandExceptionType(ob -> new TranslatableComponent("commands." + RealityCubes.MOD_ID + ".invalid_shape", ob));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rc")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.literal("radius")
                                .then(Commands.argument("value", IntegerArgumentType.integer(RealityCubeSettings.MIN_RADIUS, RealityCubeSettings.MAX_RADIUS))
                                        .executes(context -> changeRadius(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "value")))
                                )
                        )
                        .then(Commands.literal("biome")
                                .then(Commands.argument("value", ResourceLocationArgument.id())
                                        .suggests(SuggestionProviders.AVAILABLE_BIOMES)
                                        .executes(context -> changeBiome(context.getSource(), EntityArgument.getPlayers(context, "targets"), context.getArgument("value", ResourceLocation.class)))
                                )
                        )
                        .then(Commands.literal("shape")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (String name : ShapesHelper.getShapes()) {
                                                builder.suggest(name);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> changeShape(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "value")))
                                )
                        )
                        .then(Commands.literal("time")
                                .then(Commands.argument("value", LongArgumentType.longArg(-1, 24000))
                                        .executes(context -> changeTime(context.getSource(), EntityArgument.getPlayers(context, "targets"), LongArgumentType.getLong(context, "value")))
                                )
                        )
                        .then(Commands.literal("block")
                                .then(Commands.argument("value", EnumArgument.enumArgument(DefaultBlockType.class))
                                        .executes(context -> changeBlock(context.getSource(), EntityArgument.getPlayers(context, "targets"), context.getArgument("value", DefaultBlockType.class)))
                                )
                        )
                        .then(Commands.literal("fluid")
                                .then(Commands.argument("value", EnumArgument.enumArgument(DefaultFluidType.class))
                                        .executes(context -> changeFluid(context.getSource(), EntityArgument.getPlayers(context, "targets"), context.getArgument("value", DefaultFluidType.class)))
                                )
                        )
                        .then(Commands.literal("range")
                                .then(Commands.argument("value", IntegerArgumentType.integer(RealityCubeSettings.MIN_RANGE, RealityCubeSettings.MAX_RANGE))
                                        .executes(context -> changeRange(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "value")))
                                )
                        )
                        .then(Commands.literal("ticker")
                                .then(Commands.literal("clear")
                                        .executes(context -> clearTickers(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("type", EnumArgument.enumArgument(TickerType.class))
                                                .executes(context -> addTicker(context.getSource(), EntityArgument.getPlayers(context, "targets"), context.getArgument("type", TickerType.class), new CompoundTag()))
                                                .then(Commands.argument("data", CompoundTagArgument.compoundTag())
                                                        .executes(context -> addTicker(context.getSource(), EntityArgument.getPlayers(context, "targets"), context.getArgument("type", TickerType.class), CompoundTagArgument.getCompoundTag(context, "data")))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("preset")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (String name : Preset.getPresets()) {
                                                builder.suggest(name);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> applyPreset(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "value")))
                                )
                        )
                )
        );
    }

    private static int applyPreset(CommandSourceStack source, Collection<ServerPlayer> targets, String preset) {
        Preset p = Preset.getPreset(preset);
        if (p != null) {
            for (ServerPlayer player : targets) {
                RealityCubeSettings settings = RealityCubeSettings.get(player);
                settings.applyPreset(p);
            }
            return targets.size();
        }
        return 0;
    }

    private static int changeRadius(CommandSourceStack source, Collection<ServerPlayer> targets, int radius) {
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            settings.setChunkRadius(radius);
        }
        return targets.size();
    }

    private static int changeRange(CommandSourceStack source, Collection<ServerPlayer> targets, int range) {
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            settings.setRange(range);
        }
        return targets.size();
    }

    private static int clearTickers(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            settings.setTickers(ImmutableList.of());
        }
        return targets.size();
    }

    private static int addTicker(CommandSourceStack source, Collection<ServerPlayer> targets, TickerType type, CompoundTag data) {
        Ticker ticker = type.get();
        ticker.readAdditional(data);
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            List<Ticker> tickers = new ArrayList<>(settings.getTickers());
            tickers.add(ticker);
            settings.setTickers(tickers);
        }
        return targets.size();
    }

    private static int changeBiome(CommandSourceStack source, Collection<ServerPlayer> targets, ResourceLocation loc) throws CommandSyntaxException {
        Biome biome = source.getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOptional(loc).orElseThrow(() -> LocateBiomeCommand.ERROR_INVALID_BIOME.create(loc));
        ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName());
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            settings.setBiome(key);
        }
        return targets.size();
    }

    private static int changeShape(CommandSourceStack source, Collection<ServerPlayer> targets, String shape) throws CommandSyntaxException {
        GenerationShape s = ShapesHelper.getShape(shape);
        if (s == null) {
            throw INVALID_SHAPE.create(shape);
        }
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            settings.setShape(s);
        }
        return targets.size();
    }

    private static int changeTime(CommandSourceStack source, Collection<ServerPlayer> targets, long time) {
        Long value;
        if (time == -1) {
            value = null;
        } else {
            value = time;
        }
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            settings.setTime(value);
        }
        return targets.size();
    }

    private static int changeBlock(CommandSourceStack source, Collection<ServerPlayer> targets, DefaultBlockType block) {
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            settings.setDefaultBlockType(block);
        }
        return targets.size();
    }

    private static int changeFluid(CommandSourceStack source, Collection<ServerPlayer> targets, DefaultFluidType fluid) {
        for (ServerPlayer player : targets) {
            RealityCubeSettings settings = RealityCubeSettings.get(player);
            settings.setDefaultFluidType(fluid);
        }
        return targets.size();
    }
}
