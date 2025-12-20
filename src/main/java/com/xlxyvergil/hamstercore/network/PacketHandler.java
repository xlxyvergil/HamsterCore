package com.xlxyvergil.hamstercore.network;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(HamsterCore.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    public static void init() {
        // 注册网络数据包
        int id = 0;
        NETWORK.registerMessage(id++, EntityFactionSyncToClient.class, 
            EntityFactionSyncToClient::toBytes, 
            EntityFactionSyncToClient::new, 
            EntityFactionSyncToClient::handle);
        NETWORK.registerMessage(id++, EntityLevelSyncToClient.class,
            EntityLevelSyncToClient::toBytes,
            EntityLevelSyncToClient::new,
            EntityLevelSyncToClient::handle);
        NETWORK.registerMessage(id++, EntityArmorSyncToClient.class,
            EntityArmorSyncToClient::toBytes,
            EntityArmorSyncToClient::new,
            EntityArmorSyncToClient::handle);
        NETWORK.registerMessage(id++, EntityHealthModifierSyncToClient.class,
            EntityHealthModifierSyncToClient::toBytes,
            EntityHealthModifierSyncToClient::new,
            EntityHealthModifierSyncToClient::handle);
        NETWORK.registerMessage(id++, EntityShieldSyncToClient.class,
            EntityShieldSyncToClient::toBytes,
            EntityShieldSyncToClient::new,
            EntityShieldSyncToClient::handle);
    }
}