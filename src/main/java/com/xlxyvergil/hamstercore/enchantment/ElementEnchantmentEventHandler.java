package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.util.ElementModifierManager;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

/**
 * 元素附魔事件处理器
 * 负责在附魔应用和移除时同步ElementModifierManager的属性修饰符
 * 参考EnchantingInfuser 的实现模式
 */
@Mod.EventBusSubscriber
public class ElementEnchantmentEventHandler {
    
    // 用于缓存上次处理的附魔状态
    private static final String LAST_ENCHANTMENT_CACHE = "HamsterCore_LastEnchantmentCache";
    
    /**
     * 使用 ItemAttributeModifierEvent 直接添加元素修饰符
     * 参考Apotheosis AttributeAffix 实现模式
     */
    @SubscribeEvent(priority = EventPriority.LOW) // 低优先级，在主要修饰符处理完后执行
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        
        // 只处理主手槽位的物品
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 直接应用当前附魔的修饰符
        applyCurrentElementEnchantments(stack, event);
        
        // 更新附魔缓存
        updateEnchantmentCache(stack);
    }
    
    /**
     * 检查附魔是否发生变化
     */
    private static boolean hasEnchantmentChanged(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        
        CompoundTag tag = stack.getTag();
        CompoundTag currentEnchantments = new CompoundTag();
        
        // 将当前附魔信息转换为可比较的格式
        for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
            if (entry.getKey() instanceof ElementEnchantment elementEnchantment) {
                String enchantName = elementEnchantment.getEnchantmentId();
                currentEnchantments.putInt(enchantName, entry.getValue());
            }
        }
        
        // 与缓存的附魔信息比较
        CompoundTag cachedEnchantments = tag.getCompound(LAST_ENCHANTMENT_CACHE);
        return !currentEnchantments.equals(cachedEnchantments);
    }
    
    /**
     * 更新附魔缓存
     */
    private static void updateEnchantmentCache(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        
        CompoundTag tag = stack.getTag();
        CompoundTag currentEnchantments = new CompoundTag();
        
        // 将当前附魔信息缓存
        for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
            if (entry.getKey() instanceof ElementEnchantment elementEnchantment) {
                String enchantName = elementEnchantment.getEnchantmentId();
                currentEnchantments.putInt(enchantName, entry.getValue());
            }
        }
        
        tag.put(LAST_ENCHANTMENT_CACHE, currentEnchantments);
    }
    
    /**
     * 使用与GunsmithLib相同的合并策略的修饰符缓冲区
     */
    private static final ThreadLocal<Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier>> MERGE_BUFFER = ElementModifierManager.createThreadLocalMergeBuffer();
    
    /**
     * 应用当前元素附魔的修饰符
     * 直接通过 ItemAttributeModifierEvent 添加修饰符，参考Apotheosis 实现模式
     * 使用GunsmithLib的合并策略合并相同属性和操作的修饰符
     */
    private static void applyCurrentElementEnchantments(ItemStack stack, ItemAttributeModifierEvent event) {
        // 确保stack和event不为null
        if (stack == null || event == null) {
            return;
        }
        
        var buffer = MERGE_BUFFER.get();
        try {
            buffer.clear();
            
            for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
                // 确保entry、key和value不为null
                if (entry == null || entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                
                if (entry.getKey() instanceof ElementEnchantment elementEnchantment && elementEnchantment.getElementType() != null) {
                    int level = entry.getValue();
                    ElementType elementType = elementEnchantment.getElementType();
                    
                    // 确保elementType不为null
                    if (elementType == null) {
                        continue;
                    }
                    
                    ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
                    if (elementAttribute != null) {
                        Collection<AttributeModifier> modifiers = elementEnchantment.getEntityAttributes(stack, EquipmentSlot.MAINHAND, level);
                        
                        if (modifiers != null) {
                            for (AttributeModifier modifier : modifiers) {
                                // 确保修饰符不为null
                                if (modifier != null) {
                                    // 合并相同属性和操作的修饰符
                                    ElementModifierManager.mergeModifier(buffer, elementAttribute, modifier);
                                }
                            }
                        }
                    }
                }
            }
            
            // 将合并后的修饰符添加到事件中
            ElementModifierManager.applyMergedModifiers(buffer, event::addModifier);
        } finally {
            buffer.clear();
        }
    }
}