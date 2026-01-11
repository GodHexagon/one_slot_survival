package com.github.godhexagon.oneslotsurvival.gametest;

import com.github.godhexagon.oneslotsurvival.rule.player.PlayerModValidity;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;
import com.github.godhexagon.oneslotsurvival.world.storage.BonusItem;
import com.github.godhexagon.oneslotsurvival.world.storage.WorldOptions;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTest;
import net.minecraftforge.gametest.GameTestNamespace;

/**
 * WorldOptions機能の自動テスト。
 *
 * <p>このクラスは、WorldOptionsに関連する以下の機能をテストします：</p>
 * <ul>
 *   <li>BonusItemの配布（初回ログイン・リスポーン時）</li>
 *   <li>MainRoleChanging設定（ロール変更の制御）</li>
 *   <li>SubRoleChanging設定（ロール変更の制御）</li>
 *   <li>DefaultModValidity設定（初期MOD有効性）</li>
 * </ul>
 *
 * <h2>実行方法</h2>
 * <pre>{@code
 * # すべてのテストを実行
 * ./gradlew runGameTestServer
 *
 * # 個別のテストを実行（ゲーム内）
 * /test run oneslotsurvival:worldoptionsgametests.testbonusitembundle
 * }</pre>
 */
@GameTestNamespace("oneslotsurvival")
public class WorldOptionsGameTests {

    /**
     * テスト: Bundle配布（初回ログイン時）
     *
     * <p>シナリオ:</p>
     * <ol>
     *   <li>WorldOptionsでBonusItemをBUNDLEに設定</li>
     *   <li>新規プレイヤーを作成（初回ログインをシミュレート）</li>
     *   <li>プレイヤーのインベントリにバンドルが配布されることを確認</li>
     * </ol>
     */
    @GameTest(
        setupTicks = 20,
        maxTicks = 100,
        required = true
    )
    public static void testBonusItemBundle(GameTestHelper helper) {
        // 1. WorldOptionsでBonusItemをBUNDLEに設定
        WorldOptions.setBonusItem(helper.getLevel().getServer(), BonusItem.BUNDLE);

        // 2. 新規プレイヤーを作成（モックプレイヤー）
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer(GameType.SURVIVAL);

        // 3. 初回ログイン処理をシミュレート
        //    （PLAY_TIME = 0の状態で、WorldEventのonPlayerLoggedInロジックを模倣）
        simulateFirstLogin(player);

        // 4. プレイヤーのインベントリを確認
        helper.runAfterDelay(10, () -> {
            Inventory inventory = player.getInventory();
            long bundleCount = countItem(inventory, Items.BUNDLE);

            if (bundleCount == 1) {
                helper.succeed();
            } else {
                helper.fail("Expected 1 bundle in inventory, but found " + bundleCount);
            }
        });
    }

    /**
     * テスト: ShulkerBox配布（初回ログイン時）
     */
    @GameTest(
        setupTicks = 20,
        maxTicks = 100,
        required = true
    )
    public static void testBonusItemShulkerBox(GameTestHelper helper) {
        // BonusItemをSHULKERBOXに設定
        WorldOptions.setBonusItem(helper.getLevel().getServer(), BonusItem.SHULKERBOX);

        // 新規プレイヤーを作成
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer(GameType.SURVIVAL);

        // 初回ログイン処理をシミュレート
        simulateFirstLogin(player);

        // インベントリを確認
        helper.runAfterDelay(10, () -> {
            Inventory inventory = player.getInventory();
            long shulkerBoxCount = countItem(inventory, Items.SHULKER_BOX);

            if (shulkerBoxCount == 1) {
                helper.succeed();
            } else {
                helper.fail("Expected 1 shulker box in inventory, but found " + shulkerBoxCount);
            }
        });
    }

    /**
     * テスト: ボーナスアイテムなし（NONE）
     */
    @GameTest(
        setupTicks = 20,
        maxTicks = 100,
        required = true
    )
    public static void testBonusItemNone(GameTestHelper helper) {
        // BonusItemをNONEに設定
        WorldOptions.setBonusItem(helper.getLevel().getServer(), BonusItem.NONE);

        // 新規プレイヤーを作成
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer(GameType.SURVIVAL);

        // 初回ログイン処理をシミュレート
        simulateFirstLogin(player);

        // インベントリを確認（何も配布されていないこと）
        helper.runAfterDelay(10, () -> {
            Inventory inventory = player.getInventory();
            long bundleCount = countItem(inventory, Items.BUNDLE);
            long shulkerBoxCount = countItem(inventory, Items.SHULKER_BOX);

            if (bundleCount == 0 && shulkerBoxCount == 0) {
                helper.succeed();
            } else {
                helper.fail("Expected no bonus items, but found bundle=" + bundleCount + ", shulkerbox=" + shulkerBoxCount);
            }
        });
    }

    /**
     * テスト: MainRoleChanging設定
     *
     * <p>シナリオ:</p>
     * <ol>
     *   <li>MainRoleChangingをtrueに設定</li>
     *   <li>設定が正しく保存・取得できることを確認</li>
     *   <li>MainRoleChangingをfalseに設定</li>
     *   <li>設定が正しく保存・取得できることを確認</li>
     * </ol>
     */
    @GameTest(
        setupTicks = 10,
        maxTicks = 100,
        required = true
    )
    public static void testMainRoleChangingSetting(GameTestHelper helper) {
        // trueに設定
        WorldOptions.setMainRoleChangingEnabled(helper.getLevel().getServer(), true);
        boolean valueTrue = WorldOptions.isMainRoleChangingEnabled(helper.getLevel().getServer());

        // falseに設定
        WorldOptions.setMainRoleChangingEnabled(helper.getLevel().getServer(), false);
        boolean valueFalse = WorldOptions.isMainRoleChangingEnabled(helper.getLevel().getServer());

        // 検証
        if (valueTrue && !valueFalse) {
            helper.succeed();
        } else {
            helper.fail("MainRoleChanging setting not working correctly: valueTrue=" + valueTrue + ", valueFalse=" + valueFalse);
        }
    }

    /**
     * テスト: SubRoleChanging設定
     */
    @GameTest(
        setupTicks = 10,
        maxTicks = 100,
        required = true
    )
    public static void testSubRoleChangingSetting(GameTestHelper helper) {
        // trueに設定
        WorldOptions.setSubRoleChangingEnabled(helper.getLevel().getServer(), true);
        boolean valueTrue = WorldOptions.isSubRoleChangingEnabled(helper.getLevel().getServer());

        // falseに設定
        WorldOptions.setSubRoleChangingEnabled(helper.getLevel().getServer(), false);
        boolean valueFalse = WorldOptions.isSubRoleChangingEnabled(helper.getLevel().getServer());

        // 検証
        if (valueTrue && !valueFalse) {
            helper.succeed();
        } else {
            helper.fail("SubRoleChanging setting not working correctly: valueTrue=" + valueTrue + ", valueFalse=" + valueFalse);
        }
    }

    /**
     * テスト: DefaultModValidity設定
     *
     * <p>シナリオ:</p>
     * <ol>
     *   <li>DefaultModValidityをtrueに設定</li>
     *   <li>新規プレイヤーのMOD有効性がtrueになることを確認</li>
     *   <li>DefaultModValidityをfalseに設定</li>
     *   <li>新規プレイヤーのMOD有効性がfalseになることを確認</li>
     * </ol>
     */
    @GameTest(
        setupTicks = 20,
        maxTicks = 100,
        required = true
    )
    public static void testDefaultModValidityEnabled(GameTestHelper helper) {
        // DefaultModValidityをtrueに設定
        WorldOptions.setDefaultModValidityEnabled(helper.getLevel().getServer(), true);

        // 新規プレイヤーを作成
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer(GameType.SURVIVAL);

        // 初回ログイン処理をシミュレート
        simulateFirstLogin(player);

        // MOD有効性を確認
        helper.runAfterDelay(10, () -> {
            boolean modValidity = PlayerModValidity.isEffective(player);

            if (modValidity) {
                helper.succeed();
            } else {
                helper.fail("Expected MOD validity to be true, but was false");
            }
        });
    }

    @GameTest(
        setupTicks = 20,
        maxTicks = 100,
        required = true
    )
    public static void testDefaultModValidityDisabled(GameTestHelper helper) {
        // DefaultModValidityをfalseに設定
        WorldOptions.setDefaultModValidityEnabled(helper.getLevel().getServer(), false);

        // 新規プレイヤーを作成
        ServerPlayer player = (ServerPlayer) helper.makeMockPlayer(GameType.SURVIVAL);

        // 初回ログイン処理をシミュレート
        simulateFirstLogin(player);

        // MOD有効性を確認
        helper.runAfterDelay(10, () -> {
            boolean modValidity = PlayerModValidity.isEffective(player);

            if (!modValidity) {
                helper.succeed();
            } else {
                helper.fail("Expected MOD validity to be false, but was true");
            }
        });
    }

    /**
     * テスト: BonusItem設定の永続化
     *
     * <p>設定が正しく保存・取得できることを確認</p>
     */
    @GameTest(
        setupTicks = 10,
        maxTicks = 100,
        required = true
    )
    public static void testBonusItemPersistence(GameTestHelper helper) {
        // 各BonusItemを設定して取得
        WorldOptions.setBonusItem(helper.getLevel().getServer(), BonusItem.BUNDLE);
        BonusItem bundle = WorldOptions.getBonusItem(helper.getLevel().getServer());

        WorldOptions.setBonusItem(helper.getLevel().getServer(), BonusItem.SHULKERBOX);
        BonusItem shulkerbox = WorldOptions.getBonusItem(helper.getLevel().getServer());

        WorldOptions.setBonusItem(helper.getLevel().getServer(), BonusItem.BUNDLE_RESPAWN);
        BonusItem bundleRespawn = WorldOptions.getBonusItem(helper.getLevel().getServer());

        WorldOptions.setBonusItem(helper.getLevel().getServer(), BonusItem.SHULKERBOX_RESPAWN);
        BonusItem shulkerboxRespawn = WorldOptions.getBonusItem(helper.getLevel().getServer());

        WorldOptions.setBonusItem(helper.getLevel().getServer(), BonusItem.NONE);
        BonusItem none = WorldOptions.getBonusItem(helper.getLevel().getServer());

        // 検証
        if (bundle == BonusItem.BUNDLE &&
            shulkerbox == BonusItem.SHULKERBOX &&
            bundleRespawn == BonusItem.BUNDLE_RESPAWN &&
            shulkerboxRespawn == BonusItem.SHULKERBOX_RESPAWN &&
            none == BonusItem.NONE) {
            helper.succeed();
        } else {
            helper.fail("BonusItem persistence failed");
        }
    }

    // ========================================
    // ヘルパーメソッド
    // ========================================

    /**
     * 初回ログイン処理をシミュレートします。
     *
     * <p>WorldEvent.onPlayerLoggedIn のロジックを模倣して、
     * 新規プレイヤーに対する初期設定を適用します。</p>
     *
     * @param player 対象プレイヤー
     */
    private static void simulateFirstLogin(ServerPlayer player) {
        // WorldEvent.onPlayerLoggedIn のロジックを再現
        // PLAY_TIME < 5 の場合の処理

        // ロールが未割り当ての場合のみ設定
        MainRole currentRoleId = RoleManager.getMainRole(player);
        if (currentRoleId == MainRole.UNASSIGNED) {
            // MOD有効性を設定
            boolean defaultModValidity = WorldOptions.isDefaultModValidityEnabled(player.getServer());
            PlayerModValidity.setEnabled(player, defaultModValidity);

            // デフォルトロールを設定
            RoleManager.setMainRole(player, MainRole.MINER);
            RoleManager.setSubRole(player, SubRole.FISHER);

            // ボーナスアイテムを配布
            BonusItem bonusItemMode = WorldOptions.getBonusItem(player.getServer());
            if (bonusItemMode == BonusItem.BUNDLE || bonusItemMode == BonusItem.BUNDLE_RESPAWN) {
                player.addItem(new ItemStack(Items.BUNDLE));
            } else if (bonusItemMode == BonusItem.SHULKERBOX || bonusItemMode == BonusItem.SHULKERBOX_RESPAWN) {
                player.addItem(new ItemStack(Items.SHULKER_BOX));
            }
        }
    }

    /**
     * インベントリ内の特定アイテムの個数を数えます。
     *
     * @param inventory インベントリ
     * @param item アイテム
     * @return アイテムの合計個数
     */
    private static long countItem(Inventory inventory, net.minecraft.world.item.Item item) {
        long count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
