package in.sportscult.sportscultapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.sportscult.sportscultapp.R;

public class ResultsFragment extends Fragment {

    private Spinner age_group_results;
    private String age_group;
    private final String[] age_group_codes = {"0","A","B","C","D"};
    private int selection_for_age_group = 1;
    private ArrayList<Results> arraylist_of_results;
    private Map<String,String> team_profile_pic_download_urls;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private ResultsListAdapter resultsListAdapter;
    private RecyclerView list_of_results;

    public ResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_results,container,false);

        age_group_results = (Spinner)view.findViewById(R.id.age_group_results);
        arraylist_of_results = new ArrayList<Results>();
        team_profile_pic_download_urls = new HashMap<String,String>();
        list_of_results = (RecyclerView)view.findViewById(R.id.list_of_results);

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_results.setAdapter(age_group_adapter);
        //The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the default value
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        selection_for_age_group = sharedPreferences.getInt("selection_for_age_group",1);

        age_group_results.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];
        Fetching_Results_From_Firebase();
        //Listening for change in age groups
        age_group_results.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0 && position!=selection_for_age_group){
                    selection_for_age_group = position;
                    //Add the selection_for_age_group to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("selection_for_age_group",selection_for_age_group);
                    editor.commit();
                    age_group = "Group - "+age_group_codes[position];
                    //Also add selection_for_age_group to Shared Preferences
                    Fetching_Results_From_Firebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    public void Fetching_Results_From_Firebase(){

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data....");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group);
        databaseReference.child("Results").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arraylist_of_results = new ArrayList<Results>();
                if(dataSnapshot==null){
                    progressDialog.dismiss();
                    resultsListAdapter = new ResultsListAdapter(getActivity(),arraylist_of_results,team_profile_pic_download_urls);
                    list_of_results.setAdapter(resultsListAdapter);
                    list_of_results.setLayoutManager(new LinearLayoutManager(getActivity()));
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();
                    Results results = new Results(map.get("Team A"),map.get("Team B"),map.get("Team A Goals")
                    ,map.get("Team B Goals"),map.get("Venue"),map.get("Time"));
                    arraylist_of_results.add(results);
                }
                databaseReference.child("Team Names").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        team_profile_pic_download_urls = new HashMap<String, String>();
                        for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()) {
                            Map<String, String> urlmap = (Map<String, String>) ChildSnapshot.getValue();
                            team_profile_pic_download_urls.put(ChildSnapshot.getKey(), urlmap.get("Team Profile Pic Thumbnail Url"));
                        }
                        progressDialog.dismiss();
                        //Configure Adapter for ListView
                        resultsListAdapter = new ResultsListAdapter(getActivity(),arraylist_of_results,team_profile_pic_download_urls);
                        list_of_results.setAdapter(resultsListAdapter);
                        list_of_results.setLayoutManager(new LinearLayoutManager(getActivity()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(),"Some Error Occurred",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),"Some Error Occurred",Toast.LENGTH_LONG).show();
            }
        });

    }

}

class Results{
    String TeamA,TeamB,TeamAGoals,TeamBGoals,Venue,Time;
    Results(String TeamA,String TeamB,String TeamAGoals,String TeamBGoals,String Venue,String Time){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.TeamAGoals = TeamAGoals;
        this.TeamBGoals = TeamBGoals;
        this.Venue = Venue;
        this.Time = Time;
    }
}

class ResultsListAdapter extends RecyclerView.Adapter<ResultsListAdapter.ViewHolder4>{

    ArrayList<Results> resultsArrayList;
    Map<String,String> map_for_team_profile_pic_download_urls;
    Context context;

    public ResultsListAdapter(Context context,ArrayList<Results> resultsArrayList,Map<String,String> map_for_team_profile_pic_download_urls){
        this.context = context;
        this.map_for_team_profile_pic_download_urls = map_for_team_profile_pic_download_urls;
        this.resultsArrayList = resultsArrayList;
    }

    @Override
    public ViewHolder4 onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.results_card,parent,false);
        ViewHolder4 viewHolder4 = new ViewHolder4(view);
        return viewHolder4;
    }

    @Override
    public void onBindViewHolder(ViewHolder4 viewHolder, int position) {
        Results data = resultsArrayList.get(position);
        viewHolder.teamA_name.setText(data.TeamA);
        viewHolder.teamB_name.setText(data.TeamB);
        viewHolder.live_match_score_A.setText(data.TeamAGoals);
        viewHolder.live_match_score_B.setText(data.TeamBGoals);

        final ImageView tempImageViewA = viewHolder.teamA_image;
        final ImageView tempImageViewB = viewHolder.teamB_image;
        final String urlA = map_for_team_profile_pic_download_urls.get(data.TeamA);
        final String urlB = map_for_team_profile_pic_download_urls.get(data.TeamB);
        //Load profile pic thumbnails
        Picasso.with(context)
                .load(urlA)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageViewA, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(urlA)
                                //.error(R.drawable.common_full_open_on_phone)
                                .into(tempImageViewA, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                    }
                                });
                    }
                });
        Picasso.with(context)
                .load(urlB)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageViewB, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(urlB)
                                //.error(R.drawable.common_full_open_on_phone)
                                .into(tempImageViewB, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                    }
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return resultsArrayList.size();
    }

    class ViewHolder4 extends RecyclerView.ViewHolder{
        TextView teamA_name,teamB_name,live_match_score_A,live_match_score_B;
        ImageView teamA_image,teamB_image;
        ViewHolder4(View view){
            super(view);
            teamA_name = (TextView)view.findViewById(R.id.teamA_name);
            teamB_name = (TextView)view.findViewById(R.id.teamB_name);
            live_match_score_A = (TextView)view.findViewById(R.id.scoreA);
            live_match_score_B = (TextView)view.findViewById(R.id.scoreB);
            teamA_image = (ImageView)view.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)view.findViewById(R.id.teamB_image);
        }
    }
}