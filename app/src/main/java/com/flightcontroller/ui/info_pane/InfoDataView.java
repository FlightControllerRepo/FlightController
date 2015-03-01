package com.flightcontroller.ui.info_pane;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flightcontroller.R;

/**
 * Created by Nicholas on 1/25/15.
 */
public class InfoDataView extends RelativeLayout {

    private TextView title_;
    private TextView bodyText_;

    private ImageButton closeButton_;

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
                ((ViewGroup) getParent()).removeView(thisref);
            }
        });

        title_ = (TextView) findViewById(R.id.title_header_idv);
        bodyText_ = (TextView) findViewById(R.id.body_idv);
    }

    public void setText(String title, String text) {
        bodyText_.setText(text);
        title_.setText(title);
    }


}
