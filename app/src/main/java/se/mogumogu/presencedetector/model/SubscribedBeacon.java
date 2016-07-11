package se.mogumogu.presencedetector.model;

import org.altbeacon.beacon.Beacon;

public class SubscribedBeacon {

    private String aliasName;
    private Beacon beacon;
    private boolean isInRangeNotified;
    private boolean isOutOfRangeNotified;

    public SubscribedBeacon(String aliasName, Beacon beacon) {

        this.aliasName = aliasName;
        this.beacon = beacon;
    }

    public String getAliasName() {

        return aliasName;
    }

    public Beacon getBeacon() {

        return beacon;
    }

    public boolean isInRangeNotified() {

        return isInRangeNotified;
    }

    public boolean isOutOfRangeNotified() {

        return isOutOfRangeNotified;
    }

    public void setInRangeNotified(boolean inRangeNotified) {

        isInRangeNotified = inRangeNotified;
    }

    public void setOutOfRangeNotified(boolean outOfRangeNotified) {

        isOutOfRangeNotified = outOfRangeNotified;
    }

    @Override
    public String toString() {
        return "SubscribedBeacon{" +
                "aliasName='" + aliasName + '\'' +
                ", beacon=" + beacon +
                ", isInRangeNotified=" + isInRangeNotified +
                ", isOutOfRangeNotified=" + isOutOfRangeNotified +
                '}';
    }
}
