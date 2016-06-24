package se.mogumogu.presencedetection.model;

import org.altbeacon.beacon.Beacon;

public class SubscribedBeacon {

    private String aliasName;
    private Beacon beacon;

    public SubscribedBeacon(String aliasName, Beacon beacon) {

        this.aliasName = aliasName;
        this.beacon = beacon;
    }

    public Beacon getBeacon() {

        return beacon;
    }
}
