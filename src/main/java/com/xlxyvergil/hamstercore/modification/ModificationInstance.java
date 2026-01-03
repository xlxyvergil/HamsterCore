package com.xlxyvergil.hamstercore.modification;

import com.xlxyvergil.hamstercore.api.element.AffixAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public record ModificationInstance(
    Modification modification,
    UUID uuid
) {
    public static final String TAG_MODIFICATIONS = "HamsterCoreModifications";
    public static final String TAG_MODIFICATION_ID = "ModificationId";
    public static final String TAG_MODIFICATION_UUID = "ModificationUUID";
    
    // 添加EMPTY静态常量，表示空的ModificationInstance
    public static final ModificationInstance EMPTY = new ModificationInstance(null, UUID.randomUUID());

    // 空构造函数，用于创建空的ModificationInstance
    public ModificationInstance(String id) {
        this(getModificationById(id), UUID.randomUUID());
    }

    private static Modification getModificationById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        Optional<Modification> mod = ModificationRegistry.getInstance().getModification(new ResourceLocation(id));
        return mod.orElse(null);
    }

    public ModificationInstance(Modification modification) {
        this(modification, UUID.randomUUID());
    }
    
    // 复制构造函数
    public ModificationInstance(ModificationInstance other) {
        this(other.modification, other.uuid);
    }

    public static ModificationInstance fromNBT(CompoundTag tag, ModificationRegistry registry) {
        String id = tag.getString(TAG_MODIFICATION_ID);
        UUID uuid = tag.getUUID(TAG_MODIFICATION_UUID);
        Optional<Modification> mod = registry.getModification(new ResourceLocation(id));
        return mod.map(modification -> new ModificationInstance(modification, uuid)).orElse(EMPTY);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        if (this.modification != null) {
            tag.putString(TAG_MODIFICATION_ID, this.modification.id().toString());
            tag.putUUID(TAG_MODIFICATION_UUID, this.uuid);
        }
        return tag;
    }

    public boolean isValid() {
        return this.modification != null;
    }
    
    // 添加modificationId方法，返回改装件ID字符串
    public String modificationId() {
        return this.modification != null ? this.modification.id().toString() : "";
    }

    public void applyAffixes(ItemStack stack) {
        for (ModificationAffix affix : this.modification.affixes()) {
            // 直接使用json里affixes的source参数作为属性名
            AffixAPI.addAffix(
                stack,
                affix.source(),
                affix.type(),
                affix.value(),
                affix.operation(),
                this.uuid,
                "user"
            );
        }
    }

    public void removeAffixes(ItemStack stack) {
        AffixAPI.removeAffix(stack, this.uuid);
    }
}
