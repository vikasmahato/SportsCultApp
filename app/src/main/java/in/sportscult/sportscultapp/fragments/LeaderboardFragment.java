package in.sportscult.sportscultapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
public class LeaderboardFragment extends Fragment {

    private static DatabaseReference databaseReference;
    private Spinner age_group_leaderboard;
    private RecyclerView leaderboard_list;
    private static final String[] age_group_codes = {"0","A","B","C","D"};
    private static String age_group;
    private static ArrayList<TeamScoreCard> list_of_team_scorecards;
    private static Map<String,String> team_profile_pic_download_urls;
    private static LeaderBoardAdapter leaderBoardAdapter;
    private static ProgressDialog progressDialog;
    private static int selection_for_age_group = 1;

    public LeaderboardFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leaderboard,container,false);
        age_group_leaderboard = (Spinner)view.findViewById(R.id.age_group_leaderboard);
        leaderboard_list = (RecyclerView) view.findViewById(R.id.leaderboard_list);
        team_profile_pic_download_urls = new HashMap<String, String>();
        list_of_team_scorecards = new ArrayList<TeamScoreCard>();

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_leaderboard.setAdapter(age_group_adapter);
        //Add The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the default value
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);
        selection_for_age_group = sharedPreferences.getInt("selection_for_age_group",1);

        age_group_leaderboard.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];

        leaderBoardAdapter = new LeaderBoardAdapter(getActivity(),list_of_team_scorecards,team_profile_pic_download_urls);
        leaderboard_list.setAdapter(leaderBoardAdapter);
        leaderboard_list.setLayoutManager(new LinearLayoutManager(getActivity()));

        Fetching_Leaderboard_From_Firebase();

        //Listening for change in age groups
        age_group_leaderboard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

                    Fetching_Leaderboard_From_Firebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    public void Fetching_Leaderboard_From_Firebase(){

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data...");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group);
        databaseReference.child("Leaderboard").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list_of_team_scorecards = new ArrayList<TeamScoreCard>();
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    leaderboard_list.setAdapter(new LeaderBoardAdapter(getActivity(),list_of_team_scorecards,team_profile_pic_download_urls));
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();
                    TeamScoreCard teamScoreCard = new TeamScoreCard(map.get("Team Name"),map.get("Matches Played"),
                            map.get("Matches Won"),map.get("Matches Lost"),map.get("Matches Drawn"),map.get("Goals Scored"),
                            map.get("Goals Conceived"),map.get("Red Cards"),map.get("Points"));
                    list_of_team_scorecards.add(teamScoreCard);
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
                        //Configure ListView Adapter
                        leaderboard_list.setAdapter(new LeaderBoardAdapter(getActivity(),list_of_team_scorecards,team_profile_pic_download_urls));
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

class TeamScoreCard{
    String TeamName,MacthesPlayed,MatchesWon,MatchesLost,MatchesDrawn,GoalsScored,GolasConceived,RedCardsReceived,PointsAwarded;
    TeamScoreCard(String TeamName,String MacthesPlayed,String MatchesWon,String MatchesLost,String MatchesDrawn,
                  String GoalsScored,String GolasConceived,String RedCardsReceived,String PointsAwarded){


        this.TeamName = TeamName;
        this.MacthesPlayed = MacthesPlayed;
        this.MatchesWon = MatchesWon;
        this.MatchesLost = MatchesLost;
        this.MatchesDrawn = MatchesDrawn;
        this.GoalsScored = GoalsScored;
        this.GolasConceived = GolasConceived;
        this.RedCardsReceived =RedCardsReceived;
        this.PointsAwarded = PointsAwarded;

    }
}

class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder2>{

    ArrayList<TeamScoreCard> team_Scorecards;
    Map<String,String> map_for_team_profile_pic_download_urls;
    Context context;

    LeaderBoardAdapter(Context context,ArrayList<TeamScoreCard> team_Scorecards,Map<String,String> map_for_team_profile_pic_download_urls){

        this.context = context;
        this.map_for_team_profile_pic_download_urls = map_for_team_profile_pic_download_urls;
        this.team_Scorecards = team_Scorecards;

    }

    @Override
    public ViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_card,parent,false);
        ViewHolder2 viewHolder2 = new ViewHolder2(view);
        return viewHolder2;
    }

    @Override
    public void onBindViewHolder(ViewHolder2 viewHolder, int position) {
        TeamScoreCard data = team_Scorecards.get(position);
        viewHolder.team_name.setText(data.TeamName);
        viewHolder.matches_played.setText(data.MacthesPlayed);
        viewHolder.matches_won.setText(data.MatchesWon);
        viewHolder.matches_lost.setText(data.MatchesLost);
        viewHolder.matches_drawn.setText(data.MatchesDrawn);
        viewHolder.goals_scored.setText(data.GoalsScored);
        viewHolder.goals_conceived.setText(data.GolasConceived);
        viewHolder.red_cards_received.setText(data.RedCardsReceived);
        viewHolder.points_awarded.setText(data.PointsAwarded);

        final ImageView tempImageView = viewHolder.team_image;
        final String url = map_for_team_profile_pic_download_urls.get(data.TeamName);
        //Load profile pic thumbnails
        Picasso.with(context)
                .load(url)
                .resize(70,70)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(url)
                                //.error(R.drawable.common_full_open_on_phone)
                                .into(tempImageView, new Callback() {
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
        return team_Scorecards.size();
    }

    class ViewHolder2 extends RecyclerView.ViewHolder{
        TextView team_name,matches_played,matches_won,matches_lost,matches_drawn,goals_scored,goals_conceived,
            red_cards_received,points_awarded;
        ImageView team_image;

        ViewHolder2(View v) {
            super(v);
            team_name = (TextView) v.findViewById(R.id.team_name);
            matches_played = (TextView) v.findViewById(R.id.matches_played);
            matches_won = (TextView) v.findViewById(R.id.matches_won);
            matches_lost = (TextView) v.findViewById(R.id.matches_lost);
            matches_drawn = (TextView) v.findViewById(R.id.matches_drawn);
            goals_scored = (TextView) v.findViewById(R.id.goals_scored);
            goals_conceived = (TextView) v.findViewById(R.id.goals_conceived);
            red_cards_received = (TextView) v.findViewById(R.id.red_cards_received);
            points_awarded = (TextView) v.findViewById(R.id.points_awarded);

            team_image = (ImageView) v.findViewById(R.id.team_image);
        }
    }

}
