package se.mogumogu.presencedetector.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import se.mogumogu.presencedetector.R;

public abstract class ToolbarProvider extends AppCompatActivity{

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {

            case R.id.menu_item_my_beacons:
                intent = new Intent(this, SubscribedBeaconsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_item_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_item_help:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setToolbar(final Toolbar myToolbar, final boolean launcher){

        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        myToolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        setSupportActionBar(myToolbar);

        if(!launcher){

            final ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {

                actionBar.setHomeAsUpIndicator(R.drawable.icon_arrow_back);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }
}
