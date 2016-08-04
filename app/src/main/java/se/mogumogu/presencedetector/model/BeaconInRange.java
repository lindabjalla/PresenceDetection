package se.mogumogu.presencedetector.model;

public final class BeaconInRange extends AbstractEntity {

    private String major;
    private String minor;
    private String rssi;
    private String timestamp;

    public BeaconInRange(final String userId,
                         final String beaconUuid,
                         final String major,
                         final String minor,
                         final String rssi,
                         final String timestamp) {

        this.userId = userId;
        this.beaconUuid = beaconUuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.timestamp = timestamp;
    }
}
