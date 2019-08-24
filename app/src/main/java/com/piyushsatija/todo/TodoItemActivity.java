package com.piyushsatija.todo;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.piyushsatija.todo.utils.SharedPrefUtils;

import java.util.Calendar;

public class TodoItemActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView timeText;
    private FloatingActionButton saveTaskFAB;
    private EditText taskNameEditText;
    private String taskTime;
    private FirebaseFirestore db;
    private static final String TAG = "todo-item-activity";
    private static SharedPrefUtils sharedPrefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_item);
        timeText = findViewById(R.id.time_text);
        saveTaskFAB = findViewById(R.id.fab_save_task);
        saveTaskFAB.setOnClickListener(this);
        taskNameEditText = findViewById(R.id.add_task_input_text);
        findViewById(R.id.add_alarm).setOnClickListener(this);
        setupToolbar();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
    }

    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add a Task");
        }
    }

    private void showTimePickerDialog() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(TodoItemActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeText.setText(selectedHour + ":" + selectedMinute);
                taskTime = selectedHour + ":" + selectedMinute;
            }
        }, hour, minute, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private void saveToDatabase() {
        TodoItem item = new TodoItem(taskNameEditText.getText().toString(), taskTime);

        db.collection("users").document(sharedPrefUtils.getUserEmail()).collection("tasks")
                .document(Long.toString(System.currentTimeMillis()))
                .set(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_alarm:
                showTimePickerDialog();
                break;
            case R.id.fab_save_task:
                saveToDatabase();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
        }
    }
}
