package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapability;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.network.EntityShieldSyncToClient;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class ShieldEvents {
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 获取实体的护盾能力
        EntityShieldCapability shieldCap = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
        
        // 检查实体是否真正拥有有效的护盾能力
        if (shieldCap == null || shieldCap.getMaxShield() < 0) {
            return; // 没有护盾能力，直接返回
        }

        // 计算可以抵消的伤害量（1点伤害需要20点护盾抵消）
        float amount = event.getAmount();
        float shieldRequired = amount * 20.0f;
        float actualShieldConsumed = Math.min(shieldRequired, shieldCap.getCurrentShield());
        float damageAbsorbed = actualShieldConsumed / 20.0f;
        
        // 减少护盾值
        float oldShield = shieldCap.getCurrentShield();
        shieldCap.setCurrentShield(oldShield - actualShieldConsumed);
        
        // 更新受伤时间（用于恢复延迟）
        shieldCap.setLastHurtTime(entity.level().getGameTime());
        
        // 减少实际受到的伤害
        event.setAmount(amount - damageAbsorbed);
        
        // 如果伤害完全被护盾吸收，将伤害设置为0而不是取消事件
        // 这样可以确保负面状态效果仍然能够应用
        if (damageAbsorbed >= amount) {
            event.setAmount(0);
        }
        
        // 检查护盾是否被击破（从有护盾变为无护盾）
        boolean shieldBroken = oldShield > 0 && shieldCap.getCurrentShield() <= 0;
        
        // 同步护盾值到客户端（确保在服务器端）
        if (!entity.level().isClientSide()) {
            // 立即同步所有护盾变化，包括减少的情况
            PacketHandler.NETWORK.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield(), shieldCap.isGatingActive())
            );
        }
        
        // 处理磁力效果的破盾电击伤害
        if (shieldBroken) {
            handleMagneticShieldBreak(entity, event);
        }
        
        // 检查是否需要触发护盾保险机制（仅限玩家）
        if (entity instanceof Player player && shieldCap.getCurrentShield() <= 0 && !shieldCap.isGatingActive() && shieldCap.isInsuranceAvailable()) {
            int immunityTime = shieldCap.getImmunityTime();
            if (immunityTime > 0) {
                // 应用无敌效果，将毫秒转换为ticks（1秒=20 ticks）
                int immunityTicks = (int) (immunityTime / 50); // 1000ms = 20 ticks, 所以 1ms = 1/50 ticks
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, immunityTicks, 10, false, false, false));
                
                // 设置护盾保险激活状态
                shieldCap.setGatingActive(true);
                shieldCap.setGatingDuration(immunityTicks);
                
                // 设置护盾保险不可用，直到护盾恢复满
                shieldCap.setInsuranceAvailable(false);
                
                // 立即同步护盾保险状态到客户端
                PacketHandler.NETWORK.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new EntityShieldSyncToClient(player.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield(), true)
                );
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            Player player = event.player;
            
            // 获取玩家的护盾能力
            EntityShieldCapability shieldCap = player.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
            
            // 检查玩家是否真正拥有有效的护盾能力
            if (shieldCap == null || shieldCap.getMaxShield() < 0) {
                return;
            }
            
            // 记录旧的护盾值
            float oldCurrentShield = shieldCap.getCurrentShield();
            float oldMaxShield = shieldCap.getMaxShield();
            
            // 处理护盾恢复
            handleShieldRegeneration(player, shieldCap);
            
                    // 处理护盾保险机制
                    handleShieldGating(player, shieldCap);
            
            // 如果护盾值发生了变化，则同步到客户端
            if (oldCurrentShield != shieldCap.getCurrentShield() || oldMaxShield != shieldCap.getMaxShield()) {
                PacketHandler.NETWORK.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new EntityShieldSyncToClient(player.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield(), shieldCap.isGatingActive())
                );
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingUpdate(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide() && event.level instanceof ServerLevel serverLevel) {
            // 遍历所有实体并处理它们的护盾恢复
            for (Entity entityObject : serverLevel.getAllEntities()) {
                if (entityObject instanceof LivingEntity entity && !(entity instanceof Player)) { // 玩家已经在onPlayerTick中处理过了
                    EntityShieldCapability shieldCap = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
                    
                    // 检查实体是否真正拥有有效的护盾能力
                    if (shieldCap == null || shieldCap.getMaxShield() < 0) {
                        continue;
                    }
                    
                    // 记录旧的护盾值
                    float oldCurrentShield = shieldCap.getCurrentShield();
                    float oldMaxShield = shieldCap.getMaxShield();
                    
                    handleShieldRegeneration(entity, shieldCap);
                    
                    // 如果护盾值发生了变化，则同步到客户端
                    if (oldCurrentShield != shieldCap.getCurrentShield() || oldMaxShield != shieldCap.getMaxShield()) {
                        PacketHandler.NETWORK.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                            new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield(), shieldCap.isGatingActive())
                        );
                    }
                }
            }
        }
    }
    
    /**
     * 处理护盾恢复
     */
    private static void handleShieldRegeneration(LivingEntity entity, EntityShieldCapability shieldCap) {
        long currentTime = entity.level().getGameTime();
        long timeSinceLastHurt = currentTime - shieldCap.getLastHurtTime();
        
        // 确定使用的恢复延迟
        int regenDelay = shieldCap.getCurrentShield() <= 0 ? shieldCap.getRegenDelayDepleted() : shieldCap.getRegenDelay();
                
        // 如果距离上次受伤超过了恢复延迟时间且护盾未满，则开始恢复
        // regenDelay现在是以毫秒为单位，需要转换为ticks进行比较 (1 tick = 50毫秒)
        long regenDelayInTicks = regenDelay / 50;
        if (timeSinceLastHurt >= regenDelayInTicks && shieldCap.getCurrentShield() < shieldCap.getMaxShield()) {
            // 每tick恢复的护盾值
            float tickRegen = shieldCap.getRegenRate() / 20.0f;
            
            // 恢复护盾
            float newShield = Math.min(shieldCap.getMaxShield(), shieldCap.getCurrentShield() + tickRegen);
            shieldCap.setCurrentShield(newShield);
            
            // 如果护盾已经完全恢复，重新启用护盾保险
            if (newShield >= shieldCap.getMaxShield()) {
                shieldCap.setInsuranceAvailable(true);
            }       
         }
    }
    
    /**
     * 处理护盾保险机制
     */
    private static void handleShieldGating(Player player, EntityShieldCapability shieldCap) {
        // 处理护盾保险持续时间
        if (shieldCap.isGatingActive()) {
            int newDuration = shieldCap.getGatingDuration() - 1;
            shieldCap.setGatingDuration(newDuration);
            
            if (newDuration <= 0) {
                shieldCap.setGatingActive(false);
                // 同步护盾保险状态结束到客户端
                PacketHandler.NETWORK.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new EntityShieldSyncToClient(player.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield(), false)
                );
            }
        }
    }
    
    /**
     * 处理磁力效果的破盾电击伤害
     * 当护盾被击破时，如果目标具有磁力效果，则对攻击者造成电击伤害
     */
    private static void handleMagneticShieldBreak(LivingEntity entity, LivingHurtEvent event) {
        // 检查目标是否具有磁力效果
        if (entity.hasEffect(ElementEffectRegistry.Effects.MAGNETIC.get())) {
            net.minecraft.world.effect.MobEffectInstance magneticEffect = entity.getEffect(ElementEffectRegistry.Effects.MAGNETIC.get());
            if (magneticEffect != null) {
                int amplifier = magneticEffect.getAmplifier();
                
                // 计算破盾后电击伤害
                float electricDamage = com.xlxyvergil.hamstercore.element.effect.effects.MagneticEffect.calculateShieldBreakElectricDamage(entity, amplifier);
                
                // 对攻击者造成电击伤害
                if (electricDamage > 0 && event.getSource().getEntity() != null) {
                    event.getSource().getEntity().hurt(event.getSource().getEntity().damageSources().magic(), electricDamage);
                }
            }
        }
    }
}