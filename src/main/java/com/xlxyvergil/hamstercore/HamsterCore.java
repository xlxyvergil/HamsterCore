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
import com.xlxyvergil.hamstercore.level.LevelSystem;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(HamsterCore.MODID)
public class HamsterCore {
    public static final String MODID = "hamstercore";
    public static final Logger LOGGER = LogManager.getLogger();

    public HamsterCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册事件
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerCapabilities);
        
        // 初始化配置
        FactionConfig.load();
        ArmorConfig.load();
        LevelSystem.init();
        
        // 初始化元素系统
        ElementRegistry.init();
        LOGGER.info("Element system initialized");
        
        // 初始化网络包
        PacketHandler.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // 注册服务器启动事件监听器
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        
        // 注册客户端事件
        WeaponAttributeRenderer.registerEvents();
    }
    
    /**
     * 服务器启动事件处理
     */
    private void onServerStarting(ServerStartingEvent event) {
        HamsterCore.LOGGER.info("服务器启动，开始初始化武器配置和元素属性");
        
        try {
            // 初始化武器配置
            WeaponConfig.load();
            
            // 应用默认元素属性
            ElementApplier.applyElementsFromConfig();
            
            HamsterCore.LOGGER.info("武器配置和元素属性初始化完成");
        } catch (Exception e) {
            HamsterCore.LOGGER.error("初始化过程中发生错误", e);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(EntityFactionCapabilityProvider.class);
        event.register(EntityLevelCapabilityProvider.class);
        event.register(EntityArmorCapabilityProvider.class);
    }
}