package com.stuin.irs_scout.Scouter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.stuin.cleanvisuals.Slide.*;
import com.stuin.irs_scout.Data.InputData;
import com.stuin.irs_scout.Data.Match;
import com.stuin.irs_scout.Data.Measure;
import com.stuin.irs_scout.MainActivity;
import com.stuin.irs_scout.Scouter.Inputs.Input;
import com.stuin.irs_scout.Scouter.Inputs.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Page extends LinearLayout {
    public String name;
    public SliderSync sliderSync;

    private LinearLayout column;
    private List<Input> inputs = new ArrayList<>();

    Page(Context context, String name) {
        //Create phase
        super(context);
        this.name = name;

        //Set formatting
        setGravity(Gravity.CENTER);
        newCol();
    }

    public void link(Page page, Endings endings) {
        //Sets up animation
        sliderSync = new SliderSync(this, page);
        sliderSync.setup(true, 2000, -2000, 500);
        sliderSync.endings = endings;
    }

    public void add(Input objectView) {
        //Add object to column
        inputs.add(objectView);
        objectView.create(column);
    }

    public void setMeasures(Map<String, Measure> measures, Match match) {
        //Set data values
        for(Input i : inputs) {
            InputData id = i.getData();

            //Set title to team in alliance scout
            if(MainActivity.alliance && i.getClass() == Label.class && id.task.success.contains("Team"))
                ((Label) i).setText(match.getTeam(id.position));

            //Send data to input
            String key = id.task.task + ':' + match.getTeam(id.position);
            if(measures.get(key) != null)
                i.update(measures.get(key), false);
            else //Make new measure
                i.update(new Measure(id.task, match, id.position, name), false);
        }
    }

    public void newCol() {
        //Make new column
        divider();
        column = new LinearLayout(getContext());
        column.setOrientation(VERTICAL);
        column.setGravity(Gravity.CENTER);
        addView(column);
        divider();
    }

    private void divider() {
        //Add decoration line
        View view = new TextView(getContext());
        view.setMinimumHeight(600);
        view.setMinimumWidth(2);
        view.setBackgroundColor(Color.LTGRAY);
        addView(view);
    }
}
