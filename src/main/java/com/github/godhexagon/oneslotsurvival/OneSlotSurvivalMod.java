package com.github.godhexagon.oneslotsurvival;

import com.github.godhexagon.oneslotsurvival.world.attribute.ModAttributes;
import com.github.godhexagon.oneslotsurvival.world.attribute.PlayerAttributeHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(OneSlotSurvivalMod.MODID)
public final class OneSlotSurvivalMod {
    public static final String MODID = "oneslotsurvival";
    private static final Logger LOGGER = LogUtils.getLogger();

    public OneSlotSurvivalMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        // Register attributes
        ModAttributes.ATTRIBUTES.register(modBusGroup);

        // Register attribute handler (EntityAttributeModificationEvent is on mod bus)
        net.minecraftforge.event.entity.EntityAttributeModificationEvent.getBus(modBusGroup)
            .addListener(PlayerAttributeHandler::onEntityAttributeModification);

        // Register the commonSetup method for modloading
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("One Slot Survival mod is loading...");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("One Slot Survival mod server starting");
    }
}