package se.mogumogu.presencedetector.model;

import org.altbeacon.beacon.Beacon;

import java.io.Serializable;

public class SubscribedBeacon implements Serializable {

    private String aliasName;
    private Beacon beacon;
    private boolean isInRangeNotified;
    private boolean isOutOfRangeNotified;
    private boolean isInRange;

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

    public boolean isInRange(){

        return isInRange;
    }

    public SubscribedBeacon setAliasName(String aliasName) {

        this.aliasName = aliasName;
        return this;
    }

    public void setInRangeNotified(boolean inRangeNotified) {

        isInRangeNotified = inRangeNotified;
    }

    public void setOutOfRangeNotified(boolean outOfRangeNotified) {

        isOutOfRangeNotified = outOfRangeNotified;
    }

    public void setInRange(boolean inRange){

        isInRange = inRange;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {

            return true;
        }
        if (other instanceof SubscribedBeacon) {

           SubscribedBeacon otherSubscribedBeacon = (SubscribedBeacon) other;
            return aliasName.equals(otherSubscribedBeacon.getAliasName()) && beacon.equals(otherSubscribedBeacon.beacon);
        }
        return false;
    }

    @Override
    public int hashCode() {

        int result = 1;
        result = 37 * result + (aliasName != null ? aliasName.hashCode() : 0);
        result = 37 * result + (beacon != null ? beacon.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "SubscribedBeacon{" +
                "aliasName='" + aliasName + '\'' +
                ", beacon=" + beacon +
                ", isInRangeNotified=" + isInRangeNotified +
                ", isOutOfRangeNotified=" + isOutOfRangeNotified +
                ", isInRange=" + isInRange +
                '}';
    }
}
