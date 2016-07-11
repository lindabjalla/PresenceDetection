package se.mogumogu.presencedetector.model;

public class BeaconOutOfRange extends AbstractEntity{

    String timestamp;

    public BeaconOutOfRange(String userId, String beaconUuid, String timestamp){

        this.userId = userId;
        this.beaconUuid = beaconUuid;
        this.timestamp = timestamp;
    }
}
