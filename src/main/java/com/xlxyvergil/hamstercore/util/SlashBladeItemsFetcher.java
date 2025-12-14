package com.xlxyvergil.hamstercore.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 拔刀剑物品获取器，专门负责获取拔刀剑的ID和translationKey
 * 基于TACZ的ModSpecialItemsFetcher实现方式
 */
public class SlashBladeItemsFetcher {
    
    private static final String SLASHBLADE_MOD_ID = "slashblade";
    private static boolean slashBladeChecked = false;
    private static Set<ResourceLocation> slashBladeIDs = null;
    private static Set<String> slashBladeTranslationKeys = null;
    
    /**
     * 检查拔刀剑模组是否已加载
     * @return 如果拔刀剑模组已加载返回true
     */
    public static boolean isSlashBladeLoaded() {
        return ModList.get().isLoaded(SLASHBLADE_MOD_ID);
    }
    
    /**
     * 获取所有拔刀剑ID
     * @return 拔刀剑ID集合
     */
    public static Set<ResourceLocation> getSlashBladeIDs() {
        if (!slashBladeChecked) {
            loadSlashBladeData();
            slashBladeChecked = true;
        }
        return slashBladeIDs != null ? new HashSet<>(slashBladeIDs) : Collections.emptySet();
    }
    
    /**
     * 获取所有拔刀剑的translationKey
     * @return translationKey集合
     */
    public static Set<String> getSlashBladeTranslationKeys() {
        if (!slashBladeChecked) {
            loadSlashBladeData();
            slashBladeChecked = true;
        }
        return slashBladeTranslationKeys != null ? new HashSet<>(slashBladeTranslationKeys) : Collections.emptySet();
    }
    
    /**
     * 从MinecraftServer加载拔刀剑ID和translationKey
     * 推荐在服务器启动完成后的阶段调用
     * @param server MinecraftServer实例
     */
    public static Set<ResourceLocation> getSlashBladeIDs(MinecraftServer server) {
        loadSlashBladeData(server);
        return slashBladeIDs != null ? new HashSet<>(slashBladeIDs) : Collections.emptySet();
    }
    
    /**
     * 从MinecraftServer加载拔刀剑translationKey
     * @param server MinecraftServer实例
     */
    public static Set<String> getSlashBladeTranslationKeys(MinecraftServer server) {
        loadSlashBladeData(server);
        return slashBladeTranslationKeys != null ? new HashSet<>(slashBladeTranslationKeys) : Collections.emptySet();
    }
    
    /**
     * 清除缓存，强制重新加载
     */
    public static void clearCache() {
        slashBladeChecked = false;
        slashBladeIDs = null;
        slashBladeTranslationKeys = null;
    }
    
    /**
     * 加载拔刀剑数据
     */
    private static void loadSlashBladeData() {
        try {
            if (ModList.get().isLoaded(SLASHBLADE_MOD_ID)) {
                loadSlashBladeDataDirectAPI();
            } else {
                slashBladeIDs = Collections.emptySet();
                slashBladeTranslationKeys = Collections.emptySet();
            }
        } catch (Exception e) {
            slashBladeIDs = Collections.emptySet();
            slashBladeTranslationKeys = Collections.emptySet();
            e.printStackTrace();
        }
    }
    
    /**
     * 通过MinecraftServer加载拔刀剑数据
     */
    private static void loadSlashBladeData(MinecraftServer server) {
        try {
            if (ModList.get().isLoaded(SLASHBLADE_MOD_ID)) {
                loadSlashBladeDataDirectAPI(server);
            } else {
                slashBladeIDs = Collections.emptySet();
                slashBladeTranslationKeys = Collections.emptySet();
            }
        } catch (Exception e) {
            slashBladeIDs = Collections.emptySet();
            slashBladeTranslationKeys = Collections.emptySet();
            e.printStackTrace();
        }
    }
    
    /**
     * 通过直接API调用加载拔刀剑数据
     * 基于TACZ的实现方式，避免反射
     */
    private static void loadSlashBladeDataDirectAPI() {
        // 直接调用拔刀剑API获取数据，不使用反射
        try {
            slashBladeIDs = new HashSet<>();
            slashBladeTranslationKeys = new HashSet<>();
            
            // 这里应该通过API获取真实的拔刀剑数据
            // 由于我们是在初始化阶段调用，这里暂时留空
            // 实际数据将在服务器启动后通过loadSlashBladeDataDirectAPI(MinecraftServer)填充
            
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明拔刀剑未正确加载
            slashBladeIDs = Collections.emptySet();
            slashBladeTranslationKeys = Collections.emptySet();
        } catch (Exception e) {
            slashBladeIDs = Collections.emptySet();
            slashBladeTranslationKeys = Collections.emptySet();
        }
    }
    
    /**
     * 通过直接API调用加载拔刀剑数据
     * @param server MinecraftServer实例
     */
    private static void loadSlashBladeDataDirectAPI(MinecraftServer server) {
        // 直接调用拔刀剑API获取数据，不使用反射
        try {
            // 只有在拔刀剑模组加载时才尝试访问其类
            if (!isSlashBladeLoaded()) {
                slashBladeIDs = Collections.emptySet();
                slashBladeTranslationKeys = Collections.emptySet();
                return;
            }
            
            var registryAccess = server.registryAccess();
            var slashBladeRegistry = registryAccess.lookupOrThrow(
                mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition.REGISTRY_KEY
            );
            
            slashBladeIDs = new HashSet<>();
            slashBladeTranslationKeys = new HashSet<>();
            
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
                    e.printStackTrace();
                }
            }
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明拔刀剑未正确加载
            slashBladeIDs = Collections.emptySet();
            slashBladeTranslationKeys = Collections.emptySet();
        } catch (Exception e) {
            slashBladeIDs = Collections.emptySet();
            slashBladeTranslationKeys = Collections.emptySet();
        }
    }
    
    /**
     * 检查物品是否为拔刀剑
     * 直接调用拔刀剑的API
     * @param stack 物品堆
     * @return 如果是拔刀剑返回true
     */
    public static boolean isSlashBlade(ItemStack stack) {
        if (!ModList.get().isLoaded(SLASHBLADE_MOD_ID)) {
            return false;
        }
        
        try {
            // 直接调用拔刀剑的API
            return stack.getItem() instanceof mods.flammpfeil.slashblade.item.ItemSlashBlade;
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明拔刀剑未正确加载
            return false;
        } catch (Exception e) {
            // API调用失败，返回false
            return false;
        }
    }
    
    /**
     * 获取拔刀剑物品实例
     * @return 拔刀剑物品实例
     */
    public static Item getSlashBladeItem() {
        // 检查拔刀剑模组是否已加载
        if (!isSlashBladeLoaded()) {
            return null;
        }

        try {
            // 直接使用拔刀剑的API获取枪械物品
            // 注意：这里使用了编译时依赖，但在运行时只有当拔刀剑确实加载时才会执行此代码
            return new mods.flammpfeil.slashblade.item.ItemSlashBlade(
                net.minecraft.world.item.Tiers.DIAMOND, 1, -2.0F, 
                new net.minecraft.world.item.Item.Properties());
        } catch (NoClassDefFoundError e) {
            // 类不存在，说明拔刀剑未正确加载
            return null;
        } catch (Exception e) {
            // 其他异常
            return null;
        }
    }
    
    /**
     * 获取拔刀剑的translationKey
     * @param stack 物品堆
     * @return translationKey，如果不是拔刀剑则返回null
     */
    public static String getSlashBladeTranslationKey(ItemStack stack) {
        if (!isSlashBlade(stack)) {
            return null;
        }
        
        try {
            // 直接使用拔刀剑的Capability API
            var bladeState = stack.getCapability(mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE);
            if (bladeState.isPresent()) {
                String translationKey = bladeState.resolve().get().getTranslationKey();
                if (translationKey != null && !translationKey.isBlank()) {
                    return translationKey;
                }
            }
        } catch (NoClassDefFoundError e) {
            // API调用失败，返回null
        } catch (Exception e) {
            // API调用失败，返回null
        }
        
        return null;
    }
}