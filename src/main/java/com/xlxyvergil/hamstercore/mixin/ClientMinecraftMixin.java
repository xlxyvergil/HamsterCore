package com.xlxyvergil.hamstercore.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderBuffers;
import com.xlxyvergil.hamstercore.api.IRenderContext;
import com.xlxyvergil.hamstercore.api.IRenderContextProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public abstract class ClientMinecraftMixin implements IRenderContextProvider {
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    private GuiGraphics getGuiGraphicsRaw() {
        return new GuiGraphics((Minecraft) (Object) this, renderBuffers.bufferSource());
    }

    @Override
    public GuiGraphics getGuiGraphics(PoseStack pose) {
        final GuiGraphics graphics = getGuiGraphicsRaw();
        ((IRenderContext) graphics).hamstercore$setPose(pose);
        return graphics;
    }
}