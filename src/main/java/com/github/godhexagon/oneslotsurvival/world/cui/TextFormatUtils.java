package com.github.godhexagon.oneslotsurvival.world.cui;

/**
 * テキストフォーマットのためのユーティリティクラス
 * <p>
 * UI層で使用される文字列フォーマット処理を提供。
 * </p>
 */
public class TextFormatUtils {

    /**
     * 数字を序数詞に変換（1→1st, 2→2nd, 3→3rd）
     *
     * @param number 数値
     * @return 序数詞形式の文字列
     */
    public static String getOrdinal(int number) {
        return switch (number) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> number + "th";
        };
    }
}
