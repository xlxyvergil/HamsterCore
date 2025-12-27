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
                
                // 即使实体没有护盾，也要渲染状态效果
                // 检查实体是否有HamsterCore自定义的状态效果
                boolean hasHamsterCoreEffects = living.getActiveEffects().stream()
                    .anyMatch(effectInstance -> {
                        ResourceLocation effectRegistryName = BuiltInRegistries.MOB_EFFECT.getKey(effectInstance.getEffect());
                        return effectRegistryName != null && "hamstercore".equals(effectRegistryName.getNamespace());
                    });
                
                // 添加视线检测
                if (hasHamsterCoreEffects && !RenderUtils.raytrace(living)) {
                    // 完全按照 battery_shield 的渲染方式，为状态效果创建渲染上下文
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
                    
                    // 渲染状态效果图标 - 在适当位置显示（没有护盾条时的位置）
                    renderEffectIcons(living, guiGraphics, poseStack, event.getPartialTick());
                    
                    poseStack.popPose();
                }
            });
        }
    }
    
    /**
     * 渲染状态效果图标
     * 在实体头顶固定位置显示实体身上的状态效果
     */
    private static void renderEffectIcons(LivingEntity living, GuiGraphics guiGraphics, PoseStack poseStack, float partialTick) {
        // 检查实体是否拥有任何状态效果
        if (living.getActiveEffects().isEmpty()) {
            return; // 如果没有状态效果，则不渲染
        }
        
        // 计算状态效果图标的位置 - 固定位置
        int iconX = -38; // 固定位置
        int iconY = -25;  // 固定位置，在实体头顶的固定高度
        
        int effectIndex = 0;
        
        // 遍历实体的所有状态效果
        for (MobEffectInstance effectInstance : living.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            int amplifier = effectInstance.getAmplifier(); // 等级（0为第一级）
            
            // 检查是否为HamsterCore自定义的状态效果
            ResourceLocation effectRegistryName = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (effectRegistryName != null && "hamstercore".equals(effectRegistryName.getNamespace())) {
                // 获取状态效果的图标资源位置
                ResourceLocation effectTexture = new ResourceLocation("hamstercore", "textures/mob_effect/" + effectRegistryName.getPath() + ".png");
                
                // 渲染状态效果图标（6x6像素）
                // 使用GuiGraphics的blit方法渲染图标
                guiGraphics.blit(effectTexture, iconX, iconY, 0, 0, 6, 6, 6, 6);
                
                // 渲染状态效果等级（6x6像素大小）
                String levelText = String.valueOf(amplifier + 1); // 显示为1开始的等级
                guiGraphics.drawString(
                    Minecraft.getInstance().font, 
                    levelText, 
                    iconX + 6,   // 紧邻图标右侧
                    iconY,       // 与图标同一水平线
                    0xFFFFFF     // 白色文字
                );
                
                // 更新下一个图标的位置
                iconX += 14; // 每个图标占用14像素宽度（6像素图标 + 2像素间距 + 6像素等级文字）
                
                effectIndex++;
            }
        }
    }
}