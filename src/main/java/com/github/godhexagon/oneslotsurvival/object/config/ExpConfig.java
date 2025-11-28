package com.github.godhexagon.oneslotsurvival.object.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 経験値システムの設定を管理するクラス。
 *
 * <p>このクラスはForgeのSERVER configを使用しており、以下の特性を持ちます：</p>
 * <ul>
 *   <li>サーバー側で管理され、クライアントに自動同期される</li>
 *   <li>ワールドごとに設定が保存される（.minecraft/saves/ワールド名/serverconfig/）</li>
 *   <li>/reload コマンドでリロード可能</li>
 * </ul>
 *
 * <h2>設定値のアクセス方法</h2>
 * <pre>{@code
 * double levelUpExp = ExpConfig.levelUpExp;
 * double walkExpRate = ExpConfig.Stats.walkOneCm;
 * }</pre>
 */
public class ExpConfig {
    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<ServerConfig, ForgeConfigSpec> specPair =
            new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    // キャッシュされた値（高速アクセス用）
    public static double levelUpExp;
    public static double levelUpIncreaseMultiplier;

    /**
     * 統計情報ベースの経験値倍率
     */
    public static class Stats {
        public static double walkOneCm;
        public static double sprintOneCm;
        public static double crouchOneCm;
        public static double flyOneCm;
        public static double damageDealt;
        public static double damageTaken;
        public static double damageBlockedByShield;
    }

    /**
     * アイテム耐久値ベースの経験値倍率
     */
    public static class ItemDurability {
        public static double swords;
        public static double pickaxes;
        public static double axes;
        public static double shovels;
        public static double hoes;
        public static double armor;
        public static double shields;
        public static double other;
    }

    /**
     * その他の経験値倍率
     */
    public static class Other {
        public static double vanillaExpPoints;
    }

    /**
     * 設定が読み込まれた、またはリロードされたときに呼ばれるイベントハンドラ。
     * 設定値をキャッシュして、高速アクセスを可能にします。
     */
    public static void onModConfigEvent(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            bakeConfig();
        }
    }

    /**
     * 設定値をキャッシュします。
     */
    private static void bakeConfig() {
        levelUpExp = SERVER.levelUpExp.get();
        levelUpIncreaseMultiplier = SERVER.levelUpIncreaseMultiplier.get();

        Stats.walkOneCm = SERVER.stats.walkOneCm.get();
        Stats.sprintOneCm = SERVER.stats.sprintOneCm.get();
        Stats.crouchOneCm = SERVER.stats.crouchOneCm.get();
        Stats.flyOneCm = SERVER.stats.flyOneCm.get();
        Stats.damageDealt = SERVER.stats.damageDealt.get();
        Stats.damageTaken = SERVER.stats.damageTaken.get();
        Stats.damageBlockedByShield = SERVER.stats.damageBlockedByShield.get();

        ItemDurability.swords = SERVER.itemDurability.swords.get();
        ItemDurability.pickaxes = SERVER.itemDurability.pickaxes.get();
        ItemDurability.axes = SERVER.itemDurability.axes.get();
        ItemDurability.shovels = SERVER.itemDurability.shovels.get();
        ItemDurability.hoes = SERVER.itemDurability.hoes.get();
        ItemDurability.armor = SERVER.itemDurability.armor.get();
        ItemDurability.shields = SERVER.itemDurability.shields.get();
        ItemDurability.other = SERVER.itemDurability.other.get();

        Other.vanillaExpPoints = SERVER.other.vanillaExpPoints.get();
    }

    /**
     * サーバー設定の定義。
     */
    public static class ServerConfig {
        public final ForgeConfigSpec.DoubleValue levelUpExp;
        public final ForgeConfigSpec.DoubleValue levelUpIncreaseMultiplier;
        public final StatsConfig stats;
        public final ItemDurabilityConfig itemDurability;
        public final OtherConfig other;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Experience system configuration for One Slot Survival")
                .push("experience");

            levelUpExp = builder
                .comment("Experience points required to level up")
                .defineInRange("level_up_exp", 120.0, 1.0, 100000.0);

            levelUpIncreaseMultiplier = builder
                .comment("Multiplier for experience required per level (e.g., 1.1 = 10% increase per level)")
                .defineInRange("level_up_increase_multiplier", 1.0, 1.0, 2.0);

            stats = new StatsConfig(builder);
            itemDurability = new ItemDurabilityConfig(builder);
            other = new OtherConfig(builder);

            builder.pop();
        }
    }

    /**
     * 統計情報ベースの経験値設定。
     */
    public static class StatsConfig {
        public final ForgeConfigSpec.DoubleValue walkOneCm;
        public final ForgeConfigSpec.DoubleValue sprintOneCm;
        public final ForgeConfigSpec.DoubleValue crouchOneCm;
        public final ForgeConfigSpec.DoubleValue flyOneCm;
        public final ForgeConfigSpec.DoubleValue damageDealt;
        public final ForgeConfigSpec.DoubleValue damageTaken;
        public final ForgeConfigSpec.DoubleValue damageBlockedByShield;

        public StatsConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Experience gain rates for player statistics (per 100cm for movement, per 10 damage for damage stats)")
                .push("stats");

            walkOneCm = builder
                .comment("Experience rate per 100cm walked")
                .defineInRange("walk_one_cm", 0.0001, 0.0, 1.0);

            sprintOneCm = builder
                .comment("Experience rate per 100cm sprinted")
                .defineInRange("sprint_one_cm", 0.0001, 0.0, 1.0);

            crouchOneCm = builder
                .comment("Experience rate per 100cm crouched")
                .defineInRange("crouch_one_cm", 0.0002, 0.0, 1.0);

            flyOneCm = builder
                .comment("Experience rate per 100cm flown")
                .defineInRange("fly_one_cm", 0.0001, 0.0, 1.0);

            damageDealt = builder
                .comment("Experience rate per 10 damage dealt (e.g., 5.0 damage = 50 units)")
                .defineInRange("damage_dealt", 0.002, 0.0, 10.0);

            damageTaken = builder
                .comment("Experience rate per 10 damage taken (e.g., 5.0 damage = 50 units)")
                .defineInRange("damage_taken", 0.02, 0.0, 10.0);

            damageBlockedByShield = builder
                .comment("Experience rate per 10 damage blocked by shield (e.g., 5.0 damage = 50 units)")
                .defineInRange("damage_blocked_by_shield", 0.05, 0.0, 10.0);

            builder.pop();
        }
    }

    /**
     * アイテム耐久値ベースの経験値設定。
     */
    public static class ItemDurabilityConfig {
        public final ForgeConfigSpec.DoubleValue swords;
        public final ForgeConfigSpec.DoubleValue pickaxes;
        public final ForgeConfigSpec.DoubleValue axes;
        public final ForgeConfigSpec.DoubleValue shovels;
        public final ForgeConfigSpec.DoubleValue hoes;
        public final ForgeConfigSpec.DoubleValue armor;
        public final ForgeConfigSpec.DoubleValue shields;
        public final ForgeConfigSpec.DoubleValue other;

        public ItemDurabilityConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Experience gain rates for item durability loss")
                .push("item_durability");

            swords = builder
                .comment("Experience rate per durability point lost on swords")
                .defineInRange("swords", 0.5, 0.0, 10.0);

            pickaxes = builder
                .comment("Experience rate per durability point lost on pickaxes")
                .defineInRange("pickaxes", 0.5, 0.0, 10.0);

            axes = builder
                .comment("Experience rate per durability point lost on axes")
                .defineInRange("axes", 0.5, 0.0, 10.0);

            shovels = builder
                .comment("Experience rate per durability point lost on shovels")
                .defineInRange("shovels", 0.1, 0.0, 10.0);

            hoes = builder
                .comment("Experience rate per durability point lost on hoes")
                .defineInRange("hoes", 0.1, 0.0, 10.0);

            armor = builder
                .comment("Experience rate per durability point lost on armor pieces")
                .defineInRange("armor", 0.2, 0.0, 10.0);

            shields = builder
                .comment("Experience rate per durability point lost on shields")
                .defineInRange("shields", 0.0, 0.0, 10.0);

            other = builder
                .comment("Experience rate per durability point lost on other damageable items")
                .defineInRange("other", 0.1, 0.0, 10.0);

            builder.pop();
        }
    }

    /**
     * その他の経験値設定。
     */
    public static class OtherConfig {
        public final ForgeConfigSpec.DoubleValue vanillaExpPoints;

        public OtherConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Other experience settings")
                .push("other");

            vanillaExpPoints = builder
                .comment("Experience rate per vanilla experience point gained")
                .defineInRange("vanilla_exp_points", 1.0, 0.0, 100.0);

            builder.pop();
        }
    }
}
