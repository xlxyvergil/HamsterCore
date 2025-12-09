package com.xlxyvergil.hamstercore.util;

import com.tacz.guns.api.TimelessAPI;
import com.xlxyvergil.hamstercore.util.DebugLogger;
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
                DebugLogger.log("开始加载TACZ枪械ID...");
                loadTacZGunIDsDirectAPI();
                if (tacZGunIDs != null && !tacZGunIDs.isEmpty()) {
                    DebugLogger.log("成功加载 %d 个TACZ枪械ID", tacZGunIDs.size());
                    // 输出所有注册的枪械到日志
                    DebugLogger.log("TACZ枪械列表:");
                    for (ResourceLocation id : tacZGunIDs) {
                        DebugLogger.log("  - %s", id.toString());
                    }
                } else {
                    DebugLogger.log("未能加载TACZ枪械ID");
                }
            } else {
                tacZGunIDs = Collections.emptySet();
                DebugLogger.log("TACZ模组未加载，跳过枪械ID加载");
            }
        } catch (Exception e) {
            tacZGunIDs = Collections.emptySet();
            DebugLogger.log("加载TACZ枪械ID时出错: %s", e.toString());
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
        
        DebugLogger.log("TACZ getAllCommonGunIndex 返回的所有数据数量: %d", gunEntries.size());
        
        int validGunCount = 0;
        for (Map.Entry<ResourceLocation, ?> entry : gunEntries) {
            ResourceLocation gunId = entry.getKey();
            
            // 验证枪械ID是否有效（使用原版反射逻辑）
            if (TimelessAPI.getCommonGunIndex(gunId).isPresent()) {
                // 添加所有有效的枪械ID，包括其他枪械包的枪械
                tacZGunIDs.add(gunId);
                validGunCount++;
                DebugLogger.log("发现有效TACZ枪械ID: %s", gunId.toString());
            } else {
                DebugLogger.log("发现无效TACZ枪械ID: %s", gunId.toString());
            }
        }
        
        DebugLogger.log("TACZ有效枪械总数: %d", validGunCount);
    }
}