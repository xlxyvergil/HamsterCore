package com.xlxyvergil.hamstercore.content.capability;

import com.xlxyvergil.hamstercore.attribute.BaseAttributeModifierSystem;
import com.xlxyvergil.hamstercore.attribute.DerivedAttributeModifierSystem;
import com.xlxyvergil.hamstercore.attribute.PlayerLevelAttributeModifierSystem;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.network.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import com.xlxyvergil.hamstercore.util.AttributeHelper;


public class PlayerCapabilityAttacher {
    
    /**
     * 初始化玩家的Capability并同步Attribute数据
     */
    public static void initializePlayerCapabilities(Player player, int playerLevel) {
        // 1. 应用基础属性修饰符
        BaseAttributeModifierSystem.applyBaseAttributeModifiers(player);
        
        // 2. 应用基于玩家等级的属性修饰符
        PlayerLevelAttributeModifierSystem.applyPlayerLevelModifiers(player, playerLevel);
        
        // 3. 应用衍生属性修饰符
        DerivedAttributeModifierSystem.applyDerivedModifiers(player);
        
        // 4. 初始化护甲
        player.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .ifPresent(armorCap -> {
                    // 从Attribute系统获取最终护甲值
                    armorCap.setArmor(AttributeHelper.getArmor(player));
                });
        
        // 5. 初始化护盾
        player.getCapability(EntityShieldCapabilityProvider.CAPABILITY)
                .ifPresent(shieldCap -> {
                    // 从Attribute系统获取最终护盾相关值
                    shieldCap.setMaxShield((float) AttributeHelper.getShield(player));
                    shieldCap.setCurrentShield((float) AttributeHelper.getShield(player)); // 实体生成时应初始化为满护盾
                    shieldCap.setRegenRate((float) AttributeHelper.getRegenRate(player));
                    shieldCap.setRegenDelay((int) AttributeHelper.getRegenDelay(player));
                    shieldCap.setRegenDelayDepleted((int) AttributeHelper.getDepletedRegenDelay(player));
                    shieldCap.setImmunityTime((int) AttributeHelper.getImmunityTime(player));
                    
                    // 设置玩家特有的默认值
                    shieldCap.setInsuranceAvailable(true);
                });
        
        // 6. 同步到客户端
        syncPlayerCapabilitiesToClients(player);
    }
    
    public static void syncPlayerCapabilitiesToClients(Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer)) {
            return;
        }
        
        // 同步护盾数据
        EntityShieldSyncToClient.sync(player);
        
        // 同步护甲数据
        EntityArmorSyncToClient.sync(player);
        
        // 同步生命值修饰符数据
        EntityHealthModifierSyncToClient.sync(player);
        
        // 同步玩家等级和经验数据到客户端
        PlayerLevelSyncToClient.sync(player);
    }
}