package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.config.WeaponItemIds;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.fml.common.Mod;

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
        if (stack.isEmpty()) {
            return false;
        }
        
        // 获取物品的ResourceLocation
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        
        // 检查是否为已配置的武器
        return WeaponItemIds.isConfiguredWeapon(itemKey);
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
    
    /**
     * 获取该附魔对应的元素值
     * @param level 附魔等级
     * @return 元素值
     */
    public double getElementValue(int level) {
        // 根据等级计算元素值，这里使用简单的线性计算
        // 子类可以重写此方法以实现自定义的数值计算逻辑
        return 1.0 * level;
    }
    
    /**
     * 获取用于唯一标识此附魔的UUID
     * @param level 附魔等级
     * @return UUID
     */
    public UUID getEnchantmentUUID(int level) {
        // 使用附魔ID和等级生成唯一UUID
        return UUID.nameUUIDFromBytes((enchantmentId + ":" + level).getBytes());
    }
}