package com.github.godhexagon.oneslotsurvival.rule.role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoleSlotHelper implements RoleSlotProvider {
    private final List<RoleSlot> roleSlots;
    
    public RoleSlotHelper(List<RoleSlot> roleSlots) {
        this.roleSlots = roleSlots;
    }

    @Override
    public List<RoleSlot> getRoleSlots() {
        return roleSlots;
    }

    @Override
    public List<RoleSlot> getAvailableRoleSlots(int levelUpTimesInRoleToAvailable) {
        return getRoleSlots().stream()
            .filter(slot -> slot.levelUpTimesInRole() <= levelUpTimesInRoleToAvailable)
            .toList();
    }

    @Override
    public Optional<RoleSlot> getAvailableRoleSlot(int index, int levelUpTimesInRole) {
        if (-1 < index && index < getAvailableRoleSlots(levelUpTimesInRole).size()) {
            return Optional.of(getAvailableRoleSlots(levelUpTimesInRole).get(index));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasRoleSlots() {
        return !roleSlots.isEmpty();
    }
    
    @Override
    public Map<Integer, List<RoleSlot>> getJustUnlockedRoleSlotsMap() {
        Map<Integer, List<RoleSlot>> map = new HashMap<>();
        var slotsIter = getRoleSlots().iterator();
        while (slotsIter.hasNext()) {
            // slotsIterは、使用したことが分かればいいので、このタイミングで捨ててしまう
            int levelUpTimesInRole = slotsIter.next().levelUpTimesInRole();
            List<RoleSlot> roleSlots = getRoleSlots().stream()
                .filter(slot -> slot.levelUpTimesInRole() == levelUpTimesInRole)
                .toList();
            // メインロールでは正しく動く。一方でサブロールでは同じroleSlotsが何度も生成されてしまうが、Map.putでは上書きされるので問題なし。
            map.put(
                levelUpTimesInRole,
                roleSlots
            );
        }
        return map;
    }

    @Override
    public List<RoleSlot> getJustUnlockedRoleSlots(int levelUpTimesInRole) {
        if (getJustUnlockedRoleSlotsMap().containsKey(levelUpTimesInRole)) {
            return getJustUnlockedRoleSlotsMap().get(levelUpTimesInRole);
        } else {
            return List.of();
        }
    }
}
