package in.sportscult.sportscultapp.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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

import in.sportscult.sportscultapp.R;
import in.sportscult.sportscultapp.TeamDescriprion;

/**
 * Created by Vishal Gautam
 * Displays list o Fixtures
 */
public class FixtureFragment extends Fragment {

    private static DatabaseReference databaseReference;
    private Spinner age_group_fixture;
    private RecyclerView upcoming_matches_fixture;
    private static int selection_for_age_group = 1;
    private static final String[] age_group_codes = {"0","A","B","C","D"};
    private static String age_group;
    private static ArrayList<Fixture> list_of_fixtures;
    private static FixtureListAdapter fixtureListAdapter;
    private static ProgressDialog progressDialog;
    static Map<String,String> team_profile_pic_download_urls;
    private static TextView display_on_empty_fixture;

    public FixtureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fixture, container, false);

        age_group_fixture = (Spinner) view.findViewById(R.id.age_group_fixture);
        upcoming_matches_fixture = (RecyclerView) view.findViewById(R.id.upcoming_matches_fixture);
        list_of_fixtures = new ArrayList<Fixture>();
        team_profile_pic_download_urls = new HashMap<String, String>();
        display_on_empty_fixture = (TextView)view.findViewById(R.id.display_on_empty_fixture);

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_fixture.setAdapter(age_group_adapter);
        //The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the time_default value
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);
        selection_for_age_group = sharedPreferences.getInt("selection_for_age_group",1);

        age_group_fixture.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];

        fixtureListAdapter = new FixtureListAdapter(getActivity(),list_of_fixtures,team_profile_pic_download_urls,age_group);
        upcoming_matches_fixture.setAdapter(fixtureListAdapter);
        upcoming_matches_fixture.setLayoutManager(new LinearLayoutManager(getActivity()));

        Fetching_Fixtures_From_Firebase();
        //Listening for change in age groups
        age_group_fixture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                    Fetching_Fixtures_From_Firebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }


    public void Fetching_Fixtures_From_Firebase(){

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data....");
        progressDialog.setCancelable(false);

        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group);
        databaseReference.child("Fixtures").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //progressDialog.show();
                list_of_fixtures = new ArrayList<Fixture>();
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    ArrayListEmpty();
                    return;
                }
                for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> fixture_description = (Map<String,String>)ChildSnapshot.getValue();
                    Fixture fixture = new Fixture(fixture_description.get("Team A"),fixture_description.get("Team B"),fixture_description.get("Date"),
                            fixture_description.get("Time"),fixture_description.get("Venue"),fixture_description.get("Referee"));
                    list_of_fixtures.add(fixture);
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
                        if(list_of_fixtures.size()==0)
                            ArrayListEmpty();
                        else {
                            upcoming_matches_fixture.setAdapter(new FixtureListAdapter(getActivity(), list_of_fixtures, team_profile_pic_download_urls, age_group));
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
    /**
     * Hide ArrayList if it is empty
     * Display message that there are no fixtures
     */
    private void ArrayListEmpty(){
        upcoming_matches_fixture.setVisibility(View.GONE);
        display_on_empty_fixture.setVisibility(View.VISIBLE);
    }
    /**
     * Hide message
     * Show Array list with list of fixtures
     */
    private void ArrayListNotEmpty(){
        upcoming_matches_fixture.setVisibility(View.VISIBLE);
        display_on_empty_fixture.setVisibility(View.GONE);
    }

}

/**
 * Fixture Object
 */
class Fixture{
    String TeamA,TeamB,Time,Venue,Referee,Date;

    /**
     * Constructor
     * @param TeamA Name of team A
     * @param TeamB Name of Team B
     * @param Date Date of match
     * @param Time Time of match
     * @param Venue Venue of match
     * @param Referee Refree for the match
     */
    Fixture(String TeamA,String TeamB,String Date,String Time,String Venue,String Referee){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.Time = Time;
        this.Venue = Venue;
        this.Referee = Referee;
        this.Date = Date;
    }
}
/**
 * The Fixture adapeter to which binds the Array list items to the viewholder
 */
class FixtureListAdapter extends RecyclerView.Adapter<FixtureListAdapter.ViewHolder1>{
    ArrayList<Fixture> fixtureArrayList;
    Map<String,String> map_for_team_profile_pic_download_urls;
    LayoutInflater layoutInflater;
    Context context;
    String AGEGROUP;

    /**
     *
     * @param context Context of the Activity that called the constructor
     * @param fixtureArrayList Fixture ArrayList
     * @param map_for_team_profile_pic_download_urls Map with urls for profile pic
     * @param AGEGROUP Age group of teams
     */
    public FixtureListAdapter(Context context,ArrayList<Fixture> fixtureArrayList,Map<String,String> map_for_team_profile_pic_download_urls,String AGEGROUP){
        this.context = context;
        this.fixtureArrayList = fixtureArrayList;
        this.map_for_team_profile_pic_download_urls = map_for_team_profile_pic_download_urls;
        layoutInflater = LayoutInflater.from(context);
        this.AGEGROUP = AGEGROUP;
    }

    @Override
    public ViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {
        /**
         * Inflates the layout for Object
         */
        View view = layoutInflater.inflate(R.layout.fixture_card,parent,false);
        ViewHolder1 viewHolder1 = new ViewHolder1(view);
        return viewHolder1;
    }

    @Override
    public void onBindViewHolder(ViewHolder1 viewHolder, int position) {
        /**
         * Sets the contents of the viewholder by populating it from the Fixture Object
         */
        viewHolder.teamA_name.setText(fixtureArrayList.get(position).TeamA);
        viewHolder.teamB_name.setText(fixtureArrayList.get(position).TeamB);
        viewHolder.date.setText(fixtureArrayList.get(position).Date);
        viewHolder.time.setText(fixtureArrayList.get(position).Time);
        viewHolder.venue.setText(fixtureArrayList.get(position).Venue);
        viewHolder.referee.setText(fixtureArrayList.get(position).Referee);

        final ImageView tempImageViewA = viewHolder.teamA_image;
        final ImageView tempImageViewB = viewHolder.teamB_image;
        final String urlA = map_for_team_profile_pic_download_urls.get(fixtureArrayList.get(position).TeamA);
        final String urlB = map_for_team_profile_pic_download_urls.get(fixtureArrayList.get(position).TeamB);
        /**
         * Load profile pic thumbnails
         * Looks if Image is cached.
         * If it finds Cached image it loads the same or else it downloads it from firebase
         */
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
        ViewCompat.setTransitionName(viewHolder.teamA_image,fixtureArrayList.get(position).TeamA);
        ViewCompat.setTransitionName(viewHolder.teamB_image,fixtureArrayList.get(position).TeamB);

        ViewCompat.setTransitionName(viewHolder.teamA_name,fixtureArrayList.get(position).TeamA+"_");
        ViewCompat.setTransitionName(viewHolder.teamB_name,fixtureArrayList.get(position).TeamA+"_");
    }

    @Override
    public int getItemCount() {
        return fixtureArrayList.size();
    }

    /**
     * The viewHolder for the Fixture Object
     */
    class ViewHolder1 extends RecyclerView.ViewHolder{
        TextView teamA_name, teamB_name, date, time, venue, referee;
        ImageView teamA_image,teamB_image;

        LinearLayout include2,include;
        ViewHolder1(View v) {
            super(v);
            teamA_name = (TextView) v.findViewById(R.id.teamA_name);
            teamB_name = (TextView) v.findViewById(R.id.teamB_name);
            date = (TextView) v.findViewById(R.id.date);
            time = (TextView) v.findViewById(R.id.time);
            venue = (TextView) v.findViewById(R.id.venue);
            referee = (TextView) v.findViewById(R.id.referee);
            teamA_image = (ImageView)v.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)v.findViewById(R.id.teamB_image);

            include2 = (LinearLayout)v.findViewById(R.id.include2);
            teamA_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, TeamDescriprion.class);
                    intent.putExtra("EXTRA_TRANSITION_NAME", ViewCompat.getTransitionName(teamA_image));
                    intent.putExtra("EXTRA_TRANSITION_NAME_", ViewCompat.getTransitionName(teamA_name));
                    intent.putExtra("Team Name",fixtureArrayList.get(getPosition()).TeamA);
                    intent.putExtra("Age Group",AGEGROUP);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity) context,
                            teamA_image,
                            ViewCompat.getTransitionName(teamA_image));

                    context.startActivity(intent, options.toBundle());
                }
            });
            include = (LinearLayout)v.findViewById(R.id.include);
            teamB_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,TeamDescriprion.class);
                    intent.putExtra("EXTRA_TRANSITION_NAME", ViewCompat.getTransitionName(teamB_image));
                    intent.putExtra("EXTRA_TRANSITION_NAME_", ViewCompat.getTransitionName(teamB_name));
                    intent.putExtra("Team Name",fixtureArrayList.get(getPosition()).TeamB);
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
