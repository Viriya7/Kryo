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

    public static List<BlueprintGroup> getGroups() {
        return Collections.unmodifiableList(GROUPS);
    }
}