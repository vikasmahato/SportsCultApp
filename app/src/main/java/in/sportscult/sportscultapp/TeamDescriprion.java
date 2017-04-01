package in.sportscult.sportscultapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class TeamDescriprion extends AppCompatActivity {

    private static ImageView specific_team_profile_pic;
    private static TextView specific_team_name,specific_coach_name,specific_team_location;
    private static RecyclerView specific_team_players_list;
    private static String TeamName,AgeGroup;
    private static ArrayList<String> playerjerseynumberarraylist,playernamearraylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_description);

        specific_team_profile_pic = (ImageView)findViewById(R.id.specific_team_profile_pic);
        specific_team_name = (TextView)findViewById(R.id.specific_team_name);
        specific_coach_name = (TextView)findViewById(R.id.specific_team_coach_name);
        specific_team_location = (TextView)findViewById(R.id.specific_team_location);
        specific_team_players_list = (RecyclerView)findViewById(R.id.specific_team_players_list);
        playerjerseynumberarraylist = new ArrayList<String>();
        playernamearraylist = new ArrayList<String>();

        specific_team_players_list.setAdapter(new SpecificTeamAdapter(TeamDescriprion.this,playerjerseynumberarraylist,playernamearraylist));
        specific_team_players_list.setLayoutManager(new LinearLayoutManager(this));

        Bundle bundle = getIntent().getExtras();
        TeamName = bundle.getString("Team Name");
        AgeGroup = bundle.getString("Age Group");
        FetchDataForSpecificTeam();
    }

    public void FetchDataForSpecificTeam(){

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(AgeGroup).child("Team Description").child(TeamName);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,String> map = (Map<String,String>)dataSnapshot.getValue();
                specific_coach_name.setText("Coach : "+map.get("Coach Name"));
                specific_team_name.setText(TeamName);
                specific_team_location.setText("Location : "+map.get("Location"));
                DataSnapshot dataSnapshot1 = dataSnapshot.child("Players");
                for(DataSnapshot childSnapshot : dataSnapshot1.getChildren()){
                    String text = childSnapshot.getKey();
                    int index = text.indexOf('-');
                    playerjerseynumberarraylist.add(text.substring(0,index));
                    playernamearraylist.add(text.substring(index+1));
                    specific_team_players_list.setAdapter(new SpecificTeamAdapter(TeamDescriprion.this,playerjerseynumberarraylist,playernamearraylist));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Commnent this section if its possible to pass Profile Pic URL from all possible Previous Activity
        FirebaseDatabase.getInstance().getReference().child(AgeGroup).child("Team Names").child(TeamName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,String> map = (Map<String,String>) dataSnapshot.getValue();
                final String url = map.get("Team Profile Pic Thumbnail Url");
                Picasso.with(TeamDescriprion.this)
                        .load(url)
                        .resize(100,100)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(specific_team_profile_pic, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                //Try again online if cache failed
                                Picasso.with(TeamDescriprion.this)
                                        .load(url)
                                        //.error(R.drawable.common_full_open_on_phone)
                                        .into(specific_team_profile_pic, new Callback() {
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

class SpecificTeamAdapter extends RecyclerView.Adapter<SpecificTeamAdapter.SpecificTeamViewHolder>{

    Context context;
    ArrayList<String> playerjerseynumberlist,playernamelist;
    SpecificTeamAdapter(Context context, ArrayList<String> playerjerseynumberlist,ArrayList<String> playernamelist){
        this.context = context;
        this.playerjerseynumberlist = playerjerseynumberlist;
        this.playernamelist = playernamelist;
    }

    @Override
    public SpecificTeamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SpecificTeamViewHolder((LayoutInflater.from(context)).inflate(R.layout.player_description_row,parent,false));
    }

    @Override
    public void onBindViewHolder(SpecificTeamViewHolder holder, int position) {
        holder.specific_team_player_name.setText(playernamelist.get(position));
        holder.specific_team_player_jersey_number.setText(playerjerseynumberlist.get(position));
    }

    @Override
    public int getItemCount() {
        return playerjerseynumberlist.size();
    }

    class SpecificTeamViewHolder extends RecyclerView.ViewHolder{

        TextView specific_team_player_jersey_number,specific_team_player_name;
        SpecificTeamViewHolder(View view){
            super(view);
            specific_team_player_jersey_number = (TextView)view.findViewById(R.id.specific_team_player_jersey_number);
            specific_team_player_name = (TextView)view.findViewById(R.id.specific_team_player_name);
        }
    }
}