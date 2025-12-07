package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.ElementConfig;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * 元素属性工具类，提供元素属性的常用操作方法
 * 参考Apotheosis的AffixHelper实现
 */
public class ElementHelper {
    
    public static final String ELEMENT_DATA = "element_data";
    public static final String ELEMENTS = "elements";
    
    /**
     * 添加元素属性到物品
     */
    public static void addElementAttribute(ItemStack stack, ElementInstance element) {
        Map<ElementType, ElementInstance> elements = getElementAttributes(stack);
        elements.put(element.getType(), element);
        setElementAttributes(stack, elements);
    }
    
    /**
     * 设置物品的所有元素属性
     */
    public static void setElementAttributes(ItemStack stack, Map<ElementType, ElementInstance> elements) {
        CompoundTag elementData = stack.getOrCreateTagElement(ELEMENT_DATA);
        CompoundTag elementsTag = new CompoundTag();
        
        for (Map.Entry<ElementType, ElementInstance> entry : elements.entrySet()) {
            elementsTag.put(entry.getKey().getName(), entry.getValue().toNBT());
        }
        
        elementData.put(ELEMENTS, elementsTag);
    }
    
    /**
     * 获取物品的所有元素属性
     */
    public static Map<ElementType, ElementInstance> getElementAttributes(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<ElementType, ElementInstance> elements = new HashMap<>();
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        
        if (elementData != null && elementData.contains(ELEMENTS, Tag.TAG_COMPOUND)) {
            CompoundTag elementsTag = elementData.getCompound(ELEMENTS);
            
            for (String key : elementsTag.getAllKeys()) {
                ElementType type = ElementType.byName(key);
                if (type != null) {
                    ElementInstance element = ElementInstance.fromNBT(elementsTag.getCompound(key));
                    if (element != null) {
                        elements.put(type, element);
                    }
                }
            }
        }
        
        return elements;
    }
    
    /**
     * 检查物品是否有元素属性
     */
    public static boolean hasElementAttributes(ItemStack stack) {
        CompoundTag elementData = stack.getTagElement(ELEMENT_DATA);
        return elementData != null && !elementData.getCompound(ELEMENTS).isEmpty();
    }
    
    /**
     * 移除物品的指定元素属性
     */
    public static void removeElementAttribute(ItemStack stack, ElementType type) {
        Map<ElementType, ElementInstance> elements = getElementAttributes(stack);
        elements.remove(type);
        setElementAttributes(stack, elements);
    }
    
    /**
     * 应用元素属性到指定的装备槽位
     * @param stack 物品堆
     * @param slot 装备槽位
     * @param addModifier 属性修饰符添加器
     */
    public static void applyElementAttributes(ItemStack stack, EquipmentSlot slot, java.util.function.BiConsumer<Attribute, AttributeModifier> addModifier) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            Map<ElementType, ElementInstance> elements = getElementAttributes(stack);
            for (ElementInstance element : elements.values()) {
                // 应用元素属性到物品上
                // 使用原生的ATTACK_DAMAGE属性作为基础属性
                addModifier.accept(Attributes.ATTACK_DAMAGE, element.attribute().createModifier(stack, element.value()));
            }
        }
    }

    /**
     * 检查物品是否可以应用元素属性
     * 支持原版武器、工具、枪械和拔刀剑
     */
    public static boolean canApplyElementAttributes(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 检查是否为防具（不包括防具）
        Item item = stack.getItem();
        String itemName = item.toString().toLowerCase();
        
        // 检查是否为防具
        if (itemName.contains("helmet") || itemName.contains("chestplate") || 
            itemName.contains("leggings") || itemName.contains("boots") ||
            itemName.contains("armor")) {
            return false;
        }
        
        // 支持原版武器和工具
        if (isVanillaWeaponOrTool(stack)) {
            return true;
        }
        
        // 支持枪械（TACZ）
        if (isGun(stack)) {
            return true;
        }
        
        // 支持拔刀剑（SlashBlade）
        if (isSlashBlade(stack)) {
            return true;
        }
        
        // 默认不允许，只针对明确的武器和工具
        return false;
    }
    
    /**
     * 检查物品是否可以应用元素属性
     * 支持原版武器、工具、枪械和拔刀剑
     */
    public static boolean canApplyElementAttributes(ItemStack stack, ElementType type) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 检查是否为防具（不包括防具）
        Item item = stack.getItem();
        String itemName = item.toString().toLowerCase();
        
        // 检查是否为防具
        if (itemName.contains("helmet") || itemName.contains("chestplate") || 
            itemName.contains("leggings") || itemName.contains("boots") ||
            itemName.contains("armor")) {
            return false;
        }
        
        // 支持原版武器和工具
        if (isVanillaWeaponOrTool(stack)) {
            return true;
        }
        
        // 支持枪械（TACZ）
        if (isGun(stack)) {
            return true;
        }
        
        // 支持拔刀剑（SlashBlade）
        if (isSlashBlade(stack)) {
            return true;
        }
        
        // 默认允许
        return true;
    }
    
    /**
     * 检查是否为武器或工具
     */
    public static boolean isWeaponOrTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        Item item = stack.getItem();
        String itemName = item.toString().toLowerCase();
        
        // 检查是否为方块物品（排除大部分方块）
        if (item.getClass().getSimpleName().endsWith("BlockItem")) {
            return false;
        }
        
        // 检查是否为防具（不包括防具）
        if (itemName.contains("helmet") || itemName.contains("chestplate") || 
            itemName.contains("leggings") || itemName.contains("boots") ||
            itemName.contains("armor")) {
            return false;
        }
        
        // 支持原版武器和工具
        if (isVanillaWeaponOrTool(stack)) {
            return true;
        }
        
        // 支持枪械（TACZ）
        if (isGun(stack)) {
            return true;
        }
        
        // 支持拔刀剑（SlashBlade）
        if (isSlashBlade(stack)) {
            return true;
        }
        
        // 默认不允许，只针对明确的武器和工具
        return false;
    }
    
    /**
     * 检查是否为原版武器或工具
     */
    private static boolean isVanillaWeaponOrTool(ItemStack stack) {
        Item item = stack.getItem();
        String itemName = item.toString().toLowerCase();
        
        // 检查物品的攻击伤害属性
        return item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE) ||
               itemName.contains("sword") || itemName.contains("axe") || 
               itemName.contains("pickaxe") || itemName.contains("shovel") ||
               itemName.contains("hoe") || itemName.contains("bow") || 
               itemName.contains("crossbow") || itemName.contains("trident") ||
               itemName.contains("shield");
    }
    
    /**
     * 检查是否为枪械（TACZ）
     */
    private static boolean isGun(ItemStack stack) {
        // 使用反射方式检查是否为TACZ枪械，避免直接依赖
        try {
            Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            return iGunClass.getMethod("getIGunOrNull", ItemStack.class).invoke(null, stack) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否为拔刀剑
     */
    private static boolean isSlashBlade(ItemStack stack) {
        // 使用反射方式检查是否为拔刀剑，避免直接依赖
        try {
            Class<?> slashBladeClass = Class.forName("mods.flammpfeil.slashblade.item.ItemSlashBlade");
            return slashBladeClass.isInstance(stack.getItem());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取物品的基础攻击伤害
     */
    public static double getBaseAttackDamage(ItemStack stack) {
        Item item = stack.getItem();
        var attributes = item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        var damageAttributes = attributes.get(Attributes.ATTACK_DAMAGE);
        if (damageAttributes != null && !damageAttributes.isEmpty()) {
            // 获取第一个攻击伤害修饰符的值
            AttributeModifier modifier = damageAttributes.iterator().next();
            return modifier.getAmount();
        }

        // 对于TACZ枪械，需要特殊处理
        if (isGun(stack)) {
            // 使用反射方式获取TACZ枪械的伤害值
            try {
                Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
                Object iGun = iGunClass.getMethod("getIGunOrNull", ItemStack.class).invoke(null, stack);
                if (iGun != null) {
                    // 获取枪械ID
                    ResourceLocation gunId = (ResourceLocation) iGunClass.getMethod("getGunId", ItemStack.class).invoke(iGun, stack);
                    // 通过TimelessAPI获取枪械数据
                    Class<?> timelessAPIClass = Class.forName("com.tacz.guns.api.TimelessAPI");
                    Optional<?> gunIndexOptional = (Optional<?>) timelessAPIClass.getMethod("getCommonGunIndex", ResourceLocation.class).invoke(null, gunId);
                    if (gunIndexOptional.isPresent()) {
                        Object gunIndex = gunIndexOptional.get();
                        Object gunData = gunIndex.getClass().getMethod("getGunData").invoke(gunIndex);
                        Object bulletData = gunData.getClass().getMethod("getBulletData").invoke(gunData);
                        return (double) (Float) bulletData.getClass().getMethod("getDamageAmount").invoke(bulletData);
                    }
                }
            } catch (Exception e) {
                // 忽略异常，返回默认值
                return 1.0;
            }
        }

        // 对于拔刀剑，也需要特殊处理
        if (isSlashBlade(stack)) {
            // 使用反射方式获取拔刀剑的基础攻击修饰符
            try {
                // 通过Capability获取拔刀剑的状态信息
                Class<?> slashBladeClass = Class.forName("mods.flammpfeil.slashblade.item.ItemSlashBlade");
                Class<?> iSlashBladeStateClass = Class.forName("mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState");
                
                // 获取BLADESTATE字段
                Field bladeStateField = slashBladeClass.getDeclaredField("BLADESTATE");
                Object capability = bladeStateField.get(null);
                
                // 获取Capability
                Method getCapabilityMethod = ItemStack.class.getMethod("getCapability", Capability.class);
                Object lazyOptional = getCapabilityMethod.invoke(stack, capability);
                
                // 检查Capability是否存在
                Method isPresentMethod = lazyOptional.getClass().getMethod("isPresent");
                Boolean isPresent = (Boolean) isPresentMethod.invoke(lazyOptional);
                
                if (isPresent) {
                    // 获取Capability中的状态
                    Method resolveMethod = lazyOptional.getClass().getMethod("resolve");
                    Optional<?> stateOptional = (Optional<?>) resolveMethod.invoke(lazyOptional);
                    
                    if (stateOptional.isPresent()) {
                        Object state = stateOptional.get();
                        
                        // 获取基础攻击修饰符
                        Method getBaseAttackModifierMethod = iSlashBladeStateClass.getMethod("getBaseAttackModifier");
                        Float baseAttackModifier = (Float) getBaseAttackModifierMethod.invoke(state);
                        
                        // 获取锻造值
                        Method getRefineMethod = iSlashBladeStateClass.getMethod("getRefine");
                        Integer refine = (Integer) getRefineMethod.invoke(state);
                        
                        // 获取是否损坏状态
                        Method isBrokenMethod = iSlashBladeStateClass.getMethod("isBroken");
                        Boolean isBroken = (Boolean) isBrokenMethod.invoke(state);
                        
                        // 计算最终伤害值
                        float damage = baseAttackModifier;
                        
                        // 如果未损坏，计算锻造加成
                        if (!isBroken) {
                            // 获取剑的类型来判断是否是FIERCEREDGE类型
                            Class<?> swordTypeClass = Class.forName("mods.flammpfeil.slashblade.item.SwordType");
                            Method fromMethod = swordTypeClass.getMethod("from", ItemStack.class);
                            Object swordTypeEnumSet = fromMethod.invoke(null, stack);
                            
                            // 检查是否包含FIERCEREDGE类型
                            Method containsMethod = swordTypeEnumSet.getClass().getMethod("contains", Enum.class);
                            Class<?> fiercesEdgeClass = Class.forName("mods.flammpfeil.slashblade.item.SwordType");
                            Object fiercesEdgeEnum = Enum.valueOf((Class<Enum>) fiercesEdgeClass, "FIERCEREDGE");
                            boolean isFierceEdge = (Boolean) containsMethod.invoke(swordTypeEnumSet, fiercesEdgeEnum);
                            
                            // 锻造伤害面板增加计算，非线性，收益递减。(理论最大值为额外100%基础攻击)
                            float refineFactor = isFierceEdge ? 0.1F : 0.05F;
                            float attackAmplifier = (1.0F - (1.0F / (1.0F + (refineFactor * refine)))) * baseAttackModifier;
                            damage += attackAmplifier;
                        } else {
                            // 损坏的刀剑有-0.5伤害惩罚
                            damage -= 0.5F;
                        }
                        
                        // 减去1（因为Minecraft的伤害计算方式）
                        return damage - 1.0F;
                    }
                }
            } catch (Exception e) {
                // 忽略异常，返回默认值
                return 1.0;
            }
        }

        // 默认返回0
        return 0.0;
    }
    
    /**
     * 格式化元素属性的显示文本
     */
    public static String formatElementText(ElementInstance element) {
        return element.getType().getColoredName().getString() + " " + element.attribute().formatValue(element.value());
    }
    
    /**
     * 获取武器的总伤害值（所有元素伤害值相加）
     * @param stack 武器物品
     * @return 总伤害值
     */
    public static double getTotalDamage(ItemStack stack) {
        return WeaponTriggerHandler.calculateTotalDamage(getElementAttributes(stack));
    }
    
    /**
     * 获取武器的触发率
     * @param stack 武器物品
     * @return 触发率 (0.0 - 1.0)
     */
    public static double getTriggerChance(ItemStack stack) {
        Map<ElementType, ElementInstance> elements = getElementAttributes(stack);
        return WeaponTriggerHandler.getWeaponTriggerChance(stack, elements);
    }
    
    /**
     * 获取武器各元素的触发概率
     * @param stack 武器物品
     * @return 元素类型及其触发概率的映射
     */
    public static Map<ElementType, Double> getElementTriggerProbabilities(ItemStack stack) {
        return WeaponTriggerHandler.getWeaponElementTriggerProbabilities(stack);
    }
    
    /**
     * 处理武器元素触发
     * @param stack 武器物品
     * @return 触发的元素类型列表，如果没有触发则返回空列表
     */
    public static List<ElementType> processWeaponTrigger(ItemStack stack) {
        return WeaponTriggerHandler.processWeaponTrigger(stack);
    }
    
    /**
     * 设置武器的触发率
     * @param stack 武器物品
     * @param triggerChance 触发率 (0.0 - 1.0)
     */
    public static void setTriggerChance(ItemStack stack, double triggerChance) {
        WeaponTriggerHandler.setWeaponTriggerChance(stack, triggerChance);
    }
}