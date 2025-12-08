package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.compat.ModCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;

import java.util.*;

/**
 * 元素属性工具类，提供元素属性的常用操作方法
 * 参考Apotheosis的AffixHelper实现
 */
public class ElementHelper {
    
    public static final String ELEMENT_DATA = "element_data";
    public static final String ELEMENTS = "elements";
    public static final String LAST_POSITION = "last_position";
    public static final String CRITICAL_CHANCE = "critical_chance";
    public static final String CRITICAL_DAMAGE = "critical_damage";
    
    /**
     * 添加元素属性到物品
     */
    public static void addElement(ItemStack stack, ElementInstance element) {
        List<ElementInstance> elements = getElements(stack);
        elements.add(element);
        setElements(stack, elements);
    }
    
    /**
     * 设置物品的所有元素属性
     */
    public static void setElements(ItemStack stack, List<ElementInstance> elements) {
        if (stack.isEmpty() || elements.isEmpty()) {
            return;
        }
        
        CompoundTag elementData = stack.getOrCreateTagElement(ELEMENT_DATA);
        ListTag elementsList = new ListTag();
        
        int maxPosition = -1;
        for (ElementInstance element : elements) {
            elementsList.add(element.toNBT());
            maxPosition = Math.max(maxPosition, element.getPosition());
        }
        
        elementData.put(ELEMENTS, elementsList);
        elementData.putInt(LAST_POSITION, maxPosition);
    }
    
    /**
     * 获取物品的所有元素属性
     */
    public static List<ElementInstance> getElements(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ArrayList<>();
        }
        
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        if (elementData == null || !elementData.contains(ELEMENTS, Tag.TAG_LIST)) {
            return new ArrayList<>();
        }
        
        ListTag elementsList = elementData.getList(ELEMENTS, Tag.TAG_COMPOUND);
        List<ElementInstance> elements = new ArrayList<>();
        
        for (int i = 0; i < elementsList.size(); i++) {
            CompoundTag elementTag = elementsList.getCompound(i);
            ElementInstance element = ElementInstance.fromNBT(elementTag);
            if (element != null) {
                elements.add(element);
            }
        }
        
        return elements;
    }
    
    /**
     * 获取物品的所有生效元素属性
     */
    public static List<ElementInstance> getActiveElements(ItemStack stack) {
        List<ElementInstance> elements = getElements(stack);
        List<ElementInstance> activeElements = new ArrayList<>();
        
        for (ElementInstance element : elements) {
            if (element.isActive()) {
                activeElements.add(element);
            }
        }
        
        return activeElements;
    }
    
    /**
     * 检查物品是否有元素属性
     */
    public static boolean hasElementAttributes(ItemStack stack) {
        return !getElements(stack).isEmpty();
    }
    
    /**
     * 移除物品的指定元素属性
     */
    public static void removeElement(ItemStack stack, int position) {
        List<ElementInstance> elements = getElements(stack);
        elements.removeIf(e -> e.getPosition() == position);
        setElements(stack, elements);
    }
    
    /**
     * 应用元素属性到指定的装备槽位
     * @param stack 物品堆
     * @param slot 装备槽位
     * @param addModifier 属性修饰符添加器
     */
    public static void applyElementAttributes(ItemStack stack, EquipmentSlot slot, 
                                             java.util.function.BiConsumer<Attribute, AttributeModifier> addModifier) {
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            List<ElementInstance> elements = getActiveElements(stack);
            for (ElementInstance element : elements) {
                ElementAttribute attribute = element.getType().getAttribute();
                if (attribute != null && attribute.canApplyTo(stack)) {
                    AttributeModifier modifier = attribute.createModifier(stack, element.getValue());
                    addModifier.accept(Attribute.ATTACK_DAMAGE, modifier);
                }
            }
        }
    }
    
    /**
     * 检查物品是否支持元素属性
     */
    public static boolean canApplyElements(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 检查是否为防具
        Item item = stack.getItem();
        String itemName = item.toString().toLowerCase();
        
        if (itemName.contains("helmet") || itemName.contains("chestplate") || 
            itemName.contains("leggings") || itemName.contains("boots") ||
            itemName.contains("armor")) {
            return false;
        }
        
        // 首先检查是否为原版武器或工具（最可靠的判断方式）
        if (item instanceof SwordItem || item instanceof DiggerItem || 
            item instanceof BowItem || item instanceof CrossbowItem || 
            item instanceof TridentItem) {
            return true;
        }
        
        // 根据Minecraft和Forge的标准标签检查物品名称
        // 检查是否包含工具或武器的关键字，这些是Minecraft和Forge共有的标准命名
        if (itemName.contains("_sword") || itemName.contains("_pickaxe") || 
            itemName.contains("_axe") || itemName.contains("_shovel") || 
            itemName.contains("_hoe") || itemName.contains("_bow") ||
            itemName.contains("_crossbow") || itemName.contains("trident")) {
            return true;
        }
        
        // 检查NBT标签确定是否为工具或武器
        // 在Minecraft中，工具和武器通常具有Damage标签
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            // 检查是否有AttributeModifiers标签（近战武器通常有）
            if (nbt.contains("AttributeModifiers", Tag.TAG_LIST)) {
                return true;
            }
            
            // 对于弓和弩这类远程武器，虽然不一定有AttributeModifiers，
            // 但它们仍然是武器，可以应用元素属性
            // 根据物品类名进一步判断
            String itemClassName = item.getClass().getName().toLowerCase();
            if (itemClassName.contains("sword") || itemClassName.contains("pickaxe") || 
                itemClassName.contains("axe") || itemClassName.contains("shovel") || 
                itemClassName.contains("hoe") || itemClassName.contains("bow") ||
                itemClassName.contains("crossbow") || itemClassName.contains("trident")) {
                return true;
            }
        }
        
        // 检查是否为枪械（TACZ）
        if (ModCompat.isGun(stack)) {
            return true;
        }
        
        // 检查是否为拔刀剑（SlashBlade）
        if (ModCompat.isSlashBlade(stack)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取武器的触发率
     * @param stack 武器物品
     * @return 触发率 (0.0 - 1.0)
     */
    public static double getTriggerChance(ItemStack stack) {
        return ElementNBTUtils.getTriggerChance(stack);
    }
    
    /**
     * 设置武器的触发率
     * @param stack 武器物品
     * @param triggerChance 触发率 (0.0 - 1.0)
     */
    public static void setTriggerChance(ItemStack stack, double triggerChance) {
        CompoundTag elementData = stack.getOrCreateTagElement(ELEMENT_DATA);
        elementData.putDouble(ElementNBTUtils.TRIGGER_CHANCE, triggerChance);
    }
    
    /**
     * 获取武器的暴击率
     * @param stack 武器物品
     * @return 暴击率 (0.0 - 1.0)
     */
    public static double getCriticalChance(ItemStack stack) {
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        if (elementData != null && elementData.contains(CRITICAL_CHANCE, Tag.TAG_ANY_NUMERIC)) {
            return elementData.getDouble(CRITICAL_CHANCE);
        }
        return 0.0; // 默认无暴击率
    }
    
    /**
     * 设置武器的暴击率
     * @param stack 武器物品
     * @param criticalChance 暴击率 (0.0 - 1.0)
     */
    public static void setCriticalChance(ItemStack stack, double criticalChance) {
        CompoundTag elementData = stack.getOrCreateTagElement(ELEMENT_DATA);
        elementData.putDouble(CRITICAL_CHANCE, criticalChance);
    }
    
    /**
     * 获取武器的暴击伤害
     * @param stack 武器物品
     * @return 暴击伤害倍数
     */
    public static double getCriticalDamage(ItemStack stack) {
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        if (elementData != null && elementData.contains(CRITICAL_DAMAGE, Tag.TAG_ANY_NUMERIC)) {
            return elementData.getDouble(CRITICAL_DAMAGE);
        }
        return 0.0; // 默认无暴击伤害加成
    }
    
    /**
     * 设置武器的暴击伤害
     * @param stack 武器物品
     * @param criticalDamage 暴击伤害倍数
     */
    public static void setCriticalDamage(ItemStack stack, double criticalDamage) {
        CompoundTag elementData = stack.getOrCreateTagElement(ELEMENT_DATA);
        elementData.putDouble(CRITICAL_DAMAGE, criticalDamage);
    }
}