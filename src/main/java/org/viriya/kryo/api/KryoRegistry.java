package org.viriya.kryo.api;

import org.viriya.kryo.blueprint.BlueprintGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class KryoRegistry {

    private static final List<BlueprintGroup> GROUPS = new ArrayList<>();

    public static void registerGroup(BlueprintGroup group) {
        if (!GROUPS.contains(group)) {
            GROUPS.add(group);
        }
    }

    public static BlueprintGroup getGroup(String id) {
        for (BlueprintGroup group : GROUPS) {
            if (group.getId().equalsIgnoreCase(id)) {
                return group;
            }
        }
        return null;
    }

    public static List<BlueprintGroup> getGroups() {
        return Collections.unmodifiableList(GROUPS);
    }

    public static List<BlueprintGroup> getSubGroups(String parentId) {
        List<BlueprintGroup> subGroups = new ArrayList<>();
        for (BlueprintGroup group : GROUPS) {
            if (group.hasParent() && group.getParentId().equalsIgnoreCase(parentId)) {
                subGroups.add(group);
            }
        }
        return subGroups;
    }
}