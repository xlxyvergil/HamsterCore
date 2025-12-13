package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.WeaponData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 武器数据NBT工具类
 * 处理WeaponData与ItemStack之间NBT数据的读写操作
 */
public class WeaponDataNBTUtil {
    
    // NBT标签键名
    public static final String ELEMENT_DATA_TAG = "element_data";
    public static final String ELEMENTS_TAG = "elements";
    
    /**
     * 将WeaponData写入ItemStack的NBT
     * @param stack 物品堆
     * @param weaponData 武器数据
     */
    public static void writeWeaponDataToNBT(ItemStack stack, WeaponData weaponData) {
        if (stack.isEmpty() || weaponData == null) {
            return;
        }
        
        // 获取或创建NBT标签
        CompoundTag stackTag = stack.getOrCreateTag();
        CompoundTag elementDataTag = new CompoundTag();
        
        // 将WeaponData转换为NBT并保存
        elementDataTag.put(ELEMENTS_TAG, weaponData.toNBT());
        stackTag.put(ELEMENT_DATA_TAG, elementDataTag);
        
        // 更新物品堆的NBT
        stack.setTag(stackTag);
    }
    
    /**
     * 从ItemStack的NBT中读取WeaponData
     * @param stack 物品堆
     * @return WeaponData对象，如果不存在则返回null
     */
    public static WeaponData readWeaponDataFromNBT(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return null;
        }
        
        CompoundTag stackTag = stack.getTag();
        if (stackTag == null || !stackTag.contains(ELEMENT_DATA_TAG)) {
            return null;
        }
        
        CompoundTag elementDataTag = stackTag.getCompound(ELEMENT_DATA_TAG);
        if (!elementDataTag.contains(ELEMENTS_TAG)) {
            return null;
        }
        
        CompoundTag elementsTag = elementDataTag.getCompound(ELEMENTS_TAG);
        return WeaponData.fromNBT(elementsTag);
    }
    
    /**
     * 检查ItemStack是否包含武器数据
     * @param stack 物品堆
     * @return 是否包含武器数据
     */
    public static boolean hasWeaponData(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        
        CompoundTag stackTag = stack.getTag();
        return stackTag != null && stackTag.contains(ELEMENT_DATA_TAG);
    }
    
    /**
     * 从ItemStack的NBT中清除武器数据
     * @param stack 物品堆
     */
    public static void clearWeaponDataFromNBT(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        
        CompoundTag stackTag = stack.getTag();
        if (stackTag != null && stackTag.contains(ELEMENT_DATA_TAG)) {
            stackTag.remove(ELEMENT_DATA_TAG);
            stack.setTag(stackTag);
        }
    }
}