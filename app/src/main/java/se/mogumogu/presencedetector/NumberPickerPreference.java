package se.mogumogu.presencedetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

public class NumberPickerPreference extends DialogPreference {

    public static final String PREFERENCE_RSSI_THRESHOLD_KEY = "preference_rssi_threshold_key";

    private NumberPicker numberPicker;
    private String defaultValue;
    private List<String> rssiThresholdValues;

    public NumberPickerPreference(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        numberPicker = new NumberPicker(getContext());
        numberPicker.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(numberPicker);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {

        super.onBindDialogView(view);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        defaultValue = getContext().getResources().getString(R.string.rssi_threshold_default);
        String currentValue = preferences.getString(PREFERENCE_RSSI_THRESHOLD_KEY, defaultValue);

        rssiThresholdValues = new ArrayList<>();

        for (int i = -69; i <= -45; i++) {

            rssiThresholdValues.add(String.valueOf(i));
        }

        int listSize = rssiThresholdValues.size();
        Log.d("currentValue", String.valueOf(currentValue));

        final int indexOfCurrentValue = rssiThresholdValues.indexOf(currentValue);
        Log.d("index", String.valueOf(indexOfCurrentValue));

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(listSize - 1);
        numberPicker.setValue(indexOfCurrentValue);
        numberPicker.setDisplayedValues(rssiThresholdValues.toArray(new String[listSize]));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {

        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            numberPicker.clearFocus();

            Log.d("minValue", String.valueOf(numberPicker.getMinValue()));
            Log.d("defaultValue", String.valueOf(numberPicker.getMaxValue()));
            Log.d("rssiThresholdValues", rssiThresholdValues.toString());

            int index = numberPicker.getValue();
            String newValue = rssiThresholdValues.get(index);

            if (callChangeListener(newValue)) {

                Log.d("newValue", newValue);
                setValue(newValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {

        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        setValue(restorePersistedValue ? getPersistedString(this.defaultValue) : (String) defaultValue);
    }

    public void setValue(String value) {

        persistString(value);
    }
}
