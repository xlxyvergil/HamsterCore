package com.xlxyvergil.hamstercore.config;

import net.minecraft.resources.ResourceLocation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 武器道具ID管理器
 * 统一缓存来自4个配置应用器的所有武器道具ID
 * 附魔系统通过此集合确认哪些物品可以使用附魔
 * 
 * 使用示例：
 * // 1. 服务器启动时初始化（已自动调用）
 * WeaponItemIds.initializeAllWeaponIds();
 * 
 * // 2. 检查物品是否为已配置的武器
 * ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
 * boolean isWeapon = WeaponItemIds.isConfiguredWeapon(itemKey);
 * 
 * // 3. 获取所有已配置的武器ID
 * Set<ResourceLocation> allWeapons = WeaponItemIds.getAllWeaponItemIds();
 */
public class WeaponItemIds {
    
    // 存储所有武器道具ID的集合
    private static final Set<ResourceLocation> WEAPON_ITEM_IDS = new HashSet<>();
    
    // 标记是否已完成缓存初始化
    private static boolean initialized = false;
    
    /**
     * 初始化所有配置应用器的武器ID缓存
     * 此方法应在服务器启动时调用一次，收集所有应用器的武器ID
     */
    public static void initializeAllWeaponIds() {
        if (initialized) {
            return;
        }
        
        // 清空现有缓存
        clearAllWeaponItemIds();
        
        // 收集各应用器的武器ID
        Set<ResourceLocation> allIds = new HashSet<>();
        
        // 1. 收集普通配置应用器的武器ID
        allIds.addAll(collectNormalConfigApplierIds());
        
        // 2. 收集额外配置应用器的武器ID
        allIds.addAll(collectAdditionalConfigApplierIds());
        
        // 3. 收集SlashBlade配置应用器的武器ID
        allIds.addAll(collectSlashBladeConfigApplierIds());
        
        // 4. 收集TacZ配置应用器的武器ID
        allIds.addAll(collectTacZConfigApplierIds());
        
        // 批量添加到缓存
        WEAPON_ITEM_IDS.addAll(allIds);
        initialized = true;
    }
    
    /**
     * 收集普通配置应用器的武器ID
     */
    private static Set<ResourceLocation> collectNormalConfigApplierIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        // 这里应该调用NormalConfigApplier的方法来获取其武器ID
        // 由于当前架构，我们需要通过配置文件或缓存来获取
        try {
            // 获取所有普通武器配置
            var weaponConfigs = com.xlxyvergil.hamstercore.config.WeaponConfig.getAllWeaponConfigs();
            if (weaponConfigs != null) {
                for (ResourceLocation itemKey : weaponConfigs.keySet()) {
                    // 过滤掉MOD特殊物品
                    if (!isModSpecialItem(itemKey)) {
                        ids.add(itemKey);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }
    
    /**
     * 收集额外配置应用器的武器ID
     */
    private static Set<ResourceLocation> collectAdditionalConfigApplierIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        try {
            // 获取所有额外武器配置
            var additionalConfigs = com.xlxyvergil.hamstercore.config.WeaponConfig.getAdditionalWeaponConfigs();
            if (additionalConfigs != null) {
                ids.addAll(additionalConfigs.keySet());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }
    
    /**
     * 收集SlashBlade配置应用器的武器ID
     */
    private static Set<ResourceLocation> collectSlashBladeConfigApplierIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        try {
            // 检查拔刀剑模组是否已加载
            if (com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher.isSlashBladeLoaded()) {
                // 拔刀剑使用固定的物品ID "slashblade:slashblade"
                ids.add(new ResourceLocation("slashblade:slashblade"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }
    
    /**
     * 收集TacZ配置应用器的武器ID
     */
    private static Set<ResourceLocation> collectTacZConfigApplierIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        try {
            // 检查TACZ模组是否已加载
            if (net.minecraftforge.fml.ModList.get().isLoaded("tacz")) {
                // 获取所有TACZ枪械ID
                Set<ResourceLocation> gunIds = com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher.getTacZGunIDs();
                if (gunIds != null) {
                    ids.addAll(gunIds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }
    
    /**
     * 判断是否为MOD特殊物品
     */
    private static boolean isModSpecialItem(ResourceLocation itemKey) {
        String itemKeyStr = itemKey.toString();
        
        // 检查是否为TACZ枪械
        if ("tacz:modern_kinetic_gun".equals(itemKeyStr)) {
            return true;
        }
        
        // 检查是否为拔刀剑
        if ("slashblade:slashblade".equals(itemKeyStr)) {
            return true;
        }
        
        return false;
    }
    
    
    
    /**
     * 重新初始化缓存
     * 在配置重新加载时调用
     */
    public static void reinitialize() {
        initialized = false;
        initializeAllWeaponIds();
    }
    
    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 获取所有武器道具ID的不可变视图
     * @return 所有武器道具ID的集合
     */
    public static Set<ResourceLocation> getAllWeaponItemIds() {
        return Collections.unmodifiableSet(WEAPON_ITEM_IDS);
    }
    
    /**
     * 检查指定的道具ID是否为已配置的武器
     * @param itemKey 道具的ResourceLocation
     * @return 是否为已配置的武器
     */
    public static boolean isConfiguredWeapon(ResourceLocation itemKey) {
        return itemKey != null && WEAPON_ITEM_IDS.contains(itemKey);
    }
    
    /**
     * 清空所有武器道具ID（通常在重新加载配置时使用）
     */
    public static void clearAllWeaponItemIds() {
        WEAPON_ITEM_IDS.clear();
    }
}