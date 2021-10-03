package io.github.davidqf555.minecraft.realitycubes.client;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

import java.awt.event.KeyEvent;

public class KeyMappingsList {

    public static final KeyMapping REALITY_CUBE = new KeyMapping("key." + RealityCubes.MOD_ID + ".reality_cube", KeyEvent.VK_G, "category." + RealityCubes.MOD_ID);

    public static void register() {
        ClientRegistry.registerKeyBinding(REALITY_CUBE);
    }
}
