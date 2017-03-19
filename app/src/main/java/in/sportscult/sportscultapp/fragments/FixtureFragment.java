package in.sportscult.sportscultapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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

public class FixtureFragment extends Fragment {

    private static DatabaseReference databaseReference;
    private Spinner age_group_fixture;
    private ListView upcoming_matches_fixture;
    private static int selection_for_age_group = 1;
    private static final String[] age_group_codes = {"0","A","B","C","D"};
    private static String age_group;
    private static ArrayList<Fixture> list_of_fixtures;
    private static FixtureListAdapter fixtureListAdapter;
    private static Map<String,String> team_profile_pic_download_urls;

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
        upcoming_matches_fixture = (ListView) view.findViewById(R.id.upcoming_matches_fixture);
        list_of_fixtures = new ArrayList<Fixture>();
        team_profile_pic_download_urls = new HashMap<String, String>();

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_fixture.setAdapter(age_group_adapter);
        //Add The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the default value
        //Initial Run
        age_group_fixture.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];
        Fetching_Fixtures_From_Firebase();
        //Listening for change in age groups
        age_group_fixture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0 && position!=selection_for_age_group){
                    selection_for_age_group = position;
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

        list_of_fixtures = new ArrayList<Fixture>();
        team_profile_pic_download_urls = new HashMap<String, String>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group);
        databaseReference.child("Fixtures").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null){
                    fixtureListAdapter = new FixtureListAdapter(getActivity(),list_of_fixtures,team_profile_pic_download_urls);
                    upcoming_matches_fixture.setAdapter(fixtureListAdapter);
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
                        for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()){
                            Map<String,String> urlmap = (Map<String,String>)ChildSnapshot.getValue();
                            team_profile_pic_download_urls.put(ChildSnapshot.getKey(),urlmap.get("Team Profile Pic Thumbnail Url"));

                            //Configure Adapter for ListView
                            fixtureListAdapter = new FixtureListAdapter(getActivity(),list_of_fixtures,team_profile_pic_download_urls);
                            upcoming_matches_fixture.setAdapter(fixtureListAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
class Fixture{
    String TeamA,TeamB,Time,Venue,Referee,Date;
    Fixture(String TeamA,String TeamB,String Date,String Time,String Venue,String Referee){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.Time = Time;
        this.Venue = Venue;
        this.Referee = Referee;
        this.Date = Date;
    }
}

class FixtureListAdapter extends ArrayAdapter<Fixture>{
    ArrayList<Fixture> fixtureArrayList;
    Map<String,String> map_for_team_profile_pic_download_urls;
    Context context;
    public FixtureListAdapter(Context context,ArrayList<Fixture> fixtureArrayList,Map<String,String> map_for_team_profile_pic_download_urls){
        super(context, R.layout.fixture_card,fixtureArrayList);
        this.context = context;
        this.fixtureArrayList = fixtureArrayList;
        this.map_for_team_profile_pic_download_urls = map_for_team_profile_pic_download_urls;
    }

    public class ViewHolder1 {
        TextView teamA_name, teamB_name, date, time, venue, referee;
        ImageView teamA_image,teamB_image;

        ViewHolder1(View v) {
            teamA_name = (TextView) v.findViewById(R.id.teamA_name);
            teamB_name = (TextView) v.findViewById(R.id.teamB_name);
            date = (TextView) v.findViewById(R.id.date);
            time = (TextView) v.findViewById(R.id.time);
            venue = (TextView) v.findViewById(R.id.venue);
            referee = (TextView) v.findViewById(R.id.referee);
            teamA_image = (ImageView)v.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)v.findViewById(R.id.teamB_image);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder1 viewHolder = null;
        if(row==null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.fixture_card,parent,false);
            viewHolder = new ViewHolder1(row);
            row.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder1) row.getTag();
        }

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
        //Load profile pic thumbnails
        Picasso.with(getContext())
                .load(urlA)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageViewA, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(getContext())
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
        Picasso.with(getContext())
                .load(urlB)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageViewB, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(getContext())
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
        return row;
    }
}