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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Spinner mode_spinner;
    Spinner user_spinner;
    String selected_mode;
    String selected_user;
    String train_month;
    ArrayAdapter<CharSequence> adapter;
    private TextView trainingHead;
    private TextView trainFrom;
    private DatePickerDialog.OnDateSetListener trainFromDateSetListner;
    ProgressBar p;

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
                    trainFrom.setVisibility(View.VISIBLE);

                } else {
                    user_spinner.setVisibility(View.GONE);
                    trainingHead.setVisibility(View.GONE);
                    trainFrom.setVisibility(View.GONE);
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

        //Training start date
        trainFrom = (TextView) findViewById(R.id.train_from);
        trainFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = 2008;
                int month = 10;
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
                train_month = day + "/" + month + "/" + year;
                trainFrom.setText(train_month);
            }
        };
    }

    //After user click continue:
    public void staypoints_disp(View view){

        //check if the mode is Geolife Data and dates are left blanks
        if ( (selected_mode.equals("Geolife User Data")) &&
                (train_month == null))
                {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Enter dates!")
                    .setPositiveButton("Ok", null).show();
        }
        else {
            p = (ProgressBar)findViewById(R.id.progressBar1);
            if(p.getVisibility() != view.VISIBLE){
                p.setVisibility(view.VISIBLE);
                view.setVisibility(view.GONE);
            }

            Intent intent = new Intent(this, createMarkovChain.class);
            intent.putExtra("mode_selected", selected_mode);
            intent.putExtra("user_selected", selected_user);
            intent.putExtra("train_from_date_selected", train_month);
            startActivity(intent);
        }
        Log.w("myApp", "no network");
    }

    @Override
    public void onResume() {
        super.onResume();

        ProgressBar p = (ProgressBar) findViewById(R.id.progressBar1);
        Button b = (Button) findViewById(R.id.button_modesel);
        if (b.getVisibility() != View.VISIBLE) {
            b.setVisibility(View.VISIBLE);
            p.setVisibility(View.GONE);
        }
    }
}
