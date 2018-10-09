package com.example.a12sha.justanother;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Spinner mode_spinner;
    Spinner user_spinner;
    String selected_mode;
    String selected_user;
    String train_from_date;
    String train_to_date;
    String test_from_date;
    String test_to_date;
    ArrayAdapter<CharSequence> adapter;
    private TextView trainingHead;
    private TextView testhead;
    private TextView trainFrom;
    private TextView trainTo;
    private TextView testFrom;
    private TextView testTo;
    private DatePickerDialog.OnDateSetListener trainFromDateSetListner;
    private DatePickerDialog.OnDateSetListener trainToDateSetListner;
    private DatePickerDialog.OnDateSetListener testFromDateSetListner;
    private DatePickerDialog.OnDateSetListener testToDateSetListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GPS/Geolife User Data mode selection
        mode_selection();

        //User selection
        user_selecion();

        //Dates
        //Set from and to dates for text and train data
        set_dates();
    }

    private void mode_selection() {
        mode_spinner = (Spinner) findViewById(R.id.spinner_mode);
        adapter = ArrayAdapter.createFromResource(this, R.array.modes, android.R.layout.simple_list_item_checked);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mode_spinner.setAdapter(adapter);
        mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_mode = mode_spinner.getSelectedItem().toString();
                if (selected_mode.equals("Geolife User Data")) {
                    user_spinner.setVisibility(View.VISIBLE);
                    trainingHead.setVisibility(View.VISIBLE);
                    testhead.setVisibility(View.VISIBLE);
                    trainFrom.setVisibility(View.VISIBLE);
                    trainTo.setVisibility(View.VISIBLE);
                    testFrom.setVisibility(View.VISIBLE);
                    testTo.setVisibility(View.VISIBLE);

                } else {
                    user_spinner.setVisibility(View.GONE);
                    trainingHead.setVisibility(View.GONE);
                    testhead.setVisibility(View.GONE);
                    trainFrom.setVisibility(View.GONE);
                    trainTo.setVisibility(View.GONE);
                    testFrom.setVisibility(View.GONE);
                    testTo.setVisibility(View.GONE);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void user_selecion() {
        user_spinner = (Spinner) findViewById(R.id.spinner_user);
        adapter = ArrayAdapter.createFromResource(this, R.array.users, android.R.layout.simple_list_item_checked);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        user_spinner.setAdapter(adapter);
        user_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_user = user_spinner.getSelectedItem().toString();
                selected_user = selected_user.substring(5);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void set_dates() {
        trainingHead =(TextView) findViewById(R.id.train);
        testhead =(TextView) findViewById(R.id.test);

        //Training start date
        trainFrom = (TextView) findViewById(R.id.train_from);
        trainFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = 2007;
                int month = 3;
                int day = 01;

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        trainFromDateSetListner,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        trainFromDateSetListner = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "OnDateSet: date:" + year + "/" + day + "/" + month);
                train_from_date = day + "/" + month + "/" + year;
                trainFrom.setText(train_from_date);
            }
        };

        //Training till date
        trainTo = (TextView) findViewById(R.id.train_to);
        trainTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = 2012;
                int month = 7;
                int day = 01;

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        trainToDateSetListner,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        trainToDateSetListner = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "OnDateSet: date:" + year + "/" + day + "/" + month);
                train_to_date = day + "/" + month + "/" + year;
                trainTo.setText(train_to_date);
            }
        };

        //Test start date
        testFrom = (TextView) findViewById(R.id.test_from);
        testFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = 2007;
                int month = 3;
                int day = 01;

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        testFromDateSetListner,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        testFromDateSetListner = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "OnDateSet: date:" + year + "/" + day + "/" + month);
                test_from_date = day + "/" + month + "/" + year;
                testFrom.setText(test_from_date);
            }
        };

        //Test till date
        testTo = (TextView) findViewById(R.id.test_to);
        testTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = 2012;
                int month = 7;
                int day = 01;

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        testToDateSetListner,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        testToDateSetListner = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "OnDateSet: date:" + year + "/" + day + "/" + month);
                test_to_date = day + "/" + month + "/" + year;
                testTo.setText(test_to_date);
            }
        };


    }

    //After user click continue:
    public void staypoints_disp(View view){

        //check if the mode is Geolife Data and dates are left blanks
        if ( (selected_mode.equals("Geolife User Data")) &&(
                (train_from_date == null) ||
                (train_to_date == null) ||
                (test_from_date == null) ||
                (test_to_date == null))
                ){
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Enter dates!")
                    .setPositiveButton("Ok", null).show();
        }
        else {
            ProgressBar p = (ProgressBar)findViewById(R.id.progressBar1);
            if(p.getVisibility() != view.VISIBLE){ // check if it is visible
                p.setVisibility(view.VISIBLE); // if not set it to visible
                view.setVisibility(view.GONE); // use 1 or 2 as parameters.. arg0 is the view(your button) from the onclick listener
            }

            Intent intent = new Intent(this, staypoints_list.class);
            intent.putExtra("mode_selected", selected_mode);
            intent.putExtra("user_selected", selected_user);
            intent.putExtra("train_from_date_selected", train_from_date);
            intent.putExtra("train_to_date_selected", train_to_date);
            intent.putExtra("test_from_date_selected", test_from_date);
            intent.putExtra("test_to_date_selected", test_to_date);
            startActivity(intent);
        }
    }
}
