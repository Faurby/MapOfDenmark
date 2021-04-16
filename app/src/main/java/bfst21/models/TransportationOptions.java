package bfst21.models;

import java.util.HashMap;


public class TransportationOptions {

    private final HashMap<TransportationOption, Boolean> transOptions = new HashMap<>();

    public TransportationOptions() {
        transOptions.put(TransportationOption.CAR, true);
        transOptions.put(TransportationOption.BIKE, false);
        transOptions.put(TransportationOption.WALK, false);
    }

    public void chooseType(TransportationOption transOp) {
        transOptions.put(transOp, true);
        if (transOp != TransportationOption.CAR) {
            transOptions.put(TransportationOption.CAR, false);
        }
        if (transOp != TransportationOption.BIKE) {
            transOptions.put(TransportationOption.BIKE, false);
        }
        if (transOp != TransportationOption.WALK) {
            transOptions.put(TransportationOption.WALK, false);
        }
    }

    public TransportationOption returnType() {
        if (transOptions.get(TransportationOption.WALK)) {
            return TransportationOption.WALK;
        } else if (transOptions.get(TransportationOption.BIKE)) {
            return TransportationOption.BIKE;
        } else {
            return TransportationOption.CAR;
        }
    }
}
