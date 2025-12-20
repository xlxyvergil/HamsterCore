package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapability;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.network.EntityShieldSyncToClient;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
        
        if (shieldCap == null || shieldCap.getCurrentShield() <= 0) {
            return; // 没有护盾能力或者护盾为0，直接返回
        }

        // 计算可以抵消的伤害量（1点伤害需要20点护盾抵消）
        float amount = event.getAmount();
        float shieldRequired = amount * 20.0f;
        float actualShieldConsumed = Math.min(shieldRequired, shieldCap.getCurrentShield());
        float damageAbsorbed = actualShieldConsumed / 20.0f;
        
        // 减少护盾值
        shieldCap.setCurrentShield(shieldCap.getCurrentShield() - actualShieldConsumed);
        
        // 更新受伤时间（用于恢复延迟）
        shieldCap.setLastHurtTime(entity.level().getGameTime());
        
        // 减少实际受到的伤害
        event.setAmount(amount - damageAbsorbed);
        
        // 如果伤害完全被护盾吸收，则取消事件
        if (damageAbsorbed >= amount) {
            event.setCanceled(true);
        }
        
        // 同步护盾值到客户端
        if (!entity.level().isClientSide() && entity.level() instanceof ServerLevel) {
            PacketHandler.NETWORK.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield())
            );
        }
        
        // 检查是否需要触发护盾保险机制（仅限玩家）
        if (entity instanceof Player player && shieldCap.getCurrentShield() <= 0 && !shieldCap.isGatingActive()) {
            int immunityTime = shieldCap.getImmunityTime();
            if (immunityTime > 0) {
                // 应用无敌效果
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, immunityTime, 10, false, false, false));
                
                // 设置护盾保险激活状态
                shieldCap.setGatingActive(true);
                shieldCap.setGatingDuration(immunityTime);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            Player player = event.player;
            
            // 获取玩家的护盾能力
            EntityShieldCapability shieldCap = player.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
            
            if (shieldCap == null) {
                return;
            }
            
            // 处理护盾恢复
            handleShieldRegeneration(player, shieldCap);
            
            // 处理护盾保险机制
            handleShieldGating(player, shieldCap);
        }
    }
    
    @SubscribeEvent
    public static void onLivingUpdate(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide() && event.level instanceof ServerLevel serverLevel) {
            // 遍历所有实体并处理它们的护盾恢复
            for (Entity entityObject : serverLevel.getAllEntities()) {
                if (entityObject instanceof LivingEntity entity && !(entity instanceof Player)) { // 玩家已经在onPlayerTick中处理过了
                    EntityShieldCapability shieldCap = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
                    
                    if (shieldCap != null) { // 不再检查护盾是否低于最大值，始终同步以确保客户端显示正确
                        handleShieldRegeneration(entity, shieldCap);
                        
                        // 同步护盾值到客户端
                        PacketHandler.NETWORK.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                            new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield())
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
        if (timeSinceLastHurt >= regenDelay && shieldCap.getCurrentShield() < shieldCap.getMaxShield()) {
            // 每tick恢复的护盾值
            float tickRegen = shieldCap.getRegenRate() / 20.0f;
            
            // 恢复护盾
            float newShield = Math.min(shieldCap.getMaxShield(), shieldCap.getCurrentShield() + tickRegen);
            shieldCap.setCurrentShield(newShield);
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
            }
        }
    }
}