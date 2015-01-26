package com.flightcontroller.ui.info_pane;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.flightcontroller.R;

/**
 * Created by Nicholas on 1/25/15.
 */
public class InfoPaneView extends RelativeLayout {

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
    }
}
