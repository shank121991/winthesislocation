package com.example.a12sha.justanother;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.service.autofill.UserData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import java.lang.Math.*;

public class staypoints_list extends AppCompatActivity {

    //variables
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");
    private String mode_sel;
    private String user_sel;
    private String train_from_date_sel;
    private String train_to_date_sel;
    private String test_from_date_sel;
    private String test_to_date_sel;
    private List<userLocData> userlocdata= new ArrayList<>();
    private List<staypoint> train_userstaypts = new ArrayList<>();
    private List<staypoint> test_userstaypts = new ArrayList<>();
    private List<String> userstayptsaddr = new ArrayList<>();
    private List<staypoint> tempstaypts = new ArrayList<>();
    private double d_thrshld_staypts = 200; //200 meters
    private double t_thrshld_staypts = 20; // 20 minutes
    private double d_thrshld_state = 200; //200 meters
    private double t_thrshld_track = 30; //30 minutes
    private int stayptid = 1; // staypoint staring id
    private int staypoint_curr_count = 0; // staypoint staring id 0=for no staypoints, -2 for start end 2 staypoints

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TRAIN_STAYPOINTS = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staypoints_list);

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
                userstayptsaddr.add("Error: Location is turned off. Update location settings for the app.");
            else {
                try {
                    userstayptsaddr.add(fetchaddress(latitude, longitude));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //display the staypoints as a list
        ListView staypointslist = (ListView) findViewById(R.id._staypointslist);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, userstayptsaddr);
        staypointslist.setAdapter(adapter);

        for (int i=1; i<userstayptsaddr.size(); i++){
            Log.d("MyActivity", "Staypoints: Location " + userstayptsaddr.get(i));
        }

    }

    private void fetch_prev_act_data() {
        Intent intent = getIntent();
        //fetch selected mode
        mode_sel = intent.getStringExtra("mode_selected");
        //fetch user selected
        user_sel = intent.getStringExtra("user_selected");
        //fetch dates
        train_from_date_sel =  intent.getStringExtra("train_from_date_selected");
        train_to_date_sel =  intent.getStringExtra("train_to_date_selected");
        test_from_date_sel =  intent.getStringExtra("test_from_date_selected");
        test_to_date_sel =  intent.getStringExtra("test_to_date_selected");
    }

    //read user plt file in an online fashion
    public void process_user_data() throws IOException, ParseException {
        //process training data and create model
        user_sel = String.format("%03d", Integer.parseInt(user_sel));
        Date from_date = null;
        Date to_date = null;
        Date file_date = null;
        try {
            from_date = new SimpleDateFormat("dd/MM/yyyy").parse(train_from_date_sel);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            to_date = new SimpleDateFormat("dd/MM/yyyy").parse(train_to_date_sel);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //String path = "Geolife Trajectories 1.3/Data/" + user_sel + "/Trajectory/";
        String path = "Geolife Trajectories 1.3/SampledData/" + user_sel + "/";
        AssetManager am = getAssets();
        String[] files = am.list(path);
        List<String> filelist = new LinkedList<String>(Arrays.asList(files));

        for (int i=0; i< filelist.size();i++){
            try {
                file_date = new SimpleDateFormat("yyyyMMdd").parse(filelist.get(i).substring(0,8));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //process the file if the date is between from_date and to_date
            if (!from_date.after(file_date) && !to_date.before(file_date)){
                String file_read = path + filelist.get(i);
                String train_test = "train";
                process_file(file_read, train_test);
            }
        }

        //fetch staypoints addresses
        int prev_stayptid = train_userstaypts.get(0).getStayptId();

        userstayptsaddr.add(fetchaddress(train_userstaypts.get(0).getStaylatmean(), train_userstaypts.get(0).getStaylonmean()));
        int j = 1;
        for (int i=1; i< train_userstaypts.size(); i++) {
            if (i == train_userstaypts.size() -1){
                userstayptsaddr.add(fetchaddress(train_userstaypts.get(i).getStaylatmean(), train_userstaypts.get(i).getStaylonmean()));
            }
            if (train_userstaypts.get(i).getStayptId() != prev_stayptid){
                userstayptsaddr.add(fetchaddress(train_userstaypts.get(i).getStaylatmean(), train_userstaypts.get(i).getStaylonmean()));
                prev_stayptid = train_userstaypts.get(i).getStayptId();
            }
        }

        //Writer output = null;
        //File file = new File("C:\\Users\\12sha\\Documents\\thesislocation\\shashank.txt");
        //output = new BufferedWriter(new FileWriter(file));

        //for(int i=0; i<100; i++){
        //    output.write("Hello");
        //}

        //output.close();
        //System.out.println("Stypoint file has been written");

        find_states();
        //calculate_hourly_weights();
        //create_markov_chain();
    }

    //process user file
    private void process_file(String file_read, String train_test) throws IOException, ParseException {
        AssetManager am = getAssets();
        InputStream raw = am.open(file_read);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(raw, Charset.forName("UTF-8"))
        );

        String line = "";
        //skip first 6 lines
        //for (int i = 0; i<= 5; i++){
        //    reader.readLine();
        //}
        reader.readLine();

        while (( line = reader.readLine()) != null){
            //split by comma
            //String[] token = line.split(",");
            String[] token = line.split("\t");
            userLocData sample = new userLocData();
            sample.setLatitude(token[1]);
            sample.setLongitude(token[2]);
            sample.setDate(token[6]);
            sample.setTime(token[7]);
            userlocdata.add(sample);

            try {
                staypoint_curr_count = 0;
                findStayPoints(token[1], token[2], token[6], token[7], train_test);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (staypoint_curr_count != 0){
                add_start_end_times();
            }
        }
    }

    private void add_start_end_times() throws ParseException {
        staypoint check_staypoint1 = new staypoint();
        staypoint check_staypoint2 = new staypoint();
        staypoint temp_staypoint = new staypoint();
        Integer staypoint_tot_count = train_userstaypts.size();

        if (staypoint_curr_count != 0 && staypoint_curr_count != -2) {
            check_staypoint1 = train_userstaypts.get(train_userstaypts.size()-staypoint_curr_count-1);
            check_staypoint2 = train_userstaypts.get(train_userstaypts.size()-staypoint_curr_count);

            String date = check_staypoint1.getDat();
            String time = check_staypoint1.getTim();
            String end1_trj_time = date + " " + time;
            double end1_trj_lat = check_staypoint1.getStaylatmean();
            double end1_trj_lon = check_staypoint1.getStaylonmean();
            date = check_staypoint2.getDat();
            time = check_staypoint2.getTim();
            String str2_trj_time = date +  " " + time;
            double str2_trj_lat = check_staypoint2.getStaylatmean();
            double str2_trj_lon = check_staypoint2.getStaylonmean();
            String[] start_end_time = new String[2];

            start_end_time = find_added_time(end1_trj_lat, end1_trj_lon, str2_trj_lat, str2_trj_lon, end1_trj_time, str2_trj_time);
            end1_trj_time = start_end_time[0];
            str2_trj_time = start_end_time[1];

            //add new line for staypoint 1 with new end time
            temp_staypoint = new staypoint();
            date = end1_trj_time.substring(0, 10);
            time = end1_trj_time.substring(11);
            temp_staypoint.setTim(time);
            temp_staypoint.setDat(date);
            temp_staypoint.setLat(check_staypoint1.getLat());
            temp_staypoint.setLon(check_staypoint1.getLon());
            temp_staypoint.setStayptId(check_staypoint1.getStayptId());
            temp_staypoint.setStaylatmean(check_staypoint1.getStaylatmean());
            temp_staypoint.setStaylonmean(check_staypoint1.getStaylonmean());
            train_userstaypts.add(temp_staypoint);

            //add new line for staypoint 2 with new start time
            temp_staypoint = new staypoint();
            date = str2_trj_time.substring(0, 10);
            time = str2_trj_time.substring(11);
            temp_staypoint.setTim(time);
            temp_staypoint.setDat(date);
            temp_staypoint.setLat(check_staypoint2.getLat());
            temp_staypoint.setLon(check_staypoint2.getLon());
            temp_staypoint.setStayptId(check_staypoint2.getStayptId());
            temp_staypoint.setStaylatmean(check_staypoint2.getStaylatmean());
            temp_staypoint.setStaylonmean(check_staypoint2.getStaylonmean());
            train_userstaypts.add(temp_staypoint);
        }
        if (staypoint_curr_count == -2) {
            check_staypoint1 = train_userstaypts.get(train_userstaypts.size()-2);
            check_staypoint2 = train_userstaypts.get(train_userstaypts.size()-1);

            String date = check_staypoint1.getDat();
            String time = check_staypoint1.getTim();
            String end1_trj_time = date + " " + time;
            double end1_trj_lat = check_staypoint1.getStaylatmean();
            double end1_trj_lon = check_staypoint1.getStaylonmean();
            date = check_staypoint2.getDat();
            time = check_staypoint2.getTim();
            String str2_trj_time = date +  " " + time;
            double str2_trj_lat = check_staypoint2.getStaylatmean();
            double str2_trj_lon = check_staypoint2.getStaylonmean();

            String [] start_end_time = find_added_time(end1_trj_lat, end1_trj_lon, str2_trj_lat, str2_trj_lon, end1_trj_time, str2_trj_time);
            end1_trj_time = start_end_time[0];
            str2_trj_time = start_end_time[1];

            //add new line for staypoint 1 with new end time
            temp_staypoint = new staypoint();
            date = end1_trj_time.substring(0, 10);
            time = end1_trj_time.substring(11);
            temp_staypoint.setTim(time);
            temp_staypoint.setDat(date);
            temp_staypoint.setLat(check_staypoint1.getLat());
            temp_staypoint.setLon(check_staypoint1.getLon());
            temp_staypoint.setStayptId(check_staypoint1.getStayptId());
            temp_staypoint.setStaylatmean(check_staypoint1.getStaylatmean());
            temp_staypoint.setStaylonmean(check_staypoint1.getStaylonmean());
            train_userstaypts.add(temp_staypoint);

            //add new line for staypoint 2 with new start time
            temp_staypoint = new staypoint();
            date = str2_trj_time.substring(0, 10);
            time = str2_trj_time.substring(11);
            temp_staypoint.setTim(time);
            temp_staypoint.setDat(date);
            temp_staypoint.setLat(check_staypoint2.getLat());
            temp_staypoint.setLon(check_staypoint2.getLon());
            temp_staypoint.setStayptId(check_staypoint2.getStayptId());
            temp_staypoint.setStaylatmean(check_staypoint2.getStaylatmean());
            temp_staypoint.setStaylonmean(check_staypoint2.getStaylonmean());
            train_userstaypts.add(temp_staypoint);

            if (staypoint_tot_count >= 3){
                check_staypoint1 = train_userstaypts.get(staypoint_tot_count-3);
                check_staypoint2 = train_userstaypts.get(staypoint_tot_count-2);

                date = check_staypoint1.getDat();
                time = check_staypoint1.getTim();
                end1_trj_time = date + " " + time;
                end1_trj_lat = check_staypoint2.getStatelatmean();
                end1_trj_lon = check_staypoint2.getStaylonmean();
                date = check_staypoint2.getDat();
                time = check_staypoint2.getTim();
                str2_trj_time = date +  " " + time;
                str2_trj_lat = check_staypoint1.getStatelatmean();
                str2_trj_lon = check_staypoint1.getStaylonmean();

                start_end_time = find_added_time(end1_trj_lat, end1_trj_lon, str2_trj_lat, str2_trj_lon, end1_trj_time, str2_trj_time);
                end1_trj_time = start_end_time[0];
                str2_trj_time = start_end_time[1];

                //add new line for staypoint 1 with new end time
                temp_staypoint = new staypoint();
                date = end1_trj_time.substring(0, 10);
                time = end1_trj_time.substring(11);
                temp_staypoint.setTim(time);
                temp_staypoint.setDat(date);
                temp_staypoint.setLat(check_staypoint1.getLat());
                temp_staypoint.setLon(check_staypoint1.getLon());
                temp_staypoint.setStayptId(check_staypoint1.getStayptId());
                temp_staypoint.setStaylatmean(check_staypoint1.getStaylatmean());
                temp_staypoint.setStaylonmean(check_staypoint1.getStaylonmean());
                train_userstaypts.add(temp_staypoint);

                //add new line for staypoint 2 with new start time
                temp_staypoint = new staypoint();
                date = str2_trj_time.substring(0, 10);
                time = str2_trj_time.substring(11);
                temp_staypoint.setTim(time);
                temp_staypoint.setDat(date);
                temp_staypoint.setLat(check_staypoint2.getLat());
                temp_staypoint.setLon(check_staypoint2.getLon());
                temp_staypoint.setStayptId(check_staypoint2.getStayptId());
                temp_staypoint.setStaylatmean(check_staypoint2.getStaylatmean());
                temp_staypoint.setStaylonmean(check_staypoint2.getStaylonmean());
                train_userstaypts.add(temp_staypoint);
            }

        }
        //Sort staypoints based on stayids and timestamps
        order(train_userstaypts);
    }

    private static void order(List<staypoint> train_userstaypts) {

        Collections.sort(train_userstaypts, new Comparator() {

            public int compare(Object o1, Object o2) {

                Integer x1 = ((staypoint) o1).getStayptId();
                Integer x2 = ((staypoint) o2).getStayptId();
                int sComp = x1.compareTo(x2);

                if (sComp != 0) {
                    return sComp;
                }

                String x3 = ((staypoint) o1).getTim() + ((staypoint) o1).getDat() ;
                String x4 = ((staypoint) o2).getTim() + ((staypoint) o1).getDat() ;
                return x3.compareTo(x4);
            }});
    }

    private String[] find_added_time(double end1_trj_lat, double end1_trj_lon, double str2_trj_lat, double str2_trj_lon, String end1_trj_time, String str2_trj_time) throws ParseException {
        double dist_btw = meters(end1_trj_lat, end1_trj_lon, str2_trj_lat, str2_trj_lon);
        double time_btw = ((sdf.parse(str2_trj_time).getTime() - sdf.parse(end1_trj_time).getTime()) / 1000)/60;
        double delta_t;
        String[] start_end_time = new String[2];
        start_end_time[0] = null;
        start_end_time[1] = null;

        if (time_btw != 0) {
            double avg_speed = dist_btw / time_btw;
            if (avg_speed != 0) {
                if (dist_btw >= 2 * d_thrshld_staypts) {
                    delta_t = Math.min(d_thrshld_staypts, dist_btw) / avg_speed;
                } else {
                    delta_t = dist_btw / (2 * avg_speed);
                }
            } else {
                delta_t = time_btw / 2;
            }

            Date end1_trj_time_t = sdf.parse(end1_trj_time);
            Date str2_trj_time_t = sdf.parse(str2_trj_time);
            Calendar cal = Calendar.getInstance();

            cal.setTime(end1_trj_time_t);
            cal.add(Calendar.MINUTE, (int) delta_t);
            start_end_time[0] = sdf.format(cal.getTime());

            cal.setTime(str2_trj_time_t);
            cal.add(Calendar.MINUTE, (int) -delta_t);
            start_end_time[1] = sdf.format(cal.getTime());
        }
        return start_end_time;
    }

    //find staypoints from user files
    private void findStayPoints(String latidude_s, String longitude_s, String date_s, String time_s, String train_test) throws ParseException {
        staypoint newpoint = new staypoint();
        staypoint prevstaypt = new staypoint();
        staypoint firststayptincluster = new staypoint();

        //convert strings to double
        double latitude_d = Double.parseDouble(latidude_s);
        double longitude_d = Double.parseDouble(longitude_s);

        //assign new point
        newpoint.setLat(latitude_d);
        newpoint.setLon(longitude_d);
        newpoint.setDat(date_s);
        newpoint.setTim(time_s);

        //if this is the first point, add this as staypoint
        if (tempstaypts!= null && tempstaypts.isEmpty()){
            newpoint.setStayptId(stayptid);
            stayptid = stayptid + 1;
            newpoint.setStaylatmean(latitude_d);
            newpoint.setStaylonmean(longitude_d);
            tempstaypts.add(newpoint);
            }
            //from 2nd points onwards
            else {
            prevstaypt = tempstaypts.get(tempstaypts.size()-1);
            int prevstayid = prevstaypt.getStayptId();
            double prevlat = prevstaypt.getStaylatmean();
            double prevlon = prevstaypt.getStaylonmean();
            String prevlocdatetime = prevstaypt.getDat() + " " + prevstaypt.getTim();
            String newlocdatetime = newpoint.getDat() + " " + newpoint.getTim();

            Date datetime_prev = sdf.parse(prevlocdatetime);
            Date datetime_new = sdf.parse(newlocdatetime);

            int time_diff = minutes(datetime_prev, datetime_new);
            //if the two points are more than track threshold apart, add both as staypoints
            if (time_diff>= t_thrshld_track){
                newpoint.setStayptId(stayptid);
                stayptid = stayptid + 1;
                newpoint.setStaylatmean(latitude_d);
                newpoint.setStaylonmean(longitude_d);
                tempstaypts.add(newpoint);
                if (train_test.equals("train")) {
                    train_userstaypts.add(tempstaypts.get(tempstaypts.size() - 2));
                    train_userstaypts.add(tempstaypts.get(tempstaypts.size() - 1));
                    staypoint_curr_count = -2;
                }
                tempstaypts.clear();
            }
            else {
                double dist_m = meters(latitude_d, longitude_d, prevlat, prevlon);
                //if the distance between the new point and the last point in the cluster is less than threshold then add it in the
                // same cluster
                if (dist_m <= d_thrshld_staypts) {
                    newpoint.setStayptId(prevstayid);
                    tempstaypts.add(newpoint);

                    double sum_lat = 0;
                    double sum_lon = 0;
                    for (int i = 0; i < tempstaypts.size(); i++){
                        sum_lat = sum_lat + tempstaypts.get(i).getLat();
                        sum_lon = sum_lon + tempstaypts.get(i).getLon();
                    }
                    double mean_lat = sum_lat / tempstaypts.size();
                    double mean_lon = sum_lon / tempstaypts.size();

                    for (int i = 0; i < tempstaypts.size(); i++) {
                        tempstaypts.get(i).setStaylatmean(mean_lat);
                        tempstaypts.get(i).setStaylonmean(mean_lon);
                    }
                }
                //if the new point distance is more than threshold from the last point, it means it is moving away
                // check the previous cluster duration , and if it it more than threshold add it as a staypoint
                else {
                    stayptid = stayptid + 1;
                    firststayptincluster = tempstaypts.get(0);
                    String datetime_start = firststayptincluster.getDat() + " " + firststayptincluster.getTim();
                    String datetime_end = prevstaypt.getDat() + " " + prevstaypt.getTim();
                    Date datetime_start_d = sdf.parse(datetime_start);
                    Date datetime_end_d = sdf.parse(datetime_end);
                    int time_m = minutes(datetime_start_d, datetime_end_d);

                    if (time_m-1 >= t_thrshld_staypts) {
                        for (int i = 0; i < tempstaypts.size(); i++) {
                            if (train_test.equals("train")) {
                                train_userstaypts.add(tempstaypts.get(i));
                                staypoint_curr_count = staypoint_curr_count + 1;
                            }
                        }
                    }
                    tempstaypts.clear();
                    newpoint.setStayptId(stayptid);
                    stayptid = stayptid + 1;
                    newpoint.setStaylatmean(latitude_d);
                    newpoint.setStaylonmean(longitude_d);
                    tempstaypts.add(newpoint);
                }
            }
        }
    }

    //create states from statypoints
    private void find_states() {

    }

    //distance in meters between two coordinates
    private double meters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6378.137;
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000;
    }

    //difference in minnutes between two datetimes
    private int minutes(Date date1, Date date2){
        if (date1 == null || date2 == null) return 0;

        return (int)((date2.getTime()/60000) - (date1.getTime()/60000));
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
        startActivity(new Intent(this, predictions.class));
    }

}
