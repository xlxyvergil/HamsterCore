package com.xlxyvergil.hamstercore.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public interface IRenderContextProvider {
    GuiGraphics getGuiGraphics(PoseStack pose);
}