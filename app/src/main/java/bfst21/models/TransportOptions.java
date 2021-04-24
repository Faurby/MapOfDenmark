package bfst21.models;

import java.util.HashMap;


public class TransportOptions {

    private final HashMap<TransportOption, Boolean> transOptions = new HashMap<>();

    public TransportOptions() {
        transOptions.put(TransportOption.CAR, true);
        transOptions.put(TransportOption.BIKE, false);
        transOptions.put(TransportOption.WALK, false);
    }

    public void chooseType(TransportOption transOp) {
        transOptions.put(transOp, true);
        if (transOp != TransportOption.CAR) {
            transOptions.put(TransportOption.CAR, false);
        }
        if (transOp != TransportOption.BIKE) {
            transOptions.put(TransportOption.BIKE, false);
        }
        if (transOp != TransportOption.WALK) {
            transOptions.put(TransportOption.WALK, false);
        }
    }

    public TransportOption returnType() {
        if (transOptions.get(TransportOption.WALK)) {
            return TransportOption.WALK;
        } else if (transOptions.get(TransportOption.BIKE)) {
            return TransportOption.BIKE;
        } else {
            return TransportOption.CAR;
        }
    }
}
