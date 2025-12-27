package com.xlxyvergil.hamstercore.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * 简化版的 AssetsManager，参考 battery_shield 实现
 */
public class AssetsManager {
    
    public static class ImageAssets {
        public final ResourceLocation resourceLocation;
        public final int width;
        public final int height;

        public ImageAssets(String namespace, String path, int width, int height) {
            this.resourceLocation = new ResourceLocation(namespace, path);
            this.width = width;
            this.height = height;
        }

        public void blit(GuiGraphics pGuiGraphics, int x, int y) {
            this.blit(pGuiGraphics, x, y, 0, 0, width, height);
        }

        public void blit(GuiGraphics pGuiGraphics, int x, int y, int drawWidth, int drawHeight) {
            this.blit(pGuiGraphics, x, y, 0, 0, drawWidth, drawHeight);
        }

        public void blit(GuiGraphics pGuiGraphics, int x, int y, int drawX, int drawY, int drawWidth, int drawHeight) {
            pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            pGuiGraphics.blit(resourceLocation, x, y, drawX, drawY, drawWidth, drawHeight, width, height);
            pGuiGraphics.flush();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        }
    }

    // 护盾相关资源
    public static final ImageAssets SHIELD_BORDER = new ImageAssets("hamstercore", "textures/gui/shield_frame.png", 82, 6);
    public static final ImageAssets SHIELD_FILL = new ImageAssets("hamstercore", "textures/gui/shield_bar.png", 82, 6);
    public static final ImageAssets HEALTH_FILL = new ImageAssets("hamstercore", "textures/gui/health_fill.png", 82, 6);
    public static final ImageAssets HEALTH_EMPTY = new ImageAssets("hamstercore", "textures/gui/health_empty.png", 82, 6);
    
    // HamsterCore自定义状态效果图标资源 - 18x18像素
    public static final ImageAssets BLAST = new ImageAssets("hamstercore", "textures/mob_effect/blast.png", 18, 18);
    public static final ImageAssets COLD = new ImageAssets("hamstercore", "textures/mob_effect/cold.png", 18, 18);
    public static final ImageAssets CORROSIVE = new ImageAssets("hamstercore", "textures/mob_effect/corrosive.png", 18, 18);
    public static final ImageAssets ELECTRIC_CLOUD = new ImageAssets("hamstercore", "textures/mob_effect/electric_cloud.png", 18, 18);
    public static final ImageAssets ELECTRICITY = new ImageAssets("hamstercore", "textures/mob_effect/electricity.png", 18, 18);
    public static final ImageAssets GAS = new ImageAssets("hamstercore", "textures/mob_effect/gas.png", 18, 18);
    public static final ImageAssets GAS_CLOUD = new ImageAssets("hamstercore", "textures/mob_effect/gas_cloud.png", 18, 18);
    public static final ImageAssets HEAT = new ImageAssets("hamstercore", "textures/mob_effect/heat.png", 18, 18);
    public static final ImageAssets IMPACT = new ImageAssets("hamstercore", "textures/mob_effect/impact.png", 18, 18);
    public static final ImageAssets MAGNETIC = new ImageAssets("hamstercore", "textures/mob_effect/magnetic.png", 18, 18);
    public static final ImageAssets PUNCTURE = new ImageAssets("hamstercore", "textures/mob_effect/puncture.png", 18, 18);
    public static final ImageAssets RADIATION = new ImageAssets("hamstercore", "textures/mob_effect/radiation.png", 18, 18);
    public static final ImageAssets SLASH = new ImageAssets("hamstercore", "textures/mob_effect/slash.png", 18, 18);
    public static final ImageAssets TOXIN = new ImageAssets("hamstercore", "textures/mob_effect/toxin.png", 18, 18);
    public static final ImageAssets VIRAL = new ImageAssets("hamstercore", "textures/mob_effect/viral.png", 18, 18);
}