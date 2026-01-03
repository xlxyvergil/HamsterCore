package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry.Effects;
import com.xlxyvergil.hamstercore.faction.Faction;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 辐射元素效果
 * 让目标敌我不分，优先攻击附近的友军，效果持续12秒
 */
@Mod.EventBusSubscriber
public class RadiationEffect extends ElementEffect {
    
    // 辐射效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public RadiationEffect() {
        super(MobEffectCategory.HARMFUL, 0x7CFC00); // 草坪绿
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        // 辐射效果主要通过事件监听器实现优先攻击友军的逻辑
        // 这里可以添加任何即时生效的效果（如果需要的话）
        
        // 如果需要立即检查并设置目标，可以在这里调用
        if (entity instanceof Mob && entity.level() instanceof ServerLevel) {
            // 立即检查一次友军目标
            LivingEntity targetAlly = findNearestAlly(entity, 16.0);
            if (targetAlly != null) {
                ((Mob) entity).setTarget(targetAlly);
            }
        }
    }
    
    /**
     * 检查实体是否受辐射影响
     * @param entity 实体
     * @return 是否受辐射影响
     */
    public static boolean isRadiated(LivingEntity entity) {
        return entity.hasEffect(Effects.RADIATION.get());
    }
    
    /**
     * 获取实体的派系
     * @param entity 实体
     * @return 派系名称，如果没有则返回"NEUTRAL"
     */
    public static String getEntityFaction(LivingEntity entity) {
        return entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
            .map(factionCap -> {
                Faction faction = factionCap.getFaction();
                return faction != null ? faction.name() : "NEUTRAL";
            })
            .orElse("NEUTRAL");
    }
    
    /**
     * 检查两个实体是否是同派系
     * @param entity1 实体1
     * @param entity2 实体2
     * @return 是否是同派系
     */
    public static boolean areSameFaction(LivingEntity entity1, LivingEntity entity2) {
        String faction1 = getEntityFaction(entity1);
        String faction2 = getEntityFaction(entity2);
        return faction1.equals(faction2);
    }
    
    /**
     * 寻找附近的同派系友军
     * @param entity 实体
     * @param searchRange 搜索范围
     * @return 最近的同派系友军，如果没有则返回null
     */
    public static LivingEntity findNearestAlly(LivingEntity entity, double searchRange) {
        if (entity.level().isClientSide || !(entity.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        
        String entityFaction = getEntityFaction(entity);
        
        // 寻找附近的同派系实体
        List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, 
            entity.getBoundingBox().inflate(searchRange),
            nearby -> {
                // 排除自己和玩家
                if (nearby == entity || nearby instanceof Player) {
                    return false;
                }
                
                // 检查是否是同派系
                return entityFaction.equals(getEntityFaction(nearby));
            });
        
        // 返回最近的同派系实体
        LivingEntity nearestAlly = null;
        double nearestDistance = searchRange * searchRange;
        
        for (LivingEntity nearby : nearbyEntities) {
            double distance = entity.distanceToSqr(nearby);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestAlly = nearby;
            }
        }
        
        return nearestAlly;
    }
    
    /**
     * 服务器tick事件监听器，为受辐射的Mob实体设置目标
     */
    @SubscribeEvent
    public static void onLivingTick(LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 只在服务端处理，且只对Mob实体有效
        if (entity.level().isClientSide || !(entity instanceof Mob mob)) {
            return;
        }
        
        // 检查是否有辐射效果
        if (!isRadiated(entity)) {
            return;
        }
        
        // 每20tick（1秒）检查一次
        if (entity.tickCount % 20 != 0) {
            return;
        }
        
        // 寻找附近的同派系友军
        LivingEntity targetAlly = findNearestAlly(entity, 16.0);
        
        if (targetAlly != null) {
            // 设置为目标
            mob.setTarget(targetAlly);
        }
    }
}