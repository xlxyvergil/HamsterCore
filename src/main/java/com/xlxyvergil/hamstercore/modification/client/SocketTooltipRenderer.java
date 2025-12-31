package com.xlxyvergil.hamstercore.modification.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.ModificationItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SocketTooltipRenderer implements ClientTooltipComponent {

    public static final ResourceLocation SOCKET = new ResourceLocation(HamsterCore.MODID, "textures/gui/socket.png");
    private final SocketComponent comp;
    private final int spacing = Minecraft.getInstance().font.lineHeight + 2;

    public SocketTooltipRenderer(SocketComponent comp) {
        this.comp = comp;
    }

    @Override
    public int getHeight() {
        return this.spacing * this.comp.modifications().size();
    }

    @Override
    public int getWidth(Font font) {
        int maxWidth = 0;
        for (ModificationInstance inst : this.comp.modifications()) {
            maxWidth = Math.max(maxWidth, font.width(getSocketDesc(inst)) + 12);
        }
        return maxWidth;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
        for (int i = 0; i < this.comp.modifications().size(); i++) {
            gfx.blit(SOCKET, x, y + this.spacing * i, 0, 0, 0, 9, 9, 9, 9);
        }
        for (ModificationInstance inst : this.comp.modifications()) {
            if (inst.isValid()) {
                ItemStack modStack = ModificationItem.createModificationStack(inst.modification());
                PoseStack pose = gfx.pose();
                pose.pushPose();
                pose.scale(0.5F, 0.5F, 1);
                gfx.renderFakeItem(modStack, 2 * x + 1, 2 * y + 1);
                pose.popPose();
            }
            y += this.spacing;
        }
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, BufferSource pBufferSource) {
        for (int i = 0; i < this.comp.modifications().size(); i++) {
            pFont.drawInBatch(getSocketDesc(this.comp.modifications().get(i)), pX + 12, pY + 1 + this.spacing * i, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }

    public static Component getSocketDesc(ModificationInstance inst) {
        if (!inst.isValid()) {
            return Component.translatable("socket.hamstercore.empty");
        }
        return Component.literal(inst.modification().id().toString())
            .withStyle(inst.modification().rarity().getColor());
    }
}
