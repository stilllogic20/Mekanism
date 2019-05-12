package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import mekanism.api.util.ItemInfo;
import mekanism.common.util.ItemRegistryUtils;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDictCache {

    public static final HashMap<ItemInfo, List<String>> cachedKeys = new HashMap<>();
    public static final HashMap<String, List<ItemStack>> oreDictStacks = new HashMap<>();
    public static final HashMap<String, List<ItemStack>> modIDStacks = new HashMap<>();

    public static List<String> getOreDictName(@Nonnull ItemStack check) {
        if (check.isEmpty())
            return new ArrayList<>(0);

        return cachedKeys.computeIfAbsent(ItemInfo.get(check),
                k -> {
                    return Arrays.stream(OreDictionary.getOreIDs(check))
                            .mapToObj(OreDictionary::getOreName)
                            .collect(Collectors.toCollection(ArrayList::new));
                });
    }

    public static List<ItemStack> getOreDictStacks(String oreName, boolean forceBlock) {
        final Predicate<String> case1 = s -> oreName.equals(s) || oreName.equals("*");
        final Predicate<String> case2 = s -> oreName.endsWith("*") && !oreName.startsWith("*")
                && s.startsWith(oreName.substring(0, oreName.length() - 1));
        final Predicate<String> case3 = s -> oreName.startsWith("*") && !oreName.endsWith("*")
                && s.endsWith(oreName.substring(1));
        final Predicate<String> case4 = s -> oreName.startsWith("*") && oreName.endsWith("*")
                && s.contains(oreName.substring(1, oreName.length() - 1));
        final Predicate<String> checks = case1.or(case2).or(case3).or(case4);

        return oreDictStacks.computeIfAbsent(oreName, k -> {

            return Arrays
                    .stream(OreDictionary.getOreNames())
                    .filter(Objects::nonNull)
                    .filter(checks)
                    .map(OreDictionary::getOres)
                    .flatMap(Collection::stream)
                    .distinct()
                    .filter(stack -> !forceBlock || stack.copy().getItem() instanceof ItemBlock)
                    .collect(Collectors.toCollection(ArrayList::new));
        });
    }

    public static List<ItemStack> getModIDStacks(String modName, boolean forceBlock) {
        Predicate<String> case1 = s -> (modName.equals(s) || modName.equals("*"));
        Predicate<String> case2 = s -> (modName.endsWith("*") && !modName.startsWith("*")
                && s.startsWith(modName.substring(0, modName.length() - 1)));
        Predicate<String> case3 = s -> (modName.startsWith("*") && !modName.endsWith("*")
                && s.endsWith(modName.substring(1)));
        Predicate<String> case4 = s -> (modName.startsWith("*") && modName.endsWith("*")
                && s.contains(modName.substring(1, modName.length() - 1)));
        Predicate<String> checks = case1.or(case2).or(case3).or(case4);

        return modIDStacks.computeIfAbsent(modName, k -> {
            return Arrays.stream(OreDictionary.getOreNames())
                    .map(OreDictionary::getOres)
                    .flatMap(Collection::stream)
                    .distinct()
                    .filter(stack -> stack.getItem() instanceof ItemBlock)
                    .filter(stack -> checks.test(ItemRegistryUtils.getMod(stack.copy())))
                    .collect(Collectors.toCollection(ArrayList::new));
        });
    }
}