package com.flightcontroller.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flightcontroller.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A little animated button
 */
public class LoadingButton extends RelativeLayout {

    private CopyOnWriteArrayList<LoadingButtonListener> listeners_;

    private boolean enabled_;
    private boolean waiting_;

	private TextView textView_;
	private ProgressWheel progress_;

    public LoadingButton(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    initComponents(context);
	}

	public LoadingButton(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    initComponents(context);
	}
	
	public LoadingButton(Context context) {
		super(context);
		initComponents(context);
	}
	
	private void initComponents(Context context) {
        inflate(context, R.layout.loading_button, this);

        enabled_ = true;
        waiting_ = false;
        listeners_ = new CopyOnWriteArrayList<>();

		textView_ = (TextView) findViewById(R.id.event_btn_text);
        progress_ = (ProgressWheel) findViewById(R.id.event_btn_progress);
        progress_.setBarColor(Color.parseColor("#FFFFFF"));
        progress_.setBarWidth(5);
        setBackground(getResources().getDrawable(R.drawable.loading_button_background));
	}

    public void addEventButtonListener(LoadingButtonListener listener) {
        listeners_.add(listener);
    }

    public void removeEventButtonListener(LoadingButtonListener listener) {
        listeners_.remove(listener);
    }

    public void setText(final String text) {
        textView_.post(new Runnable() {
            @Override
            public void run() {
                textView_.setText(text);
            }
        });
    }

    public void stopWaiting() {
        waiting_ = false;
        this.post(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadein = new AlphaAnimation(0.0f, 1.0f);
                fadein.setDuration(500);
                fadein.setFillAfter(true);
                AlphaAnimation fadeout = new AlphaAnimation(1.0f, 0.0f);
                fadeout.setDuration(500);
                fadeout.setFillAfter(true);

                progress_.setVisibility(INVISIBLE);
                progress_.stopSpinning();
                progress_.startAnimation(fadeout);

                textView_.setVisibility(VISIBLE);
                textView_.startAnimation(fadein);

                getBackground().clearColorFilter();
            }
        });
    }

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
	     super.dispatchTouchEvent(event);
         if (waiting_ || !enabled_)
            return true;

	     switch (event.getAction()) {
	         case MotionEvent.ACTION_DOWN: {
	         	 getBackground().setColorFilter(new LightingColorFilter(0xff888888, 0x000000));
	             invalidate();
	             break;
	         }
	         case MotionEvent.ACTION_UP: {
	             beginWaiting();
                 for (LoadingButtonListener listener : listeners_)
                    listener.onPressed(this);

	             invalidate();
	             break;
	         }
	         case MotionEvent.ACTION_CANCEL: {
	        	 getBackground().clearColorFilter();
	             invalidate();
	             break;
	         }
	     }
	     return true;
	 }
		
	private void beginWaiting() {
        waiting_ = true;

		AlphaAnimation fadein = new AlphaAnimation(0.0f, 1.0f);
        fadein.setDuration(500);
        fadein.setFillAfter(true);

        progress_.startAnimation(fadein);
        progress_.spin();

        AlphaAnimation fadeout = new AlphaAnimation(1.0f, 0.0f);
        fadeout.setDuration(500);
        fadeout.setFillAfter(true);
        textView_.startAnimation(fadeout);
	}

    public interface LoadingButtonListener {
        void onPressed(LoadingButton loadingButton);
    }

    public void setEnabled(boolean enabled) {
        enabled_ = enabled;
        if (enabled_) {
            getBackground().setAlpha(255);
            textView_.setTextColor(Color.argb(255, 255, 0, 0));
        } else {
            getBackground().setAlpha(125);
            textView_.setTextColor(Color.argb(125, 255, 255, 255));
        }

    }

}
