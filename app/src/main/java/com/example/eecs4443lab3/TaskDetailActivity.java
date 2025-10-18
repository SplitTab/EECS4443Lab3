package com.example.eecs4443lab3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Task Detail screen (read-only)
 * ---------------------------------
 * Displays a task's Title, Deadline, Status, and Notes passed via Intent extras
 * from the caller. Empty values are rendered as an em dash (—).
 */
public class TaskDetailActivity extends AppCompatActivity {

    private TextView txtTitle;
    private TextView txtDeadline;
    private TextView txtStatus;
    private TextView txtNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // View bindings
        txtTitle = findViewById(R.id.txtTitle);
        txtDeadline = findViewById(R.id.txtDeadline);
        txtStatus = findViewById(R.id.txtStatus);
        txtNotes = findViewById(R.id.txtNotes);

        // Pull values from intent
        String title = getIntent().getStringExtra(MainActivity.EXTRA_TITLE);
        String deadline = getIntent().getStringExtra(MainActivity.EXTRA_DEADLINE);
        String status = getIntent().getStringExtra(MainActivity.EXTRA_STATUS);
        String notes = getIntent().getStringExtra(MainActivity.EXTRA_NOTES);

        // Populate UI (fallback to em dash for empty)
        txtTitle.setText(nullToDash(title));
        txtDeadline.setText(isNullOrEmpty(deadline) ? "—" : deadline);
        txtStatus.setText(nullToDash(status));
        txtNotes.setText(isNullOrEmpty(notes) ? "—" : notes);

        // Top app bar + back arrow
        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nullToDash(String s) {
        return isNullOrEmpty(s) ? "—" : s;
    }
}
