package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
        
        // 更新实体身上的爆炸效果
        BlastManager.updateBlasts(entity);
        
        // 更新实体身上的腐蚀效果
        CorrosiveManager.updateCorrosives(entity);
        
        // 更新实体身上的火焰效果（护甲削减部分）
        HeatManager.updateHeatEffects(entity);
        
        // 更新所有毒气云（全局更新，不依赖于特定实体）
        GasManager.updateAllGasClouds();
    }
    
    /**
     * 监听实体死亡事件
     * 在效果结束或被清除时执行清理操作
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 清理实体身上的所有元素效果
        ElementEffectManager.clearEffects(entity);
        
        // 清理实体身上的所有DoT效果
        DoTManager.clearDoTs(entity);
        
        // 清理实体身上的所有爆炸效果
        BlastManager.clearBlasts(entity);
        
        // 清理实体身上的所有腐蚀效果
        CorrosiveManager.clearCorrosives(entity);
        
        // 清理实体身上的所有火焰效果
        HeatManager.clearHeatEffects(entity);
        
        // 注意：不清理毒气云，因为即使原始目标死亡，毒气云依然存在
    }
}