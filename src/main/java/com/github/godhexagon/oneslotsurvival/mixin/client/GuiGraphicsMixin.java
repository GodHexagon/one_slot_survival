package com.github.godhexagon.oneslotsurvival.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * GuiGraphicsにMixinして、privateなspritesフィールドへのリフレクションアクセスを提供する。
 * Minecraft 1.21.9で削除されたMinecraft.getGuiSprites()の代替手段。
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Unique
    private static Field one_slot_survival$spritesField = null;

    @Unique
    private static Method one_slot_survival$getSpriteMethod = null;

    /**
     * スプライトを直接取得するヘルパーメソッド。
     * 完全にリフレクションベースで、GuiSpriteManager型への参照を避ける。
     *
     * @param spriteLocation スプライトのResourceLocation
     * @return TextureAtlasSprite
     */
    @Unique
    public TextureAtlasSprite one_slot_survival$getSprite(ResourceLocation spriteLocation) {
        try {
            // guiSprites フィールドにアクセス（1.21.9で "sprites" から "guiSprites" に名前変更、型も TextureAtlas に変更）
            if (one_slot_survival$spritesField == null) {
                // 1.21.9では "guiSprites" という名前
                try {
                    one_slot_survival$spritesField = GuiGraphics.class.getDeclaredField("guiSprites");
                } catch (NoSuchFieldException e1) {
                    // フォールバック: 旧バージョンの "sprites" を試す
                    try {
                        one_slot_survival$spritesField = GuiGraphics.class.getDeclaredField("sprites");
                    } catch (NoSuchFieldException e2) {
                        // それでも見つからない場合はエラー
                        throw new RuntimeException("Could not find 'guiSprites' or 'sprites' field in GuiGraphics");
                    }
                }
                one_slot_survival$spritesField.setAccessible(true);
            }
            Object spriteAtlas = one_slot_survival$spritesField.get(this);

            // TextureAtlas.getSprite(ResourceLocation) メソッドを取得
            if (one_slot_survival$getSpriteMethod == null) {
                Class<?> atlasClass = spriteAtlas.getClass();
                one_slot_survival$getSpriteMethod = atlasClass.getMethod("getSprite", ResourceLocation.class);
            }

            // スプライトを取得
            return (TextureAtlasSprite) one_slot_survival$getSpriteMethod.invoke(spriteAtlas, spriteLocation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sprite via reflection: " + e.getMessage(), e);
        }
    }
}
