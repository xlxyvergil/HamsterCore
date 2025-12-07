package com.xlxyvergil.hamstercore;

import com.xlxyvergil.hamstercore.client.renderer.item.WeaponAttributeRenderer;
import com.xlxyvergil.hamstercore.config.ArmorConfig;
import com.xlxyvergil.hamstercore.config.ElementConfig;
import com.xlxyvergil.hamstercore.config.FactionConfig;
import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.level.LevelSystem;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
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
        ElementConfig.load();
        LevelSystem.init();
        
        // 初始化网络包
        PacketHandler.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // 初始化派系注册表
        
        // 初始化元素系统
        ElementRegistry.init();
        LOGGER.info("Element system initialized");
        
        // 初始化元素配置
        LOGGER.info("Weapon configuration initialized with " + 
                   ElementConfig.getInstance().getAllWeaponConfigs().size() + " weapons");
                   
        // 注册客户端事件
        WeaponAttributeRenderer.registerEvents();
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(EntityFactionCapabilityProvider.class);
        event.register(EntityLevelCapabilityProvider.class);
        event.register(EntityArmorCapabilityProvider.class);
    }
}