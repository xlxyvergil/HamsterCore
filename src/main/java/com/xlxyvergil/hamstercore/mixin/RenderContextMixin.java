package com.xlxyvergil.hamstercore.mixin;

import com.xlxyvergil.hamstercore.api.IRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public abstract class RenderContextMixin implements IRenderContext {
    @Mutable
    @Shadow
    @Final
    private PoseStack pose;

    @Override
    public void hamstercore$setPose(PoseStack pose) {
        this.pose = pose;
    }
}