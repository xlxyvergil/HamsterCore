package com.xlxyvergil.hamstercore.modification.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.SocketHelper;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 改装件工具提示渲染器 - 模仿 Apotheosis 的 SocketTooltipRenderer
 * 显示装备上镶嵌的改装件
 */
@OnlyIn(Dist.CLIENT)
public class ModificationTooltipRenderer implements ClientTooltipComponent {

    public static final ResourceLocation SOCKET = new ResourceLocation(HamsterCore.MODID, "textures/gui/socket.png");

    private final ModificationComponent comp;
    private final int spacing = Minecraft.getInstance().font.lineHeight + 2;
    private final List<ModificationInstance> specialModifications;
    private final List<ModificationInstance> normalModifications;
    private final int totalSpecialSockets;
    private final int totalNormalSockets;

    public ModificationTooltipRenderer(ModificationComponent comp) {
        this.comp = comp;
        this.totalSpecialSockets = SocketHelper.getSpecialSockets(comp.socketed());
        this.totalNormalSockets = SocketHelper.getSockets(comp.socketed());
        
        // 初始化特殊和通用改装件列表，填充空槽位
        this.specialModifications = new ArrayList<>();
        this.normalModifications = new ArrayList<>();
        
        // 先填充空槽位
        for (int i = 0; i < totalSpecialSockets; i++) {
            specialModifications.add(ModificationInstance.EMPTY);
        }
        for (int i = 0; i < totalNormalSockets; i++) {
            normalModifications.add(ModificationInstance.EMPTY);
        }
        
        // 获取并分类已安装的改装件
        // 1. 从特殊槽位中获取已安装的改装件
        List<ModificationInstance> specialModsFromStack = SocketHelper.getSpecialModifications(comp.socketed());
        for (int i = 0; i < specialModsFromStack.size() && i < totalSpecialSockets; i++) {
            if (specialModsFromStack.get(i).isValid()) {
                specialModifications.set(i, specialModsFromStack.get(i));
            }
        }
        
        // 2. 从通用槽位中获取已安装的改装件
        SocketedModifications normalModsFromStack = SocketHelper.getModifications(comp.socketed());
        for (int i = 0; i < normalModsFromStack.size() && i < totalNormalSockets; i++) {
            if (normalModsFromStack.get(i).isValid()) {
                normalModifications.set(i, normalModsFromStack.get(i));
            }
        }
    }

    @Override
    public int getHeight() {
        int height = 0;
        // 特殊改装件标题和内容
        if (totalSpecialSockets > 0) {
            height += spacing; // 标题
            height += spacing * totalSpecialSockets; // 特殊改装件
        }
        // 通用改装件标题和内容
        if (totalNormalSockets > 0) {
            height += spacing; // 标题
            height += spacing * totalNormalSockets; // 通用改装件
        }
        return height;
    }

    @Override
    public int getWidth(Font font) {
        int maxWidth = 0;
        
        // 检查标题宽度
        maxWidth = Math.max(maxWidth, font.width(Component.translatable("hamstercore.modification.special_socket")));
        maxWidth = Math.max(maxWidth, font.width(Component.translatable("hamstercore.modification.normal_socket")));
        
        // 检查特殊改装件宽度
        for (ModificationInstance inst : specialModifications) {
            maxWidth = Math.max(maxWidth, font.width(getModificationDesc(inst)) + 12);
        }
        
        // 检查通用改装件宽度
        for (ModificationInstance inst : normalModifications) {
            maxWidth = Math.max(maxWidth, font.width(getModificationDesc(inst)) + 12);
        }
        
        return maxWidth;
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        int y = pY;
        
        // 渲染特殊改装件
        if (totalSpecialSockets > 0) {
            y += spacing; // 跳过标题行
            for (int i = 0; i < totalSpecialSockets; i++) {
                pGuiGraphics.blit(SOCKET, pX, y + this.spacing * i, 0, 0, 0, 9, 9, 9, 9);
            }
            
            int specialY = y;
            for (ModificationInstance inst : specialModifications) {
                if (inst.isValid()) {
                    PoseStack pose = pGuiGraphics.pose();
                    pose.pushPose();
                    pose.scale(0.5F, 0.5F, 1);
                    pGuiGraphics.renderFakeItem(inst.modificationStack(), 2 * pX + 1, 2 * specialY + 1);
                    pose.popPose();
                }
                specialY += this.spacing;
            }
            
            y += spacing * totalSpecialSockets;
        }
        
        // 渲染通用改装件
        if (totalNormalSockets > 0) {
            y += spacing; // 跳过标题行
            for (int i = 0; i < totalNormalSockets; i++) {
                pGuiGraphics.blit(SOCKET, pX, y + this.spacing * i, 0, 0, 0, 9, 9, 9, 9);
            }
            
            int normalY = y;
            for (ModificationInstance inst : normalModifications) {
                if (inst.isValid()) {
                    PoseStack pose = pGuiGraphics.pose();
                    pose.pushPose();
                    pose.scale(0.5F, 0.5F, 1);
                    pGuiGraphics.renderFakeItem(inst.modificationStack(), 2 * pX + 1, 2 * normalY + 1);
                    pose.popPose();
                }
                normalY += this.spacing;
            }
        }
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, BufferSource pBufferSource) {
        int y = pY;
        
        // 渲染特殊改装件标题和内容
        if (totalSpecialSockets > 0) {
            // 特殊改装件标题
            pFont.drawInBatch(Component.translatable("hamstercore.modification.special_socket").withStyle(net.minecraft.ChatFormatting.BLUE), 
                pX, y, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            y += spacing;
            
            // 特殊改装件内容
            for (int i = 0; i < totalSpecialSockets; i++) {
                pFont.drawInBatch(getModificationDesc(specialModifications.get(i)), pX + 12, y + 1 + this.spacing * i, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            }
            y += spacing * totalSpecialSockets;
        }
        
        // 渲染通用改装件标题和内容
        if (totalNormalSockets > 0) {
            // 通用改装件标题
            pFont.drawInBatch(Component.translatable("hamstercore.modification.normal_socket").withStyle(net.minecraft.ChatFormatting.GREEN), 
                pX, y, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            y += spacing;
            
            // 通用改装件内容
            for (int i = 0; i < totalNormalSockets; i++) {
                pFont.drawInBatch(getModificationDesc(normalModifications.get(i)), pX + 12, y + 1 + this.spacing * i, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            }
        }
    }

    public static Component getModificationDesc(ModificationInstance inst) {
        if (!inst.isValid()) return Component.translatable("socket.hamstercore.empty");
        return inst.getSocketBonusTooltip();
    }

    public static record ModificationComponent(ItemStack socketed, SocketedModifications mods) implements TooltipComponent {}
}
