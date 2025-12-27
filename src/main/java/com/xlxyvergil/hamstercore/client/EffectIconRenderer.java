package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.client.renderer.EntityEffectRenderer;
import com.xlxyvergil.hamstercore.client.util.RenderUtils;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityEffectCapability;
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

                // 从Capability中获取缓存的状态效果
                EntityEffectCapability.getCapabilityOptional(living).ifPresent(cap -> {
                    java.util.List<MobEffectInstance> cachedEffects = cap.getCachedEffects();
                    
                    // 检查实体是否有任何状态效果（可以扩展为只渲染HamsterCore自定义的状态效果）
                    boolean hasEffects = !cachedEffects.isEmpty();

                    if (hasEffects) {
                        // 完全按照 battery_shield 的方式获取 GuiGraphics
                        final GuiGraphics guiGraphics = ((com.xlxyvergil.hamstercore.api.IRenderContextProvider) Minecraft.getInstance()).getGuiGraphics(event.getPoseStack());
                        PoseStack poseStack = guiGraphics.pose();
                        poseStack.pushPose();

                        // 计算位置 - 按照 battery_shield 的方式
                        Vec3 livingFrom = living.getPosition(event.getPartialTick()).add(0, living.getBbHeight() + 0.5f, 0);
                        Vec3 posFromPlayer = fromPos.vectorTo(livingFrom);

                        // 变换矩阵 - 完全按照 battery_shield
                        poseStack.translate(posFromPlayer.x, posFromPlayer.y, posFromPlayer.z);
                        poseStack.mulPose(Axis.YP.rotationDegrees(-minecraft.getEntityRenderDispatcher().camera.getYRot()));
                        poseStack.mulPose(Axis.XP.rotationDegrees(minecraft.getEntityRenderDispatcher().camera.getXRot()));
                        poseStack.scale(-0.025f, -0.025f, 1);

                        // 渲染状态效果图标 - 使用EntityEffectRenderer进行渲染
                        EntityEffectRenderer.renderEffectIcons(poseStack, -38, -25, cachedEffects);

                        poseStack.popPose();
                    }
                });
            });
        }
    }

}