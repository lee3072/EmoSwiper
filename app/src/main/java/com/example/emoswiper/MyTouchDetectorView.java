package com.example.emoswiper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;

public class MyTouchDetectorView extends AppCompatImageView {
    private Trajectory currentTrajectory;
    private String currentEmotion;
    private String currentImage;
    private SwipeRefreshListener myListener;

    private boolean detectingSwipe;

    public MyTouchDetectorView(Context context) {
        super(context);
    }

    public MyTouchDetectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTouchDetectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startShowingImage(Trajectory a) {
        currentTrajectory = a;
        currentImage = a.image;
        currentEmotion = a.emotion;
        setImage(currentImage, currentEmotion);
    }

    public void setDetectingSwipe(boolean y, SwipeRefreshListener listener) {
        detectingSwipe = y;
        myListener = listener;
    }

    public void setImage(String jpg_name, String emotion) {
        String uri = "@drawable/" + jpg_name;  // where myresource (without the extension) is the file
        Log.d("setImage", uri);
        int imageResource = getResources().getIdentifier(uri, null, getContext().getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        setImageDrawable(res);
        currentEmotion = emotion;
        currentImage = jpg_name;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);
        if (detectingSwipe) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("TOUCH", "DOWN: " + event.getX() + ", " + event.getY() + ", " + event.getPressure());
                    currentTrajectory.trajectory.add(new Trajectory.Coordinate(event.getX(), event.getY(), event.getEventTime()-event.getDownTime()));
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d("TOUCH", "MOVE: " + event.getX() + ", " + event.getY() + ", " + event.getPressure());
                    currentTrajectory.trajectory.add(new Trajectory.Coordinate(event.getX(), event.getY(), event.getEventTime()-event.getDownTime()));
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("TOUCH", "UP: " + event.getX() + ", " + event.getY() + ", " + event.getPressure());
                    currentTrajectory.trajectory.add(new Trajectory.Coordinate(event.getX(), event.getY(), event.getEventTime()-event.getDownTime()));
                    currentTrajectory = null;
                    myListener.onSwipeRefresh();
                    setDetectingSwipe(false, null);
            }
        }
        return true;
    }
}

interface SwipeRefreshListener {
    void onSwipeRefresh();
}
