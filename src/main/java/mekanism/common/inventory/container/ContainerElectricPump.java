package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class ContainerElectricPump extends ContainerMekanism<TileEntityElectricPump> {

    public ContainerElectricPump(InventoryPlayer inventory, TileEntityElectricPump tile) {
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
            if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID != 2) {
                    if (!mergeItemStack(slotStack, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 3, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (FluidContainerUtils.isFluidContainer(slotStack) && FluidUtil.getFluidContained(slotStack) == null) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 3, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (slotID == 1) {
                if (!mergeItemStack(slotStack, 3, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotID >= 3 && slotID <= 29) {
                    if (!mergeItemStack(slotStack, 30, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID > 29) {
                    if (!mergeItemStack(slotStack, 3, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 3, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
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

    @Override
    protected void addSlots() {
        addSlotToContainer(new Slot(tileEntity, 0, 28, 20));
        addSlotToContainer(new SlotOutput(tileEntity, 1, 28, 51));
        addSlotToContainer(new SlotDischarge(tileEntity, 2, 143, 35));
    }
}