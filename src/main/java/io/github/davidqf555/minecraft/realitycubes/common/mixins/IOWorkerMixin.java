package io.github.davidqf555.minecraft.realitycubes.common.mixins;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.world.EmptyRegionStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(IOWorker.class)
public abstract class IOWorkerMixin {

    @Mutable
    @Accessor(value = "storage", remap = false)
    public abstract void setStorage(RegionFileStorage cache);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruction(File file, boolean sync, String thread, CallbackInfo info) {
        if (file.getPath().contains(RealityCubes.MOD_ID)) {
            setStorage(new EmptyRegionStorage(file, sync));
        }
    }
}
