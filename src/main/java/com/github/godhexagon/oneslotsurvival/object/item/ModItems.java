package com.github.godhexagon.oneslotsurvival.object.item;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, OneSlotSurvivalMod.MODID);

    // スロットブロック用のバリアアイテム
    public static final RegistryObject<Item> SLOT_BARRIER = ITEMS.register("slot_barrier",
            () -> new SlotBarrier(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(OneSlotSurvivalMod.MODID, "slot_barrier")))
                    .stacksTo(1)
                    .fireResistant()));
}