package com.xlxyvergil.hamstercore.content.capability;

import com.xlxyvergil.hamstercore.attribute.BaseAttributeModifierSystem;
import com.xlxyvergil.hamstercore.attribute.DerivedAttributeModifierSystem;
import com.xlxyvergil.hamstercore.attribute.EntityAttributeRegistry;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import com.xlxyvergil.hamstercore.level.LevelSystem;
import com.xlxyvergil.hamstercore.network.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class EntityCapabilityAttacher {

    /**
     * 初始化实体的Capability并同步Attribute数据
     */
    public static void initializeEntityCapabilities(LivingEntity entity) {
        // 1. 初始化派系
        entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                .ifPresent(factionCap -> {
                    factionCap.setEntityType(entity.getType());
                });

        // 2. 初始化等级
        entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                .ifPresent(levelCap -> {
                    // 计算实体等级
                    int level = LevelSystem.calculateEntityLevel(entity);
                    levelCap.setLevel(level);
                    levelCap.setInitialized(true);
                });

        // 3. 应用基础属性修饰符
        BaseAttributeModifierSystem.applyBaseAttributeModifiers(entity);

        // 4. 应用衍生属性修饰符
        DerivedAttributeModifierSystem.applyDerivedModifiers(entity);

        // 5. 初始化护甲（基于派系和等级）
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .ifPresent(armorCap -> {
                    // 从Attribute系统获取最终护甲值
                    armorCap.setArmor(AttributeHelper.getArmor(entity));
                });

        // 6. 初始化生命值修饰符（基于等级）
        applyHealthModifier(entity);

        // 7. 初始化护盾（基于派系和等级）
        entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY)
                .ifPresent(shieldCap -> {
                    // 从Attribute系统获取最终护盾相关值
                    shieldCap.setMaxShield((float) AttributeHelper.getShield(entity));
                    shieldCap.setCurrentShield((float) AttributeHelper.getShield(entity)); // 实体生成时应初始化为满护盾
                    shieldCap.setRegenRate((float) AttributeHelper.getRegenRate(entity));
                    shieldCap.setRegenDelay((int) AttributeHelper.getRegenDelay(entity));
                    shieldCap.setRegenDelayDepleted((int) AttributeHelper.getDepletedRegenDelay(entity));
                    shieldCap.setImmunityTime((int) AttributeHelper.getImmunityTime(entity));
                    
                    // 设置其他默认值
                    shieldCap.setInsuranceAvailable(true);
                });

        // 8. 同步到客户端
        syncEntityCapabilitiesToClients(entity);
    }

    /**
     * 更新实体的Attribute相关Capability
     * 这是一个简化的更新方法，主要用于只需要更新Attribute相关Capability的场景
     * 注意：此方法不会初始化派系、等级等非Attribute相关Capability
     */
    public static void initializeCapabilities(LivingEntity entity) {
        // 应用基础属性修饰符
        BaseAttributeModifierSystem.applyBaseAttributeModifiers(entity);

        // 应用衍生属性修饰符
        DerivedAttributeModifierSystem.applyDerivedModifiers(entity);

        // 更新护盾Capability
        entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
            updateShieldCapabilityFromAttributes(entity, shieldCap);
        });

        // 更新护甲Capability
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(armorCap -> {
            updateArmorCapabilityFromAttributes(entity, armorCap);
        });
    }

    private static void applyHealthModifier(LivingEntity entity) {
        entity.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY)
                .ifPresent(healthModifierCap -> {
                    // 基于实体等级计算生命值加成
                    entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                            .ifPresent(levelCap -> {
                                int level = levelCap.getLevel();
                                double healthBonus = level; // 每级+1生命值

                                // 移除旧的修饰符
                                healthModifierCap.removeModifier();

                                // 添加新的生命值修饰符
                                if (healthBonus > 0) {
                                    healthModifierCap.setModifier(new AttributeModifier(
                                            UUID.randomUUID(),
                                            "Entity Level Health Bonus",
                                            healthBonus,
                                            AttributeModifier.Operation.ADDITION
                                    ));
                                }
                            });
                });
    }

    private static void updateShieldCapabilityFromAttributes(LivingEntity entity) {
        entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            updateShieldCapabilityFromAttributes(entity, cap);
        });
    }

    private static void updateShieldCapabilityFromAttributes(LivingEntity entity, EntityShieldCapability cap) {
        cap.setMaxShield((float) AttributeHelper.getShield(entity));
        cap.setRegenRate((float) AttributeHelper.getRegenRate(entity));
        cap.setRegenDelay((int) AttributeHelper.getRegenDelay(entity));
        cap.setRegenDelayDepleted((int) AttributeHelper.getDepletedRegenDelay(entity));
        cap.setImmunityTime((int) AttributeHelper.getImmunityTime(entity));
    }

    private static void updateArmorCapabilityFromAttributes(LivingEntity entity) {
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            updateArmorCapabilityFromAttributes(entity, cap);
        });
    }

    private static void updateArmorCapabilityFromAttributes(LivingEntity entity, EntityArmorCapability cap) {
        cap.setArmor(AttributeHelper.getArmor(entity));
    }

    public static void syncEntityCapabilitiesToClients(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        // 同步护盾数据
        EntityShieldSyncToClient.sync(entity);

        // 同步护甲数据
        EntityArmorSyncToClient.sync(entity);

        // 同步等级数据
        EntityLevelSyncToClient.sync(entity);

        // 同步派系数据
        EntityFactionSyncToClient.sync(entity);

        // 同步生命值修饰符数据
        EntityHealthModifierSyncToClient.sync(entity);
        
        // 同步状态效果数据
        syncEntityEffects(entity);
    }
    
    public static void syncEntityEffects(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        
        // 更新实体状态效果Capability并同步
        entity.getCapability(EntityEffectCapability.CAPABILITY).ifPresent(cap -> {
            // 获取实体当前的所有状态效果
            java.util.Collection<MobEffectInstance> effects = entity.getActiveEffects();
            // 更新Capability中的状态效果缓存
            cap.updateEffects(effects);
            // 同步到客户端
            EntityEffectCapability.sync(entity);
        });
    }
}