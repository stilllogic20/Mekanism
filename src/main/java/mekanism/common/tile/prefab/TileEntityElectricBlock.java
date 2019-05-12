package mekanism.common.tile.prefab;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.tesla.TeslaIntegration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Method;

public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements IEnergyWrapper {

    /**
     * How much energy is stored in this block.
     */
    public double electricityStored;

    /**
     * Maximum amount of energy this machine can hold.
     */
    public double BASE_MAX_ENERGY;

    /**
     * Actual maximum energy storage, including upgrades
     */
    public double maxEnergy;

    private boolean ic2Registered = false;
    private CapabilityWrapperManager<IEnergyWrapper, TeslaIntegration> teslaManager = new CapabilityWrapperManager<>(IEnergyWrapper.class, TeslaIntegration.class);
    private CapabilityWrapperManager<IEnergyWrapper, ForgeEnergyIntegration> forgeEnergyManager = new CapabilityWrapperManager<>(IEnergyWrapper.class, ForgeEnergyIntegration.class);

    /**
     * The base of all blocks that deal with electricity. It has a facing state, initialized state, and a current amount of stored energy.
     *
     * @param name          - full name of this block
     * @param baseMaxEnergy - how much energy this block can store
     */
    public TileEntityElectricBlock(String name, double baseMaxEnergy) {
        super(name);
        BASE_MAX_ENERGY = baseMaxEnergy;
        maxEnergy = BASE_MAX_ENERGY;
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void register() {
        if (!world.isRemote && !ic2Registered) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            ic2Registered = true;
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void deregister() {
        if (!world.isRemote && ic2Registered) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            ic2Registered = false;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (MekanismUtils.useIC2()) {
            register();
        }
    }

    @Override
    public void onUpdate() {
		/*if(MekanismUtils.useIC2()) {
			register();
		}*/
    }

    @Override
    public boolean sideIsOutput(EnumFacing side) {
        return false;
    }

    @Override
    public boolean sideIsConsumer(EnumFacing side) {
        return true;
    }

    @Override
    public double getMaxOutput() {
        return 0;
    }

    @Override
    public double getEnergy() {
        return electricityStored;
    }

    @Override
    public void setEnergy(double energy) {
        electricityStored = Math.max(Math.min(energy, getMaxEnergy()), 0);
        MekanismUtils.saveChunk(this);
    }

    @Override
    public double getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            setEnergy(dataStream.readDouble());
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(getEnergy());
        return data;
    }

    @Override
    public void onChunkUnload() {
        if (MekanismUtils.useIC2()) {
            deregister();
        }
        super.onChunkUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (MekanismUtils.useIC2()) {
            deregister();
        }
    }

    @Override
    public void validate() {
        boolean wasInvalid = this.tileEntityInvalid;//workaround for pending tile entity invalidate/revalidate cycle
        super.validate();
        if (wasInvalid && MekanismUtils.useIC2()) {//re-register if we got invalidated
            register();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        electricityStored = nbtTags.getDouble("electricityStored");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setDouble("electricityStored", getEnergy());
        return nbtTags;
    }

    /**
     * Gets the scaled energy level for the GUI.
     *
     * @param i - multiplier
     *
     * @return scaled energy
     */
    public int getScaledEnergyLevel(int i) {
        return (int) (getEnergy() * i / getMaxEnergy());
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return MekanismUtils.clampToInt(acceptEnergy(from, maxReceive * MekanismConfig.current().general.FROM_RF.val(), simulate)
                                        * MekanismConfig.current().general.TO_RF.val());
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return MekanismUtils.clampToInt(pullEnergy(from, maxExtract * MekanismConfig.current().general.FROM_RF.val(), simulate)
                                        * MekanismConfig.current().general.TO_RF.val());
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public boolean canConnectEnergy(EnumFacing from) {
        return sideIsConsumer(from) || sideIsOutput(from);
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getEnergyStored(EnumFacing from) {
        return MekanismUtils.clampToInt(getEnergy() * MekanismConfig.current().general.TO_RF.val());
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getMaxEnergyStored(EnumFacing from) {
        return MekanismUtils.clampToInt(getMaxEnergy() * MekanismConfig.current().general.TO_RF.val());
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getSinkTier() {
        return !MekanismConfig.current().general.blacklistIC2.val() ? 4 : 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getSourceTier() {
        return !MekanismConfig.current().general.blacklistIC2.val() ? 4 : 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int addEnergy(int amount) {
        if (!MekanismConfig.current().general.blacklistIC2.val()) {
            setEnergy(getEnergy() + amount * MekanismConfig.current().general.FROM_IC2.val());
            return MekanismUtils.clampToInt(getEnergy() * MekanismConfig.current().general.TO_IC2.val());
        }
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public boolean isTeleporterCompatible(EnumFacing side) {
        return !MekanismConfig.current().general.blacklistIC2.val() && sideIsOutput(side);
    }

    @Override
    public boolean canOutputEnergy(EnumFacing side) {
        return sideIsOutput(side);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
        return !MekanismConfig.current().general.blacklistIC2.val() && sideIsConsumer(direction);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
        return !MekanismConfig.current().general.blacklistIC2.val() && sideIsOutput(direction) && receiver instanceof IEnergyConductor;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getStored() {
        return MekanismUtils.clampToInt(getEnergy() * MekanismConfig.current().general.TO_IC2.val());
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void setStored(int energy) {
        if (!MekanismConfig.current().general.blacklistIC2.val()) {
            setEnergy(energy * MekanismConfig.current().general.FROM_IC2.val());
        }
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getCapacity() {
        return MekanismUtils.clampToInt(getMaxEnergy() * MekanismConfig.current().general.TO_IC2.val());
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getOutput() {
        return MekanismUtils.clampToInt(getMaxOutput() * MekanismConfig.current().general.TO_IC2.val());
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getDemandedEnergy() {
        return !MekanismConfig.current().general.blacklistIC2.val() ? (getMaxEnergy() - getEnergy()) * MekanismConfig.current().general.TO_IC2.val() : 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getOfferedEnergy() {
        return !MekanismConfig.current().general.blacklistIC2.val() ? Math.min(getEnergy(), getMaxOutput()) * MekanismConfig.current().general.TO_IC2.val() : 0;
    }

    @Override
    public boolean canReceiveEnergy(EnumFacing side) {
        return sideIsConsumer(side);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getOutputEnergyUnitsPerTick() {
        return !MekanismConfig.current().general.blacklistIC2.val() ? getMaxOutput() * MekanismConfig.current().general.TO_IC2.val() : 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double injectEnergy(EnumFacing pushDirection, double amount, double voltage) {
        // nb: the facing param contains the side relative to the pushing block
        TileEntity tile = getWorld().getTileEntity(getPos().offset(pushDirection.getOpposite()));
        if (MekanismConfig.current().general.blacklistIC2.val() || CapabilityUtils.hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, pushDirection)) {
            return amount;
        }
        return amount - acceptEnergy(pushDirection.getOpposite(), amount * MekanismConfig.current().general.FROM_IC2.val(), false)
                        * MekanismConfig.current().general.TO_IC2.val();
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void drawEnergy(double amount) {
        setEnergy(Math.max(getEnergy() - (amount * MekanismConfig.current().general.FROM_IC2.val()), 0));
    }

    @Override
    public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
        double toUse = Math.min(getMaxEnergy() - getEnergy(), amount);
        if (toUse < 0.0001 || (side != null && !sideIsConsumer(side))) {
            return 0;
        }
        if (!simulate) {
            setEnergy(getEnergy() + toUse);
        }
        return toUse;
    }

    @Override
    public double pullEnergy(EnumFacing side, double amount, boolean simulate) {
        double toGive = Math.min(getEnergy(), amount);
        if (toGive < 0.0001 || (side != null && !sideIsOutput(side))) {
            return 0;
        }
        if (!simulate) {
            setEnergy(getEnergy() - toGive);
        }
        return toGive;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY || isTesla(capability, side) || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (isStrictEnergy(capability)) {
            return (T) this;
        } else if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(getForgeEnergyWrapper(side));
        } else if (isTesla(capability, side)) {
            return (T) getTeslaEnergyWrapper(side);
        }
        return super.getCapability(capability, side);
    }

    protected boolean isStrictEnergy(@Nonnull Capability capability) {
        return capability == Capabilities.ENERGY_STORAGE_CAPABILITY || capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY || capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY;
    }

    protected boolean isTesla(@Nonnull Capability capability, EnumFacing side) {
        return capability == Capabilities.TESLA_HOLDER_CAPABILITY || (capability == Capabilities.TESLA_CONSUMER_CAPABILITY && sideIsConsumer(side))
               || (capability == Capabilities.TESLA_PRODUCER_CAPABILITY && sideIsOutput(side));
    }

    protected ForgeEnergyIntegration getForgeEnergyWrapper(EnumFacing side) {
        return forgeEnergyManager.getWrapper(this, side);
    }

    protected TeslaIntegration getTeslaEnergyWrapper(EnumFacing side) {
        return teslaManager.getWrapper(this, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY || isTesla(capability, side)) {
            return side != null && !sideIsConsumer(side) && !sideIsOutput(side);
        }
        return super.isCapabilityDisabled(capability, side);
    }
}