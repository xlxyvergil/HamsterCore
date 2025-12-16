package com.xlxyvergil.hamstercore.element;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.xlxyvergil.hamstercore.enchantment.ElementEnchantment;

public class EnchantmentAffixManager {
    private static final String ENCHANTMENT_AFFIX_TAG = "EnchantmentAffixData";
    
    /**
     * 添加附魔与词缀的关联
     */
    public static void addEnchantmentAffixAssociation(ItemStack stack, UUID enchantmentUuid, List<UUID> affixUuids) {
        EnchantmentAffixData data = new EnchantmentAffixData(enchantmentUuid, affixUuids);
        
        CompoundTag stackTag = stack.getOrCreateTag();
        ListTag associationList = stackTag.getList(ENCHANTMENT_AFFIX_TAG, Tag.TAG_COMPOUND);
        associationList.add(data.toNBT());
        stackTag.put(ENCHANTMENT_AFFIX_TAG, associationList);
    }
    
    /**
     * 获取所有附魔与词缀的关联
     */
    public static List<EnchantmentAffixData> getEnchantmentAffixAssociations(ItemStack stack) {
        List<EnchantmentAffixData> associations = new ArrayList<>();
        
        if (stack.hasTag()) {
            CompoundTag stackTag = stack.getTag();
            if (stackTag.contains(ENCHANTMENT_AFFIX_TAG, Tag.TAG_LIST)) {
                ListTag associationList = stackTag.getList(ENCHANTMENT_AFFIX_TAG, Tag.TAG_COMPOUND);
                for (int i = 0; i < associationList.size(); i++) {
                    associations.add(EnchantmentAffixData.fromNBT(associationList.getCompound(i)));
                }
            }
        }
        
        return associations;
    }
    
    /**
     * 处理附魔删除事件
     */
    public static void handleEnchantmentRemoved(ItemStack stack) {
        // 获取所有附魔UUID
        Set<UUID> currentEnchantmentUuids = new HashSet<>();
        for (Enchantment enchantment : stack.getAllEnchantments().keySet()) {
            // Minecraft附魔没有直接的UUID，但可以通过自定义方式获取
            // 或者使用EnchantmentUtil.getEnchantmentId()生成唯一标识符
            currentEnchantmentUuids.add(generateEnchantmentUuid(enchantment));
        }
        
        // 获取所有关联数据
        List<EnchantmentAffixData> associations = getEnchantmentAffixAssociations(stack);
        
        // 检查哪些关联的附魔已被删除
        List<EnchantmentAffixData> removedAssociations = new ArrayList<>();
        for (EnchantmentAffixData association : associations) {
            if (!currentEnchantmentUuids.contains(association.getEnchantmentUuid())) {
                // 删除关联的词缀
                for (UUID affixUuid : association.getAffixUuids()) {
                    AffixManager.removeAffix(stack, affixUuid);
                }
                removedAssociations.add(association);
            }
        }
        
        // 更新关联数据
        associations.removeAll(removedAssociations);
        saveEnchantmentAffixAssociations(stack, associations);
    }
    
    /**
     * 生成附魔的UUID
     */
    public static UUID generateEnchantmentUuid(Enchantment enchantment) {
        // 使用Minecraft 1.20.1的API获取注册表名称
        ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        String idString = id != null ? id.toString() : "unknown";
        return UUID.nameUUIDFromBytes(idString.getBytes());
    }
    
    /**
     * 更新物品上所有附魔对应的词缀
     * 这是动态更新的核心方法，会检查所有当前附魔并确保其对应的词缀都已应用
     */
    public static void updateEnchantmentAffixes(ItemStack stack) {
        // 首先验证并清理现有关联
        verifyEnchantmentAffixes(stack);
        
        // 获取当前所有附魔
        for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
            UUID enchantmentUuid = generateEnchantmentUuid(enchantment);
            
            // 检查该附魔是否已有关联
            boolean hasAssociation = false;
            for (EnchantmentAffixData data : getEnchantmentAffixAssociations(stack)) {
                if (data.getEnchantmentUuid().equals(enchantmentUuid)) {
                    hasAssociation = true;
                    break;
                }
            }
            
            // 如果没有关联且是ElementEnchantment，则创建新的词缀并关联
            if (!hasAssociation && enchantment instanceof ElementEnchantment elementEnchantment) {
                // 创建词缀并添加到物品
                List<UUID> affixUuids = new ArrayList<>();
                
                // 获取元素类型和值
                ElementType elementType = elementEnchantment.getElementType();
                double elementValue = elementEnchantment.getElementValue(level);
                
                // 添加元素伤害词缀
                AffixManager.addAffix(stack, "element_damage", elementType.toString().toLowerCase(), elementValue, "ADDITION", "enchantment");
                
                // 为特殊元素类型添加额外的词缀
                if (elementType.isSpecial()) {
                    String specialName = elementType.toString().toLowerCase().replace("_", "_");
                    AffixManager.addAffix(stack, specialName + "_bonus", elementType.toString().toLowerCase(), elementValue * 0.5, "MULTIPLY_BASE", "enchantment");
                }
                
                // 关联附魔和词缀
                // 注意：当前AffixManager.addAffix没有返回UUID，所以我们使用临时UUID
                UUID tempAffixUuid = UUID.randomUUID();
                affixUuids.add(tempAffixUuid);
                
                if (!affixUuids.isEmpty()) {
                    addEnchantmentAffixAssociation(stack, enchantmentUuid, affixUuids);
                }
            }
        }
    }
    
    /**
     * 验证所有关联的附魔是否存在，并删除无效的关联
     */
    public static void verifyEnchantmentAffixes(ItemStack stack) {
        // 获取当前所有附魔UUID
        Set<UUID> currentEnchantmentUuids = new HashSet<>();
        for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
            currentEnchantmentUuids.add(generateEnchantmentUuid(enchantment));
        }
        
        // 获取所有关联数据
        List<EnchantmentAffixData> associations = getEnchantmentAffixAssociations(stack);
        
        // 检查哪些关联的附魔已被删除
        List<EnchantmentAffixData> removedAssociations = new ArrayList<>();
        for (EnchantmentAffixData association : associations) {
            if (!currentEnchantmentUuids.contains(association.getEnchantmentUuid())) {
                // 删除关联的词缀
                for (UUID affixUuid : association.getAffixUuids()) {
                    AffixManager.removeAffix(stack, affixUuid);
                }
                removedAssociations.add(association);
            }
        }
        
        // 更新关联数据
        if (!removedAssociations.isEmpty()) {
            associations.removeAll(removedAssociations);
            saveEnchantmentAffixAssociations(stack, associations);
        }
    }
    
    /**
     * 保存附魔与词缀的关联
     */
    private static void saveEnchantmentAffixAssociations(ItemStack stack, List<EnchantmentAffixData> associations) {
        CompoundTag stackTag = stack.getOrCreateTag();
        ListTag associationList = new ListTag();
        
        for (EnchantmentAffixData data : associations) {
            associationList.add(data.toNBT());
        }
        
        stackTag.put(ENCHANTMENT_AFFIX_TAG, associationList);
    }
}