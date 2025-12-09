package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.util.DebugLogger;
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
            DebugLogger.log("拔刀剑模组未加载，跳过数据加载");
            return;
        }
        
        try {
            DebugLogger.log("开始加载拔刀剑数据...");
            
            // 清空旧数据
            slashBladeIDs.clear();
            slashBladeTranslationKeys.clear();
            
            // 方法1: 通过注册表获取拔刀剑物品（兼容性更好）
            loadSlashBladeFromRegistry();
            
            // 方法2: 如果方法1没有获取到数据，尝试通过拔刀剑API获取
            if (slashBladeIDs.isEmpty()) {
                loadSlashBladeFromAPI(server);
            }
            
            if (!slashBladeIDs.isEmpty()) {
                DebugLogger.log("成功加载 %d 个拔刀剑ID", slashBladeIDs.size());
                DebugLogger.log("成功加载 %d 个拔刀剑translationKey", slashBladeTranslationKeys.size());
                
                // 输出所有注册的拔刀剑到日志
                for (ResourceLocation id : slashBladeIDs) {
                    DebugLogger.log("发现拔刀剑ID: %s", id.toString());
                }
            } else {
                DebugLogger.warn("未能加载任何拔刀剑数据");
            }
        } catch (Exception e) {
            DebugLogger.error("加载拔刀剑数据时出错: %s", e.toString());
            e.printStackTrace();
            slashBladeIDs.clear();
            slashBladeTranslationKeys.clear();
        }
    }
    
    /**
     * 通过物品注册表获取拔刀剑数据
     * 这种方法更加稳定，不依赖拔刀剑的具体API
     */
    private static void loadSlashBladeFromRegistry() {
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
            if (registryName != null && registryName.getNamespace().equals(SLASHBLADE_MOD_ID)) {
                slashBladeIDs.add(registryName);
                
                // 使用Minecraft原版提供的Util方法获取translationKey
                try {
                    String translationKey = item.getDescriptionId();
                    if (translationKey != null && !translationKey.isEmpty()) {
                        slashBladeTranslationKeys.add(translationKey);
                    }
                } catch (Exception e) {
                    DebugLogger.warn("获取拔刀剑 %s 的translationKey失败: %s", registryName, e.getMessage());
                }
            }
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
                
                // 尝试获取对应的translationKey
                try {
                    // 通过物品注册表找到对应的物品并获取translationKey
                    for (Item item : ForgeRegistries.ITEMS) {
                        ResourceLocation itemName = ForgeRegistries.ITEMS.getKey(item);
                        if (itemName != null && itemName.equals(id)) {
                            String translationKey = item.getDescriptionId();
                            if (translationKey != null && !translationKey.isEmpty()) {
                                slashBladeTranslationKeys.add(translationKey);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    DebugLogger.warn("获取拔刀剑 %s 的translationKey失败: %s", id, e.getMessage());
                }
            }
        } catch (Exception e) {
            DebugLogger.warn("通过拔刀剑API获取数据失败: %s", e.getMessage());
        }
    }
    
    /**
     * 设置拔刀剑ID（供外部调用设置）
     * @param ids 拔刀剑ID集合
     */
    public static void setSlashBladeIDs(Set<ResourceLocation> ids) {
        init(); // 确保兼容性检查已完成
        if (!isSlashBladeLoaded) {
            DebugLogger.warn("拔刀剑模组未加载，设置ID将被忽略");
            return;
        }
        
        slashBladeIDs.clear();
        if (ids != null) {
            slashBladeIDs.addAll(ids);
            
            // 同步更新translationKey
            updateTranslationKeysFromIDs();
        }
        DebugLogger.log("设置拔刀剑ID完成，共 %d 个", slashBladeIDs.size());
    }
    
    /**
     * 根据ID更新translationKey
     */
    private static void updateTranslationKeysFromIDs() {
        slashBladeTranslationKeys.clear();
        for (ResourceLocation id : slashBladeIDs) {
            try {
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item != null) {
                    String translationKey = item.getDescriptionId();
                    if (translationKey != null && !translationKey.isEmpty()) {
                        slashBladeTranslationKeys.add(translationKey);
                    }
                }
            } catch (Exception e) {
                DebugLogger.warn("更新拔刀剑 %s 的translationKey失败: %s", id, e.getMessage());
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
        DebugLogger.log("清空拔刀剑缓存数据");
    }
}