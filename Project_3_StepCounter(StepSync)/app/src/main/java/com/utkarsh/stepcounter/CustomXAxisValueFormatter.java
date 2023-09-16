package com.utkarsh.stepcounter;

import android.annotation.SuppressLint;
import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.List;

public class CustomXAxisValueFormatter extends ValueFormatter {
    private final List<String> labels;

    public CustomXAxisValueFormatter(List<String> labels) {
        this.labels = labels;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int index = Math.min(Math.max((int) value, 0), labels.size() - 1);

        // Log the index and label for debugging
        Log.d("XAxisDebug", "Index: " + index + ", Label: " + labels.get(index));

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long timestamp = Long.parseLong(labels.get(index));
            return sdf.format(timestamp);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return "";
    }

}