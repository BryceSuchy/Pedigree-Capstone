package com.pedigreetechnologies.diagnosticview;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Vector;


public class ViewPagerFragmentActivity extends AppCompatActivity {

    private PagerAdapter mPagerAdapter;
    ViewPager pager;
    ToggleButton toggleAB;
    int pagePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Intent intent = getIntent();

        // get rid of app name being displayed in views
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                this.initialisePaging(extras);
            }
        }

        // Adding a listener for when the page changes
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            // Sets the correct image when the page changes, used incase the page gets changed by gestures
            public void onPageSelected(int pagePosition) {
                // Check if this is the page you want.
                toggleAB = findViewById(R.id.toggle_ab);
                if (pagePosition == 1)
                    toggleAB.setChecked(true);
                else
                    toggleAB.setChecked(false);
            }
        });

    }

    /**
     * Initialise the fragments to be paged
     */
    private void initialisePaging(Bundle extras) {

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, TabGaugeFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, TabChartFragment.class.getName()));

        for(int i = 0; i < fragments.size(); i++){
            fragments.get(i).setArguments(extras);
        }

        this.mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        pager = (ViewPager)super.findViewById(R.id.viewpager);
        pager.setAdapter(this.mPagerAdapter);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Switch button that changes between gauge and graph view
        toggleAB = menu.findItem(R.id.mytoggle).getActionView().findViewById(R.id.toggle_ab);
        toggleAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    // Switches to graph view
                    // pagePosition is redundant but is used for pageListener
                    pagePosition = 1;
                    pager.setCurrentItem(pagePosition,true);

                }
                else
                {
                    // Switches to gauge view
                    // pagePosition is redundant but is used for pageListener
                    pagePosition = 0;
                    pager.setCurrentItem(pagePosition,true);
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

}


