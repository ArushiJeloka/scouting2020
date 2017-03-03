package com.stuin.irs_scout.Views;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.stuin.irs_scout.Data.Measure;
import com.stuin.irs_scout.Data.Task;
import com.stuin.irs_scout.MainActivity;
import com.stuin.irs_scout.R;

/**
 * Created by Stuart on 2/11/2017.
 */
public class Count extends Label {
    private boolean drop;
    private boolean missing;
    private boolean large = false;
    private int miss = -1;
    private int max = 18;
    private GridLayout gridLayout;

    public Count(Context context, Task task, String position) {
        super(context, task, position);
        if(!task.miss.isEmpty()) miss = 1;

        if(MainActivity.position.contains("Fuel")) large = true;
        if(large) {
            max = 100;
            if(miss != -1) miss = 2;
        }
    }
 
    @Override
    void create(LinearLayout column) {
        gridLayout = new GridLayout(getContext());
        gridLayout.setColumnCount(2);
        column.addView(gridLayout);

        part(task.success);
        if(large) part("+10");

        if(miss != -1) {
            part(task.miss);
            if(large) part("+10");
        }

    }


    @Override
    protected TextView part(String name) {
        //Simple counter button
        Button button = new Button(getContext());
        button.setText(name);
        button.setTextSize(getResources().getDimension(R.dimen.text_norm));
        button.setOnClickListener(clickListener);
        button.setOnLongClickListener(longClickListener);
        button.setGravity(Gravity.CENTER);
        views.add(button);
        gridLayout.addView(button);
        return button;
    }

    @Override
    protected void update(Measure measure, boolean send) {
        super.update(measure, send);

        //Set button text
        views.get(0).setText(task.success + ": " + measure.success);
        if(miss != -1) views.get(miss).setText(task.miss + ": " + (measure.attempt - measure.success));
    }

    private View.OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            //Add to values
            if(!drop) {
                if(view == views.get(0)) {
                    if(measure.success < max) {
                        measure.success++;
                        measure.attempt++;
                    }
                } else if(miss != -1 && view == views.get(miss)) {
                    if(measure.attempt < max) measure.attempt++;
                } else if(large && view == views.get(1)) {
                    if(measure.success + 9 < max) {
                        measure.success += 10;
                        measure.attempt += 10;
                    }
                } else if(miss != -1 && large && view == views.get(3)) {
                    if(measure.attempt + 9 < max) measure.attempt += 10;
                }
            } else {
                countDownTimer.cancel();
                drop = false;
            }

            update(measure, true);
        }
    };

    private View.OnLongClickListener longClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            boolean start = true;

            if(view == views.get(0)) {
                missing = false;
                if(measure.success > 0) {
                    measure.success--;
                    measure.attempt--;
                }
            } else if(large && view == views.get(1)) {
                int old = measure.success;
                measure.success -= 10;
                if(measure.success < 0) measure.success = 0;
                measure.attempt -= (old - measure.success);
                start = false;
            } else if(miss != -1 && view == views.get(miss)) {
                missing = true;
                if(measure.attempt > 0) measure.attempt--;
            } else if(miss != -1 && large && view == views.get(3)) {
                measure.attempt -= 10;
                if(measure.attempt < 0) measure.attempt = 0;
                start = false;
            }

            drop = true;
            if(start) countDownTimer.start();

            update(measure, false);
            return false;
        }
    };

    private CountDownTimer countDownTimer = new CountDownTimer(400, 10) {
        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
            if(drop) {
                if(!missing) {
                    if(measure.success > 0) {
                        measure.success--;
                        measure.attempt--;
                    }
                } else if(measure.attempt > 0) measure.attempt--;
                countDownTimer.start();
            }
            update(measure,false);
        }
    };
}