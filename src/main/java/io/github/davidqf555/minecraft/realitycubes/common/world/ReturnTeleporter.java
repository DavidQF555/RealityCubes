package io.github.davidqf555.minecraft.realitycubes.common.world;

import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.ReturnData;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

public class ReturnTeleporter implements ITeleporter {

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        ReturnData data = ReturnData.get(entity);
        Vec3 pos = data.getReturnLocation();
        if (pos == null) {
            RealityCubeWorldData world = RealityCubeWorldData.get((ServerLevel) entity.level);
            Vec3 center = world.getOriginPos();
            RealityCubeSettings settings = world.getSettings();
            int range = settings.getRange();
            int radius = SectionPos.sectionToBlockCoord(settings.getChunkRadius());
            pos = new Vec3(center.x() + (center.x() - entity.getX()) * range / radius, center.y() + (center.y() - entity.getY()) * range / radius, center.z() + (center.z() - entity.getZ()) * range / radius);
        }
        return new PortalInfo(pos, entity.getDeltaMovement(), data.getYRotation(), data.getXRotation());
    }
}
