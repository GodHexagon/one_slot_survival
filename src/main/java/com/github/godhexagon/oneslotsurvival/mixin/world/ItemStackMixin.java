package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.object.config.ExpConfig;
import com.github.godhexagon.oneslotsurvival.rule.player.PlayerModValidity;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerProgress;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * ItemStackクラスにMixinして、アイテムの耐久値減少時に経験値を付与します。
 *
 * <h2>フックポイントについて</h2>
 * {@link ItemStack#hurtAndBreak(int, ServerLevel, ServerPlayer, Consumer)}は、
 * アイテムの耐久値が減少する際に呼び出されるメインメソッドです。
 * このメソッドは以下の場面で呼ばれます:
 * <ul>
 *   <li>ツールでブロックを破壊したとき</li>
 *   <li>武器で攻撃したとき</li>
 *   <li>防具がダメージを吸収したとき</li>
 *   <li>その他アイテムが使用されたとき</li>
 * </ul>
 *
 * <h2>経験値付与のタイミング</h2>
 * このMixinは耐久値が実際に減少する前（HEADポイント）でインターセプトし、
 * 減少する耐久値の量に応じて経験値を付与します。
 *
 * <h2>経験値の計算式</h2>
 * 耐久値1減少 = 0.1経験値
 *
 * @see ItemStack#hurtAndBreak(int, ServerLevel, ServerPlayer, Consumer)
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public boolean is(TagKey<Item> p_204118_) {
        throw new AssertionError();
    }

    /**
     * アイテムの耐久値が減少する際に経験値を付与します。
     *
     * @param damage 減少する耐久値の量
     * @param level サーバーワールド
     * @param player アイテムを使用しているプレイヤー（nullable）
     * @param onBreak アイテムが壊れたときのコールバック
     * @param ci コールバック情報
     */
    @Inject(
        method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
        at = @At("HEAD")
    )
    private void onHurtAndBreak(
            int damage,
            ServerLevel level,
            @Nullable ServerPlayer player,
            Consumer<Item> onBreak,
            CallbackInfo ci
    ) {
        // プレイヤーがnullの場合は処理しない
        if (player == null) {
            return;
        }

        // MODが有効なプレイヤーのみ処理
        if (!PlayerModValidity.isEffective(player)) {
            return;
        }

        // ダメージ量が0以下の場合は処理しない
        if (damage <= 0) {
            return;
        }

        // アイテムタイプごとに経験値倍率を適用
        double multiplier = 0.0;

        if (this.is(ItemTags.SWORDS)) {
            multiplier = ExpConfig.ItemDurability.swords;
        } else if (this.is(ItemTags.PICKAXES)) {
            multiplier = ExpConfig.ItemDurability.pickaxes;
        } else if (this.is(ItemTags.AXES)) {
            multiplier = ExpConfig.ItemDurability.axes;
        } else if (this.is(ItemTags.SHOVELS)) {
            multiplier = ExpConfig.ItemDurability.shovels;
        } else if (this.is(ItemTags.HOES)) {
            multiplier = ExpConfig.ItemDurability.hoes;
        } else if (this.is(ItemTags.FOOT_ARMOR) || this.is(ItemTags.LEG_ARMOR) ||
                   this.is(ItemTags.CHEST_ARMOR) || this.is(ItemTags.HEAD_ARMOR)) {
            multiplier = ExpConfig.ItemDurability.armor;
        } else {
            // その他の耐久値を持つアイテム（盾、釣り竿、ハサミなど）
            multiplier = ExpConfig.ItemDurability.other;
        }

        // 経験値を付与（倍率が0より大きい場合のみ）
        if (multiplier > 0.0) {
            double expToAdd = damage * multiplier;
            PlayerProgress.recieveExp(player, expToAdd);
        }
    }
}
