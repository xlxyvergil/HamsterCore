package com.xlxyvergil.hamstercore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

import java.util.Collection;
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
            .requires(source -> source.hasPermission(0)) // 需要权限等级0，所有玩家都可以使用
            .then(Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("elementType", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            // 提供所有元素类型的建议
                            for (ElementType type : ElementType.getAllTypes()) {
                                builder.suggest(type.getName());
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                            .executes(context -> addAffix(
                                    context.getSource(),
                                    EntityArgument.getPlayer(context, "player"),
                                    StringArgumentType.getString(context, "elementType"),
                                    DoubleArgumentType.getDouble(context, "amount")
                            ))
                        )
                    )
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("elementType", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            // 提供玩家主手物品上已有词缀的元素类型建议
                            try {
                                ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                ItemStack stack = player.getMainHandItem();
                                if (!stack.isEmpty()) {
                                    WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
                                    if (weaponData != null) {
                                        weaponData.getInitialModifiers().forEach(entry -> {
                                            builder.suggest(entry.getElementType());
                                        });
                                    }
                                }
                            } catch (CommandSyntaxException e) {
                                // 忽略异常
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> removeAffix(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                StringArgumentType.getString(context, "elementType")
                        ))
                    )
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
        );
    }
    
    /**
     * 添加词缀命令
     */
    private static int addAffix(CommandSourceStack source, ServerPlayer player, String elementType, double amount) {
        ItemStack stack = player.getMainHandItem();
        
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("玩家主手上没有物品"));
            return 0;
        }
        
        ElementType type = ElementType.byName(elementType);
        if (type == null) {
            source.sendFailure(Component.literal("未知的元素类型: " + elementType));
            return 0;
        }
        
        // 添加词缀
        UUID uuid = UUID.randomUUID();
        AffixManager.addAffix(stack, "命令添加", elementType, amount, "ADDITION", uuid, "user");
        
        source.sendSuccess(() -> Component.literal("成功为 " + player.getName().getString() + " 的主手物品添加了 " + 
                amount + " 点 " + type.getDisplayName() + " 词缀"), true);
        
        return 1;
    }
    
    /**
     * 删除词缀命令
     */
    private static int removeAffix(CommandSourceStack source, ServerPlayer player, String elementType) {
        ItemStack stack = player.getMainHandItem();
        
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("玩家主手上没有物品"));
            return 0;
        }
        
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        if (weaponData == null) {
            source.sendFailure(Component.literal("物品没有词缀数据"));
            return 0;
        }
        
        // 查找并删除指定类型的第一个词缀
        boolean found = false;
        for (var entry : weaponData.getInitialModifiers()) {
            if (entry.getElementType().equals(elementType)) {
                AffixManager.removeAffix(stack, entry.getUuid());
                found = true;
                break;
            }
        }
        
        if (found) {
            ElementType type = ElementType.byName(elementType);
            String typeName = type != null ? type.getDisplayName() : elementType;
            source.sendSuccess(() -> Component.literal("成功从 " + player.getName().getString() + " 的主手物品移除了 " + 
                    typeName + " 词缀"), true);
        } else {
            source.sendFailure(Component.literal("物品上没有找到 " + elementType + " 类型的词缀"));
            return 0;
        }
        
        return 1;
    }
    
    /**
     * 列出词缀命令
     */
    private static int listAffixes(CommandSourceStack source, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("玩家主手上没有物品"));
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
}