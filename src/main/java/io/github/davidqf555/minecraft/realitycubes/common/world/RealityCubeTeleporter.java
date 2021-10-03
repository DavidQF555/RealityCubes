package io.github.davidqf555.minecraft.realitycubes.common.world;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

public class RealityCubeTeleporter implements ITeleporter {

    private final double dX;
    private final double dZ;

    public RealityCubeTeleporter(double dX, double dZ) {
        this.dX = dX;
        this.dZ = dZ;
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        destWorld.getChunk(SectionPos.blockToSectionCoord((int) dX), SectionPos.blockToSectionCoord((int) dZ));
        Vec3 top = new Vec3(dX, destWorld.getHeight(Heightmap.Types.WORLD_SURFACE, (int) dX, (int) dZ), dZ);
        return new PortalInfo(top, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
    }
}
