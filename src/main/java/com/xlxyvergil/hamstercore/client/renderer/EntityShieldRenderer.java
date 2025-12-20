package com.xlxyvergil.hamstercore.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapability;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * 实体护盾渲染器
 * 负责在实体头顶渲染护盾条
 */
public class EntityShieldRenderer {
    
    private static final ResourceLocation SHIELD_ICONS = new ResourceLocation("hamstercore", "textures/gui/icons.png");
    
    /**
     * 渲染实体的护盾条
     * @param entity 实体
     * @param partialTick 部分刻
     * @param poseStack 渲染矩阵栈
     * @param buffer 缓冲区
     */
    public static void renderEntityShield(LivingEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        // 检查实体是否拥有护盾能力
        EntityShieldCapability shieldCap = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
        if (shieldCap == null) {
            return;
        }
        
        // 获取当前护盾值和最大护盾值
        float currentShield = shieldCap.getCurrentShield();
        float maxShield = shieldCap.getMaxShield();
        
        // 只有当最大护盾值大于0且当前护盾值大于0时才渲染
        if (maxShield <= 0 || currentShield <= 0) {
            return;
        }
        
        // 检查玩家是否能看到这个实体
        if (!Minecraft.getInstance().player.hasLineOfSight(entity)) {
            return;
        }
        
        // 获取相机位置
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        
        // 获取实体的位置
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        
        // 计算实体头顶的位置（在实体上方一点）
        Vec3 entityPos = new Vec3(x, y + entity.getBbHeight() + 0.5, z);
        
        // 计算相对于相机的位置
        double dx = entityPos.x - cameraPos.x;
        double dy = entityPos.y - cameraPos.y;
        double dz = entityPos.z - cameraPos.z;
        
        // 应用渲染变换
        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);
        
        // 绘制护盾条
        renderShieldBar(currentShield, maxShield, poseStack, buffer, entity);
        
        poseStack.popPose();
    }
    
    /**
     * 渲染护盾条
     * @param currentShield 当前护盾值
     * @param maxShield 最大护盾值
     * @param poseStack 渲染矩阵栈
     * @param buffer 缓冲区
     * @param entity 实体
     */
    private static void renderShieldBar(float currentShield, float maxShield, PoseStack poseStack, MultiBufferSource buffer, LivingEntity entity) {
        // 计算护盾条的填充比例
        float shieldPercent = currentShield / maxShield;
        
        // 护盾条的基本尺寸
        int barWidth = 40;
        int barHeight = 5;
        
        // 根据护盾百分比确定颜色
        int color;
        if (shieldPercent > 0.7) {
            // 绿色
            color = 0xFF00FF00;
        } else if (shieldPercent > 0.3) {
            // 黄色
            color = 0xFFFFFF00;
        } else {
            // 红色
            color = 0xFFFF0000;
        }
        
        // 获取渲染器
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.gui());
        
        // 绘制护盾条背景（深灰色）
        fillRect(matrix, vertexConsumer, -barWidth/2, 0, barWidth, barHeight, 0xFF404040);
        
        // 绘制护盾条前景
        int fillWidth = (int)(barWidth * shieldPercent);
        if (fillWidth > 0) {
            fillRect(matrix, vertexConsumer, -barWidth/2 + 1, 1, fillWidth - 2, barHeight - 2, color);
        }
        
        // 绘制护盾数值
        Font font = Minecraft.getInstance().font;
        String shieldText = String.format("%.0f/%.0f", currentShield, maxShield);
        Component shieldComponent = Component.literal(shieldText);
        int textWidth = font.width(shieldComponent);
        
        // 设置文字渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        
        // 渲染文字（在护盾条下方）
        poseStack.pushPose();
        poseStack.translate(0, barHeight + 2, 0);
        font.drawInBatch(
            shieldComponent,
            -textWidth / 2.0F,
            0.0F,
            0xFFFFFFFF,
            false,
            matrix,
            buffer,
            Font.DisplayMode.NORMAL,
            0,
            15728880
        );
        poseStack.popPose();
        
        // 结束渲染
        RenderSystem.disableBlend();
    }
    
    /**
     * 绘制矩形
     * @param matrix 矩阵
     * @param consumer 顶点消费者
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param color 颜色
     */
    private static void fillRect(Matrix4f matrix, VertexConsumer consumer, int x, int y, int width, int height, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        
        // 左上角
        consumer.vertex(matrix, x, y + height, 0).color(r, g, b, a).endVertex();
        // 右上角
        consumer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a).endVertex();
        // 右下角
        consumer.vertex(matrix, x + width, y, 0).color(r, g, b, a).endVertex();
        // 左下角
        consumer.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
    }
}