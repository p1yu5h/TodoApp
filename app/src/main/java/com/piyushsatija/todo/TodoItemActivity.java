package com.piyushsatija.todo;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.piyushsatija.todo.utils.SharedPrefUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TodoItemActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView timeText;
    private FloatingActionButton saveTaskFAB;
    private EditText taskNameEditText;
    private String taskTime;
    private FirebaseFirestore db;
    private static final String TAG = "todo-item-activity";
    private static SharedPrefUtils sharedPrefUtils;
    private Switch taskStatusSwitch;
    private boolean taskStatus;
    private int requestCode;
    private TodoItem todoItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_item);
        timeText = findViewById(R.id.time_text);
        saveTaskFAB = findViewById(R.id.fab_save_task);
        saveTaskFAB.setOnClickListener(this);
        taskNameEditText = findViewById(R.id.add_task_input_text);
        taskStatusSwitch = findViewById(R.id.switch_task_status);
        findViewById(R.id.add_alarm).setOnClickListener(this);
        setupToolbar();
        handleIntent();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        taskStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                taskStatus = isChecked;
            }
        });
    }

    private void handleIntent() {
        Intent intent = getIntent();
        requestCode = intent.getIntExtra("request_code", 0);
        if (requestCode == TodoListActivity.REQUEST_UPDATE_TASK) {
            todoItem = intent.getParcelableExtra("todo_item");
            taskNameEditText.setText(todoItem.getTaskName());
            taskStatus = todoItem.isCompleted();
            taskStatusSwitch.setChecked(taskStatus);
            taskTime = todoItem.getTaskTime();
            timeText.setText(taskTime);
        }
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

    private void addNewTaskToDatabase() {
        String taskName = taskNameEditText.getText().toString();
        if (!taskName.isEmpty()) {
            TodoItem item = new TodoItem(taskName, taskTime);
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
    }

    private void updateTask() {
        final CollectionReference collectionReference = db.collection("users").document(sharedPrefUtils.getUserEmail()).collection("tasks");
        collectionReference.whereEqualTo("date", todoItem.date).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("taskName", taskNameEditText.getText().toString());
                        map.put("taskTime", taskTime);
                        map.put("completed", taskStatus);
                        collectionReference.document(document.getId()).set(map, SetOptions.merge());
                    }
                }
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
                if (requestCode == TodoListActivity.REQUEST_ADD_TASK) {
                    addNewTaskToDatabase();
                } else if (requestCode == TodoListActivity.REQUEST_UPDATE_TASK) {
                    updateTask();
                }
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
        }
    }
}
