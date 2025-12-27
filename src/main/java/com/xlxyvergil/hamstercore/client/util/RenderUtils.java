package com.xlxyvergil.hamstercore.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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
        
        // 计算填充宽度比例
        float fillRatio = value / max;
        int fillWidth = (int) (fill.width * fillRatio);
        
        // 简化版本 - 不分段渲染，直接渲染一条
        // 渲染填充部分（根据实际比例）
        renderBarCell(guiGraphics, x, y, width, height, fill, fillWidth);
        // 渲染边框部分（完整显示）
        renderBarCell(guiGraphics, x, y, width, height, bg, bg.width);
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
    
    /**
     * 根据状态效果获取对应的图标资源
     */
    private static AssetsManager.ImageAssets getEffectIcon(ResourceLocation effectRegistryName) {
        String path = effectRegistryName.getPath();
        
        // 根据HamsterCore自定义状态效果的路径返回对应的图标
        switch (path) {
            case "blast":
                return AssetsManager.BLAST;
            case "cold":
                return AssetsManager.COLD;
            case "corrosive":
                return AssetsManager.CORROSIVE;
            case "electric_cloud":
                return AssetsManager.ELECTRIC_CLOUD;
            case "electricity":
                return AssetsManager.ELECTRICITY;
            case "gas":
                return AssetsManager.GAS;
            case "gas_cloud":
                return AssetsManager.GAS_CLOUD;
            case "heat":
                return AssetsManager.HEAT;
            case "impact":
                return AssetsManager.IMPACT;
            case "magnetic":
                return AssetsManager.MAGNETIC;
            case "puncture":
                return AssetsManager.PUNCTURE;
            case "radiation":
                return AssetsManager.RADIATION;
            case "slash":
                return AssetsManager.SLASH;
            case "toxin":
                return AssetsManager.TOXIN;
            case "viral":
                return AssetsManager.VIRAL;
            default:
                return null; // 未找到对应的图标
        }
    }
    
    /**
     * 渲染状态效果图标 - 在指定位置渲染状态效果图标和等级
     */
    public static void renderEffectIcons(PoseStack poseStack, int iconX, int iconY, java.util.List<MobEffectInstance> effects) {
        renderEffectIcons(poseStack, iconX, iconY, effects, null);
    }
    
    /**
     * 渲染状态效果图标 - 在指定位置渲染状态效果图标和等级，带实体参数
     */
    public static void renderEffectIcons(PoseStack poseStack, int iconX, int iconY, java.util.List<MobEffectInstance> effects, net.minecraft.world.entity.LivingEntity entity) {
        // 创建 GuiGraphics - 直接使用 Minecraft 实例
        GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().mulPoseMatrix(poseStack.last().pose());
        
        // 渲染状态效果图标和等级
        int maxIconsPerColumn = 5; // 每列最多4个图标
        int columnSpacing = 22; // 列间距
        int rowSpacing = 18; // 行间距
        int maxColumns = 4; // 最大列数
        int maxDistanceFromTop = 60; // 距离实体顶部的最大距离（避免超过护盾条）
        
        int currentColumn = 0;
        int currentRow = 0;
        
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance effectInstance = effects.get(i);
            MobEffect effect = effectInstance.getEffect();
            int amplifier = effectInstance.getAmplifier(); // 等级（0为第一级）
            
            // 计算当前图标的位置
            int currentX = iconX + (currentColumn * columnSpacing);
            int currentY = iconY + (currentRow * rowSpacing);
            
            // 检查是否超过垂直限制（距离实体顶部的最大距离）
            if (Math.abs(currentY) > maxDistanceFromTop) {
                // 换到下一列
                currentRow = 0;
                currentColumn++;
                
                // 如果超出最大列数，停止渲染
                if (currentColumn >= maxColumns) {
                    break;
                }
                
                // 重新计算位置
                currentX = iconX + (currentColumn * columnSpacing);
                currentY = iconY + (currentRow * rowSpacing);
            }
            
            // 检查状态效果是否仍在持续时间
            if (effectInstance.getDuration() <= 0 && !effectInstance.isInfiniteDuration()) {
                continue; // 如果持续时间结束，则跳过渲染
            }
            
            // 获取状态效果的图标资源位置
            ResourceLocation effectRegistryName = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (effectRegistryName != null) {
                // 使用AssetsManager渲染对应的状态效果图标
                AssetsManager.ImageAssets effectIcon = getEffectIcon(effectRegistryName);
                if (effectIcon != null) {
                    // 以18x18像素完整尺寸渲染图标
                    effectIcon.blit(guiGraphics, currentX, currentY, 18, 18);
                }
                // 如果没有找到对应的图标，不渲染任何内容，这表示代码映射有问题，需要修复

                // 渲染状态效果等级（在图标右下角）
                String levelText = String.valueOf(amplifier + 1); // 显示为1开始的等级
                int textWidth = Minecraft.getInstance().font.width(levelText);
                // 在图标右下角绘制等级数字
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    levelText,
                    currentX + 18 - textWidth,   // 右对齐，靠近图标的右边
                    currentY + 12,       // 在图标下方，稍微往上一点
                    0xFFFFFF,     // 白色文字
                    true // 使用阴影
                );
            }
            
            // 更新位置计数器
            currentRow++;
            if (currentRow >= maxIconsPerColumn) {
                currentRow = 0;
                currentColumn++;
                
                // 如果超出最大列数，停止渲染
                if (currentColumn >= maxColumns) {
                    break;
                }
            }
        }
        
        guiGraphics.pose().popPose();
    }
}