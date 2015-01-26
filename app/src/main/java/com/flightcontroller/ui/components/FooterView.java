package com.flightcontroller.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.flightcontroller.R;
import com.flightcontroller.model.Drone;
import com.flightcontroller.model.DroneActions;
import com.flightcontroller.model.DroneEvent;
import com.flightcontroller.model.DroneImp;

/**
 * View for the 2 buttons at the botton of the screen. These buttons
 * are responsible for arm/disarm and takeoff/land
 *
 */
public class FooterView extends LinearLayout implements DroneEvent.DroneEventListener,
        LoadingButton.LoadingButtonListener {

    private LoadingButton armDisarmBtn_;
    private LoadingButton takeoffLandBtn_;

    public FooterView(Context context) {
        super(context);
        initComponents(context);
    }

    public FooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponents(context);
    }

    public FooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initComponents(context);
    }

    private void initComponents(Context context) {
        View.inflate(context, R.layout.app_footer, this);

        DroneImp.INSTANCE.addDroneEventListener(this);

        armDisarmBtn_ = (LoadingButton) findViewById(R.id.arm_disarm_btn);
        takeoffLandBtn_ = (LoadingButton) findViewById(R.id.takeoff_land_btn);
        armDisarmBtn_.addEventButtonListener(this);
        takeoffLandBtn_.addEventButtonListener(this);

        armDisarmBtn_.setText(DroneImp.INSTANCE.isArmed() ? "Disarm" : "Arm");
        takeoffLandBtn_.setText(DroneImp.INSTANCE.isInAir() ? "Land" : "Takeoff");
        takeoffLandBtn_.setEnabled(DroneImp.INSTANCE.isArmed());
    }

    @Override
    public void onDroneEvent(DroneEvent event, Drone drone) {
        System.out.println("EVENT POSTED");
        if (event == DroneEvent.ARMED || event == DroneEvent.DISARMED ||
                event == DroneEvent.ARM_TIMEOUT) {
            armDisarmBtn_.setText(DroneImp.INSTANCE.isArmed() ? "Disarm" : "Arm");
            armDisarmBtn_.stopWaiting();

            takeoffLandBtn_.setEnabled(DroneImp.INSTANCE.isArmed());
        }
    }

    @Override
    public void onPressed(LoadingButton loadingButton) {
        if (loadingButton == armDisarmBtn_) {
            if (!DroneImp.INSTANCE.isArmed())
                DroneActions.arm(DroneImp.INSTANCE);
            else
                DroneActions.disarm(DroneImp.INSTANCE);
        }
    }
}
