package com.github.godhexagon.oneslotsurvival.rule.level;

import com.github.godhexagon.oneslotsurvival.rule.attribute.LevelAttribute;
import com.github.godhexagon.oneslotsurvival.rule.attribute.MainRoleStartedLevel;
import com.github.godhexagon.oneslotsurvival.rule.attribute.SubRoleStartedLevel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class RoleLeveledUpTimes {
    // ===== メインロール関連 =====

    /**
     * メインロールになってからレベルアップを経験した回数を取得する。
     *
     * @param player プレイヤー。
     * @return 経験回数。
     */
    public static int getMain(Player player) {
        return LevelAttribute.getLevel(player) - MainRoleStartedLevel.get(player);
    }

    /**
     * メインロールになってからレベルアップを経験した回数を強制的に設定する。
     *
     * @param player プレイヤー。
     * @param times 経験回数。
     */
    public static void setMain(ServerPlayer player, int times) {
        MainRoleStartedLevel.set(player, LevelAttribute.getLevel(player) - Math.max(0, times));
    }

    /**
     * 現在のメインロールになってから一度もレベルアップしたことがないということにする
     *
     * @param player プレイヤー。
     */
    public static void resetMain(ServerPlayer player) {
        MainRoleStartedLevel.set(player, LevelAttribute.getLevel(player));
    }

    // ===== サブロール関連 =====

    /**
     * サブロールになってからレベルアップを経験した回数を取得する。
     *
     * @param player プレイヤー。
     * @return 経験回数。
     */
    public static int getSub(Player player) {
        return LevelAttribute.getLevel(player) - SubRoleStartedLevel.get(player);
    }

    /**
     * サブロールになってからレベルアップを経験した回数を強制的に設定する。
     *
     * @param player プレイヤー。
     * @param times 経験回数。
     */
    public static void setSub(ServerPlayer player, int times) {
        SubRoleStartedLevel.set(player, LevelAttribute.getLevel(player) - Math.max(0, times));
    }

    /**
     * 現在のサブロールになってから一度もレベルアップしたことがないということにする
     *
     * @param player プレイヤー。
     */
    public static void resetSub(ServerPlayer player) {
        SubRoleStartedLevel.set(player, LevelAttribute.getLevel(player));
    }
}
