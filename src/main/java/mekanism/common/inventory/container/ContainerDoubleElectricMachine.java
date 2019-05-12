package mekanism.common.inventory.container;

import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.tile.prefab.TileEntityDoubleElectricMachine;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerDoubleElectricMachine extends ContainerMekanism<TileEntityDoubleElectricMachine> {

    public ContainerDoubleElectricMachine(InventoryPlayer inventory, TileEntityDoubleElectricMachine tile) {
        super(tile, inventory);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotID == 2) {
                if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID != 3) {
                    if (!mergeItemStack(slotStack, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (isExtraItem(slotStack)) {
                if (slotID != 1) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (isInputItem(slotStack)) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 4 && slotID <= 30) {
                if (!mergeItemStack(slotStack, 31, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 30) {
                if (!mergeItemStack(slotStack, 4, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.getCount() == 0) {
                currentSlot.putStack(ItemStack.EMPTY);
            } else {
                currentSlot.onSlotChanged();
            }
            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            currentSlot.onTake(player, slotStack);
        }
        return stack;
    }

    private boolean isInputItem(ItemStack itemstack) {
        for (Map.Entry<DoubleMachineInput, ItemStack> entry : ((Map<DoubleMachineInput, ItemStack>) tileEntity.getRecipes()).entrySet()) {
            if (entry.getKey().itemStack.isItemEqual(itemstack)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExtraItem(ItemStack itemstack) {
        for (Map.Entry<DoubleMachineInput, ItemStack> entry : ((Map<DoubleMachineInput, ItemStack>) tileEntity.getRecipes()).entrySet()) {
            if (entry.getKey().extraStack.isItemEqual(itemstack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new Slot(tileEntity, 0, 56, 17));
        addSlotToContainer(new Slot(tileEntity, 1, 56, 53));
        addSlotToContainer(new SlotOutput(tileEntity, 2, 116, 35));
        addSlotToContainer(new SlotDischarge(tileEntity, 3, 31, 35));
    }
}