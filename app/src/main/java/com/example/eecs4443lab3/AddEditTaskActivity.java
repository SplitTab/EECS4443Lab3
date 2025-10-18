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

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Add/Edit screen with validation and a DatePicker.
 * Uses activity_add_edit_task.xml with:
 *  - inputTitle (required)
 *  - inputDeadline (tap opens DatePicker)
 *  - inputNotes (optional)
 *  - btnSave
 *  Matches Lab 3 "Task Logger" form requirements.  :contentReference[oaicite:9]{index=9} :contentReference[oaicite:10]{index=10}
 */
public class AddEditTaskActivity extends AppCompatActivity {

    private TextInputEditText inputTitle, inputDeadline, inputNotes;
    private MaterialButton btnSave;

    private final Calendar picked = new GregorianCalendar();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout: banner, TextInputLayouts, and Save button  :contentReference[oaicite:11]{index=11}
        setContentView(R.layout.activity_add_edit_task);

        inputTitle = findViewById(R.id.inputTitle);
        inputDeadline = findViewById(R.id.inputDeadline);
        inputNotes = findViewById(R.id.inputNotes);
        btnSave = findViewById(R.id.btnSave);

        View deadlineContainer = findViewById(R.id.tilDeadline);
        View.OnClickListener openPicker = v -> showDatePicker();
        inputDeadline.setOnClickListener(openPicker);
        deadlineContainer.setOnClickListener(openPicker);

        btnSave.setOnClickListener(v -> {
            String title = safeText(inputTitle);
            String deadline = safeText(inputDeadline);
            String notes = safeText(inputNotes);

            // Basic validation per lab (required title)  :contentReference[oaicite:12]{index=12}
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
    }

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
                    String formatted = DateFormat.getDateInstance(DateFormat.MEDIUM).format(picked.getTime());
                    inputDeadline.setText(formatted);
                },
                y, m, d
        );
        dlg.show();
    }



    private static String safeText(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
