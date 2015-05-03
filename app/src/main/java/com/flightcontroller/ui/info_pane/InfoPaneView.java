package com.flightcontroller.ui.info_pane;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.flightcontroller.MainActivity;
import com.flightcontroller.R;

import java.util.ArrayList;

/**
 * A pane view on the right side of the screen, which holds
 * information collected from the copter. These panes can be closed
 * and opened with new viewing options.
 */
public class InfoPaneView extends RelativeLayout {

    private Button addButton_;
    private ViewGroup parentView_;
    private InfoDataView dataView1_;
    private InfoDataView dataView2_;
    private InfoDataView dataView3_;
    private InfoDataView dataView4_;

    private ArrayList<InfoDataView> activeViews_;
    private ArrayList<InfoDataView> inactiveViews_;

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
        addButton_ = (Button) findViewById(R.id.data_add_view);
        addButton_.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                        MainActivity.getMainContext());
                builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle("Select Attribute");
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        MainActivity.getMainContext(),
                        android.R.layout.select_dialog_singlechoice);
                arrayAdapter.add("Battery Voltage");
                arrayAdapter.add("Battery Current");
                arrayAdapter.add("Altitude");
                arrayAdapter.add("GPS");

                builderSingle.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = arrayAdapter.getItem(which);
                                addObject(strName);

                                dialog.dismiss();
                            }
                        });
                builderSingle.show();

            }
        });

        dataView1_.setPane(this);
        dataView2_.setPane(this);
        dataView3_.setPane(this);
        dataView4_.setPane(this);
        parentView_ = (ViewGroup) dataView1_.getParent();

        dataView1_.setContentType(InfoDataView.ALTITUDE);
        dataView2_.setContentType(InfoDataView.BATTERY_CURRENT);
        dataView3_.setContentType(InfoDataView.BATTERY_VOLTAGE);
        dataView4_.setContentType(InfoDataView.GPS);
        activeViews_ = new ArrayList<>();
        activeViews_.add(dataView1_);
        activeViews_.add(dataView2_);
        activeViews_.add(dataView3_);
        activeViews_.add(dataView4_);
        inactiveViews_ = new ArrayList<>();
        addButton_.setVisibility(View.INVISIBLE);
    }

    public void addObject(String type) {
        InfoDataView v = inactiveViews_.remove(inactiveViews_.size() - 1);
        if (type.equals("Battery Voltage"))
            v.setContentType(InfoDataView.BATTERY_VOLTAGE);
        else if (type.equals("Battery Current"))
            v.setContentType(InfoDataView.BATTERY_CURRENT);
        else if (type.equals("Altitude"))
            v.setContentType(InfoDataView.ALTITUDE);
        else if (type.equals("GPS"))
            v.setContentType(InfoDataView.GPS);

        activeViews_.add(v);
        parentView_.addView(v);
        if (inactiveViews_.isEmpty())
            addButton_.setVisibility(View.INVISIBLE);
    }

    public void removeChild(InfoDataView view) {
        for (int i = activeViews_.indexOf(view);i < activeViews_.size() - 1;i ++)
            activeViews_.get(i).setContentType(activeViews_.get(i + 1).getContentType());

        InfoDataView v = activeViews_.remove(activeViews_.size() - 1);
        ((ViewGroup) v.getParent()).removeView(v);
        inactiveViews_.add(v);
        addButton_.setVisibility(View.VISIBLE);
    }

}
