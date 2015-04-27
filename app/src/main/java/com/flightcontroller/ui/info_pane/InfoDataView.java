package com.flightcontroller.ui.info_pane;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flightcontroller.R;
import com.flightcontroller.model.DroneImp;
import com.flightcontroller.model.attributes.core.Battery;
import com.flightcontroller.model.attributes.core.GPSPosition;
import com.flightcontroller.model.attributes.core.Orientation;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nicholas on 1/25/15.
 */
public class InfoDataView extends RelativeLayout {

    public static final int BATTERY_VOLTAGE = 0;
    public static final int BATTERY_CURRENT = 1;
    public static final int ALTITUDE = 2;
    public static final int GPS = 3;

    private int contentType_;

    private TextView title_;
    private TextView bodyText_;

    private ImageButton closeButton_;

    private InfoPaneView mainPain_;

    private Timer updateTimer_;

    public InfoDataView(Context context) {
        super(context);
        initComponents(context);
    }

    public InfoDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponents(context);
    }

    public InfoDataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initComponents(context);
    }

    private void initComponents(Context context) {
        inflate(context, R.layout.info_data, this);

        closeButton_ = (ImageButton) findViewById(R.id.close_button_idv);

        final InfoDataView thisref = this;
        closeButton_.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mainPain_.removeChild(thisref);
            }
        });

        title_ = (TextView) findViewById(R.id.title_header_idv);
        bodyText_ = (TextView) findViewById(R.id.body_idv);
    }

    private TimerTask getUpdaterTask() {
        final InfoDataView thisref = this;
        return new TimerTask() {
            @Override
            public void run() {
                thisref.post(new Runnable() {
                    @Override
                    public void run() {
                        Battery bat = (Battery) DroneImp.INSTANCE.getDroneAttribute("Battery");
                        Orientation ori = (Orientation) DroneImp.INSTANCE.getDroneAttribute("Orientation");
                        GPSPosition gps = (GPSPosition) DroneImp.INSTANCE.getDroneAttribute("GPSPosition");

                        switch (thisref.contentType_) {
                            case BATTERY_VOLTAGE:
                                thisref.setText("Battery Voltage", bat.getVoltage() + "V");
                                break;
                            case BATTERY_CURRENT:
                                thisref.setText("Battery Current", bat.getCurrent() + "A");
                                break;
                            case ALTITUDE:
                                thisref.setText("Altitude", ori.getAltitude() + "m");
                                break;
                            case GPS:
                                thisref.setText("GPS", gps.getLatitude() + " " + gps.getLongitude());
                                break;
                        }
                    }
                });
            }
        };
    }

    private void setText(String title, String text) {
        bodyText_.setText(text);
        title_.setText(title);
    }

    public void setPane(InfoPaneView pane) {
        mainPain_ = pane;
    }

    public void setContentType(int content) {
        contentType_ = content;
        if (updateTimer_ != null)
            updateTimer_.cancel();

        updateTimer_ = new Timer();
        updateTimer_.scheduleAtFixedRate(getUpdaterTask(), new Date(), 300);
    }

    public int getContentType() {
        return contentType_;
    }

}
