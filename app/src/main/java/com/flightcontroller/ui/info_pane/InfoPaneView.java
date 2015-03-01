package com.flightcontroller.ui.info_pane;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.flightcontroller.R;
import com.flightcontroller.model.DroneImp;
import com.flightcontroller.model.attributes.core.Battery;
import com.flightcontroller.model.attributes.core.Orientation;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nicholas on 1/25/15.
 */
public class InfoPaneView extends RelativeLayout {

    private Timer updateTimer_;
    private InfoDataView battery_;
    private InfoDataView altitude_;

    public InfoPaneView(Context context) {
        super(context);
        initComponents(context);
    }

    public InfoPaneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponents(context);
    }

    public InfoPaneView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initComponents(context);
    }

    private void initComponents(Context context) {
        inflate(context, R.layout.info_data_pane, this);
        battery_ = (InfoDataView) findViewById(R.id.data_view1);
        altitude_ = (InfoDataView) findViewById(R.id.data_view2);
        updateTimer_ = new Timer();
        updateTimer_.scheduleAtFixedRate(updater_, new Date(), 100);
    }

    private TimerTask updater_ = new TimerTask() {
        @Override
        public void run() {
            post(new Runnable() {
                     @Override
                     public void run() {
                         Battery bat = (Battery) DroneImp.INSTANCE.getDroneAttribute("Battery");
                         Orientation or = (Orientation) DroneImp.INSTANCE.getDroneAttribute("Orientation");
                         battery_.setText("Battery", bat.getVoltage() + "V");
                         altitude_.setText("Altitude", or.getAltitude() + "m");
                     }
                 });
        }
    };

}
