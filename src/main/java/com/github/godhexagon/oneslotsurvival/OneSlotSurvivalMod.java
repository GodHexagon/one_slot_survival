package com.github.godhexagon.oneslotsurvival;

import com.github.godhexagon.oneslotsurvival.object.attribute.ModAttributes;
import com.github.godhexagon.oneslotsurvival.object.attribute.PlayerAttributeHandler;
import com.github.godhexagon.oneslotsurvival.object.config.ExpConfig;
import com.github.godhexagon.oneslotsurvival.object.gamerule.ModGameRules;
import com.github.godhexagon.oneslotsurvival.object.item.DynamicBuilderTags;
import com.github.godhexagon.oneslotsurvival.object.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(OneSlotSurvivalMod.MODID)
public final class OneSlotSurvivalMod {
    public static final String MODID = "oneslotsurvival";
    private static final Logger LOGGER = LogUtils.getLogger();

    public OneSlotSurvivalMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        // DeferredRegistry を登録
        ModItems.ITEMS.register(modBusGroup);

        // Attribute を登録
        ModAttributes.ATTRIBUTES.register(modBusGroup);

        // Attribute ハンドラを登録（EntityAttributeModificationEvent は mod bus で発火）
        net.minecraftforge.event.entity.EntityAttributeModificationEvent.getBus(modBusGroup)
            .addListener(PlayerAttributeHandler::onEntityAttributeModification);

        // Config を登録
        context.registerConfig(ModConfig.Type.SERVER, ExpConfig.SERVER_SPEC);

        // Config イベントハンドラを登録
        ModConfigEvent.Loading.getBus(modBusGroup).addListener(ExpConfig::onModConfigEvent);
        ModConfigEvent.Reloading.getBus(modBusGroup).addListener(ExpConfig::onModConfigEvent);

        // mod ローディング用の共通セットアップメソッドを登録
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);

        // サーバーおよびその他のゲームイベントを処理するため自身を登録
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("One Slot Survival mod is loading...");

        // カスタムGameRuleを登録
        ModGameRules.register();
        LOGGER.info("Custom game rules registered");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("One Slot Survival mod server starting");

        // Builderロール用の動的タグを生成
        try {
            DynamicBuilderTags.generateBuilderTags(event.getServer().registryAccess());
            LOGGER.info("Dynamic Builder tags generated successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to generate dynamic Builder tags", e);
        }
    }
}