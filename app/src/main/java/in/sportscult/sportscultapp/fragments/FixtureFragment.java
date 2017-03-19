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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_fixture.setAdapter(age_group_adapter);
        //Add The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the default value
        age_group_fixture.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];
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

        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group).child("Fixtures");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> fixture_description = (Map<String,String>)ChildSnapshot.getValue();
                    Fixture fixture = new Fixture(fixture_description.get("Team A"),fixture_description.get("Team B"),fixture_description.get("Date"),
                            fixture_description.get("Time"),fixture_description.get("Venue"),fixture_description.get("Referee"));
                    list_of_fixtures.add(fixture);
                }

                //Configure Adapter for ListView
                fixtureListAdapter = new FixtureListAdapter(getActivity(),list_of_fixtures);
                upcoming_matches_fixture.setAdapter(fixtureListAdapter);

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
    Context context;
    public FixtureListAdapter(Context context,ArrayList<Fixture> fixtureArrayList){
        super(context, R.layout.fixture_card,fixtureArrayList);
        this.context = context;
        this.fixtureArrayList = fixtureArrayList;
    }

    public class ViewHolder1 {
        TextView teamA_name, teamB_name, date, time, venue, referee;

        ViewHolder1(View v) {
            teamA_name = (TextView) v.findViewById(R.id.teamA_name);
            teamB_name = (TextView) v.findViewById(R.id.teamB_name);
            date = (TextView) v.findViewById(R.id.date);
            time = (TextView) v.findViewById(R.id.time);
            venue = (TextView) v.findViewById(R.id.venue);
            referee = (TextView) v.findViewById(R.id.referee);
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

        return row;
    }
}