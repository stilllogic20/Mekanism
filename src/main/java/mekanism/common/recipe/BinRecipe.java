package mekanism.common.recipe;

import javax.annotation.Nonnull;
import mekanism.common.MekanismItems;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemProxy;
import mekanism.common.tier.BinTier;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class BinRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private static boolean registered;

    public BinRecipe() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(this);
            registered = true;
        }
        setRegistryName("bin");
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
        return !getCraftingResult(inv).isEmpty();
    }

    private boolean isBin(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return BasicBlockType.get(itemStack) == BasicBlockType.BIN && itemStack.getCount() <= 1;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        return getResult(inv);
    }

    public ItemStack getResult(IInventory inv) {
        ItemStack bin = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (isBin(stack)) {
                if (!bin.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                bin = stack.copy();
            }
        }

        if (bin.isEmpty() || bin.getCount() > 1) {
            return ItemStack.EMPTY;
        }

        ItemStack addStack = ItemStack.EMPTY;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && !isBin(stack)) {
                if (!addStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                addStack = stack.copy();
            }
        }

        InventoryBin binInv = new InventoryBin(bin);

        if (!addStack.isEmpty()) {
            if (!(addStack.getItem() instanceof ItemProxy)) {
                if (!binInv.getItemType().isEmpty() && !binInv.getItemType().isItemEqual(addStack)) {
                    return ItemStack.EMPTY;
                }
                binInv.add(addStack);
            }
            return bin;
        }
        return binInv.removeStack();
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }

    @SubscribeEvent
    public void onCrafting(ItemCraftedEvent event) {
        if (!getResult(event.craftMatrix).isEmpty()) {
            if (!isBin(event.crafting)) {
                for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
                    if (isBin(event.craftMatrix.getStackInSlot(i))) {
                        ItemStack bin = event.craftMatrix.getStackInSlot(i);
                        InventoryBin inv = new InventoryBin(bin.copy());

                        int size = inv.getItemCount();
                        ItemStack testRemove = inv.removeStack();
                        int newCount = size - (!testRemove.isEmpty() ? testRemove.getCount() : 0);
                        if (inv.getTier() == BinTier.CREATIVE) {
                            newCount = size;
                        }

                        ItemDataUtils.setInt(bin, "newCount", newCount);
                    }
                }
            } else {
                int bin = -1;
                int other = -1;
                for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
                    if (isBin(event.craftMatrix.getStackInSlot(i))) {
                        bin = i;
                    } else if (!isBin(event.craftMatrix.getStackInSlot(i)) && !event.craftMatrix.getStackInSlot(i).isEmpty()) {
                        other = i;
                    }
                }

                ItemStack binStack = event.craftMatrix.getStackInSlot(bin);
                ItemStack otherStack = event.craftMatrix.getStackInSlot(other);

                ItemStack testRemain = new InventoryBin(binStack.copy()).add(otherStack.copy());
                if (!testRemain.isEmpty() && testRemain.getCount() > 0) {
                    ItemStack proxy = new ItemStack(MekanismItems.ItemProxy);
                    ((ItemProxy) proxy.getItem()).setSavedItem(proxy, testRemain.copy());
                    event.craftMatrix.setInventorySlotContents(other, proxy);
                } else {
                    event.craftMatrix.setInventorySlotContents(other, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }
}