package se.mogumogu.presencedetector.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.fragment.SettingsFragment;

public final class SettingsActivity extends ToolbarProvider {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setToolbar(toolbar, false);

        getFragmentManager().beginTransaction().replace(R.id.pref_content, new SettingsFragment()).commit();
    }
}
