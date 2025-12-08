package com.xlxyvergil.hamstercore.element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 元素应用器，用于在服务器启动时将配置文件中的元素数据应用到武器上
 */
public class ElementApplier {
    
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String ELEMENT_CONFIG_FILE_NAME = "weapon_elements.json";
    
    /**
     * 从配置文件应用元素属性到武器
     */
    public static void applyElementsFromConfig() {
        HamsterCore.LOGGER.info("开始应用元素属性到武器...");
        
        // 获取武器配置
        WeaponConfig weaponConfig = WeaponConfig.getInstance();
        if (weaponConfig == null) {
            HamsterCore.LOGGER.warn("武器配置未初始化，跳过元素属性应用");
            return;
        }
        
        // 遍历所有已注册的物品并应用元素属性
        int appliedCount = 0;
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
            if (itemKey == null) continue;
            
            ItemStack stack = new ItemStack(item);
            
            // 检查物品是否可以应用元素属性
            if (ElementHelper.canApplyElements(stack)) {
                // 应用元素属性
                if (applyElementAttributesToItem(stack, weaponConfig)) {
                    appliedCount++;
                }
            }
        }
        
        HamsterCore.LOGGER.info("元素属性应用完成，共处理 {} 个物品", appliedCount);
    }
    
    /**
     * 为单个物品应用元素属性
     */
    private static boolean applyElementAttributesToItem(ItemStack stack, WeaponConfig weaponConfig) {
        WeaponConfig.WeaponData weaponData = weaponConfig.getWeaponConfig(stack);
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 设置基本属性
            ElementHelper.setCriticalChance(stack, weaponData.criticalChance);
            ElementHelper.setCriticalDamage(stack, weaponData.criticalDamage);
            ElementHelper.setTriggerChance(stack, weaponData.triggerChance);
            
            // 设置元素属性
            if (weaponData.elementRatios != null && !weaponData.elementRatios.isEmpty()) {
                // 创建元素实例列表
                int position = 0;
                for (Map.Entry<String, Double> entry : weaponData.elementRatios.entrySet()) {
                    ElementType elementType = ElementType.byName(entry.getKey());
                    if (elementType != null) {
                        ElementInstance elementInstance = new ElementInstance(
                            elementType,
                            entry.getValue(), // 元素值
                            position++,       // 位置
                            true             // 默认激活
                        );
                        
                        ElementHelper.addElement(stack, elementInstance);
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            HamsterCore.LOGGER.error("为物品 {} 应用元素属性时出错: {}", 
                ForgeRegistries.ITEMS.getKey(stack.getItem()), e.getMessage());
            return false;
        }
    }
}