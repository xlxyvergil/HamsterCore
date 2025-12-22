package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 元素效果事件管理器
 * 监听游戏事件并触发相应元素效果
 * 与ElementTriggerHandler集成，处理元素触发后的效果应用
 */
@Mod.EventBusSubscriber
public class ElementEffectEventManager {
    
    /**
     * 监听实体受伤事件
     * 在这里可以处理元素效果的触发和应用
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 获取攻击者和被攻击者
        // 这里可以根据伤害类型和元素属性来应用相应的元素效果
        
        // 示例：如果攻击者使用了带元素的武器，可以在这里触发元素效果
        // ElementTriggerHandler.handleElementTriggers(...);
    }
    
    /**
     * 监听实体更新事件
     * 每tick更新持续性效果状态
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 更新实体身上的元素效果
        ElementEffectManager.updateEffects(entity);
        
        // 更新实体身上的DoT效果
        DoTManager.updateDoTs(entity);
    }
    
    /**
     * 监听实体死亡事件
     * 在效果结束或被清除时执行清理操作
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingEvent.LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 清理实体身上的所有元素效果
        ElementEffectManager.clearEffects(entity);
        
        // 清理实体身上的所有DoT效果
        DoTManager.clearDoTs(entity);
    }
}