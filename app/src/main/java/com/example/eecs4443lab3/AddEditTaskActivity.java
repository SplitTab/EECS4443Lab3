package com.example.eecs4443lab3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Add/Edit Task screen
 * ---------------------------------
 * Simple form that captures a task's Title (required), optional Deadline (via
 * DatePicker),
 * and optional Notes. When saved, returns data to the caller via
 * setResult(...).
 *
 * Layout: activity_add_edit_task.xml
 * - inputTitle (TextInputEditText, required)
 * - inputDeadline (TextInputEditText, opens DatePicker)
 * - inputNotes (TextInputEditText, optional)
 * - btnSave (MaterialButton)
 */
public class AddEditTaskActivity extends AppCompatActivity {

    // UI references
    private TextInputEditText inputTitle;
    private TextInputEditText inputDeadline;
    private TextInputEditText inputNotes;
    private MaterialButton btnSave;

    // Holds the currently picked date for the deadline
    private final Calendar picked = new GregorianCalendar();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        // Find views
        inputTitle = findViewById(R.id.inputTitle);
        inputDeadline = findViewById(R.id.inputDeadline);
        inputNotes = findViewById(R.id.inputNotes);
        btnSave = findViewById(R.id.btnSave);

        // Open the date picker when either the deadline field or its container is
        // tapped
        View deadlineContainer = findViewById(R.id.tilDeadline);
        View.OnClickListener openPicker = v -> showDatePicker();
        inputDeadline.setOnClickListener(openPicker);
        deadlineContainer.setOnClickListener(openPicker);

        // Save button: validate and return data to caller
        btnSave.setOnClickListener(v -> {
            String title = safeText(inputTitle);
            String deadline = safeText(inputDeadline);
            String notes = safeText(inputNotes);

            // Require title
            if (TextUtils.isEmpty(title)) {
                Snackbar.make(v, "Title is required", Snackbar.LENGTH_LONG).show();
                inputTitle.requestFocus();
                return;
            }

            Intent data = new Intent();
            data.putExtra(MainActivity.EXTRA_TITLE, title);
            data.putExtra(MainActivity.EXTRA_DEADLINE, deadline);
            data.putExtra(MainActivity.EXTRA_NOTES, notes);
            data.putExtra(MainActivity.EXTRA_STATUS, "Pending");
            setResult(RESULT_OK, data);
            finish();
        });

        // Top app bar with back arrow
        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Shows a DatePickerDialog and writes the selected date to the deadline field.
     */
    private void showDatePicker() {
        int y = picked.get(Calendar.YEAR);
        int m = picked.get(Calendar.MONTH);
        int d = picked.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String formatted = DateFormat.getDateInstance(DateFormat.MEDIUM)
                            .format(picked.getTime());
                    inputDeadline.setText(formatted);
                },
                y, m, d);
        dlg.show();
    }

    /**
     * Convenience: null-safe trimmed text from a TextInputEditText.
     */
    private static String safeText(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
