package com.example.eecs4443lab3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;


import org.json.JSONArray;
import org.json.JSONObject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Intent result keys
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DEADLINE = "extra_deadline";
    public static final String EXTRA_NOTES = "extra_notes";
    public static final String EXTRA_STATUS = "extra_status";

    // Storage mode
    private enum Mode { SHARED_PREFS, SQLITE }
    private Mode currentMode = Mode.SQLITE; // default (matches your switch text)

    // Views (from activity_main.xml)  :contentReference[oaicite:4]{index=4}
    private RecyclerView recyclerView;
    private MaterialSwitch switchMode;
    private FloatingActionButton fab;

    // Data
    private final List<Task> tasks = new ArrayList<>();
    private TaskAdapter adapter;

    // Persistence
    private static final String PREFS_NAME = "tasks_prefs";
    private static final String PREFS_KEY = "tasks_json";
    private TaskDbHelper dbHelper;

    // Add/Edit launcher
    private final ActivityResultLauncher<Intent> addEditLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Task t = new Task(
                            data.getStringExtra(EXTRA_TITLE),
                            data.getStringExtra(EXTRA_DEADLINE),
                            data.getStringExtra(EXTRA_NOTES),
                            data.getStringExtra(EXTRA_STATUS)
                    );
                    tasks.add(t);
                    adapter.notifyItemInserted(tasks.size() - 1);
                    persist();
                    Snackbar.make(recyclerView, "Task saved", Snackbar.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_main.xml contains: toolbar, switchMode, recycler, fabAdd  :contentReference[oaicite:5]{index=5}
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler);
        switchMode = findViewById(R.id.switchMode);
        fab = findViewById(R.id.fabAdd);

        adapter = new TaskAdapter(tasks, new TaskAdapter.OnTaskInteraction() {
            @Override
            public void onClick(int position) {
                Task t = tasks.get(position);
                // Open detail screen  (activity_task_detail.xml)  :contentReference[oaicite:6]{index=6}
                Intent i = new Intent(MainActivity.this, TaskDetailActivity.class);
                i.putExtra(EXTRA_TITLE, t.title);
                i.putExtra(EXTRA_DEADLINE, t.deadline);
                i.putExtra(EXTRA_NOTES, t.notes);
                i.putExtra(EXTRA_STATUS, t.status);
                startActivity(i);
            }

            @Override
            public void onLongPress(int position) {
                showTaskOptions(position); // ← NEW
            }

            private void showTaskOptions(int position) {
                CharSequence[] items = {"Edit", "Delete", "Cancel"};
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(tasks.get(position).title)
                        .setItems(items, (dialog, which) -> {
                            if (which == 0) {           // Edit
                                editTaskDialog(position);
                            } else if (which == 1) {    // Delete
                                confirmDelete(position);
                            } else {                    // Cancel
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
            private void editTaskDialog(int position) {
                Task t = tasks.get(position);

                // Reuse the add/edit layout as a dialog
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_add_edit_task, null, false);

                TextView txtBanner = view.findViewById(R.id.txtBanner);
                TextInputEditText inputTitle = view.findViewById(R.id.inputTitle);
                TextInputEditText inputDeadline = view.findViewById(R.id.inputDeadline);
                TextInputEditText inputNotes = view.findViewById(R.id.inputNotes);
                MaterialButton btnSave = view.findViewById(R.id.btnSave);

                // Prefill + tweak labels
                txtBanner.setText("Edit Task");
                btnSave.setText("Update Task");
                inputTitle.setText(t.title);
                inputDeadline.setText(t.deadline);
                inputNotes.setText(t.notes);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(view)
                        .create();

                btnSave.setOnClickListener(v -> {
                    String newTitle = inputTitle.getText() == null ? "" : inputTitle.getText().toString().trim();
                    String newDeadline = inputDeadline.getText() == null ? "" : inputDeadline.getText().toString().trim();
                    String newNotes = inputNotes.getText() == null ? "" : inputNotes.getText().toString().trim();

                    if (newTitle.isEmpty()) {
                        inputTitle.setError("Title is required");
                        return;
                    }

                    // Update in-memory model
                    t.title = newTitle;
                    t.deadline = newDeadline;
                    t.notes = newNotes;
                    // status stays as-is (t.status)

                    // Refresh UI + persist to current storage (Prefs/SQLite)
                    adapter.notifyItemChanged(position);
                    persist();

                    Snackbar.make(recyclerView, "Task updated", Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

                dialog.show();
            }



        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        dbHelper = new TaskDbHelper(this);

        // Switch toggles runtime storage (SharedPreferences <-> SQLite) per lab spec  :contentReference[oaicite:7]{index=7}
        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentMode = isChecked ? Mode.SHARED_PREFS : Mode.SQLITE;
            Snackbar.make(buttonView,
                    currentMode == Mode.SHARED_PREFS ? "Using SharedPreferences" : "Using SQLite",
                    Snackbar.LENGTH_SHORT).show();
            reloadFromStorage();
        });

        fab.setOnClickListener(v -> {
            // Open add/edit form  (activity_add_edit_task.xml)  :contentReference[oaicite:8]{index=8}
            Intent i = new Intent(this, AddEditTaskActivity.class);
            addEditLauncher.launch(i);
        });

        // Default to SQLite unless user toggles
        currentMode = Mode.SQLITE;
        switchMode.setChecked(false);
        reloadFromStorage();
    }

    private void confirmDelete(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete task?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    tasks.remove(position);
                    adapter.notifyItemRemoved(position);
                    persist();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reloadFromStorage() {
        tasks.clear();
        if (currentMode == Mode.SHARED_PREFS) {
            loadFromPrefs();
        } else {
            loadFromDb();
        }
        adapter.notifyDataSetChanged();
    }

    private void persist() {
        if (currentMode == Mode.SHARED_PREFS) {
            saveToPrefs();
        } else {
            saveToDb();
        }
    }

    /* ---------------------- SharedPreferences (JSON array) ---------------------- */
    private void loadFromPrefs() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = sp.getString(PREFS_KEY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                tasks.add(new Task(
                        o.optString("title"),
                        o.optString("deadline"),
                        o.optString("notes"),
                        o.optString("status", "Pending")
                ));
            }
        } catch (Exception ignored) {}
    }

    private void saveToPrefs() {
        JSONArray arr = new JSONArray();
        try {
            for (Task t : tasks) {
                JSONObject o = new JSONObject();
                o.put("title", t.title);
                o.put("deadline", t.deadline);
                o.put("notes", t.notes);
                o.put("status", t.status);
                arr.put(o);
            }
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit().putString(PREFS_KEY, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    /* ---------------------- SQLite ---------------------- */
    private void loadFromDb() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        tasks.clear();
        try (Cursor c = db.rawQuery("SELECT title, deadline, notes, status FROM tasks ORDER BY _id DESC", null)) {
            while (c.moveToNext()) {
                tasks.add(new Task(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3)
                ));
            }
        }
    }

    private void saveToDb() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("tasks", null, null);
            for (Task t : tasks) {
                db.execSQL("INSERT INTO tasks(title, deadline, notes, status) VALUES(?,?,?,?)",
                        new Object[]{t.title, t.deadline, t.notes, t.status});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /* ---------------------- Data & UI helpers ---------------------- */
    public static class Task implements Serializable {
        public String title;
        public String deadline;
        public String notes;
        public String status; // "Pending" or "Done"

        public Task(String title, String deadline, String notes, String status) {
            this.title = title;
            this.deadline = deadline;
            this.notes = notes;
            this.status = (status == null || status.isEmpty()) ? "Pending" : status;
        }
    }

    private static class TaskDbHelper extends SQLiteOpenHelper {
        public TaskDbHelper(@NonNull Context ctx) {
            super(ctx, "tasks.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS tasks (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "deadline TEXT," +
                    "notes TEXT," +
                    "status TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS tasks");
            onCreate(db);
        }
    }

    private static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {
        interface OnTaskInteraction {
            void onClick(int position);
            void onLongPress(int position);
        }

        private final List<Task> data;
        private final OnTaskInteraction listener;

        TaskAdapter(List<Task> data, OnTaskInteraction listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull
        @Override
        public TaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new TaskVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskVH h, int position) {
            Task t = data.get(position);
            TextView line1 = h.itemView.findViewById(android.R.id.text1);
            TextView line2 = h.itemView.findViewById(android.R.id.text2);
            line1.setText(t.title);
            line2.setText((t.deadline == null || t.deadline.isEmpty() ? "No deadline" : t.deadline) +
                    " • " + t.status);

            h.itemView.setOnClickListener(v -> listener.onClick(h.getBindingAdapterPosition()));
            h.itemView.setOnLongClickListener(v -> {
                listener.onLongPress(h.getBindingAdapterPosition());
                return true;
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class TaskVH extends RecyclerView.ViewHolder {
            TaskVH(@NonNull View itemView) { super(itemView); }
        }
    }
}
