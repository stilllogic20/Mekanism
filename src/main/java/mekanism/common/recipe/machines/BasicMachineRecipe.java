package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class BasicMachineRecipe<RECIPE extends BasicMachineRecipe<RECIPE>> extends MachineRecipe<ItemStackInput, ItemStackOutput, RECIPE> {

    public BasicMachineRecipe(ItemStackInput input, ItemStackOutput output) {
        super(input, output);
    }

    public BasicMachineRecipe(ItemStack input, ItemStack output) {
        this(new ItemStackInput(input), new ItemStackOutput(output));
    }

    public boolean inputMatches(NonNullList<ItemStack> inventory, int inputIndex) {
        return getInput().useItemStackFromInventory(inventory, inputIndex, false);
    }

    public boolean canOperate(NonNullList<ItemStack> inventory, int inputIndex, int outputIndex) {
        return inputMatches(inventory, inputIndex) && getOutput().applyOutputs(inventory, outputIndex, false);
    }

    public void operate(NonNullList<ItemStack> inventory, int inputIndex, int outputIndex) {
        if (getInput().useItemStackFromInventory(inventory, inputIndex, true)) {
            getOutput().applyOutputs(inventory, outputIndex, true);
        }
    }
}