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

    private NumberPicker numberPicker;
    private String maxValue;
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
        maxValue = getContext().getResources().getString(R.string.rssi_threshold_max);
        String currentValue = preferences.getString("preference_rssi_threshold_key", maxValue);

        rssiThresholdValues = new ArrayList<>();

        for (int i = -80; i <= -45; i++) {

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
            Log.d("maxValue", String.valueOf(numberPicker.getMaxValue()));
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

        setValue(restorePersistedValue ? getPersistedString(maxValue) : (String) defaultValue);
    }

    public void setValue(String value) {

        persistString(value);
    }
}
