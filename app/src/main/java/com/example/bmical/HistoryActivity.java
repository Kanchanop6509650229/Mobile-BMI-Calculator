package com.example.bmical;

import static android.provider.BaseColumns._ID;
import static com.example.bmical.Constants.BMI;
import static com.example.bmical.Constants.CLASSIFICATION;
import static com.example.bmical.Constants.COLOR;
import static com.example.bmical.Constants.DATE;
import static com.example.bmical.Constants.HEIGHT;
import static com.example.bmical.Constants.TABLE_NAME;
import static com.example.bmical.Constants.WEIGHT;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private EventsData events;
    private TableLayout tableContent;
    private boolean alternateColor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tableContent = findViewById(R.id.tableContent);

        events = new EventsData(HistoryActivity.this);
        try {
            Cursor cursor = getEvents();
            if (cursor != null && cursor.getCount() > 0) {
                showEvents(cursor);
            } else {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {

        } finally {
            events.close();
        }
    }

    private void showEvents(Cursor cursor) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", new Locale("th", "TH"));

        while(cursor.moveToNext()) {
            TableRow row = new TableRow(this);

            TextView idView = createTextView(String.valueOf(cursor.getLong(0)));
            TextView dateView = createTextView(dateFormat.format(new Date(cursor.getLong(1))));
            TextView weightView = createTextView(cursor.getString(2));
            TextView heightView = createTextView(cursor.getString(3));
            TextView bmiView = createTextView(cursor.getString(4));
            TextView classView = createTextView(cursor.getString(5));

            int color = cursor.getInt(6);

            bmiView.setBackgroundColor(color);
            classView.setBackgroundColor(color);

            row.addView(idView);
            row.addView(dateView);
            row.addView(weightView);
            row.addView(heightView);
            row.addView(bmiView);
            row.addView(classView);

            tableContent.addView(row);
        }
    }

    private TextView createTextView(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setGravity(Gravity.CENTER);
        view.setPadding(8, 8, 8, 8);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        );
        view.setLayoutParams(params);

        return view;
    }

    private Cursor getEvents() {
        String[] FROM = {_ID, DATE, WEIGHT, HEIGHT, BMI, CLASSIFICATION, COLOR}; // เพิ่ม COLOR
        String ORDER_BY = DATE + " DESC";
        SQLiteDatabase db = events.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null, null, ORDER_BY);
        return cursor;
    }
}