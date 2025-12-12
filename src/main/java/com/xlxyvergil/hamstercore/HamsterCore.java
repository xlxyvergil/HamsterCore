package com.xlxyvergil.hamstercore;

import com.xlxyvergil.hamstercore.client.renderer.item.WeaponAttributeRenderer;
import com.xlxyvergil.hamstercore.config.ArmorConfig;
import com.xlxyvergil.hamstercore.config.FactionConfig;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.element.ElementApplier;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.api.element.ElementAttributeAPI;
import com.xlxyvergil.hamstercore.level.LevelSystem;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import com.xlxyvergil.hamstercore.enchantment.ModEnchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.Set;
import java.util.stream.Collectors;

@Mod(HamsterCore.MODID)
public class HamsterCore {
    public static final String MODID = "hamstercore";
    public static final Logger LOGGER = LogManager.getLogger();

    public HamsterCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册事件
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerCapabilities);
        
        // 注册附魔
        ModEnchantments.register(modEventBus);
        
        // 初始化配置
        FactionConfig.load();
        ArmorConfig.load();
        LevelSystem.init();
        
        // 初始化元素系统（不再需要显式调用init方法）
        LOGGER.info("Element system initialized");
        
        
        // 初始化网络包
        PacketHandler.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // 注册服务器启动事件监听器
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        
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
            SlashBladeItemsFetcher.init();
        
            // 2. 让SlashBladeItemsFetcher获取数据
            if (SlashBladeItemsFetcher.isSlashBladeLoaded()) {
                SlashBladeItemsFetcher.getSlashBladeTranslationKeys(event.getServer());
            }

            // 3. 生成配置文件（包括普通物品、TACZ枪械和拔刀剑的数据）
            com.xlxyvergil.hamstercore.config.WeaponConfig.load();
            
            // 4. 应用普通物品元素属性
            int normalAppliedCount = com.xlxyvergil.hamstercore.element.NormalItemElementApplier.applyNormalItemsElements();
            
            // 5. 应用MOD特殊物品元素属性（TACZ枪械和拔刀剑）
            int modSpecialAppliedCount = com.xlxyvergil.hamstercore.element.ElementApplier.applyModSpecialItemsElements();
            
            // 6. 应用额外的元素属性配置
            int additionalAppliedCount = com.xlxyvergil.hamstercore.element.AdditionalElementApplier.applyAdditionalElements();
            
            
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