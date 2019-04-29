package mekanism.common.tile.transmitter;

import io.netty.buffer.ByteBuf;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.transmitters.grid.GasNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityPressurizedTube extends TileEntityTransmitter<IGasHandler, GasNetwork, GasStack> implements
      IGasHandler {

    public TubeTier tier = TubeTier.BASIC;

    public float currentScale;

    public GasTank buffer = new GasTank(getCapacity());

    public GasStack lastWrite;

    @Override
    public BaseTier getBaseTier() {
        return tier.getBaseTier();
    }

    @Override
    public void setBaseTier(BaseTier baseTier) {
        tier = TubeTier.get(baseTier);
        buffer.setMaxGas(getCapacity());
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            updateShare();

            IGasHandler[] connectedAcceptors = GasUtils.getConnectedAcceptors(getPos(), getWorld());

            for (EnumFacing side : getConnections(ConnectionType.PULL)) {
                if (connectedAcceptors[side.ordinal()] != null) {
                    IGasHandler container = connectedAcceptors[side.ordinal()];

                    if (container != null) {
                        GasStack received = container.drawGas(side.getOpposite(), tier.getTubePullAmount(), false);

                        if (received != null && received.amount != 0) {
                            container.drawGas(side.getOpposite(), takeGas(received, true), true);
                        }
                    }
                }
            }

        } else {
            float targetScale =
                  getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().gasScale
                        : (float) buffer.getStored() / (float) buffer.getMaxGas();

            if (Math.abs(currentScale - targetScale) > 0.01) {
                currentScale = (9 * currentScale + targetScale) / 10;
            }
        }

        super.update();
    }

    @Override
    public void updateShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0) {
            GasStack last = getSaveShare();

            if ((last != null && !(lastWrite != null && lastWrite.amount == last.amount && lastWrite.getGas() == last
                  .getGas())) || (last == null && lastWrite != null)) {
                lastWrite = last;
                markDirty();
            }
        }
    }

    private GasStack getSaveShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null) {
            int remain = getTransmitter().getTransmitterNetwork().buffer.amount % getTransmitter()
                  .getTransmitterNetwork().transmitters.size();
            int toSave = getTransmitter().getTransmitterNetwork().buffer.amount / getTransmitter()
                  .getTransmitterNetwork().transmitters.size();

            if (getTransmitter().getTransmitterNetwork().transmitters.iterator().next().equals(getTransmitter())) {
                toSave += remain;
            }

            return new GasStack(getTransmitter().getTransmitterNetwork().buffer.getGas(), toSave);
        }

        return null;
    }

    @Override
    public void onChunkUnload() {
        if (!getWorld().isRemote && getTransmitter().hasTransmitterNetwork()) {
            if (lastWrite != null && getTransmitter().getTransmitterNetwork().buffer != null) {
                getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;

                if (getTransmitter().getTransmitterNetwork().buffer.amount <= 0) {
                    getTransmitter().getTransmitterNetwork().buffer = null;
                }
            }
        }

        super.onChunkUnload();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        if (nbtTags.hasKey("tier")) {
            tier = TubeTier.values()[nbtTags.getInteger("tier")];
        }
        buffer.setMaxGas(getCapacity());

        if (nbtTags.hasKey("cacheGas")) {
            buffer.setGas(GasStack.readFromNBT(nbtTags.getCompoundTag("cacheGas")));
        } else {
            buffer.setGas(null);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        if (lastWrite != null && lastWrite.amount > 0) {
            nbtTags.setTag("cacheGas", lastWrite.write(new NBTTagCompound()));
        } else {
            nbtTags.removeTag("cacheGas");
        }

        nbtTags.setInteger("tier", tier.ordinal());

        return nbtTags;
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.GAS;
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.PRESSURIZED_TUBE;
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, EnumFacing side) {
        return GasUtils.isValidAcceptorOnSide(tile, side);
    }

    @Override
    public boolean isValidTransmitter(TileEntity tileEntity) {
        if (!super.isValidTransmitter(tileEntity)) {
            return false;
        }
        if (!(tileEntity instanceof TileEntityPressurizedTube)) {
            return true;
        }
        GasStack buffer = getBufferWithFallback();
        GasStack otherBuffer = ((TileEntityPressurizedTube) tileEntity).getBufferWithFallback();
        return buffer == null || otherBuffer == null || buffer.isGasEqual(otherBuffer);
    }

    @Override
    public GasNetwork createNewNetwork() {
        return new GasNetwork();
    }

    @Override
    public GasNetwork createNetworkByMerging(Collection<GasNetwork> networks) {
        return new GasNetwork(networks);
    }

    @Override
    protected boolean canHaveIncompatibleNetworks() {
        return true;
    }

    @Override
    public int getCapacity() {
        return tier.getTubeCapacity();
    }

    @Nullable
    @Override
    public GasStack getBuffer() {
        if (buffer == null) {
            return null;
        }
        GasStack gas = buffer.getGas();
        return gas == null || gas.amount == 0 ? null : gas;
    }

    @Override
    public void takeShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null
              && lastWrite != null) {
            getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;
            buffer.setGas(lastWrite);
        }
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL) {
            return takeGas(stack, doTransfer);
        }

        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return false;
    }

    public int takeGas(GasStack gasStack, boolean doEmit) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().emit(gasStack, doEmit);
        } else {
            return buffer.receive(gasStack, doEmit);
        }
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{buffer};
    }

    @Override
    public IGasHandler getCachedAcceptor(EnumFacing side) {
        TileEntity tile = getCachedTile(side);

        if (CapabilityUtils.hasCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite())) {
            return CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite());
        }

        return null;
    }

    @Override
    public boolean upgrade(int tierOrdinal) {
        if (tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal() + 1) {
            tier = TubeTier.values()[tier.ordinal() + 1];

            markDirtyTransmitters();
            sendDesc = true;

            return true;
        }

        return false;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) throws Exception {
        tier = TubeTier.values()[dataStream.readInt()];

        super.handlePacketData(dataStream);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(tier.ordinal());

        super.getNetworkedData(data);

        return data;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }
}
