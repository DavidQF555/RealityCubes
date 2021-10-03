package io.github.davidqf555.minecraft.realitycubes.common.packets;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.world.RealityCubeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UseRealityCubePacket {

    private static final Component EXP = new TranslatableComponent("message." + RealityCubes.MOD_ID + ".exp").withStyle(ChatFormatting.RED);
    private static final Component WITHIN = new TranslatableComponent("message." + RealityCubes.MOD_ID + ".within").withStyle(ChatFormatting.RED);
    private static final BiConsumer<UseRealityCubePacket, FriendlyByteBuf> ENCODER = (message, buffer) -> {
    };
    private static final Function<FriendlyByteBuf, UseRealityCubePacket> DECODER = buffer -> new UseRealityCubePacket();
    private static final BiConsumer<UseRealityCubePacket, Supplier<NetworkEvent.Context>> CONSUMER = (message, context) -> {
        NetworkEvent.Context cont = context.get();
        message.handle(cont);
    };

    public static void register(int index) {
        RealityCubes.CHANNEL.registerMessage(index, UseRealityCubePacket.class, ENCODER, DECODER, CONSUMER);
    }

    private void handle(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayer player = context.getSender();
            context.enqueueWork(() -> {
                ServerLevel original = player.getLevel();
                UUID id = RealityCubeHelper.getRealityCube(original);
                if (player.getUUID().equals(id)) {
                    RealityCubeHelper.kickAll(original);
                } else if (id == null) {
                    if (calculateTotalExperience(player) >= RealityCubeSettings.INITIAL_EXP) {
                        RealityCubeHelper.decreaseExperiencePoints(player, RealityCubeSettings.INITIAL_EXP);
                        RealityCubeSettings data = RealityCubeSettings.get(player);
                        Vec3 pos = player.position();
                        ServerLevel cube = RealityCubeHelper.createRealityCube(player.getServer(), player.getUUID(), data, pos, original.dimension());
                        RealityCubeHelper.sendToRealityCube(cube, player, data, pos);
                    } else {
                        player.sendMessage(EXP, Util.NIL_UUID);
                    }
                } else {
                    player.sendMessage(WITHIN, Util.NIL_UUID);
                }
            });
            context.setPacketHandled(true);
        }
    }

    private int calculateTotalExperience(ServerPlayer player) {
        int total;
        if (player.experienceLevel <= 15) {
            total = player.experienceLevel * player.experienceLevel + player.experienceLevel * 6;
        } else if (player.experienceLevel <= 30) {
            total = (int) (player.experienceLevel * player.experienceLevel * 2.5 - 40.5 * player.experienceLevel) + 360;
        } else {
            total = (int) (4.5 * player.experienceLevel * player.experienceLevel - 162.5 * player.experienceLevel) + 2220;
        }
        return total + player.totalExperience;
    }
}
