package in.sportscult.sportscultapp.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import in.sportscult.sportscultapp.DetailedMatchDescription;
import in.sportscult.sportscultapp.R;
import in.sportscult.sportscultapp.RecyclerItemClickListener;
import in.sportscult.sportscultapp.TeamDescriprion;

/**
 * Results Fragment
 * this fragment displays the results in a recycler view
 */
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
    private static TextView display_on_empty_results;

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
        display_on_empty_results = (TextView)view.findViewById(R.id.display_on_empty_results);

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_results.setAdapter(age_group_adapter);
        //The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the time_default value
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        selection_for_age_group = sharedPreferences.getInt("selection_for_age_group",1);

        age_group_results.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];

        resultsListAdapter = new ResultsListAdapter(getActivity(),arraylist_of_results,team_profile_pic_download_urls,age_group);
        //Attaches the Adapter to list
        list_of_results.setAdapter(resultsListAdapter);
        list_of_results.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Sets onCLickListener to List items
        list_of_results.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), list_of_results, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //Pass Activity name, age group and ID to detailes match description and launch the  activity
                Intent intent = new Intent(getActivity(), DetailedMatchDescription.class);
                intent.putExtra("Activity Name","Results");
                intent.putExtra("Age Group",age_group);
                intent.putExtra("Match ID",arraylist_of_results.get(position).Key);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

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
                    ArrayListEmpty();
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();
                    Results results = new Results(childSnapshot.getKey(),map.get("Team A"),map.get("Team B"),map.get("Team A Goals")
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
                        progressDialog.dismiss();//Configure ListView Adapter
                        if(arraylist_of_results.size()==0)
                            ArrayListEmpty();
                        else {
                            list_of_results.setAdapter(new ResultsListAdapter(getActivity(),arraylist_of_results,team_profile_pic_download_urls,age_group));
                            ArrayListNotEmpty();
                        }

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

    private void ArrayListEmpty(){
        list_of_results.setVisibility(View.GONE);
        display_on_empty_results.setVisibility(View.VISIBLE);
    }
    private void ArrayListNotEmpty(){
        list_of_results.setVisibility(View.VISIBLE);
        display_on_empty_results.setVisibility(View.GONE);
    }
}

/**
 * The Results class which holds details of the Team
 */
class Results{
    String Key,TeamA,TeamB,TeamAGoals,TeamBGoals,Venue,Time;

    /**
     * @param Key Firebase Key to reference the record
     * @param TeamA Name of Team A
     * @param TeamB Name of Team B
     * @param TeamAGoals Goals of Team A
     * @param TeamBGoals Goals of Team B
     * @param Venue Venue of the match
     * @param Time Time of the match
     */
    Results(String Key,String TeamA,String TeamB,String TeamAGoals,String TeamBGoals,String Venue,String Time){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.TeamAGoals = TeamAGoals;
        this.TeamBGoals = TeamBGoals;
        this.Venue = Venue;
        this.Time = Time;
        this.Key = Key;
    }
}

/**
 * Adapter for RESULT list
 */
class ResultsListAdapter extends RecyclerView.Adapter<ResultsListAdapter.ViewHolder4>{

    ArrayList<Results> resultsArrayList;
    Map<String,String> map_for_team_profile_pic_download_urls;
    Context context;
    String AGEGROUP;

    public ResultsListAdapter(Context context,ArrayList<Results> resultsArrayList,Map<String,String> map_for_team_profile_pic_download_urls,String AGEGROUP){
        this.context = context;
        this.map_for_team_profile_pic_download_urls = map_for_team_profile_pic_download_urls;
        this.resultsArrayList = resultsArrayList;
        this.AGEGROUP = AGEGROUP;
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

        //Set Text Color For Scores
        int AColor,BColor;
        if(Integer.parseInt(data.TeamAGoals)>Integer.parseInt(data.TeamBGoals)){
            AColor = context.getResources().getColor(R.color.winning_color);
            BColor = context.getResources().getColor(R.color.loosing_color);
        }
        else if(Integer.parseInt(data.TeamAGoals)<Integer.parseInt(data.TeamBGoals)){
            BColor = context.getResources().getColor(R.color.winning_color);
            AColor = context.getResources().getColor(R.color.loosing_color);
        }
        else{
            BColor = context.getResources().getColor(R.color.draw_color);
            AColor = context.getResources().getColor(R.color.draw_color);
        }
        viewHolder.live_match_score_A.setTextColor(AColor);
        viewHolder.live_match_score_B.setTextColor(BColor);

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
        ViewCompat.setTransitionName(viewHolder.teamA_image,resultsArrayList.get(position).TeamA);
        ViewCompat.setTransitionName(viewHolder.teamB_image,resultsArrayList.get(position).TeamB);

        ViewCompat.setTransitionName(viewHolder.teamA_name,resultsArrayList.get(position).TeamA+"_");
        ViewCompat.setTransitionName(viewHolder.teamB_name,resultsArrayList.get(position).TeamA+"_");

    }

    @Override
    public int getItemCount() {
        return resultsArrayList.size();
    }

    class ViewHolder4 extends RecyclerView.ViewHolder{
        TextView teamA_name,teamB_name,live_match_score_A,live_match_score_B;
        ImageView teamA_image,teamB_image;

        LinearLayout include3,include4;
        ViewHolder4(View view){
            super(view);
            teamA_name = (TextView)view.findViewById(R.id.teamA_name);
            teamB_name = (TextView)view.findViewById(R.id.teamB_name);
            live_match_score_A = (TextView)view.findViewById(R.id.scoreA);
            live_match_score_B = (TextView)view.findViewById(R.id.scoreB);
            teamA_image = (ImageView)view.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)view.findViewById(R.id.teamB_image);

            include3 = (LinearLayout)view.findViewById(R.id.include3);
            teamA_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, TeamDescriprion.class);
                    intent.putExtra("EXTRA_TRANSITION_NAME", ViewCompat.getTransitionName(teamA_image));
                    intent.putExtra("EXTRA_TRANSITION_NAME_", ViewCompat.getTransitionName(teamA_name));
                    intent.putExtra("Team Name",resultsArrayList.get(getPosition()).TeamA);
                    intent.putExtra("Age Group",AGEGROUP);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity) context,
                            teamA_image,
                            ViewCompat.getTransitionName(teamA_image));

                    context.startActivity(intent, options.toBundle());
                }
            });
            include4 = (LinearLayout) view.findViewById(R.id.include4);
            teamB_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,TeamDescriprion.class);
                    intent.putExtra("EXTRA_TRANSITION_NAME", ViewCompat.getTransitionName(teamB_image));
                    intent.putExtra("EXTRA_TRANSITION_NAME_", ViewCompat.getTransitionName(teamB_name));
                    intent.putExtra("Team Name",resultsArrayList.get(getPosition()).TeamB);
                    intent.putExtra("Age Group",AGEGROUP);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity) context,
                            teamB_image,
                            ViewCompat.getTransitionName(teamB_image));

                    context.startActivity(intent, options.toBundle());
                }
            });
        }
    }
}