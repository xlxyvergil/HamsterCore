package com.xlxyvergil.hamstercore.modification;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 改装件实例 - 模仿 Apotheosis 的 GemInstance
 */
public record ModificationInstance(
    String modificationId,   // 改装件 ID
    UUID uuid              // 属性修饰符 UUID
) {

    public static final String TAG_MODIFICATION_ID = "ModificationId";
    public static final String TAG_MODIFICATION_UUID = "ModificationUUID";

    public static final ModificationInstance EMPTY = new ModificationInstance("", UUID.randomUUID());

    /**
     * 从 NBT 创建
     */
    public static ModificationInstance fromNBT(CompoundTag tag) {
        String id = tag.getString(TAG_MODIFICATION_ID);
        if (id == null || id.isEmpty()) {
            return EMPTY;
        }
        UUID uuid = tag.getUUID(TAG_MODIFICATION_UUID);
        return new ModificationInstance(id, uuid);
    }

    /**
     * 转换为 NBT
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        if (!this.modificationId.isEmpty()) {
            tag.putString(TAG_MODIFICATION_ID, this.modificationId);
            tag.putUUID(TAG_MODIFICATION_UUID, this.uuid);
        }
        return tag;
    }

    /**
     * 从改装件物品堆创建实例
     */
    public static ModificationInstance fromModificationStack(ItemStack modificationStack) {
        return ModificationItem.getModification(modificationStack);
    }
    
    /**
     * 从改装件定义创建实例
     */
    public static ModificationInstance fromModification(Modification modification) {
        return new ModificationInstance(modification.id().toString(), modification.uuid());
    }

    /**
     * 检查是否有效
     */
    public boolean isValid() {
        if (this.modificationId.isEmpty()) {
            return false;
        }
        DynamicHolder<Modification> modHolder = ModificationRegistry.INSTANCE.holder(ResourceLocation.parse(this.modificationId));
        return modHolder.isBound();
    }

    /**
     * 获取改装件定义
     */
    @Nullable
    public DynamicHolder<Modification> getModification() {
        if (!this.isValid()) {
            return null;
        }
        return ModificationRegistry.INSTANCE.holder(ResourceLocation.parse(this.modificationId));
    }

    /**
     * 应用所有词缀到物品
     */
    public void applyAffixes(ItemStack stack, Player player) {
        if (!this.isValid()) {
            return;
        }

        DynamicHolder<Modification> modHolder = getModification();
        if (modHolder == null || !modHolder.isBound()) {
            return;
        }

        Modification modification = modHolder.get();

        // 应用所有词缀 - 使用AffixAPI传递数据，由其他系统统一处理属性修饰符
        for (ModificationAffix affix : modification.affixes()) {
            // 使用AffixAPI添加词缀，由其他系统统一处理属性修饰符
            com.xlxyvergil.hamstercore.api.element.AffixAPI.addAffix(
                stack,
                affix.name(),
                affix.type(),
                affix.value(),
                affix.operation(),
                this.uuid,
                "modification"
            );
        }
    }

    /**
     * 从物品移除所有词缀
     */
    public void removeAffixes(ItemStack stack) {
        if (!this.isValid()) {
            return;
        }

        // 使用AffixAPI移除词缀，由其他系统统一处理属性修饰符
        com.xlxyvergil.hamstercore.api.element.AffixAPI.removeAffix(stack, this.uuid);
    }



    /**
     * 获取词缀信息
     */
    public Component getAffixInfo() {
        if (!this.isValid()) {
            return Component.empty();
        }

        DynamicHolder<Modification> modHolder = getModification();
        if (modHolder == null || !modHolder.isBound()) {
            return Component.literal("Invalid Modification");
        }

        return Component.translatable("hamstercore.modification." + this.modificationId);
    }

    /**
     * 获取改装件物品堆
     */
    public ItemStack modificationStack() {
        if (!this.isValid()) {
            return ItemStack.EMPTY;
        }

        DynamicHolder<Modification> modHolder = getModification();
        if (modHolder == null || !modHolder.isBound()) {
            return ItemStack.EMPTY;
        }

        return ModificationItem.createModificationStack(modHolder.get());
    }

    /**
     * 获取槽位奖励提示
     */
    public Component getSocketBonusTooltip() {
        if (!this.isValid()) {
            return Component.empty();
        }

        DynamicHolder<Modification> modHolder = getModification();
        if (modHolder == null || !modHolder.isBound()) {
            return Component.literal("Invalid Modification");
        }

        Modification modification = modHolder.get();
        
        // 构建tooltip，显示所有词缀
        StringBuilder sb = new StringBuilder();
        for (ModificationAffix affix : modification.affixes()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(affix.name()).append(": ").append(affix.value());
        }
        
        return Component.literal(sb.toString()).withStyle(ChatFormatting.GREEN);
    }

    /**
     * 获取描述信息
     */
    public Component getDescription() {
        return getSocketBonusTooltip();
    }
    
    /**
     * 获取互斥组列表
     */
    public java.util.List<String> getMutualExclusionGroups() {
        if (!this.isValid()) {
            return java.util.Collections.emptyList();
        }
        
        DynamicHolder<Modification> modHolder = getModification();
        if (modHolder == null || !modHolder.isBound()) {
            return java.util.Collections.emptyList();
        }
        
        return modHolder.get().mutualExclusionGroups();
    }
}
