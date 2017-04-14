package in.sportscult.sportscultapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import in.sportscult.sportscultapp.MainDrawer;
import in.sportscult.sportscultapp.R;
import in.sportscult.sportscultapp.RecyclerItemClickListener;
import in.sportscult.sportscultapp.TeamDescriprion;

/**
 * Loads the LeaderBoard and displays it in cardview
 */
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
    private static TextView display_on_empty_leaderboard;

    public LeaderboardFragment() {
        //Required constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.leaderboard_trial,container,false);
        age_group_leaderboard = (Spinner)view.findViewById(R.id.age_group_leaderboard);
        leaderboard_list = (RecyclerView) view.findViewById(R.id.leaderboard_list);
        team_profile_pic_download_urls = new HashMap<String, String>();
        list_of_team_scorecards = new ArrayList<TeamScoreCard>();
        display_on_empty_leaderboard= (TextView)view.findViewById(R.id.display_on_empty_leaderboard);

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_leaderboard.setAdapter(age_group_adapter);
        //Add The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the time_default value
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);
        selection_for_age_group = sharedPreferences.getInt("selection_for_age_group",1);

        age_group_leaderboard.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];

        //Create the leader board adapter
        leaderBoardAdapter = new LeaderBoardAdapter(getActivity(),list_of_team_scorecards,team_profile_pic_download_urls);
        leaderboard_list.setAdapter(leaderBoardAdapter);
        leaderboard_list.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Add onclick listener to leaderboard list
        leaderboard_list.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), leaderboard_list, new RecyclerItemClickListener.OnItemClickListener() {
            /**
             * Launch an intent to display team description
             * @param view The view which was clicked
             * @param position The position which was clicked
             */
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), TeamDescriprion.class);
                intent.putExtra("Age Group",age_group);
                intent.putExtra("Team Name",list_of_team_scorecards.get(position).TeamName);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
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

    /**
     * Fetch data rom Firebase 'Leaderboard' node
     */
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
                    ArrayListEmpty();
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
                        if(list_of_team_scorecards.size()==0)
                            ArrayListEmpty();
                        else {
                            leaderboard_list.setAdapter(new LeaderBoardAdapter(getActivity(),list_of_team_scorecards,team_profile_pic_download_urls));
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
        leaderboard_list.setVisibility(View.GONE);
        display_on_empty_leaderboard.setVisibility(View.VISIBLE);
    }
    private void ArrayListNotEmpty(){
        leaderboard_list.setVisibility(View.VISIBLE);
        display_on_empty_leaderboard.setVisibility(View.GONE);
    }
}

/**
 * TeamScoreCard class stores info about the team
 */
class TeamScoreCard{
    String TeamName,MacthesPlayed,MatchesWon,MatchesLost,MatchesDrawn,GoalsScored,GolasConceived,RedCardsReceived,PointsAwarded;

    /**
     * Team information stored
     * @param TeamName Team Name
     * @param MacthesPlayed Number of matches played
     * @param MatchesWon Number of matches won
     * @param MatchesLost Number of matches lost
     * @param MatchesDrawn Number of matches drawn
     * @param GoalsScored Number of goals scored
     * @param GoalsConceived Number of goals concieved
     * @param RedCardsReceived Number of red cards recieved
     * @param PointsAwarded Number of points
     */
    TeamScoreCard(String TeamName,String MacthesPlayed,String MatchesWon,String MatchesLost,String MatchesDrawn,
                  String GoalsScored,String GoalsConceived,String RedCardsReceived,String PointsAwarded){


        this.TeamName = TeamName;
        this.MacthesPlayed = MacthesPlayed;
        this.MatchesWon = MatchesWon;
        this.MatchesLost = MatchesLost;
        this.MatchesDrawn = MatchesDrawn;
        this.GoalsScored = GoalsScored;
        this.GolasConceived = GoalsConceived;
        this.RedCardsReceived =RedCardsReceived;
        this.PointsAwarded = PointsAwarded;

    }
}

/**
 * Leaderboard adapter class
 */
class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder2>{

    ArrayList<TeamScoreCard> team_Scorecards;
    Map<String,String> map_for_team_profile_pic_download_urls;
    Context context;

    /**
     * The Leaderboard adapter
     * @param context The context of the activity thst requested the Adapter
     * @param team_Scorecards ArrayList of the leaderboard objects
     * @param map_for_team_profile_pic_download_urls Map which stores the url of Team profile pics
     */
    LeaderBoardAdapter(Context context,ArrayList<TeamScoreCard> team_Scorecards,Map<String,String> map_for_team_profile_pic_download_urls){

        this.context = context;
        this.map_for_team_profile_pic_download_urls = map_for_team_profile_pic_download_urls;
        this.team_Scorecards = team_Scorecards;

    }

    @Override
    public ViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the leader_card for the view
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_card,parent,false);
        ViewHolder2 viewHolder2 = new ViewHolder2(view);
        return viewHolder2;
    }

    @Override
    public void onBindViewHolder(ViewHolder2 viewHolder, int position) {
        TeamScoreCard data = team_Scorecards.get(position);
        viewHolder.player_row_for_leaderboard_team_name.setText(data.TeamName);
        viewHolder.player_row_for_leaderboard_matches_played.setText(data.MacthesPlayed);
        viewHolder.player_row_for_leaderboard_matches_won.setText(data.MatchesWon);
        viewHolder.player_row_for_leaderboard_matches_lost.setText(data.MatchesLost);
        viewHolder.player_row_for_leaderboard_points_scored.setText(data.PointsAwarded);

        final ImageView tempImageView = viewHolder.player_row_for_leaderboard_profile_pic;
        final String url = map_for_team_profile_pic_download_urls.get(data.TeamName);

        //Load profile pic thumbnails
        if(url==null || url.equals("Not Set"))
            MainDrawer.DefaultProfilePic(data.TeamName,viewHolder.LeaderboardDefaultProfilePic);
        else {
            (viewHolder.LeaderboardDefaultProfilePic).setText("");
            Picasso.with(context)
                    .load(url)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(tempImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        /**
                         * Loads Team pic from Url if not found in Cache
                         */
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
    }

    @Override
    public int getItemCount() {
        return team_Scorecards.size();
    }
    /**
     * The viewHolder for the LeaderBoard Object
     */
    class ViewHolder2 extends RecyclerView.ViewHolder{
        TextView player_row_for_leaderboard_team_name,player_row_for_leaderboard_matches_played,player_row_for_leaderboard_matches_won,player_row_for_leaderboard_matches_lost,player_row_for_leaderboard_points_scored;
        TextView LeaderboardDefaultProfilePic;
        ImageView player_row_for_leaderboard_profile_pic;

        ViewHolder2(View v) {
            super(v);
            player_row_for_leaderboard_team_name = (TextView) v.findViewById(R.id.player_row_for_leaderboard_team_name);
            player_row_for_leaderboard_matches_played = (TextView) v.findViewById(R.id.player_row_for_leaderboard_matches_played);
            player_row_for_leaderboard_matches_won = (TextView) v.findViewById(R.id.player_row_for_leaderboard_matches_won);
            player_row_for_leaderboard_matches_lost = (TextView) v.findViewById(R.id.player_row_for_leaderboard_matches_lost);
            player_row_for_leaderboard_points_scored = (TextView) v.findViewById(R.id.player_row_for_leaderboard_points_scored);
            player_row_for_leaderboard_profile_pic = (ImageView) v.findViewById(R.id.player_row_for_leaderboard_profile_pic);
            LeaderboardDefaultProfilePic = (TextView) v.findViewById(R.id.LeaderboardDefaultProfilePic);
        }
    }

}
