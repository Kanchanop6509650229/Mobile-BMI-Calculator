package com.example.bmical;

import static android.provider.BaseColumns._ID;
import static com.example.bmical.Constants.BMI;
import static com.example.bmical.Constants.CLASSIFICATION;
import static com.example.bmical.Constants.DATE;
import static com.example.bmical.Constants.TABLE_NAME;
import static com.example.bmical.Constants.WEIGHT;

import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    DecimalFormat formatter = new DecimalFormat("#,###.##");

    private TextView header;
    private TextView weightTxt;
    private EditText weightEditText;
    private TextView heightTxt;
    private EditText heightEditText;
    private TextView bmiTxt;
    private EditText bmiResult;
    private TextView classificationTxt;
    private EditText classificationResult;
    private Button submitBtn;
    private EventsData events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        header = findViewById(R.id.header);
        weightTxt = findViewById(R.id.weight_txt);
        weightEditText = findViewById(R.id.weight_input);
        heightTxt = findViewById(R.id.height_txt);
        heightEditText = findViewById(R.id.height_input);
        bmiTxt = findViewById(R.id.bmi_txt);
        bmiResult = findViewById(R.id.bmi_input);
        classificationTxt = findViewById(R.id.classification_txt);
        classificationResult = findViewById(R.id.classification_input);
        submitBtn = findViewById(R.id.submitButton);

        weightEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        heightEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String weightStr = weightEditText.getText().toString();
                String heightStr = heightEditText.getText().toString();

                if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                    double weight = Double.parseDouble(weightStr);
                    double height = Double.parseDouble(heightStr);

                    if (height <= 0 || weight <= 0) {
                        Toast.makeText(MainActivity.this, R.string.input_zero_alert, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double bmi = getCalculatedBMI(weight, height);

                    bmiResult.setText(formatter.format(bmi));

                    classificationResult.setText(getClassification(bmi));

                    bmiResult.setBackgroundColor(getClassificationColor(bmi));
                    classificationResult.setBackgroundColor(getClassificationColor(bmi));

                    events = new EventsData(MainActivity.this);
                    try{
                        addEvent();
                        Cursor cursor = getEvents();
                        showEvents(cursor);
                    }finally{
                        events.close();
                    }

                } else {
                    Toast.makeText(MainActivity.this, R.string.no_input_alert, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addEvent() {
        String weight = String.format("%1$s", weightEditText.getText());
        String bmi = String.format("%1$s", bmiResult.getText());
        String classification = String.format("%1$s", classificationResult.getText());
        SQLiteDatabase db = events.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATE, System.currentTimeMillis());
        values.put(WEIGHT, weight);
        values.put(BMI, bmi);
        values.put(CLASSIFICATION, classification);
        db.insert(TABLE_NAME, null, values);
    }//end addEvent

    private Cursor getEvents() {
        String[] FROM = {_ID, DATE, WEIGHT, BMI, CLASSIFICATION};
        String ORDER_BY = DATE + " DESC";
        SQLiteDatabase db = events.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null, null, ORDER_BY);
        return cursor;
    }//end getEvents

    private void showEvents(Cursor cursor) {
        final ListView listView = findViewById(R.id.listView);
        final ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        while(cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("row_id", String.valueOf(cursor.getLong(0)));
            map.put("date", String.valueOf(cursor.getLong(1)));
            map.put("weight", cursor.getString(2));
            map.put("bmi", cursor.getString(3));
            map.put("classification", cursor.getString(4));
            MyArrList.add(map);
        }
        SimpleAdapter sAdap;
        sAdap = new SimpleAdapter( MainActivity.this, MyArrList, R.layout.column,
                new String[] {"row_id", "date", "weight", "bmi", "classification"},
                new int[] {R.id.col_row_id, R.id.col_date, R.id.col_weight, R.id.col_bmi, R.id.col_classification} );
        listView.setAdapter(sAdap);
    }//end showEvents

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        header.setTextSize(newConfig.fontScale*24);

        weightTxt.setTextSize(newConfig.fontScale*13);
        weightEditText.setTextSize(newConfig.fontScale*13);

        heightTxt.setTextSize(newConfig.fontScale*13);
        heightEditText.setTextSize(newConfig.fontScale*13);

        bmiTxt.setTextSize(newConfig.fontScale*13);
        bmiResult.setTextSize(newConfig.fontScale*13);

        classificationTxt.setTextSize(newConfig.fontScale*13);
        classificationResult.setTextSize(newConfig.fontScale*13);

        submitBtn.setTextSize(newConfig.fontScale*13);
    }

    private double getCalculatedBMI(double mass, double height) {
        return mass / ((height/100)*(height/100));
    }

    private String getClassification(double bmi) {
        if (bmi < 16) {
            return getString(R.string.severe_thinness);
        } else if (bmi >= 16 && bmi < 17) {
            return getString(R.string.moderate_thinness);
        } else if (bmi >= 17 && bmi < 18.5) {
            return getString(R.string.light_thinness);
        } else if (bmi >= 18.5 && bmi < 25) {
            return getString(R.string.normal);
        } else if (bmi >= 25 && bmi < 30) {
            return getString(R.string.overweight);
        } else if (bmi >= 30 && bmi < 35) {
            return getString(R.string.obese_class_i);
        } else if (bmi >= 35 && bmi < 40) {
            return getString(R.string.obese_class_ii);
        } else {
            return getString(R.string.obese_class_iii);
        }
    }

    private int getClassificationColor(double bmi) {
        if (bmi < 16) {
            return getColor(R.color.severe_thinness_color);
        } else if (bmi >= 16 && bmi < 17) {
            return getColor(R.color.moderate_thinness_color);
        } else if (bmi >= 17 && bmi < 18.5) {
            return getColor(R.color.light_thinness_color);
        } else if (bmi >= 18.5 && bmi < 25) {
            return getColor(R.color.normal_color);
        } else if (bmi >= 25 && bmi < 30) {
            return getColor(R.color.overweight_color);
        } else if (bmi >= 30 && bmi < 35) {
            return getColor(R.color.obese_class_i_color);
        } else if (bmi >= 35 && bmi < 40) {
            return getColor(R.color.obese_class_ii_color);
        } else {
            return getColor(R.color.obese_class_iii_color);
        }
    }
}

class DecimalDigitsInputFilter implements InputFilter {
    private Pattern mPattern;
    DecimalDigitsInputFilter(int digits, int digitsAfterZero) {
        mPattern = Pattern.compile("[0-9]{0," + (digits - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) +
                "})?)||(\\.)?");
    }
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Matcher matcher = mPattern.matcher(dest);
        if (!matcher.matches())
            return "";
        return null;
    }
}