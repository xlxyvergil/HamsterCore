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
}