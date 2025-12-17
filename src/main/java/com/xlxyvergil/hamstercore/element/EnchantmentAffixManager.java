package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.xlxyvergil.hamstercore.enchantment.ElementEnchantment;

import java.util.*;

/**
 * 附魔词缀管理器
 * 负责管理附魔与词缀之间的关联
 */
public class EnchantmentAffixManager {
    private static final String ENCHANTMENT_AFFIX_TAG = "enchantment_affix_data";

    static {
        MinecraftForge.EVENT_BUS.register(EnchantmentAffixManager.class);
    }

    /**
     * 获取物品上所有附魔词缀关联数据
     */
    public static List<EnchantmentAffixData> getEnchantmentAffixAssociations(ItemStack stack) {
        List<EnchantmentAffixData> associations = new ArrayList<>();
        if (!stack.hasTag()) return associations;
        
        CompoundTag tag = stack.getTag();
        if (tag.contains(ENCHANTMENT_AFFIX_TAG, Tag.TAG_LIST)) {
            ListTag listTag = tag.getList(ENCHANTMENT_AFFIX_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag dataTag = listTag.getCompound(i);
                EnchantmentAffixData data = EnchantmentAffixData.fromNBT(dataTag);
                if (data != null) {
                    associations.add(data);
                }
            }
        }
        
        return associations;
    }

    /**
     * 添加附魔词缀关联
     */
    public static void addEnchantmentAffixAssociation(ItemStack stack, String enchantmentId, int enchantmentLevel, List<UUID> affixUuids) {
        List<EnchantmentAffixData> associations = getEnchantmentAffixAssociations(stack);
        EnchantmentAffixData newData = new EnchantmentAffixData(enchantmentId, enchantmentLevel, affixUuids);
        associations.add(newData);
        saveEnchantmentAffixAssociations(stack, associations);
    }

    /**
     * 从物品上移除指定的附魔词缀关联
     */
    public static void removeEnchantmentAffixAssociation(ItemStack stack, EnchantmentAffixData data) {
        List<EnchantmentAffixData> associations = getEnchantmentAffixAssociations(stack);
        associations.removeIf(d -> d.getEnchantmentId().equals(data.getEnchantmentId()) && d.getEnchantmentLevel() == data.getEnchantmentLevel());
        saveEnchantmentAffixAssociations(stack, associations);
    }

    /**
     * 更新物品上所有附魔对应的词缀
     * 这是动态更新的核心方法，会检查所有当前附魔并确保其对应的词缀都已应用
     */
    public static void updateEnchantmentAffixes(ItemStack stack) {
        // 检查物品是否含有我们的数据（武器数据），如果没有则不处理
        if (WeaponDataManager.getWeaponData(stack) == null) {
            return;
        }
        
        // 首先验证并清理现有关联
        verifyEnchantmentAffixes(stack);
        
        // 获取当前所有附魔
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue(); // 直接从映射中获取等级，避免触发事件
            String enchantmentId = getEnchantmentId(enchantment);
            
            // 检查该附魔是否已有关联
            boolean hasAssociation = false;
            for (EnchantmentAffixData data : getEnchantmentAffixAssociations(stack)) {
                if (data.getEnchantmentId().equals(enchantmentId) && data.getEnchantmentLevel() == level) {
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
                UUID damageAffixUuid = UUID.randomUUID();
                AffixManager.addAffix(stack, elementType.getName(), elementType.getName(), elementValue, "ADDITION", damageAffixUuid, "def");
                affixUuids.add(damageAffixUuid);
                
                // 关联附魔和词缀
                if (!affixUuids.isEmpty()) {
                    addEnchantmentAffixAssociation(stack, enchantmentId, level, affixUuids);
                }
            }
        }
    }

    /**
     * 验证所有关联的附魔是否存在，并删除无效的关联
     */
    public static void verifyEnchantmentAffixes(ItemStack stack) {
        // 获取当前所有附魔ID和等级的映射
        Map<String, Integer> currentEnchantments = new HashMap<>();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            String enchantmentId = getEnchantmentId(enchantment);
            int level = entry.getValue(); // 直接从映射中获取等级，避免触发事件
            currentEnchantments.put(enchantmentId, level);
        }
        
        // 获取所有关联数据
        List<EnchantmentAffixData> associations = getEnchantmentAffixAssociations(stack);
        
        // 检查哪些关联的附魔已被删除
        List<EnchantmentAffixData> removedAssociations = new ArrayList<>();
        for (EnchantmentAffixData association : associations) {
            String enchantmentId = association.getEnchantmentId();
            int enchantmentLevel = association.getEnchantmentLevel();
            
            // 检查当前物品上是否还有这个附魔和等级
            boolean enchantmentExists = currentEnchantments.containsKey(enchantmentId) && currentEnchantments.get(enchantmentId) == enchantmentLevel;
            
            if (!enchantmentExists) {
                // 删除关联的词缀
                for (UUID uuid : association.getAffixUuids()) {
                    AffixManager.removeAffix(stack, uuid);
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

    /**
     * 获取附魔ID
     */
    private static String getEnchantmentId(Enchantment enchantment) {
        return net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getKey(enchantment).toString();
    }

    /**
     * 处理附魔移除事件
     */
    @SubscribeEvent
    public static void handleEnchantmentRemoved(net.minecraftforge.event.AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        ItemStack result = event.getOutput();

        // 检查是否是附魔移除操作
        if (left.hasTag() && result.hasTag()) {
            // 获取原物品的所有附魔
            Map<Enchantment, Integer> originalEnchantments = EnchantmentHelper.getEnchantments(left);
            // 获取结果物品的所有附魔
            Map<Enchantment, Integer> resultEnchantments = EnchantmentHelper.getEnchantments(result);

            // 检查哪些附魔被移除了
            for (Enchantment enchantment : originalEnchantments.keySet()) {
                if (!resultEnchantments.containsKey(enchantment)) {
                    // 获取附魔ID和等级
                    String enchantmentId = getEnchantmentId(enchantment);
                    int enchantmentLevel = originalEnchantments.get(enchantment);

                    // 获取关联的数据
                    List<EnchantmentAffixData> associations = getEnchantmentAffixAssociations(left);
                    for (EnchantmentAffixData data : associations) {
                        if (data.getEnchantmentId().equals(enchantmentId) && data.getEnchantmentLevel() == enchantmentLevel) {
                            // 删除关联的词缀
                            for (UUID uuid : data.getAffixUuids()) {
                                AffixManager.removeAffix(result, uuid);
                            }
                            // 从关联数据中删除
                            removeEnchantmentAffixAssociation(result, data);
                        }
                    }
                }
            }
        }
    }
}