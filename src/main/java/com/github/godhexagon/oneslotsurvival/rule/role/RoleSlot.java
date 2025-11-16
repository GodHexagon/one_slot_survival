package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record RoleSlot(TagKey<Item> itemTag, int index, int levelUpTimesInRole) {}
