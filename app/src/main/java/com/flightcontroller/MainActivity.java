package com.flightcontroller;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.flightcontroller.model.Drone;
import com.flightcontroller.model.DroneEvent;
import com.flightcontroller.model.DroneImp;
import com.flightcontroller.speech.SpeechHandler;
import com.flightcontroller.ui.MiniMapFragment;
import com.flightcontroller.ui.components.FooterView;
import com.flightcontroller.ui.components.SoundAnimationView;
import com.flightcontroller.utils.LogManager;
import com.pnikosis.materialishprogress.ProgressWheel;


public class MainActivity extends ActionBarActivity implements DroneEvent.DroneEventListener {

    private static Activity mainContext_;

    private FooterView footerView_;

    private ActionBarDrawerToggle drawerToggle_;
    private DrawerLayout drawerLayout_;

    private ProgressWheel connecting_;
    private TextView connectionText_;

    private SoundAnimationView soundAnimationView_;
    private TextView connectDisconnectButton_;
    private ImageView speechButton_;

    private SpeechHandler speechHandler_;
    private boolean isSpeaking_;

    private MiniMapFragment mapFragment_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainContext_ = this;

        speechHandler_ = new SpeechHandler();
        isSpeaking_ = false;

        LogManager.INSTANCE.addEntry("OnCreate MainActivity", LogManager.LogSeverity.INFO);
        setContentView(R.layout.activity_main);

        DroneImp.INSTANCE.addDroneEventListener(this);

        /*
        mapFragment_ = MiniMapFragment.newInstance();
        */
        connecting_ = (ProgressWheel) findViewById(R.id.awaiting_connection);
        connectionText_ = (TextView) findViewById(R.id.connection_txb);

        footerView_ = (FooterView) findViewById(R.id.footer_view);
        footerView_.setVisibility(View.INVISIBLE);

        connecting_.setVisibility(View.INVISIBLE);
        connectionText_.setVisibility(View.INVISIBLE);

        setupActionBar();
        setupDrawer();

    }
    public static Activity getMainContext() {
        return mainContext_;
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = LayoutInflater.from(this);

        View customView = inflater.inflate(R.layout.main_actionbar, null);
        soundAnimationView_ = (SoundAnimationView) customView.findViewById(R.id.sound_animation_ab);
        soundAnimationView_.setSpeechHandler(speechHandler_);

        speechButton_ = (ImageView) customView.findViewById(R.id.speech_ab);
        speechButton_.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() ==  MotionEvent.ACTION_UP) {
                    isSpeaking_ = !isSpeaking_;
                    if (isSpeaking_) {
                        speechHandler_.startListening();
                        soundAnimationView_.activate();
                    } else {
                        speechHandler_.stopListening();
                        soundAnimationView_.deactivate();
                    }
                }
                return false;
            }
        });

        connectDisconnectButton_ = (TextView) customView.findViewById(R.id.connect_disconnect_ab);
        connectDisconnectButton_.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    /*
                    DroneImp.INSTANCE.postEvent(DroneEvent.RADIO_CONNECTED);
                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            DroneImp.INSTANCE.postEvent(DroneEvent.HEARTBEAT_FIRST);
                        }
                    }, 5000);
                    */
                    if (DroneImp.INSTANCE.isConnected())
                        DroneImp.INSTANCE.disconnectRadio();
                    else
                        DroneImp.INSTANCE.connectRadio();

                }
                return false;
            }
        });

        getSupportActionBar().setCustomView(customView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    private void setupDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout_ = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle_ = new ActionBarDrawerToggle(this, drawerLayout_,
                R.string.drawer_open, R.string.drawer_close){

            @Override
            public void onDrawerClosed(View drawerView){

            }
        };

        drawerLayout_.setDrawerListener(drawerToggle_);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle_.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle_.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (drawerToggle_ != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            drawerToggle_.syncState();
        }
    }

    @Override
    public void onDroneEvent(DroneEvent event, Drone drone) {
        if (event == DroneEvent.RADIO_CONNECTED) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fadeConnectionStatus(false);
                    connectDisconnectButton_.setText("Disconnect");
                }
            });

        } else if (event == DroneEvent.RADIO_DISCONNECTED) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fadeConnectionStatus(true);
                    connectDisconnectButton_.setText("Connect");
                }
            });
        } else if (event == DroneEvent.HEARTBEAT_FIRST) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fadeConnectionStatus(true);


                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.animator.fade_in,
                            android.R.animator.fade_out);

                    fragmentTransaction.add(R.id.map_container, mapFragment_, "map");
                    fragmentTransaction.commit();


                    footerView_.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void fadeConnectionStatus(final boolean fadeOut) {
        connecting_.setVisibility(fadeOut ? View.INVISIBLE : View.VISIBLE);
        connectionText_.setVisibility(fadeOut ? View.INVISIBLE : View.VISIBLE);

        AlphaAnimation fade = new AlphaAnimation(fadeOut ? 1.0f : 0.0f,
                fadeOut ? 0.0f : 1.0f);
        fade.setDuration(1000);
        fade.setFillAfter(true);

        if (!fadeOut)
            connecting_.spin();
        else
            connecting_.stopSpinning();

        connecting_.setAnimation(fade);
        connectionText_.setAnimation(fade);
    }
}
