package com.xlxyvergil.hamstercore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.client.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 实体护盾渲染器 - 完全参照 battery_shield 的实现，进行适当简化
 */
@OnlyIn(Dist.CLIENT)
public class EntityShieldRenderer {
    
    /**
     * 渲染护盾条 - 完全按照 battery_shield 的方式
     */
    public static void renderShieldBar(float currentShield, float maxShield, PoseStack poseStack, MultiBufferSource buffer) {
        // 使用 RenderUtils 渲染护盾条 - 向右偏移10像素
        RenderUtils.renderShieldBar(poseStack, -38, -15, 96, 6, currentShield, maxShield);
    }
}