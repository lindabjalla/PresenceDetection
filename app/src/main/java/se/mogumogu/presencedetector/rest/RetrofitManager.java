package se.mogumogu.presencedetector.rest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import se.mogumogu.presencedetector.NotificationBuilder;
import se.mogumogu.presencedetector.PresenceDetectorApplication;
import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.activity.RegistrationActivity;
import se.mogumogu.presencedetector.model.SubscribedBeacon;
import se.mogumogu.presencedetector.model.Timestamp;
import se.mogumogu.presencedetector.model.UserId;

public final class RetrofitManager {

    private static final String TAG = RetrofitManager.class.getSimpleName();

    private String responseBody;
    private PresenceDetectorService service;
    private Gson gson;
    private SharedPreferences appDataPreferences;
    private Context context;
    private Intent intent;
    private Beacon beacon;
    private View view;
    private DialogFragment dialog;
    private String responseSuccess;
    private SubscribedBeacon subscribedBeacon;
    private long inRangeTime;
    private long outOfRangeTime;
    private String subscribedBeaconsJson;
    private Set<SubscribedBeacon> subscribedBeacons;
    private String beaconAliasName;
    private String errorLogMessage;

    public RetrofitManager(final String serverUrl, final Context context) {

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl + "/sys/api/")
                .addConverterFactory(new StringConverterFactory())
                .build();

        this.context = context;
        service = retrofit.create(PresenceDetectorService.class);
        gson = new Gson();
        responseSuccess = "\"response_value\":\"200\"";
        errorLogMessage = "Neither inRangeTime or outOfRangeTime matched timeInMillis, " +
                "or both inRangeTime and outOfRangeTime are the same";
    }

    public PresenceDetectorService getPresenceDetectionService() {

        return service;
    }

    public void setBeacon(final Beacon beacon) {

        this.beacon = beacon;
    }

    public void setView(final View view) {

        this.view = view;
    }

    public void setDialogFragment(final DialogFragment dialog) {

        this.dialog = dialog;
    }

    public void setSubscribedBeacon(final SubscribedBeacon subscribedBeacon) {

        this.subscribedBeacon = subscribedBeacon;
    }

    public void setInRangeTime(final long inRangeTime) {

        this.inRangeTime = inRangeTime;
    }

    public void setOutOfRangeTime(final long outOfRangeTime) {

        this.outOfRangeTime = outOfRangeTime;
    }

    public void handleResponse(final Call<String> result, final String tag) {

        appDataPreferences =
                context.getSharedPreferences(PresenceDetectorApplication.PRESENCE_DETECTOR_PREFERENCES, Context.MODE_PRIVATE);

        result.enqueue(new Callback<String>() {
            @Override
            public void onResponse(final Call<String> call, final Response<String> response) {

                Log.d("response", response.body());
                responseBody = response.body();

                if (responseBody.contains(responseSuccess)) {

                    switch (tag) {

                        case "RegistrationActivity":
                            handleResponseOnRegistrationActivity();
                            break;

                        case "ScanActivity":
                            handleResponseOnScanActivity();
                            break;

                        case "notifyInRange":
                            handleResponseOnRangeHandler(inRangeTime);
                            break;

                        case "notifyOutOfRange":
                            handleResponseOnRangeHandler(outOfRangeTime);
                            break;

                        default:
                            Log.d(TAG, "No tag matched.");
                            break;
                    }

                } else {

                    Log.d("response", responseBody);
                }
            }

            @Override
            public void onFailure(final Call<String> call, final Throwable t) {

                Log.d("onFailure", "failed");
                t.printStackTrace();
            }
        });
    }

    private void handleResponseOnRegistrationActivity() {

        final UserId userIdObject = gson.fromJson(responseBody, UserId.class);
        final String userId = userIdObject.getUserId();
        Log.d("userId from server", userId);

        appDataPreferences.edit().putString(PresenceDetectorApplication.USER_ID, userId).apply();
        appDataPreferences.edit().putBoolean(PresenceDetectorApplication.USER_IS_REGISTERED, true).apply();

        Toast.makeText(context, "The app is successfully activated.", Toast.LENGTH_LONG).show();

        intent = ((Activity) context).getIntent();
        ((Activity) context).finish();
        context.startActivity(intent);
    }

    private void handleResponseOnScanActivity() {

        final Timestamp timestampObject = gson.fromJson(responseBody, Timestamp.class);
        final String timestamp = timestampObject.getTimestamp();
        Log.d("timestamp from server", timestamp);

        final EditText aliasNameEditText = (EditText) view.findViewById(R.id.alias_name);
        final String aliasName = aliasNameEditText.getText().toString();

        final DateFormat dateFormat = DateFormat.getDateTimeInstance();
        final String dateOfSubscription = dateFormat.format(new Date(Long.parseLong(timestamp) * 1000L));
        Log.d("dateOfSubscription", dateOfSubscription);

        subscribedBeaconsJson = appDataPreferences.getString(PresenceDetectorApplication.SUBSCRIBED_BEACONS, null);
        subscribedBeacons = PresenceDetectorApplication.initializeSubscribedBeacons(subscribedBeaconsJson);

        subscribedBeacons.add(new SubscribedBeacon(aliasName, beacon, dateOfSubscription));
        subscribedBeaconsJson = gson.toJson(subscribedBeacons);
        appDataPreferences.edit().putString(PresenceDetectorApplication.TIMESTAMP, timestamp).apply();
        appDataPreferences.edit().putString(PresenceDetectorApplication.SUBSCRIBED_BEACONS, subscribedBeaconsJson).apply();
        dialog.dismiss();

        Toast.makeText(context, aliasName + " is successfully subscribed.", Toast.LENGTH_LONG).show();
        intent = new Intent(context, RegistrationActivity.class);
        context.startActivity(intent);
    }

    private void handleResponseOnRangeHandler(final long timeInMilliseconds) {

        subscribedBeaconsJson = appDataPreferences.getString(PresenceDetectorApplication.SUBSCRIBED_BEACONS, null);
        subscribedBeacons = PresenceDetectorApplication.initializeSubscribedBeacons(subscribedBeaconsJson);

        boolean isNotifyInRange = isNotifyInRange(timeInMilliseconds);
        boolean isNotifyOutOfRange = isNotifyOutOfRange(timeInMilliseconds);

        if (subscribedBeacon != null) {

            updateRangeStatus(isNotifyInRange, isNotifyOutOfRange);
            beaconAliasName = subscribedBeacon.getAliasName();
        }

        subscribedBeaconsJson = gson.toJson(subscribedBeacons);
        appDataPreferences.edit().putString(PresenceDetectorApplication.SUBSCRIBED_BEACONS, subscribedBeaconsJson).apply();

        sendNotification(isNotifyInRange, isNotifyOutOfRange);
    }

    private void updateRangeStatus(final boolean isNotifyInRange, final boolean isNotifyOutOfRange) {

        subscribedBeacons.remove(subscribedBeacon);

        if (isNotifyInRange && !isNotifyOutOfRange) {

            subscribedBeacon.setInRangeTime(inRangeTime);
            subscribedBeacon.setInRangeNotified(true);

        } else if (isNotifyOutOfRange && !isNotifyInRange) {

            subscribedBeacon.setOutOfRangeTime(outOfRangeTime);
            subscribedBeacon.setOutOfRangeNotified(true);

        } else {

            Log.e(TAG, errorLogMessage);
        }

        subscribedBeacons.add(subscribedBeacon);
    }

    private boolean isNotifyInRange(final long timeInMilliseconds) {

        return inRangeTime == timeInMilliseconds;
    }

    private boolean isNotifyOutOfRange(final long timeInMilliseconds) {

        return outOfRangeTime == timeInMilliseconds;
    }

    private void sendNotification(final boolean isNotifyInRange, final boolean isNotifyOutOfRange) {

        final NotificationBuilder notificationBuilder = new NotificationBuilder(context);

        if (isNotifyInRange && !isNotifyOutOfRange) {

            notificationBuilder.sendNotification("Beacon in range", "Beacon " + beaconAliasName + " came in the range.",
                    R.drawable.icon_bluetooth_in_range, subscribedBeacon);

        } else if (isNotifyOutOfRange && !isNotifyInRange) {

            notificationBuilder.sendNotification("Beacon out of range", "Beacon " + beaconAliasName + " went out of the range.",
                    R.drawable.icon_bluetooth_out_of_range, subscribedBeacon);

        } else {

            Log.e(TAG, errorLogMessage);
        }
    }
}
