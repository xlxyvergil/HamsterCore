package com.xlxyvergil.hamstercore.modification.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationHelper;
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
        // 添加普通槽位和特殊槽位文字标示的高度
        int normalSockets = ModificationHelper.getSockets(this.comp.socketed());
        int specialSockets = ModificationHelper.getSpecialSockets(this.comp.socketed());
        int titleLines = 0;
        if (normalSockets > 0) titleLines++;
        if (specialSockets > 0) titleLines++;
        return titleLines * this.spacing + this.spacing * this.comp.modifications().size();
    }

    @Override
    public int getWidth(Font font) {
        int maxWidth = 0;
        // 检查普通槽位和特殊槽位文字的宽度
        maxWidth = Math.max(maxWidth, font.width(Component.translatable("hamstercore.modification.normal_sockets")));
        maxWidth = Math.max(maxWidth, font.width(Component.translatable("hamstercore.modification.special_sockets")));
        // 检查所有槽位描述的宽度
        for (ModificationInstance inst : this.comp.modifications()) {
            maxWidth = Math.max(maxWidth, font.width(getSocketDesc(inst)) + 12);
        }
        return maxWidth;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
        int normalSockets = ModificationHelper.getSockets(this.comp.socketed());
        int specialSockets = ModificationHelper.getSpecialSockets(this.comp.socketed());
        int currentY = y;
        int currentIndex = 0;
        
        // 渲染普通槽位文字标示
        if (normalSockets > 0) {
            currentY += this.spacing;
        }
        
        // 渲染普通槽位的槽位图标
        for (int i = 0; i < normalSockets; i++) {
            if (currentIndex < this.comp.modifications().size()) {
                gfx.blit(SOCKET, x, currentY, 0, 0, 0, 9, 9, 9, 9);
                
                // 渲染普通槽位的改装件图标
                ModificationInstance inst = this.comp.modifications().get(currentIndex);
                if (inst.isValid()) {
                    ItemStack modStack = ModificationItem.createModificationStack(inst.modification());
                    PoseStack pose = gfx.pose();
                    pose.pushPose();
                    pose.scale(0.5F, 0.5F, 1);
                    gfx.renderFakeItem(modStack, 2 * x + 1, 2 * currentY + 1);
                    pose.popPose();
                }
                
                currentY += this.spacing;
                currentIndex++;
            }
        }
        
        // 渲染特殊槽位文字标示
        if (specialSockets > 0) {
            currentY += this.spacing;
        }
        
        // 渲染特殊槽位的槽位图标
        for (int i = 0; i < specialSockets; i++) {
            if (currentIndex < this.comp.modifications().size()) {
                gfx.blit(SOCKET, x, currentY, 0, 0, 0, 9, 9, 9, 9);
                
                // 渲染特殊槽位的改装件图标
                ModificationInstance inst = this.comp.modifications().get(currentIndex);
                if (inst.isValid()) {
                    ItemStack modStack = ModificationItem.createModificationStack(inst.modification());
                    PoseStack pose = gfx.pose();
                    pose.pushPose();
                    pose.scale(0.5F, 0.5F, 1);
                    gfx.renderFakeItem(modStack, 2 * x + 1, 2 * currentY + 1);
                    pose.popPose();
                }
                
                currentY += this.spacing;
                currentIndex++;
            }
        }
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, BufferSource pBufferSource) {
        int normalSockets = ModificationHelper.getSockets(this.comp.socketed());
        int specialSockets = ModificationHelper.getSpecialSockets(this.comp.socketed());
        int currentY = pY;
        int currentIndex = 0;
        
        // 渲染普通槽位文字标示
        if (normalSockets > 0) {
            pFont.drawInBatch(Component.translatable("hamstercore.modification.normal_sockets"), pX, currentY, 0xFFFF00, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            currentY += this.spacing;
        }
        
        // 渲染普通槽位的改装件
        for (int i = 0; i < normalSockets; i++) {
            if (currentIndex < this.comp.modifications().size()) {
                pFont.drawInBatch(getSocketDesc(this.comp.modifications().get(currentIndex)), pX + 12, currentY + 1, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
                currentY += this.spacing;
                currentIndex++;
            }
        }
        
        // 渲染特殊槽位文字标示
        if (specialSockets > 0) {
            pFont.drawInBatch(Component.translatable("hamstercore.modification.special_sockets"), pX, currentY, 0xFF00FF, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            currentY += this.spacing;
        }
        
        // 渲染特殊槽位的改装件
        for (int i = 0; i < specialSockets; i++) {
            if (currentIndex < this.comp.modifications().size()) {
                pFont.drawInBatch(getSocketDesc(this.comp.modifications().get(currentIndex)), pX + 12, currentY + 1, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
                currentY += this.spacing;
                currentIndex++;
            }
        }
    }

    public static Component getSocketDesc(ModificationInstance inst) {
        if (!inst.isValid()) {
            return Component.translatable("socket.hamstercore.empty");
        }
        String modId = inst.modification().id().toString();
        return Component.translatable("item.hamstercore.modification." + modId)
            .withStyle(inst.modification().rarity().getColor());
    }
}
