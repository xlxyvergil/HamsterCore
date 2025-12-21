package com.xlxyvergil.hamstercore.content.capability;

import com.xlxyvergil.hamstercore.config.ShieldConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import com.xlxyvergil.hamstercore.faction.Faction;
import com.xlxyvergil.hamstercore.level.HealthModifierSystem;
import com.xlxyvergil.hamstercore.network.EntityArmorSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityFactionSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityHealthModifierSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityLevelSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityShieldSyncToClient;
import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.network.PacketDistributor;
public class EntityCapabilityAttacher {
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
                    
                    // 怪物护甲与等级有关
                    armorCap.initializeEntityCapabilities(20, level);
                    
                    // 应用实体属性计算最终护甲值
                    double baseArmorAttribute = AttributeHelper.getBaseArmor(entity);
                    double armorAttribute = AttributeHelper.getArmor(entity);
                    double currentBaseArmor = armorCap.getBaseArmor();
                    double currentArmor = armorCap.getArmor();
                    double finalBaseArmor = currentBaseArmor * baseArmorAttribute;
                    double finalArmor = currentArmor * armorAttribute;
                    armorCap.setBaseArmor(finalBaseArmor);
                    armorCap.setArmor(finalArmor);
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
                    
                    // 加载护盾配置
                    ShieldConfig shieldConfig = ShieldConfig.load();
                    
                    // 1. 首先检查实体是否在entityBaseShields中明确配置
                    float baseShield = shieldConfig.getBaseShieldForEntity(entity.getType());
                    boolean hasExplicitConfig = baseShield >= 0; // -1表示未找到配置
                    
                    // 只有在entityBaseShields中明确配置了护盾值的实体才初始化护盾能力
                    if (hasExplicitConfig && baseShield > 0) {
                        // 使用EntityShieldCapability中的方法初始化护盾能力（不含属性应用）
                        shieldCap.initializeEntityCapabilities(baseShield, level, false);
                        
                        // 应用实体属性：baseShield = baseShield * MAX_SHIELD属性值
                        double maxShieldAttribute = AttributeHelper.getMaxShield(entity);
                        float maxShield = shieldCap.getMaxShield() * (float)maxShieldAttribute;
                        shieldCap.setMaxShield(maxShield);
                        shieldCap.setCurrentShield(maxShield); // 实体生成时应初始化为满护盾
                        
                        // 应用实体属性：regenRate = (15.0f + 0.05f * maxShield) * REGEN_RATE属性值
                        double regenRateAttribute = AttributeHelper.getRegenRate(entity);
                        float regenRate = shieldCap.getRegenRate() * (float)regenRateAttribute;
                        shieldCap.setRegenRate(regenRate);
                        
                        // 应用实体属性：regenDelayNormal = (3 * 20) / REGEN_DELAY属性值
                        // 应用实体属性：regenDelayDepleted = (3 * 20) / REGEN_DELAY属性值
                        double regenDelayAttribute = AttributeHelper.getRegenDelay(entity);
                        int regenDelayNormal = (int)((3 * 20) / regenDelayAttribute);
                        int regenDelayDepleted = (int)((3 * 20) / regenDelayAttribute);
                        shieldCap.setRegenDelay(regenDelayNormal);
                        shieldCap.setRegenDelayDepleted(regenDelayDepleted);
                        return; // 处理完明确配置的实体后直接返回
                    }
                    
                    // 2. 如果没有明确配置，检查实体是否属于CORPUS、OROKIN或SENTIENT派系
                    // 获取实体的派系
                    Faction faction = entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                            .map(factionCap -> factionCap.getFaction())
                            .orElse(null); // 使用null代替Faction.NONE，因为没有NONE派系
                    
                    // 检查是否为CORPUS、OROKIN或SENTIENT派系的怪物
                    if (faction == Faction.CORPUS || faction == Faction.OROKIN || faction == Faction.SENTIENT) {
                        // 检查是否为敌对怪物（通过实体分类判断）
                        MobCategory classification = entity.getType().getCategory();
                        if (classification != MobCategory.MISC && 
                            classification != MobCategory.CREATURE && 
                            classification != MobCategory.AMBIENT && 
                            classification != MobCategory.WATER_CREATURE &&
                            classification != MobCategory.WATER_AMBIENT) {
                            
                            // 获取派系的基础护盾值
                            float factionBaseShield = shieldConfig.getFactionDefaultShields()
                                    .getOrDefault(faction.name(), 0.0f);
                            
                            if (factionBaseShield > 0) {
                                // 使用EntityShieldCapability中的方法初始化护盾能力（不含属性应用）
                                shieldCap.initializeEntityCapabilities(factionBaseShield, level, false);
                                
                                // 应用实体属性：baseShield = baseShield * MAX_SHIELD属性值
                                double maxShieldAttribute = AttributeHelper.getMaxShield(entity);
                                float maxShield = shieldCap.getMaxShield() * (float)maxShieldAttribute;
                                shieldCap.setMaxShield(maxShield);
                                shieldCap.setCurrentShield(maxShield); // 实体生成时应初始化为满护盾
                                
                                // 应用实体属性：regenRate = (15.0f + 0.05f * maxShield) * REGEN_RATE属性
                                double regenRateAttribute = AttributeHelper.getRegenRate(entity);
                                float regenRate = shieldCap.getRegenRate() * (float)regenRateAttribute;
                                shieldCap.setRegenRate(regenRate);
                                
                                // 应用实体属性：regenDelayNormal = (3 * 20) / REGEN_DELAY属性
                                // 应用实体属性：regenDelayDepleted = (3 * 20) / REGEN_DELAY属性
                                double regenDelayAttribute = AttributeHelper.getRegenDelay(entity);
                                int regenDelayNormal = (int)((3 * 20) / regenDelayAttribute);
                                int regenDelayDepleted = (int)((3 * 20) / regenDelayAttribute);
                                shieldCap.setRegenDelay(regenDelayNormal);
                                shieldCap.setRegenDelayDepleted(regenDelayDepleted);
                                return; // 处理完派系相关实体后直接返回
                            }
                        }
                    }
                    
                    // 如果既没有明确配置也不是指定派系的怪物，则完全移除护盾能力
                    // 这里我们通过将护盾值设为负数来标记该实体不应具有护盾能力
                    shieldCap.setMaxShield(-1);
                    shieldCap.setCurrentShield(-1);
                });
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
            // 检查护盾值是否有效
            if (shieldCap.getMaxShield() >= 0) {
                EntityShieldSyncToClient.sync(entity);
            }
        });
    }
}