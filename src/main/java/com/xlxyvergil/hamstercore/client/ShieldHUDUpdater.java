package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapability;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * 护盾HUD更新器
 */
@OnlyIn(Dist.CLIENT)
public class ShieldHUDUpdater {
    private static final ResourceLocation SHIELD_ICONS = new ResourceLocation("hamstercore", "textures/gui/shield_bar.png");
    private static final ResourceLocation SHIELD_FRAME = new ResourceLocation("hamstercore", "textures/gui/shield_frame.png");
    private static final ResourceLocation SHIELD_ICONS_GATING = new ResourceLocation("hamstercore", "textures/gui/shield_bar_gating.png");
    private static final ResourceLocation SHIELD_FRAME_GATING = new ResourceLocation("hamstercore", "textures/gui/shield_frame_gating.png");
    
    /**
     * 渲染玩家护盾HUD（采用Malum的位置计算方式）
     * 
     * @param gui Forge GUI对象
     * @param guiGraphics GUI图形对象
     * @param player 玩家实体
     * @param width 屏幕宽度
     * @param height 屏幕高度
     */
    public static void renderShieldHUD(ForgeGui gui, GuiGraphics guiGraphics, Player player, int width, int height) {
        // 获取玩家的护盾能力
        EntityShieldCapability shieldCap = player.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
        
        if (shieldCap == null) {
            return;
        }
        
        // 获取当前护盾值和最大护盾值（每次都从实际能力中获取最新值）
        float currentShield = shieldCap.getCurrentShield();
        float maxShield = shieldCap.getMaxShield();
        
        // 如果最大护盾值为0，则不渲染
        if (maxShield <= 0) {
            return;
        }
        
        // 设置渲染状态
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, SHIELD_ICONS);
        
        // 计算护盾条位置（参考Malum的实现方式）
        int left = width / 2 - 91;  // 与原版生命值和饥饿值对齐
        
        // 获取玩家的护甲值
        float armorValue = (float) player.getAttribute(Attributes.ARMOR).getValue();
        
        // 计算顶部位置，确保护盾条不被聊天框遮挡
        // 确保护盾条显示在聊天框上方
        int top = Math.min(height - gui.leftHeight, height - 50);
        
        // 如果玩家没有护甲，则护盾条向下偏移一点，占据护甲条的位置
        if (armorValue <= 0) {
            top += 4;
        }
        
        // 计算护盾条的填充比例
        float shieldPercent = currentShield / maxShield;
        int shieldBarWidth = (int) (shieldPercent * 82); // 82是护盾条的宽度
        
        // 检查是否处于护盾保险状态
        boolean isGating = shieldCap.isGatingActive();
        
        // 绘制护盾条背景框架
        if (maxShield > 0) {
            RenderSystem.setShaderTexture(0, isGating ? SHIELD_FRAME_GATING : SHIELD_FRAME);
            guiGraphics.blit(isGating ? SHIELD_FRAME_GATING : SHIELD_FRAME, left, top, 0, 0, 82, 6, 82, 6);
        }
        
        // 绘制护盾条填充
        if (shieldBarWidth > 0) {
            RenderSystem.setShaderTexture(0, isGating ? SHIELD_ICONS_GATING : SHIELD_ICONS);
            guiGraphics.blit(isGating ? SHIELD_ICONS_GATING : SHIELD_ICONS, left, top, 0, 0, shieldBarWidth, 6, shieldBarWidth, 6);
        }
        
        // 绘制护盾数值
        String shieldText = String.format("%.0f/%.0f", currentShield, maxShield);
        
        // 缩小字体大小 - 应用0.8的缩放比例（相当于缩小约2像素）
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.8F, 0.8F, 1.0F);
        
        // 计算缩放后的文本宽度和位置
        float scaledLeft = (left + (82 - Minecraft.getInstance().font.width(shieldText)) / 2) / 0.8F;
        // 确保护文字的中心点在护盾条的中心位置（护盾条高度为6像素，中心点垂直位置是top + 3）
        float scaledTop = (top + 3) / 0.8F - (Minecraft.getInstance().font.lineHeight / 2);
        
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            shieldText,
            scaledLeft, // 护盾条中间（缩放后的位置）
            scaledTop,
            0xFFFFFF,
            true
        );
        
        guiGraphics.pose().popPose();
        
        // 更新leftHeight以确保其他GUI元素不会重叠
        float maxHealth = (float) player.getAttribute(Attributes.MAX_HEALTH).getValue();
        float absorb = Mth.ceil(player.getAbsorptionAmount());
        int healthRows = Mth.ceil((maxHealth + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        gui.leftHeight += rowHeight * 2 - 15; // 为护盾条预留空间
    }
}