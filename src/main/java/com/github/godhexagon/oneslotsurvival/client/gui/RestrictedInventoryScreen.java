package com.github.godhexagon.oneslotsurvival.client.gui;

import com.github.godhexagon.oneslotsurvival.rule.inventory.InventoryDefinition;
import com.github.godhexagon.oneslotsurvival.rule.inventory.SlotRestriction;
import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 制限付きインベントリ画面
 *
 * <p>このクラスは {@link net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen}
 * の設計パターンをコピーして実装されています。</p>
 *
 * <h2>CreativeModeInventoryScreen との類似点</h2>
 * <ul>
 *   <li>元のスロットを {@code originalSlots} にバックアップ</li>
 *   <li>{@code menu.slots} をクリアして再構築</li>
 *   <li>SlotWrapper でラップして表示位置と動作を変更</li>
 *   <li>画面を閉じるときに元のスロット配置に復元</li>
 * </ul>
 *
 * <h2>現在の実装機能</h2>
 * <p><strong>制限対象（画面外に配置して無効化）：</strong></p>
 * <ul>
 *   <li>インベントリ 27スロット（index 9-35）</li>
 *   <li>ホットバー 8スロット（index 37-44、メインハンド36以外）</li>
 * </ul>
 *
 * <p><strong>表示対象（通常通り動作）：</strong></p>
 * <ul>
 *   <li>メインハンドスロット（index 36）</li>
 *   <li>クラフトスロット（2x2）</li>
 *   <li>防具スロット（ヘルメット、チェストプレート、レギンス、ブーツ）</li>
 *   <li>オフハンドスロット</li>
 * </ul>
 *
 * <p><strong>実装方法：</strong></p>
 * <ul>
 *   <li>制限スロットを画面外（-2000, -2000）に配置</li>
 *   <li>{@link RestrictedSlotWrapper} でラップして操作を完全に防ぐ</li>
 * </ul>
 *
 * <h2>今後追加できる機能（CreativeModeInventoryScreen を参考）</h2>
 * <ul>
 *   <li>カスタムスロット配置（メインハンドを中央下部に配置など）</li>
 *   <li>カスタム背景画像の描画</li>
 *   <li>独自のボタン・ウィジェット追加</li>
 *   <li>タブシステム（複数の画面モード）</li>
 *   <li>検索機能</li>
 *   <li>スクロール機能</li>
 *   <li>ロールスロットなどの専用スロット追加</li>
 *   <li>他のコンテナ（チェスト、クラフトテーブルなど）への同様の制限適用</li>
 * </ul>
 *
 * @see net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 * @see RestrictedSlotWrapper
 */
@OnlyIn(Dist.CLIENT)
public class RestrictedInventoryScreen extends InventoryScreen {
    // 元のスロットのバックアップ（画面を閉じるときに復元用、将来的に必要になる可能性がある）
    @Nullable
    private List<Slot> originalSlots = null;

    public RestrictedInventoryScreen(Player player) {
        super(player);
    }

    @Override
    protected void init() {
        super.init();

        var oss = this.originalSlots;

        // 元のスロットをバックアップ（一度だけ）
        if (oss == null) {
            oss = ImmutableList.copyOf(this.menu.slots);
        }

        // スロットリストをクリアして再構築
        this.menu.slots.clear();

        // 各スロットを再配置
        for (int i = 0; i < oss.size(); i++) {
            Slot originalSlot = oss.get(i);
            int x = originalSlot.x;
            int y = originalSlot.y;

            // 制限対象のスロットを判定
            if (originalSlot.container instanceof Inventory && isRestrictedSlot(originalSlot.getContainerSlot())) {
                // 画面外に配置
                x = -2000;
                y = -2000;
            }

            // RestrictedSlotWrapperでラップして追加
            // 重要: スロットのindexには i を渡す（menu.slots内の位置）
            RestrictedSlotWrapper wrapper = new RestrictedSlotWrapper(originalSlot, i, x, y);
            this.menu.slots.add(wrapper);
        }

        this.originalSlots = oss;
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.slots.clear();
        this.menu.slots.addAll(this.originalSlots);
        this.originalSlots = null;
    }

    /**
     * 制限対象のスロットかどうかを判定
     *
     * @param inventoryIndex インベントリインデックス
     * @return true の場合、制限対象
     */
    private boolean isRestrictedSlot(int inventoryIndex) {
        if (this.minecraft != null && this.minecraft.player != null) {
            boolean isNotUnlockedRoleSlot = !SlotRestriction.unlockedRoleSlot(inventoryIndex, this.minecraft.player);
            return (InventoryDefinition.isRoleSlot(inventoryIndex) && isNotUnlockedRoleSlot) || InventoryDefinition.isDisableSlot(inventoryIndex);
        } else {
            // フォールバックでは、ロールスロットは無条件表示。なぜなら、アイテム配置制限はInventoryMixinが行い整合性は保たれるし、
            // ロールスロットのアイテムが表示されても、これは通常空であり、カーソルするとインジケーターが出る問題しか起こらないから。
            return InventoryDefinition.isDisableSlot(inventoryIndex);
        }
    }

    private static final ResourceLocation CUSTOM_INVENTORY_LOCATION =
            ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "textures/gui/container/custom_inventory.png");
    private static final ResourceLocation ROLE_SLOT_BG =
            ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "textures/gui/container/role_slot_background.png");

    @SuppressWarnings("null") // INFO: this.minecraft.playerアクセスについて、これはバニラと同じ実装なのでミュート。他の部分は都度確認すること。
    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // カスタム背景を描画
        // 画像が存在しない場合はバニラの背景にフォールバック
        try {
            graphics.blit(RenderPipelines.GUI_TEXTURED, CUSTOM_INVENTORY_LOCATION,
                    x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
            
            
            // 解放されたロールスロットの背景を描画
            int inventoryIndex = InventoryDefinition.ROLE_SLOT_START_INVENTORY_INDEX;
            // ロールスロットがレベルによって解放されているか判定し、解放されたスロットが終了するまで繰り返す。
            while (SlotRestriction.unlockedRoleSlot(inventoryIndex, this.minecraft.player)) {
                // ロールスロット背景を描画（18×18、間隔0px）
                // 起点: (25, 141)から縦に並べる
                int bgX = x + 25 + (InventoryDefinition.getRoleSlotIndex(inventoryIndex) * 18);
                int bgY = y + 141;
                graphics.blit(RenderPipelines.GUI_TEXTURED, ROLE_SLOT_BG,
                        bgX, bgY, 0.0F, 0.0F, 18, 18, 18, 18);
                inventoryIndex++;
            }
        } catch (Exception e) {
            // フォールバック: バニラの背景を使用
            super.renderBg(graphics, partialTick, mouseX, mouseY);
            return;
        }

        // プレイヤーのレンダリング（バニラと同じ位置）
        renderEntityInInventoryFollowsMouse(graphics, x + 26, y + 8, x + 75, y + 78, 30, 0.0625F, mouseX, mouseY, this.minecraft.player);
    }

    @Override
    protected void renderTooltip(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        // 空の解放済みロールスロットにホバーした時のカスタムツールチップ
        if (this.hoveredSlot != null && !this.hoveredSlot.hasItem() && this.minecraft != null && this.minecraft.player != null) {
            // ロールスロットの場合のみ処理
            if (this.hoveredSlot.container instanceof Inventory) {
                int inventoryIndex = this.hoveredSlot.getContainerSlot();

                // 解放済みロールスロットかチェック
                if (SlotRestriction.unlockedRoleSlot(inventoryIndex, this.minecraft.player)) {
                    // ツールチップを生成して表示
                    List<Component> tooltip = createRoleSlotTooltip(inventoryIndex, this.minecraft.player);
                    graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY, net.minecraft.world.item.ItemStack.EMPTY);
                    return;
                }
            }
        }

        // デフォルトの動作（アイテムがある場合のツールチップ）
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * ロールスロットのツールチップを生成
     *
     * @param inventoryIndex インベントリインデックス
     * @param player プレイヤー
     * @return ツールチップの行リスト
     */
    private List<Component> createRoleSlotTooltip(int inventoryIndex, Player player) {
        List<Component> tooltip = new ArrayList<>();

        int roleSlotIndex = -1;
        Role role;
        if (SlotRestriction.unlockedMainRoleSlot(inventoryIndex, player)) {
            roleSlotIndex = SlotRestriction.getMainRoleSlotIndex(inventoryIndex);
            role = RoleManager.getMainRole(player);
        } else {
            roleSlotIndex = SlotRestriction.getSubRoleSlotIndex(inventoryIndex, player);
            role = RoleManager.getSubRole(player);
        }

        // スロット名の翻訳キー
        String slotNameKey = "slot.oneslotsurvival." + role.getCommandName() + "." + roleSlotIndex + ".name";

        // スロット名を追加
        tooltip.add(Component.translatable(slotNameKey).withStyle(ChatFormatting.GOLD));

        // 説明の翻訳キー
        String descriptionKey = "slot.oneslotsurvival." + role.getCommandName() + "." + roleSlotIndex + ".description";
        Component description = Component.translatable(descriptionKey);

        // 説明文を取得して単語単位で分割
        String descriptionText = description.getString();
        List<String> wrappedLines = wrapText(descriptionText, 20); // 1行あたり約20文字で改行

        // 分割された各行をツールチップに追加
        for (String line : wrappedLines) {
            tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }

        return tooltip;
    }

    /**
     * テキストを指定した文字数で単語単位で改行する
     *
     * @param text 改行対象のテキスト
     * @param maxLength 1行あたりの最大文字数
     * @return 改行されたテキストのリスト
     */
    private List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // 現在の行に単語を追加すると最大文字数を超える場合
            if (currentLine.length() > 0 && currentLine.length() + 1 + word.length() > maxLength) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }

            // 現在の行に単語を追加
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        // 最後の行を追加
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
