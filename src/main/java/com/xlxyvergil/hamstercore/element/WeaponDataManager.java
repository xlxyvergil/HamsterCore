package com.xlxyvergil.hamstercore.element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 武器数据管理器，负责加载和管理武器的默认元素数据
 * 类似于TACZ的时间管理机制
 */
public class WeaponDataManager {
    
    private static final String CONFIG_FOLDER = "hamstercore";
    private static final String WEAPON_DATA_FILE = "weapon_data.json";
    
    // 武器数据缓存
    private static final Map<ResourceLocation, WeaponData> WEAPON_DATA_CACHE = new HashMap<>();
    
    // 静态实例
    private static WeaponDataManager INSTANCE;
    
    private WeaponDataManager() {}
    
    public static WeaponDataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WeaponDataManager();
        }
        return INSTANCE;
    }
    
    /**
     * 初始化武器数据
     */
    public void initialize() {
        HamsterCore.LOGGER.info("初始化武器数据管理器...");
        
        // 加载现有的武器数据
        loadWeaponData();
        
        // 如果没有数据，则生成默认数据
        if (WEAPON_DATA_CACHE.isEmpty()) {
            generateDefaultWeaponData();
            saveWeaponData();
        }
        
        HamsterCore.LOGGER.info("武器数据管理器初始化完成，共加载 {} 项武器数据", WEAPON_DATA_CACHE.size());
    }
    
    /**
     * 加载武器数据
     */
    private void loadWeaponData() {
        try {
            Path configPath = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get()
                .resolve(CONFIG_FOLDER)
                .resolve(WEAPON_DATA_FILE);
            
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        ResourceLocation itemKey = new ResourceLocation(entry.getKey());
                        WeaponData weaponData = WeaponData.deserialize(entry.getValue().getAsJsonObject());
                        WEAPON_DATA_CACHE.put(itemKey, weaponData);
                    }
                }
                
                HamsterCore.LOGGER.info("武器数据加载完成，共加载 {} 项", WEAPON_DATA_CACHE.size());
            } else {
                HamsterCore.LOGGER.info("未找到现有武器数据文件，将生成默认数据");
            }
        } catch (Exception e) {
            HamsterCore.LOGGER.error("加载武器数据时出错", e);
        }
    }
    
    /**
     * 生成默认武器数据
     */
    private void generateDefaultWeaponData() {
        HamsterCore.LOGGER.info("生成默认武器数据...");
        
        // 遍历所有已注册的物品
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
            if (itemKey == null) continue;
            
            ItemStack stack = new ItemStack(item);
            
            // 检查物品是否可以应用元素属性
            if (ElementHelper.canApplyElements(stack)) {
                WeaponData weaponData = createDefaultWeaponData(stack, itemKey);
                if (weaponData != null) {
                    WEAPON_DATA_CACHE.put(itemKey, weaponData);
                    HamsterCore.LOGGER.debug("添加武器数据: " + itemKey);
                }
            }
        }
        
        HamsterCore.LOGGER.info("默认武器数据生成完成，共 {} 项", WEAPON_DATA_CACHE.size());
    }
    
    /**
     * 为物品创建默认武器数据
     */
    private WeaponData createDefaultWeaponData(ItemStack stack, ResourceLocation itemKey) {
        WeaponData data = new WeaponData();
        
        // 根据物品类型设置不同的默认元素占比
        setDefaultElementRatios(stack, data);
        
        return data;
    }
    
    /**
     * 根据物品类型设置默认元素占比
     */
    private void setDefaultElementRatios(ItemStack stack, WeaponData data) {
        // 检查物品类型并设置不同的默认元素占比
        if (com.xlxyvergil.hamstercore.compat.ModCompat.isGun(stack)) {
            // 枪械类：主要是穿刺和冲击
            data.setPunctureRatio(0.6);
            data.setImpactRatio(0.3);
            data.setSlashRatio(0.1);
        } 
        else if (com.xlxyvergil.hamstercore.compat.ModCompat.isSlashBlade(stack)) {
            // 拔刀剑类：主要是切割
            data.setSlashRatio(0.7);
            data.setImpactRatio(0.2);
            data.setPunctureRatio(0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) {
            // 剑类：主要是切割
            data.setSlashRatio(0.6);
            data.setImpactRatio(0.2);
            data.setPunctureRatio(0.2);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.AxeItem) {
            // 斧类：主要是冲击和切割
            data.setImpactRatio(0.5);
            data.setSlashRatio(0.4);
            data.setPunctureRatio(0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.BowItem || 
                  stack.getItem() instanceof net.minecraft.world.item.CrossbowItem) {
            // 弓弩类：主要是穿刺
            data.setPunctureRatio(0.7);
            data.setImpactRatio(0.2);
            data.setSlashRatio(0.1);
        }
        else if (stack.getItem() instanceof net.minecraft.world.item.TridentItem) {
            // 三叉戟类：主要是冲击和穿刺
            data.setImpactRatio(0.5);
            data.setPunctureRatio(0.4);
            data.setSlashRatio(0.1);
        }
        else {
            // 其他武器：默认物理元素占比
            data.setSlashRatio(0.3);
            data.setImpactRatio(0.3);
            data.setPunctureRatio(0.4);
        }
    }
    
    /**
     * 保存武器数据
     */
    private void saveWeaponData() {
        try {
            Path configPath = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get()
                .resolve(CONFIG_FOLDER);
            
            // 确保目录存在
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
            }
            
            // 保存数据
            Path dataPath = configPath.resolve(WEAPON_DATA_FILE);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<ResourceLocation, WeaponData> entry : WEAPON_DATA_CACHE.entrySet()) {
                jsonObject.add(entry.getKey().toString(), entry.getValue().serialize());
            }
            
            String json = gson.toJson(jsonObject);
            Files.writeString(dataPath, json);
            
            HamsterCore.LOGGER.info("武器数据已保存到: " + dataPath.toAbsolutePath());
        } catch (IOException e) {
            HamsterCore.LOGGER.error("保存武器数据时出错", e);
        }
    }
    
    /**
     * 获取物品的武器数据
     */
    public WeaponData getWeaponData(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null) return null;
        
        return WEAPON_DATA_CACHE.get(itemKey);
    }
    
    /**
     * 获取物品的武器数据，如果不存在则创建默认数据
     */
    public WeaponData getOrCreateWeaponData(ItemStack stack) {
        WeaponData data = getWeaponData(stack);
        if (data == null) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemKey != null) {
                data = createDefaultWeaponData(stack, itemKey);
                if (data != null) {
                    WEAPON_DATA_CACHE.put(itemKey, data);
                }
            }
        }
        return data;
    }
}