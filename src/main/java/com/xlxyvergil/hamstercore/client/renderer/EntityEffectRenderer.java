package com.xlxyvergil.hamstercore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.client.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 实体状态效果图标渲染器 - 专门处理实体头顶状态效果图标和文本的渲染
 */
@OnlyIn(Dist.CLIENT)
public class EntityEffectRenderer {

    /**
     * 渲染状态效果图标 - 与EntityShieldRenderer类似的方式
     */
    public static void renderEffectIcons(PoseStack poseStack, int iconX, int iconY, java.util.List<MobEffectInstance> effects) {
        // 使用 RenderUtils 渲染状态效果图标 - 在指定位置并使用效果列表
        RenderUtils.renderEffectIcons(poseStack, iconX, iconY, effects);
    }
}