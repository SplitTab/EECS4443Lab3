package com.example.eecs4443lab3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Simple read-only details screen with labels:
 *  - txtTitle, txtDeadline, txtStatus, txtNotes
 * Uses activity_task_detail.xml.  :contentReference[oaicite:13]{index=13}
 * Opened via tap gesture per Lab 3.  :contentReference[oaicite:14]{index=14}
 */
public class TaskDetailActivity extends AppCompatActivity {

    private TextView txtTitle, txtDeadline, txtStatus, txtNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        txtTitle = findViewById(R.id.txtTitle);
        txtDeadline = findViewById(R.id.txtDeadline);
        txtStatus = findViewById(R.id.txtStatus);
        txtNotes = findViewById(R.id.txtNotes);

        String title = getIntent().getStringExtra(MainActivity.EXTRA_TITLE);
        String deadline = getIntent().getStringExtra(MainActivity.EXTRA_DEADLINE);
        String status = getIntent().getStringExtra(MainActivity.EXTRA_STATUS);
        String notes = getIntent().getStringExtra(MainActivity.EXTRA_NOTES);

        txtTitle.setText(nullToDash(title));
        txtDeadline.setText(nullOrEmpty(deadline) ? "—" : deadline);
        txtStatus.setText(nullToDash(status));
        txtNotes.setText(nullOrEmpty(notes) ? "—" : notes);
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
    private static String nullToDash(String s) { return nullOrEmpty(s) ? "—" : s; }
}
