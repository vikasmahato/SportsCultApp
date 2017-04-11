package in.sportscult.sportscultapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

/**
 * Created by Vishal Gautam
 * Displays the list of Live Matches
 */
public class LiveMatchFragment extends Fragment {

    private RecyclerView Live_Matches_List;
    private CardView location_card,favourite_match_card;
    private ArrayList<LiveMatch> liveMatchArrayList;
    private Map<String,String> team_profile_pic_download_urls;
    private LiveMatchAdapter liveMatchAdapter;
    //MapView mapView;
    private ProgressDialog progressDialog;
    private static TextView display_on_empty_live_match;
    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    ValueEventListener liveMatchListener;

    public LiveMatchFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_live_match, container, false);
        Live_Matches_List = (RecyclerView) view.findViewById(R.id.Live_Matches_List);
        location_card = (CardView)view.findViewById(R.id.location_card);
        //location_card.requestFocus();
        favourite_match_card = (CardView)view.findViewById(R.id.favourite_match_card);
        //favourite_match_card.requestFocus();
        team_profile_pic_download_urls = new HashMap<String, String>();
        liveMatchArrayList = new ArrayList<LiveMatch>();
        display_on_empty_live_match = (TextView)view.findViewById(R.id.display_on_empty_live_match);
      //   mapView = (MapView) view.findViewById(R.id.card_image) ;

        liveMatchAdapter = new LiveMatchAdapter(getActivity(),liveMatchArrayList,team_profile_pic_download_urls);
        Live_Matches_List.setAdapter(liveMatchAdapter);
        Live_Matches_List.setLayoutManager(new LinearLayoutManager(getActivity()));
        Live_Matches_List.setNestedScrollingEnabled(false);

        setup_top_card();

        Fetch_Live_Matches_From_Firebase();
/**
 * Add onClickListener to recycler Items
 * Launches DetailedMatch Description on click of a Card
 * Passes Activity Name, Match ID and Age Group with Intent
 */
        Live_Matches_List.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), Live_Matches_List, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), DetailedMatchDescription.class);
                intent.putExtra("Activity Name","Live");
                intent.putExtra("Match ID",liveMatchArrayList.get(position).Key);
                intent.putExtra("Age Group",liveMatchArrayList.get(position).AgeGroup);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

        return view;
    }

    /**
     * Remove database listeners to prevent app crash on restart
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        databaseReference.child("Live Matches").removeEventListener(liveMatchListener);
    }

    /**
     * Fetches the list of Live MAtches rom firebase
     */
    public void Fetch_Live_Matches_From_Firebase(){

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data....");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        databaseReference.keepSynced(true);
        /**
         * Fetch list of Live Matches from firebase database's "Live Matches" node
         **/
        liveMatchListener = databaseReference.child("Live Matches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                liveMatchArrayList = new ArrayList<LiveMatch>();
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    ArrayListEmpty();
                    setup_top_card();
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();

                    /**
                     * Fetch all relevant details from node and store it in a map
                     */
                    final LiveMatch liveMatch = new LiveMatch(childSnapshot.getKey(),map.get("Team A"),map.get("Team B"),map.get("Team A Goals"),
                            map.get("Team B Goals"),map.get("Venue"),map.get("Start Time"),map.get("Age Group"));

                    //Add LiveMatch Object to list
                    liveMatchArrayList.add(liveMatch);

                    databaseReference.child(liveMatch.AgeGroup).child("Team Names").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //Fetch Team's Profile pic
                            for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()) {
                                Map<String, String> urlmap = (Map<String, String>) ChildSnapshot.getValue();
                                team_profile_pic_download_urls.put(liveMatch.AgeGroup + ChildSnapshot.getKey(), urlmap.get("Team Profile Pic Thumbnail Url"));
                            }
                            progressDialog.dismiss();
                            if(liveMatchArrayList.size()==0)
                                ArrayListEmpty();
                            else{
                                liveMatchAdapter = new LiveMatchAdapter(getActivity(),liveMatchArrayList,team_profile_pic_download_urls);
                                /**
                                 * Add adapter to Live Match List
                                 */
                                Live_Matches_List.setAdapter(liveMatchAdapter);
                                ArrayListNotEmpty();
                                setup_top_card();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Some Error Occurred",Toast.LENGTH_LONG).show();
                        }
                    });

                }
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
     * Display message that there are no live matches
     */
    private void ArrayListEmpty(){
        display_location_card();
        Live_Matches_List.setVisibility(View.GONE);
        display_on_empty_live_match.setVisibility(View.VISIBLE);
    }

    /**
     * Hide message
     * Show Array list with list of matches
     */
    private void ArrayListNotEmpty(){
        Live_Matches_List.setVisibility(View.VISIBLE);
        display_on_empty_live_match.setVisibility(View.GONE);
    }

    public void setup_top_card(){

        display_location_card();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);
        String FavouriteAgeGroup = sharedPreferences.getString("Favourite Age Group","");
        String FavouriteTeamName = sharedPreferences.getString("Favourite Team Name","");
        if(!FavouriteAgeGroup.equals(""))
            for(LiveMatch liveMatch : liveMatchArrayList) {
                //Log.d("MY CHECK", liveMatch.AgeGroup + liveMatch.TeamB+liveMatch.TeamA);
                if (liveMatch.AgeGroup.equals(FavouriteAgeGroup) && (liveMatch.TeamB.equals(FavouriteTeamName) || liveMatch.TeamA.equals(FavouriteTeamName))) {

                    //Log.d("MY CHECK", FavouriteAgeGroup + FavouriteTeamName);

                    TextView textViewGroup = (TextView) favourite_match_card.findViewById(R.id.group);
                    TextView textViewScoreA = (TextView) favourite_match_card.findViewById(R.id.scoreA);
                    TextView textViewScoreB = (TextView) favourite_match_card.findViewById(R.id.scoreB);
                    TextView textViewTeamA = (TextView) favourite_match_card.findViewById(R.id.teamA_name);
                    TextView textViewTeamB = (TextView) favourite_match_card.findViewById(R.id.teamB_name);
                    TextView textViewStartTime = (TextView) favourite_match_card.findViewById(R.id.live_match_start_time);
                    RelativeLayout background_for_fav_match = (RelativeLayout)favourite_match_card.findViewById(R.id.background_for_fav_match);
                    final ImageView imageViewTeamA = (ImageView) favourite_match_card.findViewById(R.id.teamA_image);
                    final ImageView imageViewTeamB = (ImageView) favourite_match_card.findViewById(R.id.teamB_image);

                    textViewGroup.setText("Your Favourite Team Is Live Now!!");
                    textViewScoreA.setText(liveMatch.TeamAGoals);
                    textViewScoreB.setText(liveMatch.TeamBGoals);
                    textViewTeamA.setText(liveMatch.TeamA);
                    textViewTeamB.setText(liveMatch.TeamB);
                    textViewStartTime.setText("Start Time : " + liveMatch.StartTime);

                    //Set Text Color For Scores
                    int AColor, BColor;
                    if (Integer.parseInt(liveMatch.TeamAGoals) > Integer.parseInt(liveMatch.TeamBGoals)) {
                        AColor = getActivity().getResources().getColor(R.color.winning_color);
                        BColor = getActivity().getResources().getColor(R.color.loosing_color);
                    } else if (Integer.parseInt(liveMatch.TeamAGoals) < Integer.parseInt(liveMatch.TeamBGoals)) {
                        BColor = getActivity().getResources().getColor(R.color.winning_color);
                        AColor = getActivity().getResources().getColor(R.color.loosing_color);
                    } else {
                        BColor = getActivity().getResources().getColor(R.color.draw_color);
                        AColor = getActivity().getResources().getColor(R.color.draw_color);
                    }
                    textViewScoreA.setTextColor(AColor);
                    textViewScoreB.setTextColor(BColor);

                    if(liveMatch.TeamA.equals(FavouriteTeamName))
                        background_for_fav_match.setBackgroundColor(AColor);
                    else
                        background_for_fav_match.setBackgroundColor(BColor);

                        final String urlA = team_profile_pic_download_urls.get(liveMatch.AgeGroup + liveMatch.TeamA);
                    final String urlB = team_profile_pic_download_urls.get(liveMatch.AgeGroup + liveMatch.TeamB);
                    Picasso.with(getActivity())
                            .load(urlA)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(imageViewTeamA, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    //Try again online if cache failed
                                    Picasso.with(getActivity())
                                            .load(urlA)
                                            //.error(R.drawable.common_full_open_on_phone)
                                            .into(imageViewTeamA, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                }
                                            });
                                }
                            });
                    Picasso.with(getActivity())
                            .load(urlB)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(imageViewTeamB, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    //Try again online if cache failed
                                    Picasso.with(getActivity())
                                            .load(urlB)
                                            //.error(R.drawable.common_full_open_on_phone)
                                            .into(imageViewTeamB, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                }
                                            });
                                }
                            });
                    hide_location_card();
                    break;
                }
            }
    }

    private void display_location_card(){

        location_card.setVisibility(View.VISIBLE);
        favourite_match_card.setVisibility(View.GONE);
    }

    private void hide_location_card(){

        location_card.setVisibility(View.GONE);
        favourite_match_card.setVisibility(View.VISIBLE);
    }
}

/**
 * Live Match Object
 */
class LiveMatch{
    String TeamA,TeamB,TeamAGoals,TeamBGoals,Venue,StartTime,AgeGroup,Key;

    /**
     *
     * @param Key THe Firebase Node ID used to reference the match
     * @param TeamA Name of Team A
     * @param TeamB NAme of Team B
     * @param TeamAGoals Goals scored by team A
     * @param TeamBGoals Goals scored by team B
     * @param Venue Location of the match
     * @param StartTime Time when the match Started
     * @param AgeGroup Age grop of the competing teams
     */
    LiveMatch(String Key,String TeamA,String TeamB,String TeamAGoals,String TeamBGoals,String Venue,String StartTime,String AgeGroup){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.TeamAGoals = TeamAGoals;
        this.TeamBGoals = TeamBGoals;
        this.Venue = Venue;
        this.StartTime = StartTime;
        this.AgeGroup = AgeGroup;
        this.Key = Key;
    }
}

/**
 * The Live match adapeter to which binds the Array list items to the viewholder
 */
class LiveMatchAdapter extends RecyclerView.Adapter<LiveMatchAdapter.Viewholder3>{

    ArrayList<LiveMatch> arrayListForLiveMatch;
    Map<String,String> urlMap;
    LayoutInflater layoutInflater;
    Context context;

    /**
     *
     * @param context Context of the Activity that called the constructor
     * @param arrayListForLiveMatch An ArrayList of LiveMatch object
     * @param urlMap A map of the team profile pic urls
     */
    LiveMatchAdapter(Context context,ArrayList<LiveMatch> arrayListForLiveMatch,Map<String,String> urlMap){
        this.arrayListForLiveMatch = arrayListForLiveMatch;
        layoutInflater = LayoutInflater.from(context);
        this.urlMap = urlMap;
        this.context = context;
    }

    @Override
    public Viewholder3 onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflates the layout of the viewholder
        View view = layoutInflater.inflate(R.layout.live_match_card,parent,false);
        Viewholder3 viewholder3 = new Viewholder3(view);
        return viewholder3;
    }

    @Override
    public void onBindViewHolder(Viewholder3 viewHolder, int position) {
        /**
         * Sets the contents of the viewholder by populating it from the LiveMatch Object
         */
        LiveMatch data = arrayListForLiveMatch.get(position);
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

        viewHolder.live_match_start_time.setText("Start Time : " + data.StartTime);
        viewHolder.live_match_venue.setText("Venue : " + data.Venue);
        //Display the Age group of the teams
        switch (data.AgeGroup){
            case "Group - A":
                viewHolder.group.setText("Group - A");
                break;
            case "Group - B":
                viewHolder.group.setText("Group - B");
                break;
            case "Group - C":
                viewHolder.group.setText("Group - C");
                break;
            case "Group - D":
                viewHolder.group.setText("Group - D");
                break;
            default:
                viewHolder.group.setText("Unspecified Group");
                break;
        }

        final ImageView tempImageViewA = viewHolder.teamA_image;
        final ImageView tempImageViewB = viewHolder.teamB_image;
        final String urlA = urlMap.get(data.AgeGroup + data.TeamA);
        final String urlB = urlMap.get(data.AgeGroup + data.TeamB);
        /**
         * Load Team Pic of TEAM A
         * Looks if Image is cached.
         * If it finds Cached image it loads the same
         */
        Picasso.with(context)
                .load(urlA)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageViewA, new Callback() {
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

        /**
         * Load Team Pic of TEAM B
         * Looks if Image is cached.
         * If it finds Cached image it loads the same
         */

        Picasso.with(context)
                .load(urlB)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageViewB, new Callback() {
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
    }

    @Override
    public int getItemCount() {
        return arrayListForLiveMatch.size();
    }

    /**
     * The viewHolder for the LiveMatch Object
     */
    class Viewholder3 extends RecyclerView.ViewHolder{
        LinearLayout live_match_detail_card;
        TextView teamA_name,teamB_name,live_match_score_A,live_match_score_B,live_match_start_time,live_match_venue,group;
        ImageView teamA_image,teamB_image;
        Viewholder3(View view){
            super(view);
            live_match_detail_card = (LinearLayout)view.findViewById(R.id.live_match_detail_card);
            teamA_name = (TextView)view.findViewById(R.id.teamA_name);
            teamB_name = (TextView)view.findViewById(R.id.teamB_name);
            live_match_score_A = (TextView)view.findViewById(R.id.scoreA);
            live_match_score_B = (TextView)view.findViewById(R.id.scoreB);
            live_match_start_time = (TextView)view.findViewById(R.id.live_match_start_time);
            live_match_venue = (TextView)view.findViewById(R.id.live_match_venue);
            teamA_image = (ImageView)view.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)view.findViewById(R.id.teamB_image);
            group = (TextView)view.findViewById(R.id.group);
        }
    }


}