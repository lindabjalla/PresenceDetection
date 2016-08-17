package se.mogumogu.presencedetector.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.altbeacon.beacon.Beacon;

public final class SubscribedBeacon implements Parcelable {

    private String aliasName;
    private Beacon beacon;
    private String dateOfSubscription;
    private boolean isInRangeNotified;
    private boolean isOutOfRangeNotified;
    private boolean isInRange;
    private long inRangeTime;
    private long outOfRangeTime;

    public SubscribedBeacon(final String aliasName, final Beacon beacon, final String dateOfSubscription) {

        this.aliasName = aliasName;
        this.beacon = beacon;
        this.dateOfSubscription = dateOfSubscription;
    }

    protected SubscribedBeacon(final Parcel in) {

        aliasName = in.readString();
        beacon = in.readParcelable(Beacon.class.getClassLoader());
        dateOfSubscription = in.readString();
        isInRangeNotified = in.readByte() != 0;
        isOutOfRangeNotified = in.readByte() != 0;
        isInRange = in.readByte() != 0;
        inRangeTime = in.readLong();
        outOfRangeTime = in.readLong();
    }

    public static final Creator<SubscribedBeacon> CREATOR = new Creator<SubscribedBeacon>() {
        @Override
        public SubscribedBeacon createFromParcel(final Parcel in) {

            return new SubscribedBeacon(in);
        }

        @Override
        public SubscribedBeacon[] newArray(final int size) {

            return new SubscribedBeacon[size];
        }
    };

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {

        out.writeString(aliasName);
        out.writeParcelable(beacon, flags);
        out.writeString(dateOfSubscription);
        out.writeByte((byte) (isInRangeNotified ? 1 : 0));
        out.writeByte((byte) (isOutOfRangeNotified ? 1 : 0));
        out.writeByte((byte) (isInRange ? 1 : 0));
        out.writeLong(inRangeTime);
        out.writeLong(outOfRangeTime);
    }

    public String getAliasName() {

        return aliasName;
    }

    public Beacon getBeacon() {

        return beacon;
    }

    public String getDateOfSubscription() {

        return dateOfSubscription;
    }

    public boolean isInRangeNotified() {

        return isInRangeNotified;
    }

    public boolean isOutOfRangeNotified() {

        return isOutOfRangeNotified;
    }

    public boolean isInRange() {

        return isInRange;
    }

    public long getInRangeTime() {

        return inRangeTime;
    }

    public long getOutOfRangeTime() {

        return outOfRangeTime;
    }

    public SubscribedBeacon setAliasName(final String aliasName) {

        this.aliasName = aliasName;

        return this;
    }

    public void setBeacon(final Beacon beacon) {

        this.beacon = beacon;
    }

    public void setInRangeNotified(final boolean inRangeNotified) {

        isInRangeNotified = inRangeNotified;
    }

    public void setOutOfRangeNotified(final boolean outOfRangeNotified) {

        isOutOfRangeNotified = outOfRangeNotified;
    }

    public void setInRange(final boolean inRange) {

        isInRange = inRange;
    }

    public void setInRangeTime(final long inRangeTime) {

        this.inRangeTime = inRangeTime;
    }

    public void setOutOfRangeTime(final long outOfRangeTime) {

        this.outOfRangeTime = outOfRangeTime;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) {

            return true;
        }

        if (other instanceof SubscribedBeacon) {

            final SubscribedBeacon otherSubscribedBeacon = (SubscribedBeacon) other;

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
                ", dateOfSubscription='" + dateOfSubscription + '\'' +
                ", isInRangeNotified=" + isInRangeNotified +
                ", isOutOfRangeNotified=" + isOutOfRangeNotified +
                ", isInRange=" + isInRange +
                ", inRangeTime=" + inRangeTime +
                ", outOfRangeTime=" + outOfRangeTime +
                '}';
    }
}
