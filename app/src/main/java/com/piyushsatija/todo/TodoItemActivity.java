package com.piyushsatija.todo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.piyushsatija.todo.utils.SharedPrefUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    private RecyclerView commentRecyclerView;
    private FirestoreRecyclerAdapter<Comment, CommentViewHolder> adapter;
    private ImageButton addCommentBtn;

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
        addCommentBtn = findViewById(R.id.add_comment);
        addCommentBtn.setOnClickListener(this);
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
        if (todoItem != null) setupCommentsRecyclerView();
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
            if (taskTime != null) timeText.setText(getString(R.string.text_reminder, taskTime));
        }
    }

    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(requestCode == TodoListActivity.REQUEST_ADD_TASK ? "Add a Task" : "Update Task");
        }
    }

    private void setupCommentsRecyclerView() {
        commentRecyclerView = findViewById(R.id.comments_recyclerview);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        db.collection("users").document(sharedPrefUtils.getUserEmail())
                .collection("tasks")
                .whereEqualTo("date", todoItem.date).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Query query = db.collection("users").document(sharedPrefUtils.getUserEmail())
                                .collection("tasks")
                                .document(document.getId())
                                .collection("comments")
                                .orderBy("date", Query.Direction.DESCENDING);
                        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                                .setQuery(query, Comment.class)
                                .build();
                        adapter = new FirestoreRecyclerAdapter<Comment, CommentViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull CommentViewHolder commentViewHolder, int position, @NonNull final Comment comment) {
                                commentViewHolder.setComment(comment.getComment());
                                Log.d("comment", comment.getComment());
                            }

                            @NonNull
                            @Override
                            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
                                return new CommentViewHolder(view);
                            }
                        };
                        commentRecyclerView.setAdapter(adapter);
                        adapter.startListening();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();
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
                String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                timeText.setText(getString(R.string.text_reminder, formattedTime));
                taskTime = formattedTime;
                setReminder(selectedHour, selectedMinute);
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
        final CollectionReference collectionReference = db.collection("users")
                .document(sharedPrefUtils.getUserEmail()).collection("tasks");
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

    private void addComment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Comment");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String commentText = input.getText().toString();
                if (!commentText.isEmpty()) {
                    final Comment comment = new Comment(commentText);
                    final CollectionReference collectionReference = db.collection("users")
                            .document(sharedPrefUtils.getUserEmail()).collection("tasks");
                    collectionReference.whereEqualTo("date", todoItem.date).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    collectionReference.document(document.getId())
                                            .collection("comments")
                                            .add(comment);
                                }
                            }
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void setReminder(int hour, int minute) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("title", taskNameEditText.getText().toString());
        Random r = new Random();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, r.nextInt(100000), alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 1);
        // if notification time is before selected time, send notification the next day
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        if (manager != null) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
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
            case R.id.add_comment:
                if (requestCode == TodoListActivity.REQUEST_ADD_TASK) {
                    Toast.makeText(this, getString(R.string.toast_comment), Toast.LENGTH_SHORT).show();
                } else if (requestCode == TodoListActivity.REQUEST_UPDATE_TASK) {
                    addComment();
                }
                break;
        }
    }
}

class CommentViewHolder extends RecyclerView.ViewHolder {
    private View view;

    CommentViewHolder(View itemView) {
        super(itemView);
        view = itemView;
    }

    void setComment(String comment) {
        TextView textView = view.findViewById(R.id.comment_textview);
        textView.setText(comment);
    }
}