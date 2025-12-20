package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.client.renderer.EntityShieldRenderer;
import com.xlxyvergil.hamstercore.config.ClientConfig;
import com.xlxyvergil.hamstercore.config.ShieldConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShieldBarRenderer {
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 检查配置是否启用了实体护盾条显示
        if (!ClientConfig.getInstance().isShowEntityShieldBar()) {
            return;
        }
        
        // 只在实体渲染阶段处理
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        
        // 获取玩家视角位置
        Vec3 playerEyePosition = mc.player.getEyePosition(event.getPartialTick());
        
        // 遍历所有实体并渲染它们的护盾条
        mc.level.entitiesForRendering().forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity && entity != mc.player) {
                // 检查实体是否在渲染距离内（例如64格以内）
                double distanceSq = playerEyePosition.distanceToSqr(entity.position());
                if (distanceSq > 64 * 64) {
                    return; // 距离太远，跳过渲染
                }
                
                // 简化视线检测，使用Minecraft内置的hasLineOfSight方法
                if (!mc.player.hasLineOfSight(livingEntity)) {
                    return; // 没有视线接触，跳过渲染
                }
                
                // 检查实体是否应该有护盾
                if (!shouldEntityHaveShield(livingEntity)) {
                    return; // 实体不应该有护盾，跳过渲染
                }
                
                // 检查实体是否拥有护盾能力并且护盾值大于0
                livingEntity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
                    float currentShield = shieldCap.getCurrentShield();
                    float maxShield = shieldCap.getMaxShield();
                    if (maxShield > 0 && currentShield > 0) {
                        EntityShieldRenderer.renderEntityShield(
                            livingEntity,
                            currentShield,
                            maxShield,
                            event.getPartialTick(),
                            event.getPoseStack(),
                            mc.renderBuffers().bufferSource()
                        );
                    }
                });
            }
        });
    }
    
    /**
     * 检查实体是否应该有护盾
     * @param entity 要检查的实体
     * @return 如果实体应该有护盾返回true，否则返回false
     */
    private static boolean shouldEntityHaveShield(LivingEntity entity) {
        ShieldConfig shieldConfig = ShieldConfig.load();
        
        // 1. 玩家总是可以有护盾
        if (entity instanceof net.minecraft.world.entity.player.Player) {
            return true;
        }
        
        // 2. 检查是否是配置文件中指定的实体
        if (shieldConfig.hasShieldConfigured(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()))) {
            return true;
        }
        
        // 3. 检查是否是允许护盾的派系生物
        return entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
            .map(factionCap -> ShieldConfig.isFactionShieldEnabled(factionCap.getFaction().name()))
            .orElse(false);
    }
}