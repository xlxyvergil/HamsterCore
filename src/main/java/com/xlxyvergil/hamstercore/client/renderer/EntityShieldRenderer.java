package com.xlxyvergil.hamstercore.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

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
     * 渲染实体的护盾条（纯渲染函数，不包含任何检查逻辑）
     * @param entity 实体
     * @param currentShield 当前护盾值
     * @param maxShield 最大护盾值
     * @param partialTick 部分刻
     * @param poseStack 渲染矩阵栈
     * @param buffer 缓冲区
     */
    public static void renderEntityShield(LivingEntity entity, float currentShield, float maxShield, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        // 获取相机位置
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        
        // 获取实体的位置
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        
        // 计算实体头顶的位置（在实体上方一点）
        Vec3 entityPos = new Vec3(x, y + entity.getBbHeight() + 0.3, z);
        
        // 计算相对于相机的位置
        double dx = entityPos.x - cameraPos.x;
        double dy = entityPos.y - cameraPos.y;
        double dz = entityPos.z - cameraPos.z;
        
        // 应用渲染变换（参考ClientEvents中的renderFactionTag方法）
        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F); // X轴使用正值避免镜像翻转
        
        // 绘制护盾条
        renderShieldBar(currentShield, maxShield, poseStack, buffer);
        
        poseStack.popPose();
    }
    
    /**
     * 渲染护盾条
     * @param currentShield 当前护盾值
     * @param maxShield 最大护盾值
     * @param poseStack 渲染矩阵栈
     * @param buffer 缓冲区
     */
    private static void renderShieldBar(float currentShield, float maxShield, PoseStack poseStack, MultiBufferSource buffer) {
        // 计算护盾条的填充比例
        float shieldPercent = currentShield / maxShield;
        
        // 护盾条的基本尺寸（参考ClientEvents中的文本尺寸）
        Font font = Minecraft.getInstance().font;
        String sampleText = "100%"; // 百分比显示的最大长度
        int textWidth = font.width(sampleText);
        int barWidth = textWidth + 4; // 护盾条比文本稍宽一些
        int barHeight = 3; // 与文本高度协调
        
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
        
        // 绘制护盾数值（显示百分比）
        int shieldPercentInt = (int) Math.round(shieldPercent * 100); // 使用已有的float shieldPercent
        String shieldText = shieldPercentInt + "%";
        Component shieldComponent = Component.literal(shieldText);
        textWidth = font.width(shieldComponent); // 使用已有的变量
        
        // 设置文字渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        
        // 渲染文字（在护盾条下方）
        poseStack.pushPose();
        poseStack.translate(0, barHeight + 3, 0); // 增加一些间距
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