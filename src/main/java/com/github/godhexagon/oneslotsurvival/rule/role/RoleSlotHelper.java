package com.github.godhexagon.oneslotsurvival.rule.role;

import java.util.List;
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
    
}
