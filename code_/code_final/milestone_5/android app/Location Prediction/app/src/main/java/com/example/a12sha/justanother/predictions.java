package com.example.a12sha.justanother;

import android.content.Intent;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class predictions extends AppCompatActivity {
    private Button paths_run;
    Spinner state_spinner;
    Spinner hour_spinner;
    Spinner conf_spinner;
    String selected_hour;
    String selected_state;
    int selected_conf;
    ArrayAdapter<CharSequence> adapter_hour;
    ArrayAdapter<CharSequence> adapter_confs;
    ArrayAdapter<String> adapter_state;
    public ArrayList<states> state_list = new ArrayList<>();
    public ArrayList<String> states_ids = new ArrayList<>();
    public ArrayList<qelement> paths = new ArrayList<>();
    public List<String> path_list = new ArrayList<>();

    double[][] mc_0;
    double[][] mc_1;
    double[][] mc_2;
    double[][] mc_3;
    double[][] mc_4;
    double[][] mc_5;
    double[][] mc_6;
    double[][] mc_7;
    double[][] mc_8;
    double[][] mc_9;
    double[][] mc_10;
    double[][] mc_11;
    double[][] mc_12;
    double[][] mc_13;
    double[][] mc_14;
    double[][] mc_15;
    double[][] mc_16;
    double[][] mc_17;
    double[][] mc_18;
    double[][] mc_19;
    double[][] mc_20;
    double[][] mc_21;
    double[][] mc_22;
    double[][] mc_23;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predictions);

        //fetch data from previous screen (mode, user, dates)
        fetch_prev_act_data();

        //state selection
        state_selection();

        //hour selection
        hour_selection();

        //hour selection
        conf_selection();

        paths_run = (Button) findViewById(R.id.button_paths);
        paths_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get new confidence value
                SeekBar simpleSeekBar=(SeekBar) findViewById(R.id.seekBar_conf); // initiate the Seekbar
                selected_conf = simpleSeekBar.getProgress();

                //clear path list and paths
                path_list.clear();
                paths.clear();

                // calculate new paths
                calc_paths();

                //display the address as a list
                display_list();
            }
        });
    }
    public void display_list(){
        ListView pathlist = (ListView) findViewById(R.id.pathlist);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, path_list){
            public Object getItem(int position)
            {
                return Html.fromHtml(path_list.get(position));
            }
        };
        pathlist.setAdapter(adapter);
    }
    public void calc_paths() {
        PriorityQueue<qelement> q_path = new PriorityQueue<>();

        //double conf = Double.parseDouble(selected_conf.replaceAll("%", "")) / 100;
        double conf = selected_conf/(double)100;

        //based on hour select the markov chain matrix
        int hour = Integer.parseInt(selected_hour);
        double[][] sel_mc;
        sel_mc = select_mc(hour);

        int nxt_hour = hour + 1;

        //look for the row based on the state id in selected markov chain
        double stateid = Double.parseDouble(selected_state);
        int row_id;
        row_id = look_rowid(stateid);

        for (int i = 1; i < sel_mc.length; i++) {
            q_path.add(new qelement(sel_mc[0][i], sel_mc[row_id][i], nxt_hour, stateid, 1.0, hour));
        }

        //Boolean allow_last = true;
        while (!q_path.isEmpty()) {
            qelement next_path = q_path.remove();
            if ((next_path.getProb() > conf)) {
                    //|| (allow_last == true)) {

                //if (next_path.getProb() <= conf)
                //    allow_last = false;
                int read_hour = next_path.hour;
                paths.add(next_path);

                if (read_hour == 23)
                    continue;
                sel_mc = select_mc(read_hour);
                row_id = look_rowid(next_path.stateid);

                //add the next row to the queue
                for (int i = 1; i < sel_mc.length; i++)
                    q_path.add(new qelement(sel_mc[0][i], sel_mc[row_id][i]*next_path.prob, read_hour + 1, next_path.stateid, next_path.prob, next_path.hour));


            }
        }

        //assign path ids
        if (paths.size() == 0)
            return;

        paths.get(0).setPath_id(1);

        for(int i = 1; i < paths.size(); i++){
            //continuation of a path
            if (paths.get(i).getHour() == paths.get(i-1).getHour() + 1 &&
                    paths.get(i).getFrom_state() == paths.get(i-1).getStateid() &&
                    paths.get(i).getFrom_hour() == paths.get(i-1).getHour() &&
                    paths.get(i).getFrom_prob() == paths.get(i-1).getProb())
                paths.get(i).setPath_id(paths.get(i-1).path_id);
            else{
                int pathid = 0;
                int row_count = 0;

                for(int j = 0; j < i; j++){
                    row_count = row_count + 1;
                    if (paths.get(j).getStateid() == paths.get(i).getFrom_state() &&
                            paths.get(j).getHour() == paths.get(i).getFrom_hour() &&
                            paths.get(j).getProb() == paths.get(i).getFrom_prob()){
                        pathid = paths.get(j).getPath_id();
                        break;
                    }
                }
                for(int j = 0; j < i ; j++){
                    if (paths.get(j).getFrom_state() == paths.get(i).getFrom_state() &&
                            paths.get(j).getFrom_hour() == paths.get(i).getFrom_hour() &&
                            paths.get(j).getFrom_prob() == paths.get(i).getFrom_prob()) {
                        pathid = 0;
                    }
                }

                int new_pathid;
                if (pathid != 0){
                    if (paths.get(row_count - 1).getPath_id() != paths.get(row_count).getPath_id())
                        paths.get(i).setPath_id(pathid);
                    else{
                        new_pathid = get_max_pathid(paths) + 1;
                        paths.get(i).setPath_id(new_pathid);
                    }
                } else {
                    new_pathid = get_max_pathid(paths) + 1;
                    paths.get(i).setPath_id(new_pathid);
                }
            }
        }
        String pathid_prob;
        int tot_paths = total_paths();

        //add parent to all the paths
        add_parent(tot_paths, hour);

        //sort paths based on keys: pathid, hour, probability
        sort_paths();

        //add trails to all the paths
        add_trails();

        //form path list for display
        for(int k = 1; k <= tot_paths; k++) {
            pathid_prob = "<h1><b>PATH:</b><br/></h1> ";
            for (int i = 0; i < paths.size(); i++) {
                if (paths.get(i).getPath_id() == k) {
                    int stateid_int = (int)paths.get(i).getStateid();
                    int color_rgb = (int)((1 - paths.get(i).getProb()) * 255);
                    String hex = String.format("#%02x%02x%02x", color_rgb, color_rgb, color_rgb);
                    pathid_prob = pathid_prob + " <b><font color=" +
                            hex  + "> " + (Integer.toString(stateid_int) + " </font></b>" +
                            //"</font>" + "<sup> <font color=\"Silver\">" + Integer.toString(paths.get(i).getHour()) + "</font> </sup>" +
                            //"<font color=\"Olive\">" + "<sub>" + "(" +
                            //String.format("%.2f", paths.get(i).getProb()) +
                            //")" + "</sub>" + "</font>" +
                            "<font color=\"Black\">" +"->" + "</font>");
                }
            }
            path_list.add(pathid_prob);
        }
    }

    private void add_trails() {

        // if paths is empty no trains can be added
        if (paths.size() == 0)
            return;

        ArrayList<qelement> temp_paths = new ArrayList<>();
        int prev_path = paths.get(0).getPath_id();
        double stateid;
        int hour;
        double prob;

        for (int i = 1; i < paths.size(); i++){
            if (paths.get(i).getPath_id() != prev_path ||
                    i == paths.size()-1 ){
                if (paths.get(i).getPath_id() != prev_path){
                    stateid = paths.get(i-1).getStateid();
                    hour = paths.get(i-1).getHour();
                    prob = paths.get(i-1).getProb();
                }
                else{
                    stateid = paths.get(i).getStateid();
                    hour = paths.get(i).getHour();
                    prob = paths.get(i).getProb();
                }

                int next_hour = hour + 1;

                if (hour != 23) {
                    PriorityQueue<qelement> q_path = new PriorityQueue<>();
                    double[][] sel_mc;
                    sel_mc = select_mc(hour);
                    int row_id;
                    row_id = look_rowid(stateid);

                    for (int j = 1; j < sel_mc.length; j++) {
                        q_path.add(new qelement(sel_mc[0][j], sel_mc[row_id][j] * prob, next_hour, stateid, prob, hour));
                    }

                    if (!q_path.isEmpty()) {
                        qelement next_path = q_path.remove();
                        next_path.setPath_id(prev_path);
                        temp_paths.add(next_path);
                    }
                }
                prev_path = paths.get(i).getPath_id();
            }
        }
        for (int i = 0; i < temp_paths.size(); i++)
            paths.add(temp_paths.get(i));
        sort_paths();
    }

    private void sort_paths() {

        //key 1 sort based on pathid
        for(int i = 0; i < paths.size(); i++){
            for(int j = 0;j < paths.size() -1; j++){
                if (paths.get(j).getPath_id() > paths.get(j+1).getPath_id()){
                    qelement temp = paths.get(j);
                    paths.set(j, paths.get(j+1));
                    paths.set(j+1, temp);
                }
            }
        }

        //key 2 sort based on hour
        for(int i = 0; i < paths.size(); i++){
            for(int j = 0;j < paths.size() -1; j++){
                if (paths.get(j).getHour() > paths.get(j+1).getHour() &&
                        paths.get(j).getPath_id() == paths.get(j+1).getPath_id()){
                    qelement temp = paths.get(j);
                    paths.set(j, paths.get(j+1));
                    paths.set(j+1, temp);
                }
            }
        }
    }


    private void add_parent(int tot_paths, int hour) {
        int k = 0;
        int[] hour_list = new int[23];
        int smallest_hour = 24;
        int smallest_hour_row = -1;
        qelement temp_qelement = new qelement(0, 0, 0, 0, 0, 0);

        hour_list = init_hourlist();
        for(int i = 1; i <= tot_paths; i++) {
            for (int j = 0; j < paths.size(); j++) {
                if (paths.get(j).getPath_id() == i) {
                    hour_list[k] = paths.get(j).getHour();
                    k = k + 1;
                    if (smallest_hour > paths.get(j).getHour()) {
                        smallest_hour_row = j;
                        smallest_hour = paths.get(j).getHour();
                    }
                }
            }
            int min_hour_row = smallest_hour_row;
            int min_hour = getMinValue(hour_list);
            while (min_hour != hour + 1) {
                for (int j = 0; j < paths.size(); j++) {
                    if (paths.get(j).getStateid() == paths.get(min_hour_row).getFrom_state() &&
                            paths.get(j).getHour() == paths.get(min_hour_row).getFrom_hour() &&
                            paths.get(j).getProb() == paths.get(min_hour_row).getFrom_prob()){
                        temp_qelement.setStateid(paths.get(j).getStateid());
                        temp_qelement.setProb(paths.get(j).getProb());
                        temp_qelement.setHour(paths.get(j).getHour());
                        temp_qelement.setFrom_state(paths.get(j).getFrom_state());
                        temp_qelement.setFrom_prob(paths.get(j).getFrom_prob());
                        temp_qelement.setFrom_hour(paths.get(j).getFrom_hour());
                        temp_qelement.setPath_id(i);
                        paths.add(temp_qelement);
                        min_hour = paths.get(j).getHour();
                        min_hour_row = j;
                        temp_qelement = new qelement(0, 0, 0, 0, 0, 0);
                        break;
                    }
                }
            }
            k = 0;
            hour_list = init_hourlist();
            smallest_hour = 24;
            smallest_hour_row = -1;
        }
    }

    private int[] init_hourlist() {
        int[] hour_list = new int[23];
        // /initialize hourlist with more than max possible 24 Hrs
        for(int i = 0; i < hour_list.length; i++){
            hour_list[i] = 24;
        }
        return hour_list;
    }


    private int getMinValue(int[] hour_list) {
        int min_hour = 24;
        for(int i = 0; i < hour_list.length; i++){
            if (min_hour > hour_list[i])
                min_hour = hour_list[i];
        }
        return min_hour;
    }

    private void find_parent() {

    }

    private int total_paths() {
        int tot_paths;
        ArrayList<String> path_ids = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            path_ids.add(Integer.toString(paths.get(i).getPath_id()));
        }

        HashSet<String> noDupstateIds = new HashSet<String>();
        noDupstateIds.addAll(path_ids);
        tot_paths = noDupstateIds.size();
        return  tot_paths;
    }

    private int get_max_pathid(ArrayList<qelement> paths) {
        int max_id = paths.get(0).getPath_id();
        for(int i = 1; i < paths.size(); i++){
            if (paths.get(i).getPath_id() > max_id)
                max_id = paths.get(i).getPath_id();
        }
        return max_id;
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

    private double[][] select_mc(int hour) {
        double[][] sel_mc;
        if (hour == 0)
            sel_mc = mc_1;
        else if (hour == 1)
            sel_mc = mc_2;
        else if (hour == 2)
            sel_mc = mc_3;
        else if (hour == 3)
            sel_mc = mc_4;
        else if (hour == 4)
            sel_mc = mc_5;
        else if (hour == 5)
            sel_mc = mc_6;
        else if (hour == 6)
            sel_mc = mc_7;
        else if (hour == 7)
            sel_mc = mc_8;
        else if (hour == 8)
            sel_mc = mc_9;
        else if (hour == 9)
            sel_mc = mc_10;
        else if (hour == 10)
            sel_mc = mc_11;
        else if (hour == 11)
            sel_mc = mc_12;
        else if (hour == 12)
            sel_mc = mc_13;
        else if (hour == 13)
            sel_mc = mc_14;
        else if (hour == 14)
            sel_mc = mc_15;
        else if (hour == 15)
            sel_mc = mc_16;
        else if (hour == 16)
            sel_mc = mc_17;
        else if (hour == 17)
            sel_mc = mc_18;
        else if (hour == 18)
            sel_mc = mc_19;
        else if (hour == 19)
            sel_mc = mc_20;
        else if (hour == 20)
            sel_mc = mc_21;
        else if (hour == 21)
            sel_mc = mc_22;
        else if (hour == 22)
            sel_mc = mc_23;
        else
            sel_mc = mc_0;
        return sel_mc;
    }

    private void conf_selection() {
        /*conf_spinner = (Spinner) findViewById(R.id.spinner_confs);
        adapter_confs = ArrayAdapter.createFromResource(this, R.array.confs, android.R.layout.simple_spinner_item);
        adapter_confs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conf_spinner.setAdapter(adapter_confs);
        conf_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_conf = conf_spinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), selected_conf, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
        SeekBar simpleSeekBar=(SeekBar) findViewById(R.id.seekBar_conf); // initiate the Seekbar
        simpleSeekBar.setMax(100);
        simpleSeekBar.setProgress(10);
        selected_conf = simpleSeekBar.getProgress();
    }

    private void state_selection() {

        for(int i = 0; i < state_list.size(); i++){
            states_ids.add(Double.toString(state_list.get(i).getStateid()));
        }

        state_spinner = (Spinner) findViewById(R.id.spinner_state);
        adapter_state = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        states_ids);
        adapter_state.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state_spinner.setAdapter(adapter_state);
        state_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_state = state_spinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), selected_state, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void hour_selection() {
        hour_spinner = (Spinner) findViewById(R.id.spinner_hour);
        adapter_hour = ArrayAdapter.createFromResource(this, R.array.hours, android.R.layout.simple_spinner_item);
        adapter_hour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hour_spinner.setAdapter(adapter_hour);
        hour_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_hour = hour_spinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), selected_hour, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void fetch_prev_act_data() {
        Intent intent = getIntent();
        Bundle bundle = getIntent().getExtras();
        state_list = bundle.getParcelableArrayList("state_list");

        Object[] objectArray;

        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_0");
        if(objectArray!=null){
            mc_0 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_0[i]= (double[]) objectArray[i];
            }
        }

        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_1");
        if(objectArray!=null){
            mc_1 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_1[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_2");
        if(objectArray!=null){
            mc_2 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_2[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_3");
        if(objectArray!=null){
            mc_3 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_3[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_4");
        if(objectArray!=null){
            mc_4 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_4[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_5");
        if(objectArray!=null){
            mc_5 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_5[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_6");
        if(objectArray!=null){
            mc_6 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_6[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_7");
        if(objectArray!=null){
            mc_7 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_7[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_8");
        if(objectArray!=null){
            mc_8 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_8[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_9");
        if(objectArray!=null){
            mc_9 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_9[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_10");
        if(objectArray!=null){
            mc_10 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_10[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_11");
        if(objectArray!=null){
            mc_11 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_11[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_12");
        if(objectArray!=null){
            mc_12 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_12[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_13");
        if(objectArray!=null){
            mc_13 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_13[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_14");
        if(objectArray!=null){
            mc_14 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_14[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_15");
        if(objectArray!=null){
            mc_15 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_15[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_16");
        if(objectArray!=null){
            mc_16 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_16[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_17");
        if(objectArray!=null){
            mc_17 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_17[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_18");
        if(objectArray!=null){
            mc_18 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_18[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_19");
        if(objectArray!=null){
            mc_19 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_19[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_20");
        if(objectArray!=null){
            mc_20 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_20[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_21");
        if(objectArray!=null){
            mc_21 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_21[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_22");
        if(objectArray!=null){
            mc_22 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_22[i]= (double[]) objectArray[i];
            }
        }
        objectArray = (Object[]) intent.getExtras().getSerializable("markov_chain_23");
        if(objectArray!=null){
            mc_23 = new double[objectArray.length][];
            for(int i=0;i<objectArray.length;i++){
                mc_23[i]= (double[]) objectArray[i];
            }
        }
    }
}
