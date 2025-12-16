package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.element.EnchantmentAffixData;
import com.xlxyvergil.hamstercore.enchantment.ElementEnchantment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        String idString = id != null ? id.toString() : "unknown";
        return UUID.nameUUIDFromBytes(idString.getBytes());
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