package com.xlxyvergil.hamstercore.util;

import com.tacz.guns.api.TimelessAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MOD特殊物品ID获取器
 * 基于ck目录中其他模组的最佳实践，使用ModList方式而不是反射
 * 直接调用TimelessAPI获取TACZ枪械ID，TACZ没有translationKey
 */
public class ModSpecialItemsFetcher {
    
    private static boolean tacZChecked = false;
    private static Set<ResourceLocation> tacZGunIDs = null;
    
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
        @SuppressWarnings("unchecked")
        Set<Map.Entry<ResourceLocation, ?>> gunEntries = (Set<Map.Entry<ResourceLocation, ?>>) (Object) TimelessAPI.getAllCommonGunIndex();
        tacZGunIDs = new HashSet<>();
        
        
        for (Map.Entry<ResourceLocation, ?> entry : gunEntries) {
            ResourceLocation gunId = entry.getKey();
            
            // 验证枪械ID是否有效（使用原版反射逻辑）
            if (TimelessAPI.getCommonGunIndex(gunId).isPresent()) {
                // 添加所有有效的枪械ID，包括其他枪械包的枪械
                tacZGunIDs.add(gunId);
            } else {
            }
        }
        
    }
}