package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.modifier.ElementAttributeModifierEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

/**
 * 元素附魔事件处理器
 * 负责在附魔应用和移除时同步ElementAttributeModifierEntry的属性修饰符
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
        
        // 检查附魔是否发生变化，如果没有变化则不重新应用修饰符
        boolean enchantmentChanged = hasEnchantmentChanged(stack);
        if (enchantmentChanged) {
            // 应用当前附魔的修饰符
            applyCurrentElementEnchantments(stack, event);
        }
        
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
    private static final ThreadLocal<Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier>> MERGE_BUFFER = ThreadLocal.withInitial(() -> new HashMap<>());
    
    /**
     * 将ElementEnchantment的修饰符转换为ElementAttributeModifierEntry格式
     * @param elementEnchantment 元素附魔
     * @param stack 物品堆
     * @param slot 装备槽位
     * @param level 附魔等级
     * @return ElementAttributeModifierEntry列表
     */
    private static List<ElementAttributeModifierEntry> convertToElementModifierEntries(
            ElementEnchantment elementEnchantment, ItemStack stack, EquipmentSlot slot, int level) {
        List<ElementAttributeModifierEntry> elementModifiers = new ArrayList<>();
        
        if (elementEnchantment == null || stack == null || elementEnchantment.getElementType() == null) {
            return elementModifiers;
        }
        
        ElementType elementType = elementEnchantment.getElementType();
        RegistryObject<ElementAttribute> elementAttribute = ElementRegistry.getAttribute(elementType);
        
        if (elementAttribute != null && elementAttribute.isPresent()) {
            Collection<AttributeModifier> modifiers = elementEnchantment.getEntityAttributes(stack, slot, level);
            
            if (modifiers != null) {
                for (AttributeModifier modifier : modifiers) {
                    try {
                        if (modifier == null) {
                            continue;
                        }
                        
                        // 创建ElementAttributeModifierEntry
                        ElementAttributeModifierEntry modifierEntry = new ElementAttributeModifierEntry(
                            elementType,
                            modifier.getId(),
                            modifier.getAmount(),
                            modifier.getOperation()
                        );
                        
                        elementModifiers.add(modifierEntry);
                        
                    } catch (Exception e) {
                        System.err.println("Error converting ElementEnchantment modifier: " + e.getMessage());
                    }
                }
            }
        }
        
        return elementModifiers;
    }
    
    /**
     * 应用当前元素附魔的修饰符
     * 使用ElementAttributeModifierEntry直接应用修饰符，与WeaponDefaultModifierHandler保持一致
     * 确保所有修饰符都正确出现在AttributeModifiers中
     */
    private static void applyCurrentElementEnchantments(ItemStack stack, ItemAttributeModifierEvent event) {
        // 确保stack不为null
        if (stack == null) {
            return;
        }
        
        List<ElementAttributeModifierEntry> allModifiers = new ArrayList<>();
        
        // 先移除当前所有元素附魔的旧修饰符，避免重复添加
        for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
            if (entry != null && entry.getKey() instanceof ElementEnchantment elementEnchantment && elementEnchantment.getElementType() != null) {
                ElementType elementType = elementEnchantment.getElementType();
                // 移除该元素类型的所有修饰符
                ElementAttributeModifierEntry.removeElementModifiers(stack, elementType);
            }
        }
        
        // 收集所有需要应用的新修饰符
        for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
            // 确保entry、key和value不为null
            if (entry == null || entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            
            if (entry.getKey() instanceof ElementEnchantment elementEnchantment && elementEnchantment.getElementType() != null) {
                int level = entry.getValue();
                
                // 转换为ElementAttributeModifierEntry格式
                List<ElementAttributeModifierEntry> enchantmentModifiers = 
                    convertToElementModifierEntries(elementEnchantment, stack, EquipmentSlot.MAINHAND, level);
                
                if (!enchantmentModifiers.isEmpty()) {
                    allModifiers.addAll(enchantmentModifiers);
                }
            }
        }
        
        // 使用ElementAttributeModifierEntry直接应用修饰符，与WeaponDefaultModifierHandler保持一致
        if (!allModifiers.isEmpty()) {
            // 应用新的附魔修饰符到ItemStack的AttributeModifiers NBT中
            ElementAttributeModifierEntry.applyModifiers(stack, allModifiers, EquipmentSlot.MAINHAND);
            
            // 同时应用到事件中，确保实体获得正确的属性加成
            Map<Pair<Attribute, AttributeModifier.Operation>, AttributeModifier> buffer = MERGE_BUFFER.get();
            try {
                buffer.clear(); // 清空之前的内容
                
                // 使用带合并策略的方式应用修饰符到事件中
                ElementAttributeModifierEntry.applyModifiersWithMergeStrategy(stack, allModifiers, EquipmentSlot.MAINHAND, 
                    (attr, mod) -> ElementAttributeModifierEntry.mergeModifier(buffer, attr, mod));
                
                // 将合并后的修饰符应用到事件中
                ElementAttributeModifierEntry.applyMergedModifiers(buffer, event::addModifier);
            } finally {
                buffer.clear(); // 确保缓冲区被清空，避免内存泄漏
            }
        }
    }
}