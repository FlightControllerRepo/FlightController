package com.flightcontroller.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.flightcontroller.speech.SpeechHandler;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nicholas on 1/24/15.
 */
public class SoundAnimationView extends View {

    private static final int NUMBER_OF_POINTS = 100;
    private static final int DRAW_UPDATE = 50;

    private boolean activated_;

    private float time_;
    private Timer updateTimer_;
    private TimerTask updateTimerTask_;

    private float[] lines_;

    private Paint paintObject_;

    private float maxVolume_;
    private SpeechHandler speechHandler_;

    public SoundAnimationView(Context context) {
        super(context);
        initDrawing();
    }

    public SoundAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDrawing();
    }

    private void initDrawing() {
        paintObject_ = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintObject_.setStrokeWidth(3.0f);

        lines_ = new float[4 * NUMBER_OF_POINTS];

        updateTimerTask_ = new TimerTask() {
            @Override
            public void run() {
                maxVolume_ = Math.max(maxVolume_, speechHandler_.getAudioVolume());
                float xscale = getWidth() / (float) NUMBER_OF_POINTS;
                float yscale = speechHandler_.getAudioVolume() /
                        maxVolume_ * (getHeight() / 2.0f) * 0.85f;
                for (int i = 0;i < NUMBER_OF_POINTS;i ++) {
                    lines_[i * 4] = i * xscale;
                    lines_[i * 4 + 1] = yscale * (float) Math.sin(i * xscale + time_) +
                            getHeight() / 2.0f;

                    lines_[i * 4 + 2] = (i + 1) * xscale;
                    lines_[i * 4 + 3] = yscale * (float) Math.sin((i + 1) * xscale + time_) +
                            getHeight() / 2.0f;
                }

                time_ += 0.2f;

                post(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                });
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (activated_)
            canvas.drawLines(lines_, paintObject_);
    }

    public void activate() {
        activated_ = true;

        updateTimer_ = new Timer();
        updateTimer_.scheduleAtFixedRate(updateTimerTask_, new Date(), DRAW_UPDATE);
    }

    public void deactivate() {
        activated_ = false;
        updateTimer_.cancel();
    }

    public void setSpeechHandler(SpeechHandler speechHandler) {
        speechHandler_ = speechHandler;
    }
}
