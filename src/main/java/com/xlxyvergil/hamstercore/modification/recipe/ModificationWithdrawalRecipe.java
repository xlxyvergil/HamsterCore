package com.xlxyvergil.hamstercore.modification.recipe;

import com.google.common.collect.Lists;
import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.ModificationItems;
import com.xlxyvergil.hamstercore.modification.SocketHelper;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

/**
 * 卸载配方 - 模仿 Apotheosis 的 WithdrawalRecipe
 */
public class ModificationWithdrawalRecipe extends SmithingTransformRecipe implements ReactiveSmithingRecipe {

    public static final ResourceLocation RECIPE_ID = new ResourceLocation("hamstercore", "withdrawal");
    
    // Forge smithing slot indices
    public static final int TEMPLATE = 0, BASE = 1, ADDITION = 2;

    public ModificationWithdrawalRecipe(ResourceLocation id) {
        super(id, Ingredient.EMPTY, 
            Ingredient.EMPTY, 
            Ingredient.of(ModificationItems.PRECISION_SCREWDRIVER.get()), 
            ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack base = container.getItem(BASE);
        ItemStack add = container.getItem(ADDITION);

        // 添加物品必须是卸载工具（精密螺丝刀）
        if (!isWithdrawalTool(add)) {
            return false;
        }

        // 基础物品必须有已安装的改装件（特殊或通用）
        boolean hasValidNormalMods = SocketHelper.getModifications(base).stream().anyMatch(ModificationInstance::isValid);
        boolean hasValidSpecialMods = SocketHelper.getSpecialModifications(base).stream().anyMatch(ModificationInstance::isValid);
        
        return hasValidNormalMods || hasValidSpecialMods;
    }

    /**
     * 检查是否是卸载工具
     */
    private boolean isWithdrawalTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == com.xlxyvergil.hamstercore.modification.ModificationItems.PRECISION_SCREWDRIVER.get();
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack base = container.getItem(BASE);
        ItemStack out = base.copy();
        if (out.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        // 先移除所有已安装改装件对应的词缀
        // 移除通用改装件词缀
        SocketedModifications normalMods = SocketHelper.getModifications(base);
        for (ModificationInstance mod : normalMods.modifications()) {
            if (mod.isValid()) {
                com.xlxyvergil.hamstercore.api.element.AffixAPI.removeAffix(out, mod.uuid());
            }
        }
        
        // 移除特殊改装件词缀
        List<ModificationInstance> specialMods = SocketHelper.getSpecialModifications(base);
        for (ModificationInstance mod : specialMods) {
            if (mod.isValid()) {
                com.xlxyvergil.hamstercore.api.element.AffixAPI.removeAffix(out, mod.uuid());
            }
        }
        
        // 清空输出物品的改装件数据
        SocketHelper.setModifications(out, Lists.newArrayList());
        SocketHelper.setSpecialModifications(out, Lists.newArrayList());
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return RECIPE_ID;
    }

    @Override
    public void onCraft(Container inv, Player player, ItemStack output) {
        // 在Forge 1.20.1中，inputSlots可能不是标准Container
        // 使用正确的槽位访问方式
        ItemStack base = inv.getItem(BASE);
        
        // 如果获取不到，尝试使用反射或直接访问inputSlots
        if (base.isEmpty() && inv.getItem(ADDITION).isEmpty()) {
            // 尝试通过inputSlots直接访问槽位
            try {
                java.lang.reflect.Field slotsField = Container.class.getDeclaredField("inputSlots");
                slotsField.setAccessible(true);
                Object slots = slotsField.get(inv);
                if (slots != null) {
                    java.lang.reflect.Method getFirst = slots.getClass().getMethod("getFirst");
                    Object firstSlot = getFirst.invoke(slots);
                    if (firstSlot != null) {
                        java.lang.reflect.Method getItem = firstSlot.getClass().getMethod("getItem");
                        base = (ItemStack) getItem.invoke(firstSlot);
                    }
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        
        if (base.isEmpty()) {
            return;
        }
        
        // 获取通用改装件
        SocketedModifications normalMods = SocketHelper.getModifications(base);
        for (int i = 0; i < normalMods.size(); i++) {
            ItemStack stack = normalMods.get(i).modificationStack();
            if (!stack.isEmpty()) {
                if (!player.addItem(stack)) {
                    net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                        player.level(), player.getX(), player.getY(), player.getZ(), stack);
                    player.level().addFreshEntity(entity);
                }
            }
        }
        
        // 获取特殊改装件
        List<ModificationInstance> specialMods = SocketHelper.getSpecialModifications(base);
        for (ModificationInstance specialMod : specialMods) {
            if (specialMod.isValid()) {
                ItemStack stack = specialMod.modificationStack();
                if (!stack.isEmpty()) {
                    if (!player.addItem(stack)) {
                        net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                            player.level(), player.getX(), player.getY(), player.getZ(), stack);
                        player.level().addFreshEntity(entity);
                    }
                }
            }
        }

        // 先移除原始物品上所有已安装改装件对应的词缀
        // 移除通用改装件词缀
        SocketedModifications originalNormalMods = SocketHelper.getModifications(base);
        for (ModificationInstance mod : originalNormalMods.modifications()) {
            if (mod.isValid()) {
                com.xlxyvergil.hamstercore.api.element.AffixAPI.removeAffix(base, mod.uuid());
            }
        }
        
        // 移除特殊改装件词缀
        List<ModificationInstance> originalSpecialMods = SocketHelper.getSpecialModifications(base);
        for (ModificationInstance mod : originalSpecialMods) {
            if (mod.isValid()) {
                com.xlxyvergil.hamstercore.api.element.AffixAPI.removeAffix(base, mod.uuid());
            }
        }
        
        // 清空原始物品的改装件数据
        SocketHelper.setModifications(base, Lists.newArrayList());
        SocketHelper.setSpecialModifications(base, Lists.newArrayList());
    }

    public static class Serializer implements RecipeSerializer<ModificationWithdrawalRecipe> {
        public static Serializer INSTANCE = new Serializer();

        @Override
        public ModificationWithdrawalRecipe fromJson(ResourceLocation pRecipeId, com.google.gson.JsonObject pJson) {
            return new ModificationWithdrawalRecipe(pRecipeId);
        }

        @Override
        public ModificationWithdrawalRecipe fromNetwork(ResourceLocation pRecipeId, net.minecraft.network.FriendlyByteBuf pBuffer) {
            return new ModificationWithdrawalRecipe(pRecipeId);
        }

        @Override
        public void toNetwork(net.minecraft.network.FriendlyByteBuf pBuffer, ModificationWithdrawalRecipe pRecipe) {
        }
    }
}


