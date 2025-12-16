package com.xlxyvergil.hamstercore.util;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MOD特殊物品ID获取器
 * 基于ck目录中其他模组的最佳实践，使用ModList方式而不是反射
 * 直接调用TimelessAPI获取TACZ枪械ID，TACZ没有translationKey
 */
public class ModSpecialItemsFetcher {
    
    private static boolean tacZChecked = false;
    private static Set<ResourceLocation> tacZGunIDs = null;
    
    // 缓存已配置的TACZ枪械物品堆
    private static final Map<ResourceLocation, ItemStack> taczGunStacks = new ConcurrentHashMap<>();
    
    /**
     * 检查TACZ模组是否已加载
     * @return 如果TACZ模组已加载返回true
     */
    public static boolean isTaczLoaded() {
        return ModList.get().isLoaded("tacz");
    }
    
    /**
     * 获取所有TACZ枪械ID
     * @return TACZ枪械ID集合
     */
    public static Set<ResourceLocation> getTacZGunIDs() {
        if (!tacZChecked) {
            loadTacZGunIDs();
            tacZChecked = true;
        }
        return tacZGunIDs != null ? new HashSet<>(tacZGunIDs) : Collections.emptySet();
    }
    
    /**
     * 清除缓存，强制重新加载
     */
    public static void clearCache() {
        tacZChecked = false;
        tacZGunIDs = null;
    }
    
    /**
     * 加载TACZ枪械ID
     */
    private static void loadTacZGunIDs() {
        try {
            if (ModList.get().isLoaded("tacz")) {
                loadTacZGunIDsDirectAPI();
                if (tacZGunIDs != null && !tacZGunIDs.isEmpty()) {
                } else {
                }
            } else {
                tacZGunIDs = Collections.emptySet();
            }
        } catch (Exception e) {
            tacZGunIDs = Collections.emptySet();
            e.printStackTrace();
        }
    }
    
    /**
     * 通过直接API调用加载TACZ枪械ID
     * 基于ck目录中其他模组的最佳实践，避免反射
     */
    private static void loadTacZGunIDsDirectAPI() {
        // 直接调用TimelessAPI.getAllCommonGunIndex()，不使用反射
        // 修复类型不匹配问题：使用 Map.Entry<ResourceLocation, ?> 而不是具体的 CommonGunIndex 类型
        try {
            @SuppressWarnings("unchecked")
            Set<Map.Entry<ResourceLocation, ?>> gunEntries = (Set<Map.Entry<ResourceLocation, ?>>) (Object) TimelessAPI.getAllCommonGunIndex();
            tacZGunIDs = new HashSet<>();
            
            for (Map.Entry<ResourceLocation, ?> entry : gunEntries) {
                ResourceLocation gunId = entry.getKey();
                
                // 验证枪械ID是否有效（直接调用API）
                if (TimelessAPI.getCommonGunIndex(gunId).isPresent()) {
                    // 添加所有有效的枪械ID，包括其他枪械包的枪械
                    tacZGunIDs.add(gunId);
                } else {
                }
            }
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明TACZ未正确加载
            tacZGunIDs = Collections.emptySet();
        } catch (Exception e) {
            tacZGunIDs = Collections.emptySet();
        }
    }
    
    /**
     * 检查物品是否为TACZ枪械
     * 直接调用TACZ的API
     * @param stack 物品堆
     * @return 如果是TACZ枪械返回true
     */
    public static boolean isTacZGun(ItemStack stack) {
        if (!ModList.get().isLoaded("tacz")) {
            return false;
        }
        
        try {
            // 直接调用TACZ的API
            return IGun.getIGunOrNull(stack) != null;
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明TACZ未正确加载
            return false;
        } catch (Exception e) {
            // API调用失败，返回false
            return false;
        }
    }
    
    /**
     * 获取TACZ枪械物品实例
     * @return TACZ枪械物品实例
     */
    public static Item getTaczGunItem() {
        // 检查TACZ模组是否已加载
        if (!isTaczLoaded()) {
            return null;
        }

        try {
            // 直接使用TACZ的API获取枪械物品
            // 注意：这里使用了编译时依赖，但在运行时只有当TACZ确实加载时才会执行此代码
            return com.tacz.guns.init.ModItems.MODERN_KINETIC_GUN.get();
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明TACZ未正确加载
            return null;
        } catch (Exception e) {
            // 其他异常
            return null;
        }
    }
    
    /**
     * 获取TACZ枪械的ID
     * @param stack 物品堆
     * @return 枪械ID，如果不是TACZ枪械则返回null
     */
    public static String getTacZGunId(ItemStack stack) {
        if (!isTacZGun(stack)) {
            return null;
        }
        
        try {
            IGun iGun = IGun.getIGunOrNull(stack);
            if (iGun != null) {
                ResourceLocation gunId = iGun.getGunId(stack);
                if (gunId != null && !gunId.toString().equals("tacz:empty")) {
                    return gunId.toString();
                }
            }
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明TACZ未正确加载
            return null;
        } catch (Exception e) {
            // API调用失败，返回null
        }
        return null;
    }
    
    /**
     * 缓存已配置的TACZ枪械物品堆到全局映射中
     * @param gunId 枪械ID
     * @param stack 配置好的物品堆
     */
    public static void cacheTaczGunStack(ResourceLocation gunId, ItemStack stack) {
        if (gunId != null && stack != null && !stack.isEmpty()) {
            taczGunStacks.put(gunId, stack);
        }
    }
    
    /**
     * 获取已配置的TACZ枪械物品堆
     * @param gunId 枪械ID
     * @return 对应的配置好的物品堆
     */
    public static ItemStack getTaczGunStack(ResourceLocation gunId) {
        return taczGunStacks.get(gunId);
    }
}