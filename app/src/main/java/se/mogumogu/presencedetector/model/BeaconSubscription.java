package se.mogumogu.presencedetector.model;

public final class BeaconSubscription extends AbstractEntity {

    public BeaconSubscription(final String userId, final String beaconUuid) {

        this.userId = userId;
        this.beaconUuid = beaconUuid;
    }
}
