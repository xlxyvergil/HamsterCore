package com.xlxyvergil.hamstercore.modification;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;

public enum ModificationCategory {
    GENERAL("general"),
    SPECIAL("special");

    private static final Map<String, ModificationCategory> BY_ID = new HashMap<>();

    static {
        for (ModificationCategory category : values()) {
            BY_ID.put(category.name, category);
        }
    }

    private final String name;

    ModificationCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public static ModificationCategory byId(String name) {
        return BY_ID.get(name);
    }

    public static final Codec<ModificationCategory> CODEC = ExtraCodecs.stringResolverCodec(ModificationCategory::getName, ModificationCategory::byId);
}
