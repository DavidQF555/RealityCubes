package io.github.davidqf555.minecraft.realitycubes.common.packets;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateClientLevelsPacket {

    private static final BiConsumer<UpdateClientLevelsPacket, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeUtf(message.key.location().toString());
    };
    private static final Function<FriendlyByteBuf, UpdateClientLevelsPacket> DECODER = buffer -> new UpdateClientLevelsPacket(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buffer.readUtf())));
    private static final BiConsumer<UpdateClientLevelsPacket, Supplier<NetworkEvent.Context>> CONSUMER = (message, context) -> {
        NetworkEvent.Context cont = context.get();
        message.handle(cont);
    };

    private final ResourceKey<Level> key;

    public UpdateClientLevelsPacket(ResourceKey<Level> key) {
        this.key = key;
    }

    public static void register(int index) {
        RealityCubes.CHANNEL.registerMessage(index, UpdateClientLevelsPacket.class, ENCODER, DECODER, CONSUMER);
    }

    private void handle(NetworkEvent.Context context) {
        NetworkDirection dir = context.getDirection();
        if (dir == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft.getInstance().player.connection.levels().add(key);
            });
            context.setPacketHandled(true);
        }
    }

}
