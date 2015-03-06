package com.flightcontroller.ui.info_pane;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.flightcontroller.R;

import java.util.ArrayList;

/**
 * Created by Nicholas on 1/25/15.
 */
public class InfoPaneView extends RelativeLayout {

    private InfoDataView dataView1_;
    private InfoDataView dataView2_;
    private InfoDataView dataView3_;
    private InfoDataView dataView4_;

    private ArrayList<InfoDataView> activeViews_;


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
        dataView1_ = (InfoDataView) findViewById(R.id.data_view1);
        dataView2_ = (InfoDataView) findViewById(R.id.data_view2);
        dataView3_ = (InfoDataView) findViewById(R.id.data_view3);
        dataView4_ = (InfoDataView) findViewById(R.id.data_view4);
        dataView1_.setPane(this);
        dataView2_.setPane(this);
        dataView3_.setPane(this);
        dataView4_.setPane(this);

        dataView1_.setContentType(InfoDataView.ALTITUDE);
        dataView2_.setContentType(InfoDataView.BATTERY_CURRENT);
        dataView3_.setContentType(InfoDataView.BATTERY_VOLTAGE);
        dataView4_.setContentType(InfoDataView.ALTITUDE);
        activeViews_ = new ArrayList<>();
        activeViews_.add(dataView1_);
        activeViews_.add(dataView2_);
        activeViews_.add(dataView3_);
        activeViews_.add(dataView4_);
    }

    public void removeChild(InfoDataView view) {
        for (int i = activeViews_.indexOf(view);i < activeViews_.size() - 1;i ++)
            activeViews_.get(i).setContentType(activeViews_.get(i + 1).getContentType());

        InfoDataView v = activeViews_.remove(activeViews_.size() - 1);
        ((ViewGroup) v.getParent()).removeView(v);
    }

}
