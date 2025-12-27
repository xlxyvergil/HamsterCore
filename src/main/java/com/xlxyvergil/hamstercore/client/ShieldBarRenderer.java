package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.client.renderer.EntityShieldRenderer;
import com.xlxyvergil.hamstercore.client.util.RenderUtils;
import com.xlxyvergil.hamstercore.config.ClientConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.api.IRenderContextProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
 * 护盾条渲染器 - 完全参照 battery_shield 的实现，进行适当简化
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShieldBarRenderer {
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 检查配置是否启用了实体护盾条显示
        if (!ClientConfig.getInstance().isShowEntityShieldBar()) {
            return;
        }
        
        // 完全按照 battery_shield 的实现
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        
        // 完全按照 battery_shield 的方式获取 GuiGraphics
        final GuiGraphics guiGraphics = ((IRenderContextProvider) Minecraft.getInstance()).getGuiGraphics(event.getPoseStack());
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            Vec3 fromPos = minecraft.player.getEyePosition(event.getPartialTick());
            
            // 完全按照 battery_shield 的实体获取方式
            minecraft.level.getEntities(minecraft.player,
                    AABB.ofSize(minecraft.player.position(), 32, 32, 32),
                    EntitySelector.LIVING_ENTITY_STILL_ALIVE).forEach((entity) -> {
                LivingEntity living = (LivingEntity) entity;
                
                // 完全按照 battery_shield 的视线检测
                if (RenderUtils.raytrace(living)) return;
                
                // 检查实体是否拥有护盾能力并且最大护盾值大于0
                living.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
                    float currentShield = shieldCap.getCurrentShield();
                    float maxShield = shieldCap.getMaxShield();
                    
                    if (maxShield > 0) {
                        // 完全按照 battery_shield 的渲染方式
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
                        
                        // 渲染护盾条 - 使用我们简化的方法
                        EntityShieldRenderer.renderShieldBar(
                            currentShield,
                            maxShield,
                            poseStack,
                            minecraft.renderBuffers().bufferSource()
                        );
                        
                        poseStack.popPose();
                    }
                });
            });
        }
    }
}