package in.sportscult.sportscultapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.sportscult.sportscultapp.R;

public class SettingsFragment extends Fragment {

    private static Spinner fav_team_age_group,fav_team_name;
    private static ArrayList<String> team_name_arraylist;
    private static int selection_for_age_group = 0;
    private static final String[] age_group_codes = {"0","A","B","C","D"};
    private static String age_group = "Group - 0";
    private static int selection_for_team=0;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings,container,false);

        fav_team_age_group = (Spinner)view.findViewById(R.id.fav_team_age_group);
        fav_team_name = (Spinner)view.findViewById(R.id.fav_team_name);
        team_name_arraylist = new ArrayList<String>();
        team_name_arraylist.add("Select Team");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fav_team_age_group.setAdapter(age_group_adapter);

        ArrayAdapter<String> team_name_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,team_name_arraylist);
        team_name_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fav_team_name.setAdapter(team_name_adapter);

        fav_team_age_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=selection_for_age_group){
                    selection_for_age_group = position;
                    age_group = "Group - "+age_group_codes[position];
                    FetchTeamNames();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fav_team_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0 && position!=selection_for_team && selection_for_age_group!=0){
                    selection_for_team = position;
                    editor.putString("Favourite Age Group",age_group);
                    editor.putString("Favourite Team Name",team_name_arraylist.get(position));
                    editor.commit();
                    Toast.makeText(getActivity(),"Favorite Team has been added",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    public void FetchTeamNames(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group).child("Team Names");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                team_name_arraylist = new ArrayList<String>();
                team_name_arraylist.add("Select Team");
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren())
                    team_name_arraylist.add(childSnapshot.getKey());
                ArrayAdapter<String> team_name_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,team_name_arraylist);
                team_name_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                fav_team_name.setAdapter(team_name_adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
