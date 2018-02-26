package com.pedigreetechnologies.diagnosticview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;
import java.util.Vector;


public class ViewPagerFragmentActivity extends AppCompatActivity {
    private PagerAdapter mPagerAdapter;

    ViewPager pager;

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
        final Switch switchAB = menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchAB);
        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Switches to graph view
                    pager.setCurrentItem(1,true);

                }
                else
                {
                    // Switches to gauge view
                    pager.setCurrentItem(0,true);
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