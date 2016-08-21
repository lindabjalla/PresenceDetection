package se.mogumogu.presencedetector.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import se.mogumogu.presencedetector.R;

public final class HelpActivity extends ToolbarProvider {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.help_toolbar);
        setToolbar(toolbar, false);
    }
}
