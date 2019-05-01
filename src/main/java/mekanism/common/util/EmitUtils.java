package mekanism.common.util;

import java.util.Set;
import mekanism.common.base.SplitInfo;
import mekanism.common.base.SplitInfo.DoubleSplitInfo;
import mekanism.common.base.SplitInfo.IntegerSplitInfo;
import mekanism.common.base.target.EnergyAcceptorTarget;
import mekanism.common.base.target.Target;

public class EmitUtils {

    /**
     * @param <HANDLER> The handler of our target.
     * @param <TYPE> The type of the number
     * @param <EXTRA> Any extra information we may need.
     * @param <TARGET> The emitter target.
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param totalTargets The total number of targets. Note: this number is bigger than availableTargets.size if any
     * targets have more than one acceptor.
     * @param splitInfo Information containing the split.
     * @param toSend Any extra information such as gas stack or fluid stack.
     * @return The amount that actually got sent.
     */
    private static <HANDLER, TYPE extends Number & Comparable<TYPE>, EXTRA, TARGET extends Target<HANDLER, TYPE, EXTRA>>
    TYPE sendToAcceptors(Set<TARGET> availableTargets, int totalTargets, SplitInfo<TYPE> splitInfo, EXTRA toSend) {
        if (availableTargets.isEmpty() || totalTargets == 0) {
            return splitInfo.getTotalSent();
        }

        //Simulate addition, sending when the requested amount is less than the amountPer
        // splitInfo gets adjusted to account for how much is actually sent
        availableTargets.forEach(target -> target.sendPossible(toSend, splitInfo));

        //Only run this if we changed the amountPer from when we first/last ran things
        while (splitInfo.amountPerChanged) {
            splitInfo.amountPerChanged = false;
            //splitInfo gets adjusted to account for how much is actually sent,
            // and if amountPer got changed again and we need to rerun this
            availableTargets.forEach(target -> target.shiftNeeded(splitInfo));
        }

        //Evenly distribute the remaining amount we have to give between all targets and handlers
        // splitInfo gets adjusted to account for how much is actually sent
        availableTargets.forEach(target -> target.sendRemainingSplit(splitInfo));
        return splitInfo.getTotalSent();
    }

    /**
     * @param <HANDLER> The handler of our target.
     * @param <EXTRA> Any extra information we may need
     * @param <TARGET> The emitter target
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param totalTargets The total number of targets. Note: this number is bigger than availableTargets.size if any
     * targets have more than one acceptor.
     * @param amountToSplit The amount to split between all the targets
     * @param toSend Any extra information such as gas stack or fluid stack.
     * @return The amount that actually got sent.
     */
    public static <HANDLER, EXTRA, TARGET extends Target<HANDLER, Integer, EXTRA>> int sendToAcceptors(
          Set<TARGET> availableTargets, int totalTargets, int amountToSplit, EXTRA toSend) {
        return sendToAcceptors(availableTargets, totalTargets, new IntegerSplitInfo(amountToSplit, totalTargets),
              toSend);
    }

    /**
     * @param availableTargets The EnergyAcceptorWrapper targets to send energy fairly to.
     * @param totalTargets The total number of targets. Note: this number is bigger than availableTargets.size if any
     * targets have more than one acceptor.
     * @param amountToSplit The amount of energy to attempt to send
     * @return The amount that actually got sent
     */
    public static double sendToAcceptors(Set<EnergyAcceptorTarget> availableTargets, int totalTargets,
          double amountToSplit) {
        return sendToAcceptors(availableTargets, totalTargets, new DoubleSplitInfo(amountToSplit, totalTargets),
              amountToSplit);
    }
}