package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.modifier.ElementAttributeModifierEntry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElementEnchantment extends Enchantment {
    protected final ElementType elementType;
    protected final int maxLevel;
    protected final String enchantmentId;

    public ElementEnchantment(Rarity rarity, ElementType elementType, int maxLevel, String enchantmentId) {
        super(rarity, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.elementType = elementType;
        this.maxLevel = maxLevel;
        this.enchantmentId = enchantmentId;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    public ElementType getElementType() {
        return elementType;
    }
    
    public String getEnchantmentId() {
        return enchantmentId;
    }
    
    @Override
    public boolean canEnchant(ItemStack stack) {
        return true;
    }
    
    @Override
    public boolean isDiscoverable() {
        return true;
    }
    
    @Override
    public boolean isTradeable() {
        return true;
    }
    
    @Override
    public boolean isTreasureOnly() {
        return false;
    }
    
    public Collection<AttributeModifier> getEntityAttributes(ItemStack stack, EquipmentSlot slot, int level) {
        // 基类提供默认实现，子类可以重写以自定义数值计算
        if (slot == EquipmentSlot.MAINHAND && this.elementType != null) {
            // 获取元素属性注册对象
            var attributeRegistry = ElementRegistry.getAttribute(this.elementType);
            if (attributeRegistry != null && attributeRegistry.isPresent()) {
                // 获取实际的元素属性
                ElementAttribute elementAttribute = attributeRegistry.get();
                // 创建默认的属性修饰符（基类的默认实现）
                double value = elementAttribute.getDefaultValue() * level;
                UUID modifierId = ElementRegistry.getModifierUUID(this.elementType, level);
                // 确保元素类型名称不为null
                String elementName = this.elementType.getName() != null ? this.elementType.getName() : "unknown";
                AttributeModifier modifier = new AttributeModifier(
                    modifierId, 
                    "hamstercore:" + elementName, 
                    value, 
                    AttributeModifier.Operation.ADDITION
                );
                
                Collection<AttributeModifier> modifiers = new ArrayList<>();
                modifiers.add(modifier);
                return modifiers;
            }
        }
        
        return Collections.emptyList();
    }
    
    /**
     * 直接返回ElementAttributeModifierEntry格式的修饰符列表
     * 避免在EventHandler中进行额外的转换
     */
    public List<ElementAttributeModifierEntry> getElementAttributeModifiers(ItemStack stack, EquipmentSlot slot, int level) {
        List<ElementAttributeModifierEntry> modifiers = new ArrayList<>();
        
        if (slot == EquipmentSlot.MAINHAND && this.elementType != null) {
            // 获取元素属性注册对象
            var attributeRegistry = ElementRegistry.getAttribute(this.elementType);
            if (attributeRegistry != null && attributeRegistry.isPresent()) {
                // 获取实际的元素属性
                ElementAttribute elementAttribute = attributeRegistry.get();
                // 创建默认的属性修饰符
                double value = elementAttribute.getDefaultValue() * level;
                UUID modifierId = ElementRegistry.getModifierUUID(this.elementType, level);
                // 确保元素类型名称不为null
                String elementName = this.elementType.getName() != null ? this.elementType.getName() : "unknown";
                
                // 直接创建ElementAttributeModifierEntry
                ElementAttributeModifierEntry modifierEntry = new ElementAttributeModifierEntry(
                    this.elementType,
                    modifierId,
                    value,
                    "hamstercore:" + elementName,
                    AttributeModifier.Operation.ADDITION
                );
                
                modifiers.add(modifierEntry);
            }
        }
        
        return modifiers;
    }
}