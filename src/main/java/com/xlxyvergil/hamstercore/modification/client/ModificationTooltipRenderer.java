package com.xlxyvergil.hamstercore.modification.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

/**
 * 改装件工具提示渲染器 - 模仿 Apotheosis 的 SocketTooltipRenderer
 * 显示装备上镶嵌的改装件
 */
@OnlyIn(Dist.CLIENT)
public class ModificationTooltipRenderer implements ClientTooltipComponent {

    public static final ResourceLocation SOCKET = new ResourceLocation(HamsterCore.MODID, "textures/gui/socket.png");

    private final ModificationComponent comp;
    private final int spacing = Minecraft.getInstance().font.lineHeight + 2;

    public ModificationTooltipRenderer(ModificationComponent comp) {
        this.comp = comp;
    }

    @Override
    public int getHeight() {
        return this.spacing * this.comp.mods.size();
    }

    @Override
    public int getWidth(Font font) {
        int maxWidth = 0;
        for (ModificationInstance inst : this.comp.mods.modifications()) {
            maxWidth = Math.max(maxWidth, font.width(getModificationDesc(inst)) + 12);
        }
        return maxWidth;
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        for (int i = 0; i < this.comp.mods.size(); i++) {
            pGuiGraphics.blit(SOCKET, pX, pY + this.spacing * i, 0, 0, 0, 9, 9, 9, 9);
        }
        int y = pY;
        for (ModificationInstance inst : this.comp.mods.modifications()) {
            if (inst.isValid()) {
                PoseStack pose = pGuiGraphics.pose();
                pose.pushPose();
                pose.scale(0.5F, 0.5F, 1);
                pGuiGraphics.renderFakeItem(inst.modificationStack(), 2 * pX + 1, 2 * y + 1);
                pose.popPose();
            }
            y += this.spacing;
        }
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, BufferSource pBufferSource) {
        for (int i = 0; i < this.comp.mods.size(); i++) {
            pFont.drawInBatch(getModificationDesc(this.comp.mods.get(i)), pX + 12, pY + 1 + this.spacing * i, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }

    public static Component getModificationDesc(ModificationInstance inst) {
        if (!inst.isValid()) return Component.translatable("modification.hamstercore.empty");
        return inst.getSocketBonusTooltip();
    }

    public static record ModificationComponent(ItemStack socketed, SocketedModifications mods) implements TooltipComponent {}
}
