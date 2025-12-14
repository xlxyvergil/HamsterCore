package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.config.WeaponItemIds;
import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElementEnchantment extends Enchantment {
    protected final ElementType elementType;
    protected final int maxLevel;
    protected final String enchantmentId;
    // 移除了硬编码的elementModifierId字段

    public ElementEnchantment(Rarity rarity, ElementType elementType, int maxLevel, String enchantmentId) {
        super(rarity, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.elementType = elementType;
        this.maxLevel = maxLevel;
        this.enchantmentId = enchantmentId;
        // 移除了硬编码的elementModifierId字段
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
        // 使用WeaponItemIds来判断物品是否可以应用元素附�?
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return WeaponItemIds.isConfiguredWeapon(itemKey);
    }
    
    @Override
    public boolean isDiscoverable() {
        // 确保可以在附魔台发现
        return true;
    }
    
    @Override
    public boolean isTradeable() {
        // 确保可以在交易中获得
        return true;
    }
    
    @Override
    public boolean isTreasureOnly() {
        // 不设置为宝藏附魔，以便更容易获得
        return false;
    }
    
    public Collection<AttributeModifier> getEntityAttributes(ItemStack stack, EquipmentSlot slot, int level) {
        // 基类提供默认实现，子类可以重写以自定义数值计算
        if (slot == EquipmentSlot.MAINHAND && this.elementType != null) {
            // 获取元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(this.elementType);
            if (elementAttribute != null) {
                // 创建默认的属性修饰符（基类的默认实现）
                double value = elementAttribute.getDefaultValue() * level;
                UUID modifierId = ElementUUIDManager.getOrCreateUUID(stack, this.elementType, level);
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
    
}
