package in.sportscult.sportscultapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;


public class ListOfTeams extends AppCompatActivity {

    private static Spinner list_of_teams_age_spinner;
    private static int selection_for_age_group = 1;
    private static final String[] age_group_codes = {"0","A","B","C","D"};
    private static String age_group;
    private static RecyclerView list_of_teams;
    private static TeamAdapter teamAdapter;
    private static ArrayList<TeamInformation> teamInformationArrayList;
    private static ArrayList<AdditionalInformation> additionalInformationArrayList;
    private static DatabaseReference RootReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_teams);

        list_of_teams_age_spinner = (Spinner)findViewById(R.id.list_of_teams_age_spinner);
        teamInformationArrayList = new ArrayList<TeamInformation>();
        additionalInformationArrayList = new ArrayList<AdditionalInformation>();

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        list_of_teams_age_spinner.setAdapter(age_group_adapter);
        //The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the time_default value
        final SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        selection_for_age_group = sharedPreferences.getInt("selection_for_age_group",1);

        list_of_teams_age_spinner.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];

        list_of_teams = (RecyclerView)findViewById(R.id.list_of_teams);
        teamAdapter = new TeamAdapter(this,teamInformationArrayList,additionalInformationArrayList);
        list_of_teams.setAdapter(teamAdapter);
        list_of_teams.setLayoutManager(new LinearLayoutManager(this));

        Fetching_Teams_From_Firebase();
        //Listening for change in age groups
        list_of_teams_age_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                    Fetching_Teams_From_Firebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        list_of_teams.addOnItemTouchListener(new RecyclerItemClickListener(this, list_of_teams, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ListOfTeams.this,TeamDescriprion.class);
                intent.putExtra("Age Group",age_group);
                intent.putExtra("Team Name",teamInformationArrayList.get(position).TeamName);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }

    public void Fetching_Teams_From_Firebase(){

        RootReference.child(age_group).child("Team Names").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teamInformationArrayList = new ArrayList<TeamInformation>();
                additionalInformationArrayList = new ArrayList<AdditionalInformation>();

                if(dataSnapshot.getValue()==null){
                    list_of_teams.setAdapter(new TeamAdapter(ListOfTeams.this,teamInformationArrayList,additionalInformationArrayList));
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){

                    Map<String,String> map = (Map<String, String>)childSnapshot.getValue();
                    TeamInformation tempData = new TeamInformation(childSnapshot.getKey(),map.get("Team Profile Pic Thumbnail Url"));
                    teamInformationArrayList.add(tempData);

                }
                RootReference.child(age_group).child("Team Description").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                            Map<String,String> map1 = (Map<String,String>)dataSnapshot1.getValue();
                            AdditionalInformation tempData1 = new AdditionalInformation(map1.get("Coach Name"),map1.get("Location"));
                            additionalInformationArrayList.add(tempData1);
                            list_of_teams.setAdapter(new TeamAdapter(ListOfTeams.this,teamInformationArrayList,additionalInformationArrayList));
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

class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder>{

    Context context;
    ArrayList<TeamInformation> teamInformations;
    ArrayList<AdditionalInformation> teamAdditionalInformation;
    TeamAdapter(Context context,ArrayList<TeamInformation> teamInformations,ArrayList<AdditionalInformation> teamAdditionalInformation){
        this.context = context;
        this.teamInformations = teamInformations;
        this.teamAdditionalInformation = teamAdditionalInformation;
    }

    class TeamViewHolder extends RecyclerView.ViewHolder{

        ImageView team_profile_pic;
        TextView team_profile_name,team_profile_coach_name,team_profile_location;
        TeamViewHolder(View view){
            super(view);
            team_profile_pic = (ImageView)view.findViewById(R.id.team_profile_pic);
            team_profile_name= (TextView)view.findViewById(R.id.team_profile_name);
            team_profile_coach_name = (TextView)view.findViewById(R.id.team_profile_coach_name);
            team_profile_location = (TextView)view.findViewById(R.id.team_profile_location);
        }
    }

    @Override
    public TeamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.team_description_row,parent,false);
        TeamViewHolder viewHolder = new TeamViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TeamViewHolder holder, int position) {
        final TeamInformation tempInformation = teamInformations.get(position);
        final AdditionalInformation tempAdditionalInformation = teamAdditionalInformation.get(position);

        holder.team_profile_name.setText(tempInformation.TeamName);
        holder.team_profile_coach_name.setText(tempAdditionalInformation.CoachName);
        holder.team_profile_location.setText(tempAdditionalInformation.Location);

        final ImageView imageView = holder.team_profile_pic;
        Picasso.with(context)
                .load(tempInformation.TeamThumbnail)
                .resize(50,50)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(tempInformation.TeamThumbnail)
                                //.error(R.drawable.common_full_open_on_phone)
                                .into(imageView, new Callback() {
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
        return teamInformations.size();
    }
}

class TeamInformation{
    String TeamName,TeamThumbnail;
    TeamInformation(String TeamName,String TeamThumbnail){
        this.TeamName = TeamName;
        this.TeamThumbnail = TeamThumbnail;
    }
}
class AdditionalInformation{
    String CoachName,Location;
    AdditionalInformation(String CoachName,String Location){
        this.CoachName = CoachName;
        this.Location = Location;
    }
}