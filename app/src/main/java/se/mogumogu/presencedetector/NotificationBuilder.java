package se.mogumogu.presencedetector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import se.mogumogu.presencedetector.activity.BeaconDetailsActivity;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public final class NotificationBuilder {

    private Context context;

    public NotificationBuilder(final Context context) {

        this.context = context;
    }

    public void sendNotification(final String title,
                                 final String text,
                                 final int resourceId,
                                 final SubscribedBeacon subscribedBeacon) {

        Log.d("sendNotification", "came into the method");

        final NotificationCompat.Builder notificationBuilder = buildNotification(resourceId, title, text);

        final Intent intent = new Intent(context, BeaconDetailsActivity.class);

        putSubscribedBeaconIntoIntent(subscribedBeacon, intent);

        final TaskStackBuilder stackBuilder = buildStack(intent);
        final PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder.setContentIntent(pendingIntent);

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(PresenceDetectorApplication.NOTIFICATION_ID, notificationBuilder.build());
        Log.d("sendNotification", "notification sent");
    }

    private NotificationCompat.Builder buildNotification(final int resourceId, final String title, final String text) {

        return (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(resourceId)
                .setContentTitle(title)
                .setContentText(text)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 100, 200, 300})
                .setLights(0xFF0000FF, 100, 3000)
                .setPriority(Notification.PRIORITY_DEFAULT);
    }

    private void putSubscribedBeaconIntoIntent(final SubscribedBeacon subscribedBeacon, final Intent intent) {

        final Bundle bundle = new Bundle();
        bundle.putParcelable(PresenceDetectorApplication.SUBSCRIBED_BEACON, subscribedBeacon);
        intent.putExtras(bundle);
    }

    private TaskStackBuilder buildStack(final Intent intent) {

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(BeaconDetailsActivity.class);
        stackBuilder.addNextIntent(intent);

        return stackBuilder;
    }
}
