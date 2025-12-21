package com.xlxyvergil.hamstercore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xlxyvergil.hamstercore.content.capability.PlayerCapabilityAttacher;
import com.xlxyvergil.hamstercore.element.AffixManager;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 词缀命令类
 * 用于向玩家主手上的道具添加和删除词缀
 */
public class AffixCommand {
    
    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 主命令 /hamsteraffix
        dispatcher.register(Commands.literal("hamsteraffix")
            .requires(source -> source.hasPermission(2)) // 需要权限等级2，管理员级别
            .then(Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("affix", StringArgumentType.string())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                            .executes(context -> addAffix(
                                    context.getSource(),
                                    EntityArgument.getPlayer(context, "player"),
                                    StringArgumentType.getString(context, "affix"),
                                    DoubleArgumentType.getDouble(context, "amount")
                            ))
                        )
                    )
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("affix", StringArgumentType.string())
                        .executes(context -> removeAffix(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                StringArgumentType.getString(context, "affix")
                        ))
                    )
                )
            )
            .then(Commands.literal("clear")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> clearAffixes(
                            context.getSource(),
                            EntityArgument.getPlayer(context, "player")
                    ))
                )
            )
            .then(Commands.literal("list")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> listAffixes(
                            context.getSource(),
                            EntityArgument.getPlayer(context, "player")
                    ))
                )
            )
            .then(Commands.literal("reinit")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> reinitializeShield(
                            context.getSource(),
                            EntityArgument.getPlayer(context, "player")
                    ))
                )
            )
        );
    }
    
    /**
     * 添加词缀命令
     */
    private static int addAffix(CommandSourceStack source, ServerPlayer player, String affixName, double amount) throws CommandSyntaxException {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal(player.getName().getString() + " 的主手没有物品"));
            return 0;
        }
        
        ElementType elementType = ElementType.byName(affixName);
        if (elementType == null) {
            source.sendFailure(Component.literal("未知的词缀类型: " + affixName));
            return 0;
        }
        
        // 获取物品的武器数据
        WeaponData weaponData = WeaponDataManager.getOrCreateWeaponData(stack);
        
        // 添加词缀
        weaponData.addInitialModifier(elementType, amount, "command");
        
        // 保存更新后的数据
        WeaponDataManager.saveWeaponData(stack, weaponData);
        
        source.sendSuccess(() -> Component.literal("成功为 " + player.getName().getString() + " 的主手物品添加词缀: " + 
                elementType.getDisplayName() + " (" + amount + ")"), true);
        
        return 1;
    }
    
    /**
     * 删除词缀命令
     */
    private static int removeAffix(CommandSourceStack source, ServerPlayer player, String affixName) throws CommandSyntaxException {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal(player.getName().getString() + " 的主手没有物品"));
            return 0;
        }
        
        ElementType elementType = ElementType.byName(affixName);
        if (elementType == null) {
            source.sendFailure(Component.literal("未知的词缀类型: " + affixName));
            return 0;
        }
        
        // 获取物品的武器数据
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        if (weaponData == null) {
            source.sendFailure(Component.literal(player.getName().getString() + " 的主手物品没有词缀数据"));
            return 0;
        }
        
        // 删除词缀
        boolean removed = weaponData.removeInitialModifier(elementType, "command");
        if (!removed) {
            source.sendFailure(Component.literal(player.getName().getString() + " 的主手物品没有指定的词缀: " + 
                    elementType.getDisplayName()));
            return 0;
        }
        
        // 保存更新后的数据
        WeaponDataManager.saveWeaponData(stack, weaponData);
        
        source.sendSuccess(() -> Component.literal("成功为 " + player.getName().getString() + " 的主手物品删除词缀: " + 
                elementType.getDisplayName()), true);
        
        return 1;
    }
    
    /**
     * 清空词缀命令
     */
    private static int clearAffixes(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal(player.getName().getString() + " 的主手没有物品"));
            return 0;
        }
        
        // 获取物品的武器数据
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        if (weaponData == null) {
            source.sendFailure(Component.literal(player.getName().getString() + " 的主手物品没有词缀数据"));
            return 0;
        }
        
        // 清空词缀
        weaponData.clearInitialModifiers();
        
        // 保存更新后的数据
        WeaponDataManager.saveWeaponData(stack, weaponData);
        
        source.sendSuccess(() -> Component.literal("成功清空 " + player.getName().getString() + " 的主手物品的所有词缀"), true);
        
        return 1;
    }
    
    /**
     * 列出词缀命令
     */
    private static int listAffixes(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal(player.getName().getString() + " 的主手没有物品"));
            return 0;
        }
        
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        if (weaponData == null || weaponData.getInitialModifiers().isEmpty()) {
            source.sendSuccess(() -> Component.literal(player.getName().getString() + " 的主手物品没有词缀"), true);
            return 1;
        }
        
        source.sendSuccess(() -> Component.literal(player.getName().getString() + " 的主手物品词缀列表:"), true);
        
        for (var entry : weaponData.getInitialModifiers()) {
            ElementType type = ElementType.byName(entry.getElementType());
            String typeName = type != null ? type.getDisplayName() : entry.getElementType();
            
            Component affixInfo = Component.literal("- " + typeName + ": " + entry.getAmount() + 
                    " (来源: " + entry.getSource() + ")");
            source.sendSuccess(() -> affixInfo, true);
        }
        
        return 1;
    }
    
    /**
     * 重新初始化护盾命令
     */
    private static int reinitializeShield(CommandSourceStack source, ServerPlayer player) {
        // 重新初始化玩家的护盾能力
        // 注意：这里我们需要获取玩家的等级来调用正确的方法
        player.getCapability(com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider.CAPABILITY)
            .ifPresent(levelCap -> {
                PlayerCapabilityAttacher.initializePlayerCapabilities(player, levelCap.getPlayerLevel());
            });
        
        source.sendSuccess(() -> Component.literal("成功为 " + player.getName().getString() + " 重新初始化了护盾能力"), true);
        
        return 1;
    }
}