package com.example.a12sha.justanother;

import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class createMarkovChain extends AppCompatActivity {

    //variables
    SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");
    private String mode_sel;
    private String user_sel;
    private String train_from_date_sel;
    private List<hourlyweights> train_timeslotteddata = new ArrayList<>();
    public List<String> user_state_addr = new ArrayList<>();
    public ArrayList<states> state_list = new ArrayList<>();
    private int unique_stateids;

    // markov chain for each hour
    double[][] mc_0 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_1 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_2 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_3 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_4 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_5 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_6 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_7 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_8 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_9 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_10 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_11 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_12 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_13 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_14 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_15 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_16 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_17 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_18 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_19 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_20 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_21 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_22 = new double[unique_stateids+1][unique_stateids+1];
    double[][] mc_23 = new double[unique_stateids+1][unique_stateids+1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staypoints_list);

        // Time loggers to track the time of execution and creation of markov chain
        long tStart = System.currentTimeMillis();

        //fetch data from previous screen (mode, user, dates)
        fetch_prev_act_data();

        //if mode selected is "Geolife User Data", then process user location file
        if (mode_sel.equals("Geolife User Data")){
            View b = findViewById(R.id.button);
            b.setVisibility(View.VISIBLE);
            try {
                process_user_data();
            } catch (IOException e) {
                Log.wtf("MyActivity", "Error reading the file" + e);
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //if mode selected is "GPS", process user current gps locations
        if (mode_sel.equals("GPS")){
            View b = findViewById(R.id.button);
            b.setVisibility(View.GONE);

            GPSTracker gps = new GPSTracker(this);
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            if (latitude == 0 && longitude == 0)
                user_state_addr.add("Error: Location is turned off. Update location settings for the app.");
            else {
                try {
                    user_state_addr.add(fetchaddress(latitude, longitude));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //display the address as a list
        ListView staypointslist = (ListView) findViewById(R.id._staypointslist);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, user_state_addr);
        staypointslist.setAdapter(adapter);

        // end of process creating markov chain, track time
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        Log.i("Total States:", Integer.toString(unique_stateids));
        Log.i("Markov Chains Time:", Double.toString(elapsedSeconds));
    }

    private void fetch_prev_act_data() {
        Intent intent = getIntent();
        //fetch selected mode
        mode_sel = intent.getStringExtra("mode_selected");
        //fetch user selected
        user_sel = intent.getStringExtra("user_selected");
        //fetch dates
        train_from_date_sel =  intent.getStringExtra("train_from_date_selected");
    }

    //process training data and create model
    public void process_user_data() throws IOException, ParseException {

        //take user id and entered month
        user_sel = String.format("%03d", Integer.parseInt(user_sel));
        Date from_date = null;
        try {
            from_date = new SimpleDateFormat("dd/MM/yyyy").parse(train_from_date_sel);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //read the file file based on userid and month
        Calendar cal = Calendar.getInstance();
        cal.setTime(from_date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        String month_s = String.format("%02d", month);
        String path = "userdata/" + user_sel + "/" + Integer.toString(year) + month_s + "/";
        AssetManager am = getAssets();
        String[] files = am.list(path);
        List<String> filelist = new LinkedList<String>(Arrays.asList(files));

        //process the file
        if (filelist.size()!=0) {
            String file_read = path + filelist.get(0);
            String train_test = "train";
            process_file(file_read, train_test);
        }

        //fetch state addresses
        for (int i = 0; i < state_list.size(); i++){
            String state_addr = "(" + Double.toString(state_list.get(i).getStateid()) + ")" +
                    fetchaddress(state_list.get(i).getLat(), state_list.get(i).getLon());
            state_list.get(i).setAddress(state_addr);
            user_state_addr.add(state_addr);
        }
    }

    //process user file
    private void process_file(String file_read, String train_test) throws IOException, ParseException {
        AssetManager am = getAssets();
        InputStream raw = am.open(file_read);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(raw, Charset.forName("UTF-8"))
        );

        String line = "";
        reader.readLine();

        while (( line = reader.readLine()) != null){
            String[] token = line.split("\t");
            hourlyweights new_row = new hourlyweights();
            new_row.setDate(token[1]);
            new_row.setStateid(token[2]);
            new_row.setAvg_latitude(token[3]);
            new_row.setAvg_longitude(token[4]);
            new_row.setHour_w(Arrays.copyOfRange(token, 5, 29));
            train_timeslotteddata.add(new_row);
        }

        create_state_list();        //create unique state list for display in on screens and lists
        create_blank_model();       //create model with 0's with all the states
        populate_model();           //populate the model with actual probabilities
    }

    private void create_state_list() {
        Boolean found = false;
        states temp_state = new states();
        for(int i = 0; i < train_timeslotteddata.size(); i++){
            found = false;
            //look if the state id is already is state list
            for(int j = 0; j < state_list.size(); j++){
                if (state_list.get(j).getStateid() == Double.parseDouble(train_timeslotteddata.get(i).getStateid())){
                    found = true;
                }
            }
            if (found == false) {
                temp_state.setLat(Double.parseDouble(train_timeslotteddata.get(i).getAvg_latitude()));
                temp_state.setLon(Double.parseDouble(train_timeslotteddata.get(i).getAvg_longitude()));
                temp_state.setStateid(Double.parseDouble(train_timeslotteddata.get(i).getStateid()));
                state_list.add(temp_state);
                temp_state = new states();
            }
        }
    }

    private void create_blank_model() {
        // count and list unique StateIds----------------------------
        List<String> stateids = new ArrayList<String>();


        for(int i = 0; i < train_timeslotteddata.size();i++){
            stateids.add(train_timeslotteddata.get(i).getStateid());
        }

        HashSet<String> noDupstateIds = new HashSet<String>();

        noDupstateIds.addAll(stateids);
        unique_stateids = noDupstateIds.size();
        List<String> uniq_stateids_s = new ArrayList<String>(noDupstateIds);

        List<Double> uniq_stateids = new ArrayList<Double>(uniq_stateids_s.size());
        for(String current:uniq_stateids_s){
            uniq_stateids.add(Double.parseDouble(current));
        }
        Collections.sort(uniq_stateids);

        //Create Empty Hourly Markov chains------------------------------
        mc_0 = new double[unique_stateids+1][unique_stateids+1];
        mc_1 = new double[unique_stateids+1][unique_stateids+1];
        mc_2 = new double[unique_stateids+1][unique_stateids+1];
        mc_3 = new double[unique_stateids+1][unique_stateids+1];
        mc_4 = new double[unique_stateids+1][unique_stateids+1];
        mc_5 = new double[unique_stateids+1][unique_stateids+1];
        mc_6 = new double[unique_stateids+1][unique_stateids+1];
        mc_7 = new double[unique_stateids+1][unique_stateids+1];
        mc_8 = new double[unique_stateids+1][unique_stateids+1];
        mc_9 = new double[unique_stateids+1][unique_stateids+1];
        mc_10 = new double[unique_stateids+1][unique_stateids+1];
        mc_11 = new double[unique_stateids+1][unique_stateids+1];
        mc_12 = new double[unique_stateids+1][unique_stateids+1];
        mc_13 = new double[unique_stateids+1][unique_stateids+1];
        mc_14 = new double[unique_stateids+1][unique_stateids+1];
        mc_15 = new double[unique_stateids+1][unique_stateids+1];
        mc_16 = new double[unique_stateids+1][unique_stateids+1];
        mc_17 = new double[unique_stateids+1][unique_stateids+1];
        mc_18 = new double[unique_stateids+1][unique_stateids+1];
        mc_19 = new double[unique_stateids+1][unique_stateids+1];
        mc_20 = new double[unique_stateids+1][unique_stateids+1];
        mc_21 = new double[unique_stateids+1][unique_stateids+1];
        mc_22 = new double[unique_stateids+1][unique_stateids+1];
        mc_23 = new double[unique_stateids+1][unique_stateids+1];

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_0[0][i+1] = uniq_stateids.get(i);
            mc_0[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_1[0][i+1] = uniq_stateids.get(i);
            mc_1[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_2[0][i+1] = uniq_stateids.get(i);
            mc_2[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_3[0][i+1] = uniq_stateids.get(i);
            mc_3[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_4[0][i+1] = uniq_stateids.get(i);
            mc_4[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_5[0][i+1] = uniq_stateids.get(i);
            mc_5[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_6[0][i+1] = uniq_stateids.get(i);
            mc_6[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_7[0][i+1] = uniq_stateids.get(i);
            mc_7[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_8[0][i+1] = uniq_stateids.get(i);
            mc_8[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_9[0][i+1] = uniq_stateids.get(i);
            mc_9[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_10[0][i+1] = uniq_stateids.get(i);
            mc_10[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_11[0][i+1] = uniq_stateids.get(i);
            mc_11[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_12[0][i+1] = uniq_stateids.get(i);
            mc_12[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_13[0][i+1] = uniq_stateids.get(i);
            mc_13[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_14[0][i+1] = uniq_stateids.get(i);
            mc_14[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_15[0][i+1] = uniq_stateids.get(i);
            mc_15[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_16[0][i+1] = uniq_stateids.get(i);
            mc_16[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_17[0][i+1] = uniq_stateids.get(i);
            mc_17[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_18[0][i+1] = uniq_stateids.get(i);
            mc_18[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_19[0][i+1] = uniq_stateids.get(i);
            mc_19[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_20[0][i+1] = uniq_stateids.get(i);
            mc_20[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_21[0][i+1] = uniq_stateids.get(i);
            mc_21[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_22[0][i+1] = uniq_stateids.get(i);
            mc_22[i+1][0] = uniq_stateids.get(i);
        }

        for (int i=0;i<uniq_stateids.size();i++) {
            mc_23[0][i+1] = uniq_stateids.get(i);
            mc_23[i+1][0] = uniq_stateids.get(i);
        }

    }

    private void populate_model() {
        //populates train_markovchain
        List<hourlyweights> day_timeslotteddata = new ArrayList<>();
        String prev_date = train_timeslotteddata.get(0).getDate();
        day_timeslotteddata.add(train_timeslotteddata.get(0));

        for(int i = 1; i < train_timeslotteddata.size();i++){
            if (prev_date.equals(train_timeslotteddata.get(i).getDate())){
                day_timeslotteddata.add(train_timeslotteddata.get(i));
            }
            if (!prev_date.equals(train_timeslotteddata.get(i).getDate())) {
                        create_day_markov_chain(day_timeslotteddata);
                        day_timeslotteddata.clear();
                        prev_date = train_timeslotteddata.get(i).getDate();
                        day_timeslotteddata.add(train_timeslotteddata.get(i));
            }
            if (i == train_timeslotteddata.size()-1){
                create_day_markov_chain(day_timeslotteddata);
                day_timeslotteddata.clear();
            }
        }
        //divide the cell values with sum of the rows to calculate the probabilities
        assign_small_prob();
        assign_prob();
    }

    private void assign_small_prob() {
        for(int i = 1; i < mc_0.length; i++){
            for(int j = 1; j < mc_0.length; j++){
                if (mc_0[i][j] == 0)
                    mc_0[i][j] = 0.00001;
            }
        }

        for(int i = 1; i < mc_1.length; i++){
            for(int j = 1; j < mc_1.length; j++){
                if (mc_1[i][j] == 0)
                    mc_1[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_2.length; i++){
            for(int j = 1; j < mc_2.length; j++){
                if (mc_2[i][j] == 0)
                    mc_2[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_3.length; i++){
            for(int j = 1; j < mc_3.length; j++){
                if (mc_3[i][j] == 0)
                    mc_3[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_4.length; i++){
            for(int j = 1; j < mc_4.length; j++){
                if (mc_4[i][j] == 0)
                    mc_4[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_5.length; i++){
            for(int j = 1; j < mc_5.length; j++){
                if (mc_5[i][j] == 0)
                    mc_5[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_6.length; i++){
            for(int j = 1; j < mc_6.length; j++){
                if (mc_6[i][j] == 0)
                    mc_6[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_7.length; i++){
            for(int j = 1; j < mc_7.length; j++){
                if (mc_7[i][j] == 0)
                    mc_7[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_8.length; i++){
            for(int j = 1; j < mc_8.length; j++){
                if (mc_8[i][j] == 0)
                    mc_8[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_9.length; i++){
            for(int j = 1; j < mc_9.length; j++){
                if (mc_9[i][j] == 0)
                    mc_9[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_10.length; i++){
            for(int j = 1; j < mc_10.length; j++){
                if (mc_10[i][j] == 0)
                    mc_10[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_11.length; i++){
            for(int j = 1; j < mc_11.length; j++){
                if (mc_11[i][j] == 0)
                    mc_11[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_12.length; i++){
            for(int j = 1; j < mc_12.length; j++){
                if (mc_12[i][j] == 0)
                    mc_12[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_13.length; i++){
            for(int j = 1; j < mc_13.length; j++){
                if (mc_13[i][j] == 0)
                    mc_13[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_14.length; i++){
            for(int j = 1; j < mc_14.length; j++){
                if (mc_14[i][j] == 0)
                    mc_14[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_15.length; i++){
            for(int j = 1; j < mc_15.length; j++){
                if (mc_15[i][j] == 0)
                    mc_15[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_16.length; i++){
            for(int j = 1; j < mc_16.length; j++){
                if (mc_16[i][j] == 0)
                    mc_16[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_17.length; i++){
            for(int j = 1; j < mc_17.length; j++){
                if (mc_17[i][j] == 0)
                    mc_17[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_18.length; i++){
            for(int j = 1; j < mc_18.length; j++){
                if (mc_18[i][j] == 0)
                    mc_18[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_19.length; i++){
            for(int j = 1; j < mc_19.length; j++){
                if (mc_19[i][j] == 0)
                    mc_19[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_20.length; i++){
            for(int j = 1; j < mc_20.length; j++){
                if (mc_20[i][j] == 0)
                    mc_20[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_21.length; i++){
            for(int j = 1; j < mc_21.length; j++){
                if (mc_21[i][j] == 0)
                    mc_21[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_22.length; i++){
            for(int j = 1; j < mc_22.length; j++){
                if (mc_22[i][j] == 0)
                    mc_22[i][j] = 0.00001;
            }
        }
        for(int i = 1; i < mc_23.length; i++){
            for(int j = 1; j < mc_23.length; j++){
                if (mc_23[i][j] == 0)
                    mc_23[i][j] = 0.00001;
            }
        }
    }

    private void assign_prob() {
        double sum_row = 0;

        for(int i = 1; i < mc_0.length; i++){
            for(int j = 1; j < mc_0.length; j++){
                sum_row = sum_row + mc_0[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_0.length; j++) {
                    mc_0[i][j] = mc_0[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_1.length; i++){
            for(int j = 1; j < mc_1.length; j++){
                sum_row = sum_row + mc_1[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_1.length; j++) {
                    mc_1[i][j] = mc_1[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_2.length; i++){
            for(int j = 1; j < mc_2.length; j++){
                sum_row = sum_row + mc_2[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_2.length; j++) {
                    mc_2[i][j] = mc_2[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_3.length; i++){
            for(int j = 1; j < mc_3.length; j++){
                sum_row = sum_row + mc_3[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_3.length; j++) {
                    mc_3[i][j] = mc_3[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_4.length; i++){
            for(int j = 1; j < mc_4.length; j++){
                sum_row = sum_row + mc_4[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_4.length; j++) {
                    mc_4[i][j] = mc_4[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_5.length; i++){
            for(int j = 1; j < mc_5.length; j++){
                sum_row = sum_row + mc_5[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_5.length; j++) {
                    mc_5[i][j] = mc_5[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_6.length; i++){
            for(int j = 1; j < mc_6.length; j++){
                sum_row = sum_row + mc_6[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_6.length; j++) {
                    mc_6[i][j] = mc_6[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_7.length; i++){
            for(int j = 1; j < mc_7.length; j++){
                sum_row = sum_row + mc_7[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_7.length; j++) {
                    mc_7[i][j] = mc_7[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_8.length; i++){
            for(int j = 1; j < mc_8.length; j++){
                sum_row = sum_row + mc_8[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_8.length; j++) {
                    mc_8[i][j] = mc_8[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_9.length; i++){
            for(int j = 1; j < mc_9.length; j++){
                sum_row = sum_row + mc_9[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_9.length; j++) {
                    mc_9[i][j] = mc_9[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_10.length; i++){
            for(int j = 1; j < mc_10.length; j++){
                sum_row = sum_row + mc_10[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_10.length; j++) {
                    mc_10[i][j] = mc_10[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_11.length; i++){
            for(int j = 1; j < mc_11.length; j++){
                sum_row = sum_row + mc_11[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_11.length; j++) {
                    mc_11[i][j] = mc_11[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_12.length; i++){
            for(int j = 1; j < mc_12.length; j++){
                sum_row = sum_row + mc_12[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_12.length; j++) {
                    mc_12[i][j] = mc_12[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_13.length; i++){
            for(int j = 1; j < mc_13.length; j++){
                sum_row = sum_row + mc_13[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_13.length; j++) {
                    mc_13[i][j] = mc_13[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_14.length; i++){
            for(int j = 1; j < mc_14.length; j++){
                sum_row = sum_row + mc_14[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_14.length; j++) {
                    mc_14[i][j] = mc_14[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_15.length; i++){
            for(int j = 1; j < mc_15.length; j++){
                sum_row = sum_row + mc_15[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_15.length; j++) {
                    mc_15[i][j] = mc_15[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_16.length; i++){
            for(int j = 1; j < mc_16.length; j++){
                sum_row = sum_row + mc_16[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_16.length; j++) {
                    mc_16[i][j] = mc_16[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_17.length; i++){
            for(int j = 1; j < mc_17.length; j++){
                sum_row = sum_row + mc_17[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_17.length; j++) {
                    mc_17[i][j] = mc_17[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_18.length; i++){
            for(int j = 1; j < mc_18.length; j++){
                sum_row = sum_row + mc_18[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_18.length; j++) {
                    mc_18[i][j] = mc_18[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_19.length; i++){
            for(int j = 1; j < mc_19.length; j++){
                sum_row = sum_row + mc_19[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_19.length; j++) {
                    mc_19[i][j] = mc_19[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_20.length; i++){
            for(int j = 1; j < mc_20.length; j++){
                sum_row = sum_row + mc_20[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_20.length; j++) {
                    mc_20[i][j] = mc_20[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_21.length; i++){
            for(int j = 1; j < mc_21.length; j++){
                sum_row = sum_row + mc_21[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_21.length; j++) {
                    mc_21[i][j] = mc_21[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_22.length; i++){
            for(int j = 1; j < mc_22.length; j++){
                sum_row = sum_row + mc_22[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_22.length; j++) {
                    mc_22[i][j] = mc_22[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
        for(int i = 1; i < mc_23.length; i++){
            for(int j = 1; j < mc_23.length; j++){
                sum_row = sum_row + mc_23[i][j];
            }
            if (sum_row != 0) {
                for (int j = 1; j < mc_23.length; j++) {
                    mc_23[i][j] = mc_23[i][j] / sum_row;
                }
            }
            sum_row = 0;
        }
    }

    private void create_day_markov_chain(List<hourlyweights> day_timeslotteddata) {
        double prob, row_stateid, col_stateid;
        for (int i = 0; i < 23; i++){
            for(int j = 0; j< day_timeslotteddata.size(); j++){
                row_stateid = Double.parseDouble(day_timeslotteddata.get(j).getStateid());
                for(int k=0; k< day_timeslotteddata.size(); k++){
                    col_stateid = Double.parseDouble(day_timeslotteddata.get(k).getStateid());
                    prob = Double.parseDouble(day_timeslotteddata.get(j).getiHour_w(i)) *
                            Double.parseDouble(day_timeslotteddata.get(k).getiHour_w(i+1));
                    int rowid = look_rowid(row_stateid);
                    int colid = look_colid(col_stateid);
                    if ((rowid != 0) && (colid != 0))
                        populate_cell_mc(i, rowid, colid, prob);
                }
            }
        }

        //for transition from 23-0 hour
        for(int j = 0; j< day_timeslotteddata.size(); j++){
            row_stateid = Double.parseDouble(day_timeslotteddata.get(j).getStateid());
            for(int k=0; k< day_timeslotteddata.size(); k++){
                col_stateid = Double.parseDouble(day_timeslotteddata.get(k).getStateid());
                prob = Double.parseDouble(day_timeslotteddata.get(j).getiHour_w(23)) *
                        Double.parseDouble(day_timeslotteddata.get(k).getiHour_w(0));
                int rowid = look_rowid(row_stateid);
                int colid = look_colid(col_stateid);
                if ((rowid != 0) && (colid != 0))
                    populate_cell_mc(23, rowid, colid, prob);
            }
        }

    }

    private void populate_cell_mc(int hour, int rowid, int colid, double prob) {
        if (hour == 0)
            mc_1[rowid][colid] = mc_1[rowid][colid] + prob;
        else if (hour == 1)
            mc_2[rowid][colid] = mc_2[rowid][colid] + prob;
        else if (hour == 2)
            mc_3[rowid][colid] = mc_3[rowid][colid] + prob;
        else if (hour == 3)
            mc_4[rowid][colid] = mc_4[rowid][colid] + prob;
        else if (hour == 4)
            mc_5[rowid][colid] = mc_5[rowid][colid] + prob;
        else if (hour == 5)
            mc_6[rowid][colid] = mc_6[rowid][colid] + prob;
        else if (hour == 6)
            mc_7[rowid][colid] = mc_7[rowid][colid] + prob;
        else if (hour == 7)
            mc_8[rowid][colid] = mc_8[rowid][colid] + prob;
        else if (hour == 8)
            mc_9[rowid][colid] = mc_9[rowid][colid] + prob;
        else if (hour == 9)
            mc_10[rowid][colid] = mc_10[rowid][colid] + prob;
        else if (hour == 10)
            mc_11[rowid][colid] = mc_11[rowid][colid] + prob;
        else if (hour == 11)
            mc_12[rowid][colid] = mc_12[rowid][colid] + prob;
        else if (hour == 12)
            mc_13[rowid][colid] = mc_13[rowid][colid] + prob;
        else if (hour == 13)
            mc_14[rowid][colid] = mc_14[rowid][colid] + prob;
        else if (hour == 14)
            mc_15[rowid][colid] = mc_15[rowid][colid] + prob;
        else if (hour == 15)
            mc_16[rowid][colid] = mc_16[rowid][colid] + prob;
        else if (hour == 16)
            mc_17[rowid][colid] = mc_17[rowid][colid] + prob;
        else if (hour == 17)
            mc_18[rowid][colid] = mc_18[rowid][colid] + prob;
        else if (hour == 18)
            mc_19[rowid][colid] = mc_19[rowid][colid] + prob;
        else if (hour == 19)
            mc_20[rowid][colid] = mc_20[rowid][colid] + prob;
        else if (hour == 20)
            mc_21[rowid][colid] = mc_21[rowid][colid] + prob;
        else if (hour == 21)
            mc_22[rowid][colid] = mc_22[rowid][colid] + prob;
        else if (hour == 22)
            mc_23[rowid][colid] = mc_23[rowid][colid] + prob;
        else
            mc_0[rowid][colid] = mc_0[rowid][colid] + prob;
    }

    private int look_colid(double col_stateid) {
        int colid = 0;
        for(int i = 1;i < mc_0.length;i++){
            if (mc_0[0][i] == col_stateid) {
                colid = i;
                break;
            }
        }
        return colid;
    }

    private int look_rowid(double row_stateid) {
        int rowid = 0;
        for(int i = 1;i < mc_0.length;i++){
            if (mc_0[i][0] == row_stateid) {
                rowid = i;
                break;
            }
        }
        return rowid;
    }

    //fetch address based on latitude and logitude info
    private String fetchaddress(double latitude, double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> address = geocoder.getFromLocation(latitude, longitude, 1);
        Address obj = address.get(0);
        String add = obj.getAddressLine(0);
        add = add + "," + obj.getAdminArea();
        add = add + "," + obj.getCountryName();
        return add;
    }

    public void openPredictions(View view) {
        Intent intent = new Intent(this, predictions.class);

        //send state list
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("state_list", state_list);
        intent.putExtras(bundle);

        //send markov chains
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_0", mc_0);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_1", mc_1);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_2", mc_2);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_3", mc_3);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_4", mc_4);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_5", mc_5);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_6", mc_6);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_7", mc_7);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_8", mc_8);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_9", mc_9);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_10", mc_10);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_11", mc_11);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_12", mc_12);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_13", mc_13);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_14", mc_14);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_15", mc_15);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_16", mc_16);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_17", mc_17);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_18", mc_18);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_19", mc_19);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_20", mc_20);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_21", mc_21);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_22", mc_22);
        intent.putExtras(bundle);
        bundle = new Bundle();
        bundle.putSerializable("markov_chain_23", mc_23);
        intent.putExtras(bundle);

        startActivity(intent);
    }

}
