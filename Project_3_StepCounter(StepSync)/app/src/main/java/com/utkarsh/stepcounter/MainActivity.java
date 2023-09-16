package com.utkarsh.stepcounter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView stepCountTextView;
    private ImageView resetButton;
    private ImageView showGraphButton;
    private ImageView showDistanceButton;
    private double stepLengthCm = 0.0;
    private LineChart lineChart;
    private DBHelper dbHelper;
    private int stepCount = 0;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private String todayDate; // Add this variable
    private boolean isSensorAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepCountTextView = findViewById(R.id.stepCountTextView);
        resetButton = findViewById(R.id.resetButton);
        showGraphButton = findViewById(R.id.showGraphButton);
        lineChart = findViewById(R.id.lineChart);

        dbHelper = new DBHelper(this);
        initSensor();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.clearStepData();
                stepCount = 0;
                updateStepCount();
            }
        });

        showGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGraphPopup();
            }
        });

        showDistanceButton = findViewById(R.id.showDistanceButton);

        showDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDistanceInputDialog();
            }
        });

        loadSavedStepCount();
    }

    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (stepSensor != null) {
            isSensorAvailable = true;
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            isSensorAvailable = false;
            Toast.makeText(this, "Step detector sensor not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedStepCount() {
        // Load today's step count from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        stepCount = sharedPreferences.getInt(todayDate, 0);
        updateStepCount();
    }

    private void updateStepCount() {
        stepCountTextView.setText("Step Count: " + stepCount);
    }

    private void showGraphPopup() {
        ArrayList<BarDataModel> dataModels = dbHelper.getAllStepCounts();

        if (dataModels.isEmpty()) {
            Toast.makeText(this, "No data available for graph", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < dataModels.size(); i++) {
            BarDataModel dataModel = dataModels.get(i);
            entries.add(new Entry(i, dataModel.getTotalSteps()));
            labels.add(dataModel.getTime()); // Use time as labels on X-axis
        }

        LineDataSet dataSet = new LineDataSet(entries, "Step Count");
        dataSet.setColor(getResources().getColor(R.color.black));
        dataSet.setValueTextColor(getResources().getColor(R.color.white));
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);

        if (!labels.isEmpty()) {
            // Check if labels are not empty before setting the X-axis formatter
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new CustomXAxisValueFormatter(labels));
        }

        // Create a new AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Step Count Graph");

        // Inflate the custom dialog layout
        View customDialogView = getLayoutInflater().inflate(R.layout.custom_line_chart_layout, null);
        builder.setView(customDialogView); // Set the custom dialog layout as the content view

        // Set the LineChart in the custom dialog layout
        LineChart customLineChart = customDialogView.findViewById(R.id.customLineChart);
        customLineChart.setData(lineData);
        customLineChart.getDescription().setEnabled(false);
        customLineChart.animateY(2000);

        // Set a "Close" button and its action
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Dismiss the dialog when the "Close" button is clicked
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                // Increment the step count for each step detected
                stepCount++;
                updateStepCount();

                // Save the step count in the database with today's date and time
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                dbHelper.insertStepData(todayDate, currentTime, stepCount); // Use todayDate and currentTime here
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed for step counting
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        // Save the current step count in SharedPreferences
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(todayDate, stepCount);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the sensor listener to prevent battery drain
        if (isSensorAvailable) {
            sensorManager.unregisterListener(stepListener, stepSensor);
        }
    }

    private void showDistanceInputDialog() {
        // Inflate the distance input dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_distance_input, null);

        // Find views in the dialog layout
        EditText stepLengthEditText = dialogView.findViewById(R.id.stepLengthEditText);
        Button calculateDistanceButton = dialogView.findViewById(R.id.calculateDistanceButton);

        // Create the AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Step Length (cm)");
        builder.setView(dialogView);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Set a click listener for the "Calculate Distance" button
        calculateDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the step length entered by the user
                String stepLengthStr = stepLengthEditText.getText().toString().trim();
                if (!stepLengthStr.isEmpty()) {
                    try {
                        // Parse the step length as a double
                        stepLengthCm = Double.parseDouble(stepLengthStr);

                        // Calculate the distance based on step count and step length
                        double distanceMeters = (stepCount * stepLengthCm) / 100.0; // Convert cm to meters

                        // Display the distance in a popup
                        showDistanceResultPopup(distanceMeters);

                        // Dismiss the input dialog
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        // Handle invalid input
                        Toast.makeText(MainActivity.this, "Invalid step length", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Show the input dialog
        dialog.show();
    }

    private void showDistanceResultPopup(double distanceMeters) {
        // Create a new AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Distance Calculation Result");
        builder.setMessage("Distance: " + distanceMeters + " meters\n\nYou are doing Great! Keep Going.");

        // Set a "Close" button and its action
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Dismiss the dialog when the "Close" button is clicked
            }
        });

        // Create the AlertDialog
        AlertDialog resultDialog = builder.create();
        resultDialog.show();
    }
}