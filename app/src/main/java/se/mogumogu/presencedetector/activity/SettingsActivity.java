package se.mogumogu.presencedetector.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        myToolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        setSupportActionBar(myToolbar);

        context = this;

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            actionBar.setHomeAsUpIndicator(R.drawable.icon_arrow_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.pref_content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {

            case R.id.menu_item_my_beacons:
                intent = new Intent(this, SubscribedBeaconsActivity.class);
                context.startActivity(intent);
                return true;

            case R.id.menu_item_settings:
                intent = new Intent(this, SettingsActivity.class);
                context.startActivity(intent);
                return true;

            case R.id.menu_item_help:
                intent = new Intent(this, HelpActivity.class);
                context.startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
