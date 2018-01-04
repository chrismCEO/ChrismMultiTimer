package com.chrismsolutions.chrismmultitimer;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.chrismsolutions.chrismmultitimer.billingUtil.IabHelper;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<List<MultiTimer>>
{

    private MultiTimerArrayAdapter multiTimerAdapter;
    private ListView timerListView;
    public static int LOADER_ID = 1;
    private MultiTimerAdHelper adHelper;
    private boolean showAds;
    private IabHelper mHelper;
    private MenuItem removeAdMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAds();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name_without_chrism);
        setSupportActionBar(toolbar);

        //Show the logo in the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setIcon(R.mipmap.round_logo);
        }

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, fab);
                popupMenu.inflate(R.menu.add_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId())
                        {
                            case R.id.add_new_timer:
                                MultiTimerDialogFragment fragment = new MultiTimerDialogFragment();
                                fragment.show(getSupportFragmentManager(), MultiTimerDialogFragment.DIALOG_TAG);
                                break;

                            case R.id.add_hidden_timer:
                                MultiTimerHiddenDialogFragment hiddenDialogFragment = new MultiTimerHiddenDialogFragment();
                                hiddenDialogFragment.show(getSupportFragmentManager(), MultiTimerHiddenDialogFragment.DIALOG_TAG);
                                break;

                            default:
                                throw new IllegalArgumentException("Illegal menu choice: " + menuItem.getItemId());
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        timerListView = findViewById(R.id.list);
        timerListView.setEmptyView(findViewById(R.id.empty_view));

        multiTimerAdapter = new MultiTimerArrayAdapter(this, new ArrayList<MultiTimer>());
        timerListView.setAdapter(multiTimerAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        //For test purposes only
        //TestDB.testDB(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (mHelper == null)
        {
            mHelper = adHelper.getIabHelper();
        }
        if (!mHelper.handleActivityResult(requestCode, resultCode, data))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else
        {
            changeDesign();
        }
    }

    public void changeDesign()
    {
        //There's been a possible change in ownership, change the layout
        RelativeLayout relativeLayout = findViewById(R.id.relative_layout_main);
        adHelper.createAd(relativeLayout);

        if (removeAdMenuItem != null)
        {
            removeAdMenuItem.setVisible(adHelper.showAd());
        }
    }

    /**
     * Set the @adHelper object if we show ads or need to check ownership
     */
    private void setContentViewAds()
    {
        //Show ads if user has not payed to remove them
        if (getIntent() != null && getIntent().hasExtra(ChrismAdHelper.IS_PREMIUM_USER))
        {
            showAds = !getIntent().getBooleanExtra(ChrismAdHelper.IS_PREMIUM_USER, true);
            adHelper = new MultiTimerAdHelper(this, false, false);
            if (showAds)
            {
                changeDesign();
            }
        }
        else
        {
            adHelper = new MultiTimerAdHelper(this, true, false);
        }
    }

    /**
     * Run this when user clicks on "Remove ads" menu item
     * @param item
     */
    public void removeAds(MenuItem item)
    {
        adHelper.removeAds();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        removeAdMenuItem = menu.findItem(R.id.remove_ads);
        removeAdMenuItem.setVisible(adHelper.showAd());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        boolean result = false;

        switch (id)
        {
            case R.id.action_settings:
                result = true;
                break;

            case R.id.remove_ads:
                result = adHelper.removeAds();
                break;
        }

        return result && super.onOptionsItemSelected(item);
    }

    /**
     * Get all relevant timers from the database, which have show = 1
     * @return
     */
    private Cursor queryMultiTimerDB()
    {
        String[] projection = {
                MultiTimerEntry._ID,
                MultiTimerEntry.COLUMN_TIMER_NAME,
                MultiTimerEntry.COLUMN_TIMER_CLOCK,
                MultiTimerEntry.COLUMN_TIMER_SHOW,
                MultiTimerEntry.COLUMN_TIMER_INFO
        };

        String selection = MultiTimerEntry.COLUMN_TIMER_SHOW + MultiTimerProvider.getSqlJoker();
        String[] selectionArgs = new String[]{String.valueOf(MultiTimerEntry.VALUE_TIMER_SHOW_TRUE)};
        String sortOrder = MultiTimerEntry.COLUMN_TIMER_NAME + " ASC";

        return getContentResolver().query(
                MultiTimerEntry.CONTENT_URI_TIMER,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    @Override
    public Loader<List<MultiTimer>> onCreateLoader(int i, Bundle bundle)
    {
        return new MultiTimerLoader(MainActivity.this, queryMultiTimerDB());
    }


    /**
     * Keep a list of all the timers shown, and add/remove timers based on user action
     * @param loader
     * @param multiTimers
     */
    @Override
    public void onLoadFinished(Loader<List<MultiTimer>> loader, List<MultiTimer> multiTimers)
    {
        if (multiTimers != null && !multiTimers.isEmpty())
        {
            if (multiTimerAdapter.isEmpty())
            {
                //The app has started, add all timers
                multiTimerAdapter.addAll(multiTimers);
            }
            else
            {
                //A new timer has been created, recreate the adapter with all timers
                ArrayList<MultiTimer> originalList = new ArrayList<MultiTimer>();
                copyFromList(multiTimerAdapter.getMultiTimerArrayList(), originalList);
                multiTimerAdapter.clear();

                int k = 0;
                for (int i = 0; i < multiTimers.size(); i++)
                {
                    MultiTimer multiTimer = multiTimers.get(i);
                    if (multiTimer.getId() != originalList.get(k).getId())
                    {
                        //Add the new timer
                        multiTimerAdapter.add(multiTimer);
                    }
                    else
                    {
                        //Add the existing timer
                        multiTimerAdapter.add(originalList.get(k));
                        if (k+1 < originalList.size())
                        {
                            k++;
                        }
                    }
                }
            }
        }
    }

    private void copyFromList(ArrayList<MultiTimer> from, ArrayList<MultiTimer> to)
    {
        to.addAll(from);
    }

    @Override
    public void onLoaderReset(Loader<List<MultiTimer>> loader)
    {
        multiTimerAdapter.clear();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    public void setDeleted(int id)
    {
        MultiTimer timer = null;

        for (int i = 0; i < multiTimerAdapter.getCount(); i++)
        {
            timer = multiTimerAdapter.getItem(i);
            if (timer != null && timer.getId() == id)
            {
                timer.setDeleted(true);
                multiTimerAdapter.remove(timer);
                break;
            }
        }
    }
}
