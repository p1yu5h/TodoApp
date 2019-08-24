package com.piyushsatija.todo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.piyushsatija.todo.utils.SharedPrefUtils;

public class TodoListActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView todoListRecyclerView;
    private ActionBar actionBar;
    private FloatingActionButton createTaskFAB;
    public static final int REQUEST_ADD_TASK = 100;
    public static final int REQUEST_UPDATE_TASK = 200;
    private static SharedPrefUtils sharedPrefUtils;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirestoreRecyclerAdapter<TodoItem, TaskViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);
        createTaskFAB = findViewById(R.id.fab_create_todo);
        createTaskFAB.setOnClickListener(this);
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        setupToolbar();
        setupViews();
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_todo_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.log_out:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sharedPrefUtils.clearAllPrefs();
                        startActivity(new Intent(TodoListActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

    private void setupToolbar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle("Welcome, " + sharedPrefUtils.getUserName());
    }

    private void setupViews() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("users").document(sharedPrefUtils.getUserEmail()).collection("tasks")
                .orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<TodoItem> options = new FirestoreRecyclerOptions.Builder<TodoItem>()
                .setQuery(query, TodoItem.class)
                .build();
        todoListRecyclerView = findViewById(R.id.todo_list_recyclerview);
        todoListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FirestoreRecyclerAdapter<TodoItem, TaskViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull TaskViewHolder taskViewHolder, int position, @NonNull final TodoItem todoItem) {
                taskViewHolder.setTaskName(todoItem.getTaskName());
                taskViewHolder.setTaskTime(todoItem.getTaskTime());
                taskViewHolder.markCompleted(todoItem.isCompleted());
                taskViewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(TodoListActivity.this, TodoItemActivity.class);
                        intent.putExtra("request_code", REQUEST_UPDATE_TASK);
                        intent.putExtra("todo_item", todoItem);
                        startActivityForResult(intent, REQUEST_UPDATE_TASK);
                    }
                });
            }

            @NonNull
            @Override
            public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_list_item, parent, false);
                return new TaskViewHolder(view);
            }
        };
        todoListRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_create_todo) {
            Intent intent = new Intent(TodoListActivity.this, TodoItemActivity.class);
            intent.putExtra("request_code", REQUEST_ADD_TASK);
            startActivityForResult(intent, REQUEST_ADD_TASK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_TASK) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_UPDATE_TASK) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Task Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

class TaskViewHolder extends RecyclerView.ViewHolder {
    private View view;

    TaskViewHolder(View itemView) {
        super(itemView);
        view = itemView;
    }

    View getView() {
        return view;
    }

    void setTaskName(String taskName) {
        TextView textView = view.findViewById(R.id.task_name);
        textView.setText(taskName);
    }

    void setTaskTime(String taskTime) {
        TextView textView = view.findViewById(R.id.task_time);
        textView.setText(taskTime);
    }

    void markCompleted(boolean complete) {
//        view.findViewById(R.id.card_task).setBackgroundColor(complete ? Color.GREEN: Color.WHITE);
    }
}