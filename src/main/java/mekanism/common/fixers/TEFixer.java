package mekanism.common.fixers;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.fixers.MekanismDataFixers.MekFixers;
import net.minecraft.nbt.NBTTagCompound;

public class TEFixer extends BaseMekanismFixer {

    private final Map<String, String> tileEntityNames = new HashMap<>();
    private final String modid;

    protected TEFixer(String modid, MekFixers fixer) {
        super(fixer);
        this.modid = modid;
    }

    protected void putEntry(String oldName, String newName) {
        String newLocation = modid + ":" + newName;
        //Fix both the old name (without "minecraft:" for multiparts), and the one with "minecraft:" for in world TEs
        tileEntityNames.put(oldName, newLocation);
        tileEntityNames.put("minecraft:" + oldName.toLowerCase(), newLocation);
    }

    @Override
    @Nonnull
    public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound compound) {
        String teLoc = compound.getString("id");
        //Fix multipart
        if (teLoc.equals("mcmultipart:multipart.ticking") || teLoc.equals("mcmultipart:multipart.nonticking")) {
            if (compound.hasKey("parts")) {
                NBTTagCompound parts = compound.getCompoundTag("parts");
                for (String sID : parts.getKeySet()) {
                    NBTTagCompound part = parts.getCompoundTag(sID);
                    if (part.hasKey("tile")) {
                        fixTagCompound(part.getCompoundTag("tile"));
                    }
                }
            }
        } else if (tileEntityNames.containsKey(teLoc)) {
            //Fix other TEs but only if they are one of ours
            String newID = tileEntityNames.get(teLoc);
            compound.setString("id", newID);
            Mekanism.logger.info("Fixed TE from {} to {}", teLoc, newID);
        }
        return compound;
    }
}