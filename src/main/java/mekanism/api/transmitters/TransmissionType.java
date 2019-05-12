package mekanism.api.transmitters;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.translation.I18n;

public enum TransmissionType {
    ENERGY("EnergyNetwork", "Energy"),
    FLUID("FluidNetwork", "Fluids"),
    GAS("GasNetwork", "Gases"),
    ITEM("InventoryNetwork", "Items"),
    HEAT("HeatNetwork", "Heat");

    private String name;
    private String transmission;

    TransmissionType(String n, String t) {
        name = n;
        transmission = t;
    }

    public static boolean checkTransmissionType(ITransmitter sideTile, TransmissionType type) {
        return type.checkTransmissionType(sideTile);
    }

    public static boolean checkTransmissionType(TileEntity tile1, TransmissionType type) {
        return checkTransmissionType(tile1, type, null);
    }

    public static boolean checkTransmissionType(TileEntity tile1, TransmissionType type, TileEntity tile2) {
        return type.checkTransmissionType(tile1, tile2);
    }

    public String getName() {
        return name;
    }

    public String getTransmission() {
        return transmission;
    }

    public String localize() {
        return I18n.translateToLocal(getTranslationKey());
    }

    public String getTranslationKey() {
        return "transmission." + getTransmission();
    }

    public boolean checkTransmissionType(ITransmitter transmitter) {
        return transmitter.getTransmissionType() == this;
    }

    public boolean checkTransmissionType(TileEntity sideTile, TileEntity currentTile) {
        return sideTile instanceof ITransmitter && ((ITransmitter) sideTile).getTransmissionType() == this;
    }
}