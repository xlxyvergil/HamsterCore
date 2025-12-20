package com.xlxyvergil.hamstercore.content.capability;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.ShieldConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import com.xlxyvergil.hamstercore.faction.Faction;
import com.xlxyvergil.hamstercore.level.HealthModifierSystem;
import com.xlxyvergil.hamstercore.network.EntityArmorSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityFactionSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityHealthModifierSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityLevelSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityShieldSyncToClient;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class EntityCapabilityAttacher {
    
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity livingEntity) {
            // 附加实体等级能力
            event.addCapability(EntityLevelCapability.ID, new EntityLevelCapabilityProvider());
            
            // 附加实体护甲能力
            EntityArmorCapabilityProvider armorProvider = new EntityArmorCapabilityProvider();
            armorProvider.setEntityType(livingEntity.getType());
            event.addCapability(EntityArmorCapability.ID, armorProvider);
            
            // 附加实体派系能力
            EntityFactionCapabilityProvider factionProvider = new EntityFactionCapabilityProvider();
            factionProvider.setEntityType(livingEntity.getType());
            event.addCapability(EntityFactionCapability.ID, factionProvider);
            
            // 附加实体生命值修饰符能力
            event.addCapability(EntityHealthModifierCapability.ID, new EntityHealthModifierCapabilityProvider());
            
            // 检查实体是否应该拥有护盾能力
            if (shouldHaveShieldCapability(livingEntity)) {
                // 附加实体护盾能力
                event.addCapability(EntityShieldCapability.ID, new EntityShieldCapabilityProvider());
            }
        }
    }
    
    /**
     * 检查实体是否应该拥有护盾能力
     * @param entity 实体
     * @return 如果实体应该拥有护盾能力则返回true，否则返回false
     */
    private static boolean shouldHaveShieldCapability(LivingEntity entity) {
        // 玩家总是拥有护盾能力
        if (entity instanceof Player) {
            return true;
        }
        
        // 加载护盾配置
        ShieldConfig shieldConfig = ShieldConfig.load();
        
        // 获取实体类型和分类
        EntityType<?> entityType = entity.getType();
        MobCategory classification = entityType.getCategory();
        
        // 排除被动生物、中立生物和环境生物
        if (classification == MobCategory.CREATURE || 
            classification == MobCategory.AMBIENT || 
            classification == MobCategory.WATER_CREATURE ||
            classification == MobCategory.WATER_AMBIENT) {
            return false;
        }
        
        // 获取实体的派系
        String factionName = entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
            .map(factionCap -> {
                Faction faction = factionCap.getFaction();
                return faction != null ? faction.name() : "OROKIN";
            })
            .orElse("OROKIN"); // 默认派系
            
        // 检查该派系是否启用护盾
        boolean factionHasShield = ShieldConfig.isFactionShieldEnabled(factionName);
        
        // 获取基础护盾值
        float baseShield = shieldConfig.getBaseShieldForEntity(entity.getType(), factionName);
        
        // 检查实体是否在配置文件中有明确的护盾设置
        boolean entityHasShieldConfig = baseShield >= 0; // -1表示未找到配置
        
        // 确定是否应该给予护盾：
        // 1. 实体在配置文件中有护盾设置
        // 2. 或者实体所属派系默认有护盾（Corpus/Orokin/Sentient）且为敌对怪物
        return entityHasShieldConfig || factionHasShield;
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // 只处理服务端
        if (!(event.getLevel() instanceof ServerLevel)) return;
        
        // 获取实体
        LivingEntity entity = event.getEntity();
        
        initializeEntityCapabilities(entity);
        
        // 立即同步到客户端，确保客户端能获取到正确的数据
        syncEntityCapabilitiesToClients(entity);
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (!event.getLevel().isClientSide()) {
                // 服务端：初始化实体能力
                initializeEntityCapabilities(livingEntity);
                
                // 立即同步到客户端，确保客户端能获取到正确的数据
                syncEntityCapabilitiesToClients(livingEntity);
            } else {
                // 客户端：如果实体是玩家，不需要做任何事
                // 如果是非玩家实体，等待服务端同步数据
            }
        }
    }
    
    // 当玩家开始跟踪实体时，同步实体数据到该玩家
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity entity && event.getEntity() instanceof ServerPlayer player) {
            // 同步等级
            entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityLevelSyncToClient(entity.getId(), levelCap.getLevel())
                );
            });
            
            // 同步派系
            entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(factionCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityFactionSyncToClient(entity.getId(), factionCap.getFaction())
                );
            });
            
            // 同步护甲
            entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(armorCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityArmorSyncToClient(entity.getId(), armorCap.getArmor())
                );
            });
            
            // 同步生命值修饰符
            entity.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY).ifPresent(healthCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityHealthModifierSyncToClient(entity.getId(), healthCap.getHealthModifier(), healthCap.isInitialized())
                );
            });
            
            // 同步护盾（仅当实体拥有护盾能力时）
            entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield())
                );
            });
        }
    }
    
    /**
     * 按照正确的顺序初始化实体的所有能力：
     * 1. 派系
     * 2. 等级
     * 3. 护甲（基于派系和等级）
     * 4. 生命值修饰符（基于等级）
     * 5. 护盾（基于派系和等级）
     */
    public static void initializeEntityCapabilities(LivingEntity entity) {
        // 1. 初始化派系
        entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                .ifPresent(factionCap -> {
                    // 确保派系被初始化
                    factionCap.getFaction();
                });
        
        // 2. 初始化等级
        entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                .ifPresent(levelCap -> {
                    // 只有当等级尚未初始化时才进行初始化
                    if (!levelCap.isInitialized()) {
                        levelCap.initializeLevel(entity);
                    }
                });
        
        // 3. 初始化护甲（基于派系和等级）
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .ifPresent(armorCap -> {
                    // 获取实体的等级
                    int level = entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                        .map(levelCap -> levelCap.getLevel())
                        .orElse(20); // 默认等级20
                    
                    // 初始化实体能力，传入基础等级20和当前等级
                    // 对于玩家，护甲与等级无关；对于怪物，护甲与等级有关
                    if (entity instanceof Player) {
                        // 玩家护甲与等级无关，设置默认护甲值为200点
                        armorCap.setBaseArmor(200.0);
                        armorCap.setArmor(200.0);
                    } else {
                        // 怪物护甲与等级有关
                        armorCap.initializeEntityCapabilities(20, level);
                    }
                });
        
        // 4. 初始化生命值修饰符（基于等级）
        HealthModifierSystem.applyHealthModifier(entity);
        
        // 5. 初始化护盾（基于派系和等级）
        initializeShieldCapability(entity);
        
        // 在所有能力初始化完成后，同步到所有正在跟踪该实体的玩家
        syncEntityCapabilitiesToClients(entity);
    }
    
    /**
     * 初始化实体护盾能力
     */
    private static void initializeShieldCapability(LivingEntity entity) {
        entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY)
                .ifPresent(shieldCap -> {
                    // 获取实体的等级
                    int level = entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                        .map(levelCap -> levelCap.getLevel())
                        .orElse(20); // 默认等级20
                    
                    // 获取实体的派系
                    String factionName = entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                        .map(factionCap -> {
                            Faction faction = factionCap.getFaction();
                            return faction != null ? faction.name() : "OROKIN";
                        })
                        .orElse("OROKIN"); // 默认派系
                    
                    // 检查是否为玩家
                    boolean isPlayer = entity instanceof Player;
                    
                    // 玩家特殊处理
                    if (isPlayer) {
                        // 加载护盾配置
                        ShieldConfig shieldConfig = ShieldConfig.load();
                        
                        // 获取玩家基础护盾值（玩家护盾与等级无关）
                        float baseShield = shieldConfig.getPlayerBaseShield();
                        
                        // 设置最大护盾和当前护盾（不乘以等级系数）
                        float maxShield = baseShield;
                        shieldCap.setMaxShield(maxShield);
                        shieldCap.setCurrentShield(maxShield);
                        
                        // 计算并设置护盾恢复速率（每秒护盾回复 = 15 + 0.05 × 护盾容量）
                        float regenRate = 15.0f + 0.05f * maxShield;
                        shieldCap.setRegenRate(regenRate);
                        
                        // 计算并设置护盾恢复延迟
                        int regenDelayNormal = 2 * 20; // 玩家护盾恢复延迟：2秒
                        int regenDelayDepleted = 6 * 20; // 玩家护盾耗尽时恢复延迟：6秒
                        shieldCap.setRegenDelay(regenDelayNormal);
                        shieldCap.setRegenDelayDepleted(regenDelayDepleted);
                        
                        // 计算并设置护盾保险时间
                        int immunityTime = calculateImmunityTime(maxShield);
                        shieldCap.setImmunityTime(immunityTime);
                        return;
                    }
                    
                    // 加载护盾配置
                    ShieldConfig shieldConfig = ShieldConfig.load();
                    
                    // 检查该派系是否启用护盾
                    boolean factionHasShield = ShieldConfig.isFactionShieldEnabled(factionName);
                    
                    // 获取基础护盾值
                    float baseShield = shieldConfig.getBaseShieldForEntity(entity.getType(), factionName);
                    
                    // 检查实体是否在配置文件中明确设置了护盾值
                    boolean entityHasShieldConfig = baseShield >= 0; // -1表示未找到配置
                    
                    // 确定是否应该给予护盾：
                    // 1. 实体在配置文件中有护盾设置
                    // 2. 或者实体所属派系默认有护盾（Corpus/Orokin/Sentient）且为敌对怪物
                    if (!entityHasShieldConfig && !factionHasShield) {
                        return;
                    }
                    
                    // 如果基础护盾值小于等于0，则不赋予护盾
                    if (baseShield <= 0) {
                        return;
                    }
                    
                    // 计算护盾系数（怪物护盾与等级有关）
                    float shieldCoefficient = 1 + 0.02f * (float) Math.pow(level - 20, 1.76);
                    float maxShield = baseShield * shieldCoefficient;
                    
                    // 设置最大护盾和当前护盾
                    shieldCap.setMaxShield(maxShield);
                    shieldCap.setCurrentShield(maxShield);
                    
                    // 计算并设置护盾恢复速率（每秒护盾回复 = 15 + 0.05 × 护盾容量）
                    float regenRate = 15.0f + 0.05f * maxShield;
                    shieldCap.setRegenRate(regenRate);
                    
                    // 计算并设置护盾恢复延迟
                    int regenDelayNormal = 3 * 20; // 怪物（mobs）护盾恢复延迟：3秒
                    int regenDelayDepleted = 3 * 20; // 怪物护盾耗尽时恢复延迟：3秒
                    shieldCap.setRegenDelay(regenDelayNormal);
                    shieldCap.setRegenDelayDepleted(regenDelayDepleted);
                });
    }
    
    /**
     * 计算护盾保险机制的免疫时间
     */
    private static int calculateImmunityTime(float shield) {
        if (shield < 53) {
            // 低护盾值情况：免疫时间 = 护盾量/180 + 1/3 秒
            return (int) ((shield / 180.0 + 1.0/3.0) * 20);
        } else if (shield < 1150) {
            // 中等护盾值情况：免疫时间 = (护盾量/350)^0.65 + 1/3 秒
            return (int) ((Math.pow(shield / 350.0, 0.65) + 1.0/3.0) * 20);
        } else {
            // 高护盾值情况：免疫时间 = 2.5 秒
            return (int) (2.5 * 20);
        }
    }
    
    /**
     * 同步实体的所有能力到跟踪该实体的所有客户端
     */
    public static void syncEntityCapabilitiesToClients(LivingEntity entity) {
        // 同步等级到客户端
        EntityLevelSyncToClient.sync(entity);
        
        // 同步派系到客户端
        EntityFactionSyncToClient.sync(entity);
        
        // 同步护甲到客户端
        EntityArmorSyncToClient.sync(entity);
        
        // 同步生命值修饰符到客户端
        EntityHealthModifierSyncToClient.sync(entity);
        
        // 同步护盾到客户端（仅当实体拥有护盾能力时）
        entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
            EntityShieldSyncToClient.sync(entity);
        });
    }
}