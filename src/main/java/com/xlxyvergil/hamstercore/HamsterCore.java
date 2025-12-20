package com.xlxyvergil.hamstercore;

import com.xlxyvergil.hamstercore.client.renderer.item.WeaponAttributeRenderer;
import com.xlxyvergil.hamstercore.config.ArmorConfig;
import com.xlxyvergil.hamstercore.config.ClientConfig;
import com.xlxyvergil.hamstercore.config.FactionConfig;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.level.LevelSystem;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import com.xlxyvergil.hamstercore.enchantment.ModEnchantments;
import com.xlxyvergil.hamstercore.config.AdditionalConfigApplier;
import com.xlxyvergil.hamstercore.config.NormalConfigApplier;
import com.xlxyvergil.hamstercore.config.SlashBladeConfigApplier;
import com.xlxyvergil.hamstercore.config.TacZConfigApplier;
import com.xlxyvergil.hamstercore.config.WeaponItemIds;
import com.xlxyvergil.hamstercore.config.TacZWeaponConfig;
import com.xlxyvergil.hamstercore.config.SlashBladeWeaponConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HamsterCore.MODID)
public class HamsterCore {
    public static final String MODID = "hamstercore";

    public HamsterCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册事件
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerCapabilities);
        
        // 注册附魔
        ModEnchantments.register(modEventBus);
                
        // 初始化网络包
        PacketHandler.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // 注册服务器启动事件监听器
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        
        // 加载客户端配置
        ClientConfig.load();
        
        // 注册客户端事件
        WeaponAttributeRenderer.registerEvents();
    }
    
    
    
    
    

    /**
     * 服务器启动完成事件处理
     * 在这个阶段，所有模组的物品都已完成注册和初始化，可以安全获取
     */
    private void onServerStarted(ServerStartedEvent event) {
        
        try {                  
            // 1. 初始化兼容性检查 - 在服务器启动时检查，确保所有模组都已加载
            // 只有在模组加载后才初始化对应的获取器
        FactionConfig.load();
        ArmorConfig.load();
        LevelSystem.init();

            // 2. 让SlashBladeItemsFetcher获取数据
            // 直接检查模组是否加载，避免触发类初始化
            if (ModList.get().isLoaded("slashblade")) {
                SlashBladeItemsFetcher.getSlashBladeTranslationKeys(event.getServer());
            }

            // 3. 生成配置文件（包括普通物品、TACZ枪械和拔刀剑的数据）
            WeaponConfig.init();
            
            // 生成TACZ配置文件
            if (ModList.get().isLoaded("tacz")) {
                TacZWeaponConfig.generateTacZWeaponsConfig();
                // 加载TACZ配置文件
                TacZWeaponConfig.loadTacZConfigFile();
            }
            
            // 生成拔刀剑配置文件
            if (ModList.get().isLoaded("slashblade")) {
                SlashBladeWeaponConfig.generateSlashBladeWeaponsConfig(event.getServer());
                // 加载拔刀剑配置文件
                SlashBladeWeaponConfig.loadSlashBladeConfigFile();
            }
            
            // 4. 应用所有配置
            NormalConfigApplier.load();
            
            // 只有在对应模组加载时才应用配置
            if (ModList.get().isLoaded("tacz")) {
                TacZConfigApplier.load();
            }
            
            if (ModList.get().isLoaded("slashblade")) {
                SlashBladeConfigApplier.load();
            }
            
            // 应用额外配置
            AdditionalConfigApplier.load();
            
            // 5. 初始化武器ID缓存 - 在所有配置加载完成后收集所有武器ID
            WeaponItemIds.initializeAllWeaponIds();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(EntityFactionCapabilityProvider.class);
        event.register(EntityLevelCapabilityProvider.class);
        event.register(EntityArmorCapabilityProvider.class);
    }
}