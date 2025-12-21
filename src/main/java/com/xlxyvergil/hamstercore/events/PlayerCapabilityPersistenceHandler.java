package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class PlayerCapabilityPersistenceHandler {
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        
        // 复制所有Capability数据
        copyPlayerCapabilities(oldPlayer, newPlayer);
    }
    
    private static void copyPlayerCapabilities(Player oldPlayer, Player newPlayer) {
        // 复制护盾Capability
        copyShieldCapability(oldPlayer, newPlayer);
        
        // 复制护甲Capability
        copyArmorCapability(oldPlayer, newPlayer);
        
        // 复制等级Capability
        copyLevelCapability(oldPlayer, newPlayer);
        
        // 复制阵营Capability
        copyFactionCapability(oldPlayer, newPlayer);
        
        // 复制生命值修饰符Capability
        copyHealthModifierCapability(oldPlayer, newPlayer);
    }
    
    private static void copyShieldCapability(Player oldPlayer, Player newPlayer) {
        EntityShieldCapability oldCap = oldPlayer.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
        EntityShieldCapability newCap = newPlayer.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
        
        if (oldCap != null && newCap != null) {
            newCap.deserializeNBT(oldCap.serializeNBT());
        }
    }
    
    private static void copyArmorCapability(Player oldPlayer, Player newPlayer) {
        EntityArmorCapability oldCap = oldPlayer.getCapability(EntityArmorCapabilityProvider.CAPABILITY).orElse(null);
        EntityArmorCapability newCap = newPlayer.getCapability(EntityArmorCapabilityProvider.CAPABILITY).orElse(null);
        
        if (oldCap != null && newCap != null) {
            newCap.deserializeNBT(oldCap.serializeNBT());
        }
    }
    
    private static void copyLevelCapability(Player oldPlayer, Player newPlayer) {
        EntityLevelCapability oldCap = oldPlayer.getCapability(EntityLevelCapabilityProvider.CAPABILITY).orElse(null);
        EntityLevelCapability newCap = newPlayer.getCapability(EntityLevelCapabilityProvider.CAPABILITY).orElse(null);
        
        if (oldCap != null && newCap != null) {
            newCap.deserializeNBT(oldCap.serializeNBT());
        }
    }
    
    private static void copyFactionCapability(Player oldPlayer, Player newPlayer) {
        EntityFactionCapability oldCap = oldPlayer.getCapability(EntityFactionCapabilityProvider.CAPABILITY).orElse(null);
        EntityFactionCapability newCap = newPlayer.getCapability(EntityFactionCapabilityProvider.CAPABILITY).orElse(null);
        
        if (oldCap != null && newCap != null) {
            newCap.deserializeNBT(oldCap.serializeNBT());
        }
    }
    
    private static void copyHealthModifierCapability(Player oldPlayer, Player newPlayer) {
        EntityHealthModifierCapability oldCap = oldPlayer.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY).orElse(null);
        EntityHealthModifierCapability newCap = newPlayer.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY).orElse(null);
        
        if (oldCap != null && newCap != null) {
            newCap.deserializeNBT(oldCap.serializeNBT());
        }
    }
}