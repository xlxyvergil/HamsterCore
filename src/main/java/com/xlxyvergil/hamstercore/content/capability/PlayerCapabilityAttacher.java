package com.xlxyvergil.hamstercore.content.capability;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.ShieldConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import com.xlxyvergil.hamstercore.faction.Faction;
import com.xlxyvergil.hamstercore.level.HealthModifierSystem;
import com.xlxyvergil.hamstercore.level.PlayerLevelManager;
import com.xlxyvergil.hamstercore.level.PlayerLevelUpEvent;
import com.xlxyvergil.hamstercore.network.EntityArmorSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityFactionSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityHealthModifierSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityLevelSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityShieldSyncToClient;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class PlayerCapabilityAttacher {
    
    // 生命值修饰符UUID
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("7f00f62b-4121-4374-895f-e7fcdefe875f");
    
    /**
     * 根据玩家等级初始化玩家的所有能力：
     * 1. 护甲（基于玩家等级）
     * 2. 生命值修饰符（基于玩家等级）
     * 3. 护盾（基于玩家等级）
     */
    public static void initializePlayerCapabilities(Player player, int playerLevel) {
        // 1. 初始化护甲（基于玩家等级）
        player.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .ifPresent(armorCap -> {
                    // 计算基础护甲加成（从2级开始每6级+20基础护甲）
                    int baseArmorBonus = PlayerLevelManager.getBaseArmorBonus(playerLevel);
                    
                    // 玩家基础护甲值为200点 + 等级加成
                    double baseArmor = 200.0 + baseArmorBonus;
                    armorCap.setBaseArmor(baseArmor);
                    
                    // 应用实体属性计算最终护甲值
                    double baseArmorAttribute = AttributeHelper.getBaseArmor(player);
                    double armorAttribute = AttributeHelper.getArmor(player);
                    double finalBaseArmor = baseArmor * baseArmorAttribute;
                    double finalArmor = finalBaseArmor * armorAttribute;
                    armorCap.setArmor(finalArmor);
                });
        
        // 2. 初始化生命值修饰符（基于玩家等级）
        applyHealthModifier(player, PlayerLevelManager.getHealthBonus(playerLevel));
        
        // 3. 初始化护盾（基于玩家等级）
        initializeShieldCapability(player, PlayerLevelManager.getShieldBonus(playerLevel));
        
        // 在所有能力初始化完成后，同步到所有正在跟踪该实体的玩家
        syncPlayerCapabilitiesToClients(player);
    }
    
    /**
     * 初始化玩家护盾能力
     */
    private static void initializeShieldCapability(Player player, int shieldBonus) {
        player.getCapability(EntityShieldCapabilityProvider.CAPABILITY)
                .ifPresent(shieldCap -> {
                    // 加载护盾配置
                    ShieldConfig shieldConfig = ShieldConfig.load();
                    
                    // 获取玩家基础护盾值（100点）+ 等级加成
                    float baseShield = 100.0f + shieldBonus;
                    
                    // 使用EntityShieldCapability中的方法初始化护盾能力（不含属性应用）
                    shieldCap.initializeEntityCapabilities(baseShield, 1, true); // 等级不影响护盾
                    
                    // 应用实体属性：baseShield = baseShield * MAX_SHIELD属性值
                    double maxShieldAttribute = AttributeHelper.getMaxShield(player);
                    float maxShield = baseShield * (float)maxShieldAttribute;
                    shieldCap.setMaxShield(maxShield);
                    shieldCap.setCurrentShield(maxShield); // 实体生成时应初始化为满护盾
                    
                    // 应用实体属性：regenRate = (15.0f + 0.05f * maxShield) * REGEN_RATE属性值
                    double regenRateAttribute = AttributeHelper.getRegenRate(player);
                    float regenRate = (15.0f + 0.05f * maxShield) * (float)regenRateAttribute;
                    shieldCap.setRegenRate(regenRate);
                    
                    // 应用实体属性：regenDelayNormal = (2 * 20) / REGEN_DELAY属性值
                    // 应用实体属性：regenDelayDepleted = (6 * 20) / REGEN_DELAY属性值
                    double regenDelayAttribute = AttributeHelper.getRegenDelay(player);
                    int regenDelayNormal = (int)((2 * 20) / regenDelayAttribute);
                    int regenDelayDepleted = (int)((6 * 20) / regenDelayAttribute);
                    shieldCap.setRegenDelay(regenDelayNormal);
                    shieldCap.setRegenDelayDepleted(regenDelayDepleted);
                });
    }
    
    /**
     * 应用生命值修饰符
     */
    private static void applyHealthModifier(Player player, int bonus) {
        AttributeInstance healthAttribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            // 移除旧的修饰符
            AttributeModifier oldModifier = healthAttribute.getModifier(HEALTH_MODIFIER_UUID);
            if (oldModifier != null) {
                healthAttribute.removeModifier(oldModifier);
            }
            
            // 添加新的修饰符
            if (bonus > 0) {
                AttributeModifier modifier = new AttributeModifier(
                    HEALTH_MODIFIER_UUID,
                    "PlayerLevelHealthBonus",
                    bonus,
                    AttributeModifier.Operation.ADDITION
                );
                healthAttribute.addPermanentModifier(modifier);
                
                // 确保玩家当前生命值不超过新的最大生命值
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth((float) player.getMaxHealth());
                }
            }
        }
    }
    
    /**
     * 同步玩家的所有能力到跟踪该玩家的所有客户端
     */
    public static void syncPlayerCapabilitiesToClients(Player player) {
        // 同步等级到客户端
        EntityLevelSyncToClient.sync(player);
        
        // 同步派系到客户端
        EntityFactionSyncToClient.sync(player);
        
        // 同步护甲到客户端
        EntityArmorSyncToClient.sync(player);
        
        // 同步生命值修饰符到客户端
        EntityHealthModifierSyncToClient.sync(player);
        
        // 同步护盾到客户端
        EntityShieldSyncToClient.sync(player);
    }
    
    /**
     * 监听玩家等级升级事件
     */
    @SubscribeEvent
    public static void onPlayerLevelUp(PlayerLevelUpEvent event) {
        Player player = event.getPlayer();
        int playerLevel = event.getPlayerLevel();
        
        // 根据玩家等级重新初始化玩家能力数据
        initializePlayerCapabilities(player, playerLevel);
    }
}