package se.mogumogu.presencedetection.model;

public class BeaconSubscription extends AbstractEntity{

    public BeaconSubscription(String userId, String beaconUuid){

        super.userId = userId;
        super.beaconUuid = beaconUuid;
    }
}
