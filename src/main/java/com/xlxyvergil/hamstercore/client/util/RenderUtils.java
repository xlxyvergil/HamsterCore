package com.xlxyvergil.hamstercore.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashSet;
import java.util.Set;

/**
 * 简化版的 RenderUtils，参考 battery_shield 实现
 */
public class RenderUtils {
    
    public static final Set<ResourceLocation> blendedHeadTextures = new HashSet<>();
    static int CLIP_WIDTH = 3;

    /**
     * 渲染血条 - 参考 battery_shield 的 renderHealth
     */
    public static void renderHealth(GuiGraphics guiGraphics, int x, int y, int width, int height, double value, double max) {
        renderHealth(guiGraphics, x, y, width, height, value, max, false);
    }

    public static void renderHealth(GuiGraphics guiGraphics, int x, int y, int width, int height, double value, double max, boolean force) {
        AssetsManager.HEALTH_EMPTY.blit(guiGraphics, x, y, 0, 0, width, height);
        int targetValue = (int) (width * value / max);
        int realClip = (int) (1.0 * width / AssetsManager.HEALTH_FILL.width * CLIP_WIDTH);
        if (targetValue >= AssetsManager.HEALTH_FILL.width) {
            AssetsManager.HEALTH_FILL.blit(guiGraphics, x, y, 0, 0, AssetsManager.HEALTH_FILL.width, height);
        } else if (targetValue <= realClip) {
            AssetsManager.HEALTH_FILL.blit(guiGraphics, x, y, 0, 0, targetValue, height);
        } else {
            AssetsManager.HEALTH_FILL.blit(guiGraphics, x, y, 0, 0, realClip, height);
            AssetsManager.HEALTH_FILL.blit(guiGraphics, x + realClip, y, AssetsManager.HEALTH_FILL.width - targetValue, 0, targetValue, height);
        }
    }

    /**
     * 渲染条形图 - 完全复制 battery_shield 的 renderBar 逻辑
     */
    public static void renderBar(GuiGraphics guiGraphics,
                                 int x,
                                 int y,
                                 int width,
                                 int height,
                                 AssetsManager.ImageAssets bg,
                                 AssetsManager.ImageAssets fill,
                                 float value,
                                 float max) {
        renderBar(guiGraphics, x, y, width, height, bg, fill, value, max, false);
    }

    public static void renderBar(GuiGraphics guiGraphics,
                                 int x,
                                 int y,
                                 int width,
                                 int height,
                                 AssetsManager.ImageAssets bg,
                                 AssetsManager.ImageAssets fill,
                                 float value,
                                 float max,
                                 boolean force) {
        if (max <= 0) return;
        
        // 简化版本 - 不分段渲染，直接渲染一条
        // 护盾填充和边框位置一致
        renderBarCell(guiGraphics, x, y, width, height, fill, value);
        renderBarCell(guiGraphics, x, y, width, height, bg, max);
    }

    /**
     * 渲染条形图单元 - 完全复制 battery_shield 的 renderBarCell 逻辑
     */
    protected static void renderBarCell(GuiGraphics guiGraphics, int x, int y, int width, int height, AssetsManager.ImageAssets im, float value) {
        if (value < 0) return;
        
        int targetValue = (int) (width * value / im.width);
        int realClip = (int) (1.0 * width / im.width * CLIP_WIDTH);
        
        if (targetValue >= im.width) {
            im.blit(guiGraphics, x, y, 0, 0, im.width, height);
        } else if (targetValue <= realClip) {
            im.blit(guiGraphics, x, y, 0, 0, targetValue, height);
        } else {
            im.blit(guiGraphics, x, y, 0, 0, realClip, height);
            im.blit(guiGraphics, x + realClip, y, im.width - targetValue, 0, targetValue, height);
        }
    }

    /**
     * 渲染护盾条 - 简化版本，参考 EntityShieldRenderer 的实现
     */
    public static void renderShieldBar(PoseStack poseStack, int x, int y, int width, int height, float currentShield, float maxShield) {
        if (maxShield <= 0) return;
        
        // 创建 GuiGraphics - 直接使用 Minecraft 实例
        GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().mulPoseMatrix(poseStack.last().pose());
        
        // 使用 AssetsManager 的资源
        renderBar(guiGraphics, x, y, width, height, 
                 AssetsManager.SHIELD_BORDER, 
                 AssetsManager.SHIELD_FILL, 
                 currentShield, 
                 maxShield, 
                 true);
        
        // 渲染百分比文字 - 显示在护盾条右边
        renderShieldText(guiGraphics, x + 100, y - 2, currentShield, maxShield);
        
        guiGraphics.pose().popPose();
    }
    
    /**
     * 渲染护盾文字 - 参考 battery_shield 的方式
     */
    public static void renderShieldText(GuiGraphics guiGraphics, int x, int y, float value, float max) {
        if (max <= 0) return;
        
        float percent = (value / max) * 100.0f;
        String text = String.format("%.0f%%", percent);
        
        guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFF);
    }
    
    /**
     * 视线检测 - 完全复制 battery_shield 的 raytrace 方法
     * 如果实体不可见（被遮挡），返回true（跳过渲染）
     */
    public static boolean raytrace(LivingEntity target) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return false;
        
        Vec3 fromPos = Minecraft.getInstance().player.getEyePosition();
        Vec3 toPos = target.getEyePosition();
        
        // 简化的视线检测 - 检查两点之间是否有方块遮挡
        BlockHitResult result = Minecraft.getInstance().level.clip(
            new ClipContext(
                fromPos, 
                toPos, 
                ClipContext.Block.OUTLINE, 
                ClipContext.Fluid.NONE,
                Minecraft.getInstance().player
            )
        );
        
        return result.getType() != BlockHitResult.Type.MISS;
    }
}