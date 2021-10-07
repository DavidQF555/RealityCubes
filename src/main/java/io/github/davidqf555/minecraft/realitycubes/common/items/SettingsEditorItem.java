package io.github.davidqf555.minecraft.realitycubes.common.items;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.capabilities.RealityCubeSettings;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class SettingsEditorItem extends Item implements Consumer<RealityCubeSettings> {

    private final Consumer<RealityCubeSettings> effect;

    public SettingsEditorItem(Consumer<RealityCubeSettings> effect) {
        super(new Properties().tab(RealityCubes.TAB).stacksTo(1));
        this.effect = effect;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (!item.isEmpty()) {
            if (!player.isCreative()) {
                item.setCount(item.getCount() - 1);
            }
            if (player instanceof ServerPlayer) {
                accept(RealityCubeSettings.get(player));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 1));
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, item);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
            return InteractionResultHolder.sidedSuccess(item, world.isClientSide());
        }
        return super.use(world, player, hand);
    }

    @Override
    public void accept(RealityCubeSettings settings) {
        effect.accept(settings);
    }
}
