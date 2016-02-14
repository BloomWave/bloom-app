package com.remavito.resonance;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;

import com.remavito.resonance.player.Player;


public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public Player player;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    static final int PLAYER_STATE = 42;
    static final int PLAYER_STATE_PAUSED = 0;
    static final int PLAYER_STATE_RUNNING = 1;
    static final String TAG = "MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        player = new Player(this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.playBtn);
        fab.setTag(R.string.player_state, PLAYER_STATE_PAUSED);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FloatingActionButton btn = (FloatingActionButton) view;
                int state = (int) btn.getTag(R.string.player_state);
                if(state == PLAYER_STATE_PAUSED) {
                    btn.setImageResource(android.R.drawable.ic_media_pause);
                    btn.setTag(R.string.player_state, PLAYER_STATE_RUNNING);
                    Log.d("Floating Button", "Pressed play button");
                    if(player.isRunning())
                        player.play_audio();
                    else
                        player.start();
                }
                else if(state == PLAYER_STATE_RUNNING){
                    btn.setImageResource(android.R.drawable.ic_media_play);
                    btn.setTag(R.string.player_state, PLAYER_STATE_PAUSED);
                    Log.d("Floating Button", "Pressed paused button");
                    player.pause_audio();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public ListAdapter getSongAdapter() {
        return player.getSongAdapter();
    }
}
