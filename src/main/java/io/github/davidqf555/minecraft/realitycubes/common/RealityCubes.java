package io.github.davidqf555.minecraft.realitycubes.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

@Mod("realitycubes")
public class RealityCubes {

    public static final String MOD_ID = "realitycubes";
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, MOD_ID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public RealityCubes() {
        MixinBootstrap.init();
        Mixins.addConfiguration(MOD_ID + ".mixins.json");
        MinecraftForge.EVENT_BUS.register(this);
    }
}
