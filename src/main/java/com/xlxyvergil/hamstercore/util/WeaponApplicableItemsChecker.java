package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.compat.ModCompat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;

/**
 * 武器适用性检查工具类
 * 用于确定哪些物品可以应用元素属性
 */
public class WeaponApplicableItemsChecker {
    
    /**
     * 检查物品是否可以应用元素属性
     * @param stack 物品堆
     * @return 是否可以应用元素属性
     */
    public static boolean canApplyElements(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 检查是否为TACZ枪械
        if (ModCompat.isTACZLoaded() && ModCompat.isGun(stack)) {
            return true;
        }
        
        // 检查是否为SlashBlade拔刀剑
        if (ModCompat.isSlashBladeLoaded() && ModCompat.isSlashBlade(stack)) {
            return true;
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
        
        return false;
    }
}