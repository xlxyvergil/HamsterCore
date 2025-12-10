package com.xlxyvergil.hamstercore.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 拔刀剑物品获取器，专门负责获取拔刀剑的ID和translationKey
 * 基于ck目录中其他模组的最佳实践实现
 */
public class SlashBladeItemsFetcher {
    
    private static final String SLASHBLADE_MOD_ID = "slashblade";
    private static boolean isSlashBladeLoaded = false;
    private static boolean initialized = false;
    
    private static Set<ResourceLocation> slashBladeIDs = new HashSet<>();
    private static Set<String> slashBladeTranslationKeys = new HashSet<>();
    
    /**
     * 初始化兼容性检查
     * 应该在模组加载阶段调用
     */
    public static void init() {
        if (!initialized) {
            isSlashBladeLoaded = ModList.get().isLoaded(SLASHBLADE_MOD_ID);
            initialized = true;
            DebugLogger.log("拔刀剑兼容性检查完成: %s", isSlashBladeLoaded ? "已加载" : "未加载");
        }
    }
    
    /**
     * 检查拔刀剑模组是否已加载
     * @return 如果拔刀剑模组已加载返回true
     */
    public static boolean isSlashBladeLoaded() {
        if (!initialized) {
            init();
        }
        return isSlashBladeLoaded;
    }
    
    /**
     * 获取所有拔刀剑ID
     * @return 拔刀剑ID集合，如果模组未加载返回空集合
     */
    public static Set<ResourceLocation> getSlashBladeIDs() {
        if (!isSlashBladeLoaded()) {
            return Collections.emptySet();
        }
        return new HashSet<>(slashBladeIDs);
    }
    
    /**
     * 获取所有拔刀剑的translationKey
     * @return translationKey集合，如果模组未加载返回空集合
     */
    public static Set<String> getSlashBladeTranslationKeys() {
        if (!isSlashBladeLoaded()) {
            return Collections.emptySet();
        }
        return new HashSet<>(slashBladeTranslationKeys);
    }
    
    /**
     * 从MinecraftServer加载拔刀剑ID和translationKey
     * 推荐在服务器启动完成后的阶段调用
     * @param server MinecraftServer实例
     */
    public static Set<ResourceLocation> getSlashBladeIDs(MinecraftServer server) {
        loadSlashBladeData(server);
        return new HashSet<>(slashBladeIDs);
    }
    
    /**
     * 从MinecraftServer加载拔刀剑translationKey
     * @param server MinecraftServer实例
     */
    public static Set<String> getSlashBladeTranslationKeys(MinecraftServer server) {
        loadSlashBladeData(server);
        return new HashSet<>(slashBladeTranslationKeys);
    }
    
    /**
     * 从MinecraftServer加载拔刀剑数据
     * @param server MinecraftServer实例
     */
    private static void loadSlashBladeData(MinecraftServer server) {
        if (!isSlashBladeLoaded()) {
            return;
        }
        
        try {
            
            // 清空旧数据
            slashBladeIDs.clear();
            slashBladeTranslationKeys.clear();
        
            
            // 方法2: 总是通过拔刀剑API获取，以确保获取到所有刀剑定义
            loadSlashBladeFromAPI(server);
            
            if (!slashBladeIDs.isEmpty()) {
                // 输出获取到的拔刀剑ID数量和部分ID
                System.out.println("[HamsterCore] 获取到拔刀剑ID数量: " + slashBladeIDs.size());
                System.out.println("[HamsterCore] 获取到translationKey数量: " + slashBladeTranslationKeys.size());
                
                // 输出前5个ID用于调试
                int count = 0;
                for (ResourceLocation id : slashBladeIDs) {
                    if (count < 5) {
                        System.out.println("[HamsterCore] 拔刀剑ID示例: " + id);
                        count++;
                    }
                }
                
                // 输出前5个translationKey用于调试
                count = 0;
                for (String key : slashBladeTranslationKeys) {
                    if (count < 5) {
                        System.out.println("[HamsterCore] translationKey示例: " + key);
                        count++;
                    }
                }
            } else {
                System.out.println("[HamsterCore] 未获取到任何拔刀剑ID");
            }
        } catch (Exception e) {
            e.printStackTrace();
            slashBladeIDs.clear();
            slashBladeTranslationKeys.clear();
        }
    }
    
    /**
     * 通过拔刀剑API获取数据（备用方法）
     * @param server MinecraftServer实例
     */
    private static void loadSlashBladeFromAPI(MinecraftServer server) {
        try {
            var registryAccess = server.registryAccess();
            var slashBladeRegistry = registryAccess.lookupOrThrow(
                mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition.REGISTRY_KEY
            );
            
            // 遍历所有拔刀剑定义
            for (var entry : slashBladeRegistry.listElements().toList()) {
                ResourceLocation id = entry.key().location();
                slashBladeIDs.add(id);
                
                // 构建正确的translationKey
                try {
                    // 拔刀剑的translationKey格式是：item.slashblade.{刀剑名称}
                    String bladeName = id.getPath();
                    String translationKey = "item.slashblade." + bladeName;
                    slashBladeTranslationKeys.add(translationKey);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }
    
    /**
     * 设置拔刀剑ID（供外部调用设置）
     * @param ids 拔刀剑ID集合
     */
    public static void setSlashBladeIDs(Set<ResourceLocation> ids) {
        init(); // 确保兼容性检查已完成
        if (!isSlashBladeLoaded) {
            return;
        }
        
        slashBladeIDs.clear();
        if (ids != null) {
            slashBladeIDs.addAll(ids);
            
            // 同步更新translationKey
            updateTranslationKeysFromIDs();
        }
    }
    
    /**
     * 根据ID更新translationKey
     * 使用SlashBlade的registry直接构建translationKey，而不是通过Forge注册表
     */
    private static void updateTranslationKeysFromIDs() {
        slashBladeTranslationKeys.clear();
        for (ResourceLocation id : slashBladeIDs) {
            try {
                // 直接构建translationKey，格式为：item.slashblade.{刀剑名称}
                String bladeName = id.getPath();
                String translationKey = "item.slashblade." + bladeName;
                slashBladeTranslationKeys.add(translationKey);
                DebugLogger.log("构建translationKey: %s", translationKey);
            } catch (Exception e) {
                DebugLogger.log("构建translationKey失败: %s, 错误: %s", id, e.getMessage());
            }
        }
    }
    
    /**
     * 重新加载数据
     * @param server MinecraftServer实例
     */
    public static void reloadData(MinecraftServer server) {
        loadSlashBladeData(server);
    }
    
    /**
     * 清空缓存的数据
     */
    public static void clearData() {
        slashBladeIDs.clear();
        slashBladeTranslationKeys.clear();
    }
}