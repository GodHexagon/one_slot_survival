package com.github.godhexagon.oneslotsurvival.world.util.level;

import com.github.godhexagon.oneslotsurvival.world.util.attribute.MainRoleLevel;
import com.github.godhexagon.oneslotsurvival.world.util.attribute.MainRoleRemainingExp;
import com.github.godhexagon.oneslotsurvival.world.util.inventory.SlotRestriction;
import com.github.godhexagon.oneslotsurvival.world.util.role.MainRole;
import com.github.godhexagon.oneslotsurvival.world.util.role.RoleManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * 永続化レイヤーを隠し、経験値のルールを提供する。
 *
 * <p>このクラスは、プレイヤーのメインロール経験値を操作するための静的メソッドを提供します。
 * 経験値は「残り経験値」として管理されており、経験値を獲得すると残り経験値が減少します。</p>
 *
 * <h2>経験値システムの仕様</h2>
 * <ul>
 *   <li>経験値獲得 = 残り経験値を減算</li>
 *   <li>残り経験値が0以下になると、レベルアップが発生する</li>
 *   <li>経験値の単位は実数（double）で管理</li>
 * </ul>
 */
public class Exp {
    /**
     * プレイヤーの経験値の蓄積を返す。
     * 
     * @param player プレイヤー。
     * @return 次のレベルアップまでの残り経験値。
     */
    public static double getMain(Player player) {
        // まだレベルシステムに参加していないプレイヤーは、最初の経験値を設定
        if (MainRoleLevel.getLevel(player) <= Level.UNDEFINED_LEVEL) {
            return Level.LEVEL_UP_EXP;
        }

        return MainRoleRemainingExp.getRemainingExp(player);
    }

    /**
     * メインロールの経験値を加算します（残り経験値を減算します）
     *
     * <p>このメソッドは、プレイヤーが何らかのアクションを行った際に経験値を獲得する際に使用されます。
     * 経験値は「残り経験値」として管理されているため、経験値を獲得すると残り経験値が減少します。</p>
     *
     * <p><b>使用例:</b></p>
     * <pre>{@code
     * // プレイヤーが100ブロック歩いたときに5.0経験値を付与
     * Exp.addMain(player, 5.0);
     *
     * // ダメージを与えたときに、ダメージ量に応じた経験値を付与
     * Exp.addMain(player, damageAmount * 0.1);
     * }</pre>
     *
     * @param player 経験値を付与するプレイヤー
     * @param amount 付与する経験値量（正の数）
     */
    public static void addMain(ServerPlayer player, double amount) {
        // まだレベルシステムに参加していないプレイヤーは、最初の経験値を設定
        if (MainRoleLevel.getLevel(player) <= Level.UNDEFINED_LEVEL) {
            MainRoleLevel.setLevel(player, 1);
            MainRoleRemainingExp.setRemainingExp(player, Level.LEVEL_UP_EXP - amount);
            return;
        }

        double remaining = MainRoleRemainingExp.getRemainingExp(player);

        // レベルアップ処理
        if (remaining < 0) {
            int newLevel = MainRoleLevel.getLevel(player) + 1;
            MainRoleLevel.setLevel(player, newLevel);
            remaining += Level.LEVEL_UP_EXP;

            // WARN: この実装は臨時実装。真似してはいけない
            MainRole role = RoleManager.getRole(player);
            // WARN: SPECIALTY_ITEM_LINEUPにないロールはレベルアップシステムに参加しない。これらはそもそも経験値は増やさなくてよい。ただその実装は大変なので出力時にはじく。
            if (SlotRestriction.SPECIALTY_ITEM_LINEUP.containsKey(role)) {
                // レベルアップ祝福メッセージ
                Component congratsMessage = Component.literal("Congratulations! ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("You've reached Level " + newLevel + "!")
                                .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(congratsMessage);

                // 解放されたアイテムタグの通知
                int roleSlotIndex = newLevel - 2;
                if (roleSlotIndex >= 0 && roleSlotIndex < SlotRestriction.SPECIALTY_ITEM_LINEUP.get(role).size()) {
                    TagKey<Item> capableItemTag = SlotRestriction.SPECIALTY_ITEM_LINEUP.get(role).get(roleSlotIndex);

                    String tagName = capableItemTag.location().toString();
                    Component rewardMessage = Component.literal("Reward: ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal("New role slot unlocked! ")
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal("[" + tagName + "]")
                                    .withStyle(ChatFormatting.AQUA));
                    player.sendSystemMessage(rewardMessage);
                }
            }
        }

        MainRoleRemainingExp.setRemainingExp(player, remaining - amount);
    }

    /**
     * プレイヤーの経験値を初期化する
     * 
     * @param player プレイヤー。
     */
    public static void clear(ServerPlayer player) {
        MainRoleRemainingExp.setRemainingExp(player, Level.LEVEL_UP_EXP);
    }
}
