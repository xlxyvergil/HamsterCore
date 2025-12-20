package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.client.renderer.EntityShieldRenderer;
import com.xlxyvergil.hamstercore.config.ClientConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
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
        if (!ClientConfig.SHOW_ENTITY_SHIELD_BAR.get()) {
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
                
                // 检查实体是否在玩家视线范围内
                AABB entityBoundingBox = entity.getBoundingBoxForCulling();
                if (!mc.player.hasLineOfSight(entity)) {
                    return; // 没有视线接触，跳过渲染
                }
                
                // 检查实体是否拥有护盾能力并且护盾值大于0
                livingEntity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
                    if (shieldCap.getMaxShield() > 0 && shieldCap.getCurrentShield() > 0) {
                        EntityShieldRenderer.renderEntityShield(
                            livingEntity,
                            event.getPartialTick(),
                            event.getPoseStack(),
                            mc.renderBuffers().bufferSource()
                        );
                    }
                });
            }
        });
    }
}