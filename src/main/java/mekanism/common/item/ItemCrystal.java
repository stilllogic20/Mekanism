package mekanism.common.item;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.Resource;
import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemCrystal extends ItemMekanism implements IMetaItem {

    public ItemCrystal() {
        super();
        setHasSubtypes(true);
    }

    @Override
    public String getTexture(int meta) {
        return Resource.values()[meta].getName() + "Crystal";
    }

    @Override
    public int getVariants() {
        return Resource.values().length;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> itemList) {
        if (isInCreativeTab(tabs)) {
            for (int counter = 0; counter < Resource.values().length; counter++) {
                itemList.add(new ItemStack(this, 1, counter));
            }
        }
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack item) {
        if (item.getItemDamage() <= Resource.values().length - 1) {
            return "item." + Resource.values()[item.getItemDamage()].getName().toLowerCase(Locale.ROOT) + "Crystal";
        }
        return "Invalid";
    }
}