package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.client.renderer.EntityShieldRenderer;
import com.xlxyvergil.hamstercore.config.ClientConfig;
import com.xlxyvergil.hamstercore.config.ShieldConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.client.util.RayTrace;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShieldBarRenderer {
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 检查配置是否启用了实体护盾条显示
        if (!ClientConfig.getInstance().isShowEntityShieldBar()) {
            return;
        }
        
        // 在粒子渲染完成后进行渲染，确保护盾条显示在最上层
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
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
                // 检查实体是否在渲染距离内（16格以内）
                double distanceSq = playerEyePosition.distanceToSqr(entity.position());
                if (distanceSq > 32 * 32) {
                    return; // 距离太远，跳过渲染
                }
                
                // 使用更精确的视线检测
                RayTrace rayTrace = new RayTrace();
                if (!rayTrace.entityReachable(32, mc, playerEyePosition, livingEntity)) {
                    return; // 没有视线接触，跳过渲染
                }
                
                // 检查实体是否应该有护盾
                if (!shouldEntityHaveShield(livingEntity)) {
                    return; // 实体不应该有护盾，跳过渲染
                }
                
                // 检查实体是否拥有护盾能力并且最大护盾值大于0
                // 如果满足条件，则同时渲染护盾条和百分比数据（它们在同一个渲染调用中）
                livingEntity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
                    float currentShield = shieldCap.getCurrentShield();
                    float maxShield = shieldCap.getMaxShield();
                    if (maxShield > 0) { // 只要最大护盾值大于0就渲染，即使当前护盾为0也要显示空的护盾条
                        // 这个调用会同时渲染护盾条和百分比数据
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
        // 直接检查实体是否拥有护盾能力，这是最准确的判断方式
        // 因为我们已经在EntityCapabilityAttacher中根据entityBaseShields配置决定是否附加护盾能力
        return entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY)
            .map(shieldCap -> {
                // 检查护盾值是否有效（大于等于0）
                float maxShield = shieldCap.getMaxShield();
                return maxShield >= 0;
            })
            .orElse(false);
    }
}