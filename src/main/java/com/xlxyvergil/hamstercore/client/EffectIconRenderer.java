package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xlxyvergil.hamstercore.HamsterCore;

import com.xlxyvergil.hamstercore.client.renderer.EntityEffectRenderer;
import com.xlxyvergil.hamstercore.client.util.RenderUtils;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.lang.Math;

/**
 * 状态效果图标渲染器 - 专门处理实体头顶状态效果的显示
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EffectIconRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 检查是否为正确的渲染阶段
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            Vec3 fromPos = minecraft.player.getEyePosition(event.getPartialTick());

            // 获取范围内的实体
            minecraft.level.getEntities(minecraft.player,
                    AABB.ofSize(minecraft.player.position(), 32, 32, 32),
                    EntitySelector.LIVING_ENTITY_STILL_ALIVE).forEach((entity) -> {
                LivingEntity living = (LivingEntity) entity;

                // 进行视线检测
                boolean isBlocked = RenderUtils.raytrace(living);
                if (isBlocked) return;

                // 从管理器中获取缓存的状态效果
                java.util.List<MobEffectInstance> cachedEffects = EntityEffectManager.getEntityEffects(living.getId());
                
                // 检查实体是否有任何状态效果
                boolean hasEffects = !cachedEffects.isEmpty();

                if (hasEffects) {
                    // 完全按照 battery_shield 的方式获取 GuiGraphics
                    final GuiGraphics guiGraphics = ((com.xlxyvergil.hamstercore.api.IRenderContextProvider) Minecraft.getInstance()).getGuiGraphics(event.getPoseStack());
                    PoseStack poseStack = guiGraphics.pose();
                    poseStack.pushPose();

                    // 计算位置 - 参考护盾的实现，将图标显示在实体右侧
                    // 为了正确处理不同视角，我们需要确保图标始终在实体的右侧（相对于玩家视角）
                    Vec3 entityPos = living.getPosition(event.getPartialTick());
                    
                    // 获取玩家相机的yaw（水平旋转）角度，用于确定实体的右侧方向
                    float cameraYaw = minecraft.getEntityRenderDispatcher().camera.getYRot();
                    
                    // 计算相对于相机视角的右侧位置
                    // 将实体的右侧偏移转换为世界坐标，考虑相机方向
                    double xzOffset = living.getBbWidth() * 0.5f + 0.5f;
                    // 右侧偏移：从相机角度看，实体右侧的方向
                    // 旋转-90度来获得垂直于相机观察方向的右侧方向
                    double offsetX = xzOffset * Math.sin(Math.toRadians(cameraYaw - 90));
                    double offsetZ = -xzOffset * Math.cos(Math.toRadians(cameraYaw - 90));
                    
                    Vec3 livingFrom = entityPos.add(offsetX, living.getBbHeight() * 0.5f, offsetZ); // 放在实体右侧中间位置
                    Vec3 posFromPlayer = fromPos.vectorTo(livingFrom);

                    // 变换矩阵 - 完全按照 battery_shield
                    poseStack.translate(posFromPlayer.x, posFromPlayer.y, posFromPlayer.z);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-minecraft.getEntityRenderDispatcher().camera.getYRot()));
                    poseStack.mulPose(Axis.XP.rotationDegrees(minecraft.getEntityRenderDispatcher().camera.getXRot()));
                    poseStack.scale(-0.025f, -0.025f, 1);

                    // 渲染状态效果图标 - 使用EntityEffectRenderer进行渲染，从上方开始垂直排列，整体向上移动10像素
                    EntityEffectRenderer.renderEffectIcons(poseStack, -9, -46, cachedEffects, living);

                    poseStack.popPose();
                }
            });
        }
    }

}