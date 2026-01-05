package com.xlxyvergil.hamstercore.modification.recipe;

import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.ModificationItem;
import com.xlxyvergil.hamstercore.modification.ModificationItems;
import com.xlxyvergil.hamstercore.modification.ModificationRegistry;
import com.xlxyvergil.hamstercore.modification.SocketHelper;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 安装配方 - 模仿 Apotheosis 的 SocketingRecipe
 */
public class ModificationSocketingRecipe extends SmithingTransformRecipe {

    public static final ResourceLocation RECIPE_ID = new ResourceLocation("hamstercore", "socketing");
    
    // Forge smithing slot indices
    public static final int TEMPLATE = 0, BASE = 1, ADDITION = 2;

    public ModificationSocketingRecipe(ResourceLocation id) {
        super(id, Ingredient.EMPTY, 
            Ingredient.EMPTY, 
            Ingredient.of(ModificationItems.MODIFICATION.get()), 
            ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack base = container.getItem(BASE);
        ItemStack add = container.getItem(ADDITION);

        // 添加物品必须是改装件
        if (!isModification(add)) {
            return false;
        }

        // 检查改装件是否需要特殊槽位
        String modId = ModificationItem.getModificationId(add);
        if (modId == null || modId.isEmpty()) {
            return false;
        }

        DynamicHolder<com.xlxyvergil.hamstercore.modification.Modification> modHolder = 
            ModificationRegistry.INSTANCE.holder(ResourceLocation.parse(modId));
        if (modHolder == null || !modHolder.isBound()) {
            return false;
        }

        boolean needsSpecialSocket = modHolder.get().useSpecialSocket();
        
        // 检查互斥组：获取新改装件的互斥组
        java.util.List<String> newModGroups = modHolder.get().mutualExclusionGroups();
        
        // 获取所有已安装的改装件（特殊和通用）
        List<ModificationInstance> allMods = new java.util.ArrayList<>();
        allMods.addAll(SocketHelper.getSpecialModifications(base));
        allMods.addAll(SocketHelper.getModifications(base).modifications());
        
        // 检查互斥组冲突
        for (ModificationInstance existingMod : allMods) {
            if (existingMod.isValid()) {
                DynamicHolder<com.xlxyvergil.hamstercore.modification.Modification> existingModHolder = existingMod.getModification();
                if (existingModHolder != null && existingModHolder.isBound()) {
                    java.util.List<String> existingGroups = existingModHolder.get().mutualExclusionGroups();
                    // 检查是否有交集
                    for (String newGroup : newModGroups) {
                        if (existingGroups.contains(newGroup)) {
                            return false; // 互斥组冲突，不能安装
                        }
                    }
                }
            }
        }
        
        // 检查槽位是否可用
        if (needsSpecialSocket) {
            // 需要特殊槽位：检查是否有空特殊槽位
            List<ModificationInstance> specialMods = SocketHelper.getSpecialModifications(base);
            for (ModificationInstance inst : specialMods) {
                if (!inst.isValid()) {
                    return true; // 有空位
                }
            }
            return false; // 没有空特殊槽位
        } else {
            // 需要通用槽位：检查是否有空通用槽位
            return SocketHelper.hasEmptySockets(base);
        }
    }

    /**
     * 检查是否是改装件
     */
    private boolean isModification(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == ModificationItems.MODIFICATION.get();
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack base = container.getItem(BASE).copy();
        ItemStack add = container.getItem(ADDITION).copy();

        // 获取改装件ID
        String modId = ModificationItem.getModificationId(add);
        if (modId == null || modId.isEmpty()) {
            return ItemStack.EMPTY;
        }

        DynamicHolder<com.xlxyvergil.hamstercore.modification.Modification> modHolder = 
            ModificationRegistry.INSTANCE.holder(ResourceLocation.parse(modId));
        if (modHolder == null || !modHolder.isBound()) {
            return ItemStack.EMPTY;
        }

        boolean needsSpecialSocket = modHolder.get().useSpecialSocket();
        
        // 创建新的改装件实例
        ModificationInstance newInst = new ModificationInstance(modId, java.util.UUID.randomUUID());
        
        if (needsSpecialSocket) {
            // 安装到特殊槽位
            List<ModificationInstance> specialMods = SocketHelper.getSpecialModifications(base);
            for (int i = 0; i < specialMods.size(); i++) {
                if (!specialMods.get(i).isValid()) {
                    specialMods.set(i, newInst);
                    SocketHelper.setSpecialModifications(base, specialMods);
                    newInst.applyAffixes(base, null);
                    return base;
                }
            }
        } else {
            // 安装到通用槽位
            int slot = SocketHelper.getFirstEmptySocket(base);
            if (slot < 0) {
                return ItemStack.EMPTY;
            }

            SocketedModifications mods = SocketHelper.getModifications(base);
            List<ModificationInstance> modificationList = new java.util.ArrayList<>(mods.modifications());
            
            int sockets = SocketHelper.getSockets(base);
            if (modificationList.size() < sockets) {
                while (modificationList.size() < sockets) {
                    modificationList.add(ModificationInstance.EMPTY);
                }
            }

            modificationList.set(slot, newInst);
            SocketHelper.setModifications(base, modificationList);
            new SocketedModifications(modificationList).applyAllModifications(base, null);
            
            return base;
        }

        return ItemStack.EMPTY;
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

    public static class Serializer implements RecipeSerializer<ModificationSocketingRecipe> {
        public static Serializer INSTANCE = new Serializer();

        @Override
        public ModificationSocketingRecipe fromJson(ResourceLocation pRecipeId, com.google.gson.JsonObject pJson) {
            return new ModificationSocketingRecipe(pRecipeId);
        }

        @Override
        public ModificationSocketingRecipe fromNetwork(ResourceLocation pRecipeId, net.minecraft.network.FriendlyByteBuf pBuffer) {
            return new ModificationSocketingRecipe(pRecipeId);
        }

        @Override
        public void toNetwork(net.minecraft.network.FriendlyByteBuf pBuffer, ModificationSocketingRecipe pRecipe) {
        }
    }
}


