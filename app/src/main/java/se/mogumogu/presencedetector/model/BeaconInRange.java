package se.mogumogu.presencedetector.model;

public class BeaconInRange extends AbstractEntity {

    private String major;
    private String minor;
    private String rssi;
    private String timestamp;

    public BeaconInRange(String userId, String beaconUuid, String major, String minor, String rssi, String timestamp) {

        this.userId = userId;
        this.beaconUuid = beaconUuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.timestamp = timestamp;
    }
}
