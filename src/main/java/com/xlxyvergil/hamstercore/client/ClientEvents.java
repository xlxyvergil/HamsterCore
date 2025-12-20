package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.ClientConfig;
import com.xlxyvergil.hamstercore.config.DisplayConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.client.util.RayTrace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderNamePlate(RenderNameTagEvent event) {
        // 检查配置是否启用了名称标签信息显示
        if (!DisplayConfig.getInstance().isShowNameTagInfo()) {
            return;
        }
        
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            // 检查实体是否有派系Capability
            livingEntity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(factionCap -> {
                // 获取实体等级
                int[] levelHolder = {1};
                livingEntity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                    levelHolder[0] = levelCap.getLevel();
                });
                
                // 获取实体护甲值
                double[] armorHolder = {0};
                livingEntity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(armorCap -> {
                    armorHolder[0] = armorCap.getArmor();
                });
                
                // 创建等级组件
                MutableComponent levelComponent = Component.literal("Lv." + levelHolder[0] + " ");
                
                // 创建派系名称组件
                Component factionComponent = Component.translatable("hamstercore.faction." + factionCap.getFaction().name().toLowerCase());
                
                // 创建护甲值组件
                Component armorComponent = Component.literal(" (" + String.format("%.1f", armorHolder[0]) + ")");
                
                // 组合等级、派系和护甲组件
                Component fullComponent = levelComponent.append(factionComponent).append(armorComponent);
                
                // 调整偏移量，使派系标签显示在L2Hostility信息下方
                float offset = -0.3f; // 显示在默认位置下方
                
                // 渲染派系标签
                renderFactionTag(event, fullComponent, event.getPoseStack(), offset);
            });
        }
    }

    protected static void renderFactionTag(RenderNameTagEvent event, Component text, PoseStack pose, float offset) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        
        // 计算距离，只在适当距离内渲染
        double distanceSqr = dispatcher.distanceToSqr(event.getEntity());
        if (distanceSqr > 1024.0D) { // 32 blocks^2
            return;
        }
        
        // 视线检测
        RayTrace rayTrace = new RayTrace();
        Vec3 playerEyePosition = mc.player.getEyePosition(1.0F);
        if (!rayTrace.entityReachable(32, mc, playerEyePosition, (LivingEntity) event.getEntity())) {
            return; // 没有视线接触，跳过渲染
        }

        float f = event.getEntity().getNameTagOffsetY() + offset;
        pose.pushPose();
        pose.translate(0.0F, f, 0.0F);
        pose.mulPose(dispatcher.cameraOrientation());
        pose.scale(-0.025F, -0.025F, 0.025F);

        Font font = mc.font;
        float centerX = (float) (-font.width(text) / 2);
        Font.DisplayMode mode = mc.player.hasLineOfSight(event.getEntity()) ?
                Font.DisplayMode.SEE_THROUGH :
                Font.DisplayMode.NORMAL;
        
        // 渲染文本背景
        float backgroundOpacity = mc.options.getBackgroundOpacity(0.25F);
        int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;
        
        // 渲染文本
        font.drawInBatch(
            text, 
            centerX, 
            0, 
            0xFFFFFFFF, // 白色文字
            false, 
            pose.last().pose(), 
            event.getMultiBufferSource(), 
            mode, 
            backgroundColor, 
            LightTexture.FULL_BRIGHT
        );

        pose.popPose();
    }
}