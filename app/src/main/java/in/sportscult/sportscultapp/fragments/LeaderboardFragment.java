package in.sportscult.sportscultapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
public class LeaderboardFragment extends Fragment {

    private static DatabaseReference databaseReference;
    private Spinner age_group_leaderboard;
    private ListView leaderboard_list;
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
        leaderboard_list = (ListView)view.findViewById(R.id.leaderboard_list);

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_leaderboard.setAdapter(age_group_adapter);
        //Add The Functionality to get the selection_for_age_group from Shared Preferences
        //If no data stored in Shared Preferences then do nothing,it will work on the default value
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);
        selection_for_age_group = sharedPreferences.getInt("selection_for_age_group",1);

        age_group_leaderboard.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];

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
        progressDialog.setMessage("Fetching Leaderboard...");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        list_of_team_scorecards = new ArrayList<TeamScoreCard>();
        team_profile_pic_download_urls = new HashMap<String, String>();

        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group);
        databaseReference.child("Leaderboard").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    leaderBoardAdapter = new LeaderBoardAdapter(getActivity(),list_of_team_scorecards,team_profile_pic_download_urls);
                    leaderboard_list.setAdapter(leaderBoardAdapter);
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
                        for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()) {
                            Map<String, String> urlmap = (Map<String, String>) ChildSnapshot.getValue();
                            team_profile_pic_download_urls.put(ChildSnapshot.getKey(), urlmap.get("Team Profile Pic Thumbnail Url"));
                        }
                        progressDialog.dismiss();
                        //Configure ListView Adapter
                        leaderBoardAdapter = new LeaderBoardAdapter(getActivity(),list_of_team_scorecards,team_profile_pic_download_urls);
                        leaderboard_list.setAdapter(leaderBoardAdapter);
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

class LeaderBoardAdapter extends ArrayAdapter<TeamScoreCard>{

    ArrayList<TeamScoreCard> team_Scorecards;
    Map<String,String> map_for_team_profile_pic_download_urls;
    Context context;

    LeaderBoardAdapter(Context context,ArrayList<TeamScoreCard> team_Scorecards,Map<String,String> map_for_team_profile_pic_download_urls){

        super(context,R.layout.leaderboard_card,team_Scorecards);
        this.context = context;
        this.map_for_team_profile_pic_download_urls = map_for_team_profile_pic_download_urls;
        this.team_Scorecards = team_Scorecards;

    }

    public class ViewHolder2 {
        TextView team_name,matches_played,matches_won,matches_lost,matches_drawn,goals_scored,goals_conceived,
            red_cards_received,points_awarded;
        ImageView team_image;

        ViewHolder2(View v) {
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

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder2 viewHolder = null;
        if(row==null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.leaderboard_card,parent,false);
            viewHolder = new ViewHolder2(row);
            row.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder2) row.getTag();
        }

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
        Picasso.with(getContext())
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
                        Picasso.with(getContext())
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
        return row;
    }

}
