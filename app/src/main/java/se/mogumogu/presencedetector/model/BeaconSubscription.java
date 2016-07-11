package se.mogumogu.presencedetector.model;

public class BeaconSubscription extends AbstractEntity{

    public BeaconSubscription(String userId, String beaconUuid){

        this.userId = userId;
        this.beaconUuid = beaconUuid;
    }
}
