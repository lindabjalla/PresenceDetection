package se.mogumogu.presencedetector.model;

import android.os.Parcel;
import android.os.Parcelable;

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
