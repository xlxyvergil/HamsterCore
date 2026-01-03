package com.xlxyvergil.hamstercore.modification;

import com.xlxyvergil.hamstercore.api.element.AffixAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public record ModificationInstance(
    DynamicModificationHolder holder,
    UUID uuid
) {
    public static final String TAG_MODIFICATIONS = "HamsterCoreModifications";
    public static final String TAG_MODIFICATION_ID = "ModificationId";
    public static final String TAG_MODIFICATION_UUID = "ModificationUUID";
    
    // 添加EMPTY静态常量，表示空的ModificationInstance
    public static final ModificationInstance EMPTY = new ModificationInstance(DynamicModificationHolder.empty(), UUID.randomUUID());

    // 空构造函数，用于创建空的ModificationInstance
    public ModificationInstance(String id) {
        this(createHolderFromString(id), UUID.randomUUID());
    }
    
    private static DynamicModificationHolder createHolderFromString(String id) {
        if (id != null) {
            try {
                return new DynamicModificationHolder(new ResourceLocation(id));
            } catch (Exception e) {
                // 如果ResourceLocation格式不正确，返回空holder
                return DynamicModificationHolder.empty();
            }
        } else {
            return DynamicModificationHolder.empty();
        }
    }



    public ModificationInstance(Modification modification) {
        this(createHolderFromModification(modification), UUID.randomUUID());
    }
    
    private static DynamicModificationHolder createHolderFromModification(Modification modification) {
        if (modification != null && modification.id() != null) {
            try {
                return new DynamicModificationHolder(modification.id());
            } catch (Exception e) {
                // 如果ResourceLocation出现问题，返回空holder
                return DynamicModificationHolder.empty();
            }
        } else {
            return DynamicModificationHolder.empty();
        }
    }
    
    // 复制构造函数
    public ModificationInstance(ModificationInstance other) {
        this(other.holder, other.uuid);
    }

    public static ModificationInstance fromNBT(CompoundTag tag, ModificationRegistry registry) {
        String id = tag.getString(TAG_MODIFICATION_ID);
        // 检查ID是否为空或无效
        if (id == null || id.isEmpty()) {
            return EMPTY; // 如果ID为空，直接返回空实例
        }
        UUID uuid = tag.getUUID(TAG_MODIFICATION_UUID);
        DynamicModificationHolder holder = createHolderFromNBT(id);
        if (holder == null) {
            return EMPTY; // 如果创建holder失败，返回空实例
        }
        return new ModificationInstance(holder, uuid);
    }
    
    private static DynamicModificationHolder createHolderFromNBT(String id) {
        try {
            // 尝试使用tryParse解析，如果失败则尝试直接构造（捕获异常）
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location == null) {
                // 如果tryParse失败，尝试直接构造（可能抛出异常）
                location = new ResourceLocation(id);
            }
            return new DynamicModificationHolder(location);
        } catch (Exception e) {
            // 如果ResourceLocation格式不正确，返回null
            return null;
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        if (this.holder != null && this.holder.isBound() && this.holder.getId() != null) {
            tag.putString(TAG_MODIFICATION_ID, this.holder.getId().toString());
            tag.putUUID(TAG_MODIFICATION_UUID, this.uuid);
        }
        return tag;
    }

    public boolean isValid() {
        return this.holder != null && this.holder.isBound() && this.holder.get() != null;
    }
    
    // 添加modificationId方法，返回改装件ID字符串
    public String modificationId() {
        return this.holder != null && this.holder.isBound() && this.holder.getId() != null ? this.holder.getId().toString() : "";
    }
    
    // 添加modification方法，返回改装件实例
    public Modification modification() {
        return this.holder != null && this.holder.isBound() ? this.holder.get() : null;
    }

    public void applyAffixes(ItemStack stack) {
        if (this.holder != null && this.holder.isBound() && this.holder.get() != null) {
            for (ModificationAffix affix : this.holder.get().affixes()) {
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
    }

    public void removeAffixes(ItemStack stack) {
        AffixAPI.removeAffix(stack, this.uuid);
    }
}
