package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapability;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * 护盾HUD更新器
 */
public class ShieldHUDUpdater {
    private static final ResourceLocation SHIELD_ICONS = new ResourceLocation("textures/gui/icons.png");
    
    // 添加静态变量来存储当前渲染的护盾值
    private static float renderedCurrentShield = 0;
    private static float renderedMaxShield = 0;
    private static boolean shieldDataInitialized = false;
    
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
        
        // 获取当前护盾值和最大护盾值
        float currentShield = shieldCap.getCurrentShield();
        float maxShield = shieldCap.getMaxShield();
        
        // 更新渲染缓存值
        if (!shieldDataInitialized || currentShield != renderedCurrentShield || maxShield != renderedMaxShield) {
            renderedCurrentShield = currentShield;
            renderedMaxShield = maxShield;
            shieldDataInitialized = true;
        }
        
        // 如果最大护盾值为0，则不渲染
        if (renderedMaxShield <= 0) {
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
        
        // 计算顶部位置
        int top = height - gui.leftHeight;
        
        // 如果玩家没有护甲，则护盾条向下偏移一点，占据护甲条的位置
        if (armorValue <= 0) {
            top += 4;
        }
        
        // 计算护盾条的填充比例
        float shieldPercent = renderedCurrentShield / renderedMaxShield;
        int shieldBarWidth = (int) (shieldPercent * 81); // 81是标准血条的宽度
        
        // 根据护盾百分比确定颜色
        int color;
        if (shieldPercent > 0.7) {
            // 绿色
            color = 0xFF00FF00;
        } else if (shieldPercent > 0.3) {
            // 黄色
            color = 0xFFFFFF00;
        } else {
            // 红色
            color = 0xFFFF0000;
        }
        
        // 检查是否处于护盾保险状态
        boolean isGating = shieldCap.isGatingActive();
        if (isGating) {
            // 金色边框表示护盾保险状态
            color = 0xFFFFD700;
        }
        
        // 绘制护盾条背景（外边框）
        guiGraphics.fill(left, top, left + 81, top + 5, 0xFF404040); // 深灰色背景
        
        // 绘制护盾条填充
        if (shieldBarWidth > 0) {
            guiGraphics.fill(left + 1, top + 1, left + 1 + shieldBarWidth, top + 4, color);
        }
        
        // 绘制护盾数值
        String shieldText = String.format("%.0f/%.0f", renderedCurrentShield, renderedMaxShield);
        int textWidth = Minecraft.getInstance().font.width(shieldText);
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            shieldText,
            left + 81 + 5, // 条右侧5像素处
            top - 2,
            0xFFFFFF,
            true
        );
        
        // 更新leftHeight以确保其他GUI元素不会重叠
        float maxHealth = (float) player.getAttribute(Attributes.MAX_HEALTH).getValue();
        float absorb = Mth.ceil(player.getAbsorptionAmount());
        int healthRows = Mth.ceil((maxHealth + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        gui.leftHeight += rowHeight * 2 - 15; // 为护盾条预留空间
    }
    
    /**
     * 提供一个方法用于外部更新护盾显示值
     * 当EntityShieldSyncToClient数据包到达时调用此方法
     */
    public static void updateShieldDisplay(float currentShield, float maxShield) {
        renderedCurrentShield = currentShield;
        renderedMaxShield = maxShield;
        shieldDataInitialized = true;
    }
}