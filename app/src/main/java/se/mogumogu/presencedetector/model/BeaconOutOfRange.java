package se.mogumogu.presencedetector.model;

public final class BeaconOutOfRange extends AbstractEntity{

    private String userId;
    private String beaconUuid;
    private String timestamp;

    public BeaconOutOfRange(final String userId, final String beaconUuid, final String timestamp){

        this.userId = userId;
        this.beaconUuid = beaconUuid;
        this.timestamp = timestamp;
    }
}
