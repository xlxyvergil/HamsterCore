package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapability;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;

@OnlyIn(Dist.CLIENT)
public class PlayerLevelHUDRenderer {
    
    // 经验条纹理资源位置
    private static final ResourceLocation EXPERIENCE_BAR = 
        ResourceLocation.fromNamespaceAndPath(HamsterCore.MODID, "textures/gui/experience_bar.png");
    private static final ResourceLocation EXPERIENCE_FRAME = 
        ResourceLocation.fromNamespaceAndPath(HamsterCore.MODID, "textures/gui/experience_frame.png");
    
    // 数字格式化方法，防止数字过长
    private static String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fk", number / 1000.0);
        } else if (number < 1000000000) {
            return String.format("%.1fm", number / 1000000.0);
        } else {
            return String.format("%.1fb", number / 1000000000.0);
        }
    }
    
    public static void renderPlayerLevelHUD(ForgeGui gui, GuiGraphics guiGraphics, Player player, int width, int height) {
        // 获取玩家等级能力
        PlayerLevelCapability cap = player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).orElse(null);
        
        if (cap == null) {
            return;
        }
        
        // 获取玩家等级
        int playerLevel = cap.getPlayerLevel();
        
        // 设置渲染位置（快捷道具栏左侧，底边与道具栏底边平行）
        int x = width / 2 - 91 - 1 - 5; // 91是快捷道具栏左侧到屏幕中心的距离，再减去经验条宽度和间距
        int y = height - 22; // 底边与道具栏底边平行（22是经验条高度）
        
        Font font = Minecraft.getInstance().font;
        
        // 渲染等级文本（向上提升5像素）
        String levelText = "Lv." + playerLevel + "/30";
        int textX = x - font.width(levelText) - 2; // 文本在经验条左边
        int textY = y + 11 - font.lineHeight / 2 - 5; // 文本垂直居中并向上提升5像素
        guiGraphics.drawString(font, Component.literal(levelText), textX, textY, 0xFFFFFF, false);
        
        // 渲染剩余经验文本（在等级文本下方）
        int expToNext = cap.getExperienceToNextLevel();
        int currentExp = cap.getCurrentLevelExperience();
        int remainingExp = expToNext - currentExp;
        String remainingExpText = formatNumber(remainingExp);
        int remainingTextX = x - font.width(remainingExpText) - 2;
        int remainingTextY = textY + font.lineHeight + 1; // 在等级文本下方
        guiGraphics.drawString(font, Component.literal(remainingExpText), remainingTextX, remainingTextY, 0xFFFF00, false);
        
        // 渲染经验进度条（竖直）
        renderExperienceBar(guiGraphics, cap, x, y);
    }
    
    private static void renderExperienceBar(GuiGraphics guiGraphics, PlayerLevelCapability cap, int x, int y) {
        // 获取当前等级经验和升级所需经验
        int currentExp = cap.getCurrentLevelExperience();
        int expToNext = cap.getExperienceToNextLevel();
        
        // 计算经验条的填充比例
        float fillRatio = expToNext > 0 ? (float) currentExp / expToNext : 0;
        
        // 经验条背景框架（竖直放置，5x22像素）
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, EXPERIENCE_FRAME);
        guiGraphics.blit(EXPERIENCE_FRAME, x, y, 0, 0, 5, 22, 5, 22);
        
        // 经验条填充（从下往上填充）
        int fillHeight = (int) (22 * fillRatio);
        if (fillHeight > 0) {
            RenderSystem.setShaderTexture(0, EXPERIENCE_BAR);
            // 从底部开始绘制，所以需要调整y坐标
            guiGraphics.blit(EXPERIENCE_BAR, x, y + 22 - fillHeight, 0, 22 - fillHeight, 5, fillHeight, 5, 22);
        }
    }
}