package in.sportscult.sportscultapp;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import in.sportscult.sportscultapp.Animations.ExpandCollapse;
import in.sportscult.sportscultapp.Utils.ExpandAndCollapseViewUtil;

public class DetailedMatchDescription extends AppCompatActivity {

    private static String OpenerActivity,UniqueMatchID,AgeGroup;
    private static DatabaseReference databaseReference;
    private static ArrayList<EventInformation> eventinformationarraylist;
    private static ArrayList<PlayerDetails> lineupA,lineupB;
    private static RecyclerView eventinformationrecyclerview,lineup_recyclerview;
    private static TextView team1name,team1score,team2name,team2score,lineup_teama,lineup_teamb;
    private static final int STARTOFMATCH = -1;
    private static final int ENDOFMATCH = 1000;
    private static int selection_for_team_name;

    private static final int DURATION = 250;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailed_match_description_trial);

        Bundle bundle = getIntent().getExtras();
        OpenerActivity = bundle.getString("Activity Name");
        UniqueMatchID = bundle.getString("Match ID");
        if(bundle.getString("Age Group")!=null)
            AgeGroup = bundle.getString("Age Group");
        else
            AgeGroup = "";
        if(OpenerActivity.equals("Results"))
            databaseReference = FirebaseDatabase.getInstance().getReference().child(AgeGroup).child("Results").child(UniqueMatchID);
        else
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Live Matches").child(UniqueMatchID);

        prepare_array_list();
        team1name = (TextView)findViewById(R.id.team1name);
        team1score = (TextView)findViewById(R.id.team1score);
        team2name = (TextView)findViewById(R.id.team2name);
        team2score = (TextView)findViewById(R.id.team2score);
        lineup_teama = (TextView)findViewById(R.id.lineup_teama);
        lineup_teamb = (TextView)findViewById(R.id.lineup_teamb);
        lineupA = new ArrayList<PlayerDetails>();
        lineupB = new ArrayList<PlayerDetails>();
        eventinformationrecyclerview = (RecyclerView)findViewById(R.id.eventinformationrecyclerview);
        eventinformationrecyclerview.setAdapter(new TimeLineAdapter(this,eventinformationarraylist));
        eventinformationrecyclerview.setLayoutManager(new LinearLayoutManager(this));

        lineup_recyclerview = (RecyclerView)findViewById(R.id.lineup_recyclerview);
        lineup_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        display_lineup_of_teamA(lineup_teama);
        //Set the adapter

        Fetch_MatchDescription_From_Firebase();
    }

    public void setAdapterForLineup(){
        if(selection_for_team_name==1)
            lineup_recyclerview.setAdapter(new LineupAdapter(this,lineupA));
        else
            lineup_recyclerview.setAdapter(new LineupAdapter(this,lineupB));
    }

    public void prepare_array_list(){
        eventinformationarraylist = new ArrayList<EventInformation>();
        eventinformationarraylist.add(0,new EventInformation(STARTOFMATCH,"Match Started","","",-1));
        if(OpenerActivity.equals("Results"))
            eventinformationarraylist.add(1,new EventInformation(ENDOFMATCH,"End Of Match","","",-1));
        else
            eventinformationarraylist.add(1,new EventInformation(ENDOFMATCH,"Match Is Going On","","",-1));
    }

    public void display_lineup_of_teamA(View view){

        lineup_teama.setBackgroundColor(getResources().getColor(R.color.blue_500));
        lineup_teamb.setBackgroundColor(getResources().getColor(R.color.viewBg));
        selection_for_team_name=1;
        setAdapterForLineup();
    }

    public void display_lineup_of_teamB(View view){

        lineup_teama.setBackgroundColor(getResources().getColor(R.color.viewBg));
        lineup_teamb.setBackgroundColor(getResources().getColor(R.color.blue_500));
        selection_for_team_name=2;
        setAdapterForLineup();
    }

     public void toggletimeline(View view){
        ImageView ExpandTimeline = (ImageView)findViewById(R.id.ExpandTimeline);
        new ExpandCollapse(eventinformationrecyclerview, ExpandTimeline);
    }

    public void togglelineup(View view){
        LinearLayout layout_detail = (LinearLayout) findViewById(R.id.layout_detail);
        ImageView ExpandLineup = (ImageView)findViewById(R.id.ExpandLineup);
        new ExpandCollapse(layout_detail, ExpandLineup);
    }

    private void Fetch_MatchDescription_From_Firebase() {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                prepare_array_list();
                lineupA = new ArrayList<PlayerDetails>();
                lineupB = new ArrayList<PlayerDetails>();
                Map<String,String> tempmap = (Map<String, String>)dataSnapshot.getValue();
                team1name.setText(tempmap.get("Team A"));
                team2name.setText(tempmap.get("Team B"));
                team1score.setText(tempmap.get("Team A Goals"));
                team2score.setText(tempmap.get("Team B Goals"));
                lineup_teama.setText(tempmap.get("Team A"));
                lineup_teamb.setText(tempmap.get("Team B"));

                //Set Text Colors For Scores
                int AColor,BColor;
                if(Integer.parseInt(tempmap.get("Team A Goals"))>Integer.parseInt(tempmap.get("Team B Goals"))){
                    AColor = getResources().getColor(R.color.winning_color);
                    BColor = getResources().getColor(R.color.loosing_color);
                }
                else if(Integer.parseInt(tempmap.get("Team A Goals"))<Integer.parseInt(tempmap.get("Team B Goals"))){
                    BColor = getResources().getColor(R.color.winning_color);
                    AColor = getResources().getColor(R.color.loosing_color);
                }
                else{
                    BColor = getResources().getColor(R.color.draw_color);
                    AColor = getResources().getColor(R.color.draw_color);
                }
                team1score.setTextColor(AColor);
                team2score.setTextColor(BColor);

                DataSnapshot GoalsSnapshot = dataSnapshot.child("Goals");
                for(DataSnapshot childSnapshot : GoalsSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>) childSnapshot.getValue();
                    eventinformationarraylist.add(new EventInformation(Integer.parseInt(map.get("Time")),map.get("Player Name"),map.get("Player Jersey Number"),map.get("Team Name"),0));
                }
                DataSnapshot RedCardSnapshot = dataSnapshot.child("Red Cards");
                for(DataSnapshot childSnapshot : RedCardSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>) childSnapshot.getValue();
                    eventinformationarraylist.add(new EventInformation(Integer.parseInt(map.get("Time")),map.get("Player Name"),map.get("Player Jersey Number"),map.get("Team Name"),1));
                }
                DataSnapshot SubstitutionSnapshot = dataSnapshot.child("Substitutions");
                for(DataSnapshot childSnapshot : SubstitutionSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>) childSnapshot.getValue();
                    eventinformationarraylist.add(new EventInformation(Integer.parseInt(map.get("Time")),map.get("Player Name Out"),map.get("Player Name In"),map.get("Player Jersey Number Out"),
                            map.get("Player Jersey Number In"),map.get("Team Name")));
                }

                Collections.sort(eventinformationarraylist,new GenerateTimeLine());
                eventinformationrecyclerview.setAdapter(new TimeLineAdapter(DetailedMatchDescription.this,eventinformationarraylist));

                DataSnapshot TeamALineup = dataSnapshot.child("Lineups").child("Team A");
                for(DataSnapshot childSnapshot : TeamALineup.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();
                    lineupA.add(new PlayerDetails(map.get("Jersey Number"),map.get("Player Name")));
                    //Log.d("MY CHECK",map.get("Jersey Number")+map.get("Player Name"));
                }

                DataSnapshot TeamBLineup = dataSnapshot.child("Lineups").child("Team B");
                for(DataSnapshot childSnapshot : TeamBLineup.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();
                    lineupB.add(new PlayerDetails(map.get("Jersey Number"),map.get("Player Name")));
                    //Log.d("MY CHECK",map.get("Jersey Number")+map.get("Player Name"));
                }

                setAdapterForLineup();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

class PlayerDetails{
    String JerseyNumber,PlayerName;
    PlayerDetails(String JerseyNumber,String PlayerName){
        this.JerseyNumber = JerseyNumber;
        this.PlayerName = PlayerName;
    }
}

class EventInformation{
    String PlayerNameOut,PlayerJerseyNumberOut,PlayerJerseyNumberIn,PlayerNameIn,TeamName;
    int type,Time;
    /*
    Types :
    0 - GOALS
    1 - RED CARDS
    2 - SUBSTITUTIONS
     */
    EventInformation(int Time,String PlayerName,String PlayerJerseyNumber,String TeamName,int type){
        this.Time = Time;
        this.PlayerNameOut = PlayerName;
        this.PlayerJerseyNumberOut = PlayerJerseyNumber;
        this.type = type;
        this.PlayerJerseyNumberIn = "";
        this.PlayerNameIn = "";
        this.TeamName = TeamName;
    }

    EventInformation(int Time,String PlayerNameOut,String PlayerNameIn,String PlayerJerseyNumberOut,String PlayerJerseyNumberIn,String TeamName){
        this.Time = Time;
        this.PlayerNameOut = PlayerNameOut;
        this.PlayerJerseyNumberOut = PlayerJerseyNumberOut;
        this.PlayerNameIn = PlayerNameIn;
        this.PlayerJerseyNumberIn = PlayerJerseyNumberIn;
        this.type = 2;
        this.TeamName = TeamName;
    }
}

class GenerateTimeLine implements Comparator<EventInformation>{
    @Override
    public int compare(EventInformation o1, EventInformation o2) {
        if(o1.Time<o2.Time)
            return -1;
        if(o1.Time>o2.Time)
            return 1;
        if(o1.type<o2.type)
            return -1;
        else
            return 1;
    }
}

class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder>{

    Context context;
    ArrayList<EventInformation> arrayList;
    TimeLineAdapter(Context context,ArrayList<EventInformation> arrayList){
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.timeline_item,parent,false);
        return new TimeLineViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(TimeLineViewHolder holder, int position) {
        EventInformation eventinformation = arrayList.get(position);
        if(eventinformation.Time==-1 || eventinformation.Time==1000){
            holder.time.setVisibility(View.GONE);
            holder.playerjersey.setVisibility(View.GONE);
            holder.teamresponsible.setVisibility(View.GONE);
        }
        else {
            holder.time.setText(eventinformation.Time + " \"");
            holder.teamresponsible.setText(eventinformation.TeamName);
            holder.playerjersey.setText(eventinformation.PlayerJerseyNumberOut);
        }
        holder.playername.setText(eventinformation.PlayerNameOut);
        holder.playerjersey2.setText(eventinformation.PlayerJerseyNumberIn);
        holder.playername2.setText(eventinformation.PlayerNameIn);

        switch (eventinformation.type){
            case 0:
                holder.mTimeLineView.setMarker(ContextCompat.getDrawable(context,R.drawable.time_goal));
                break;
            case 1:
                holder.mTimeLineView.setMarker(ContextCompat.getDrawable(context,R.drawable.time_red_card));
                break;
            case 2:
                holder.mTimeLineView.setMarker(ContextCompat.getDrawable(context,R.drawable.time_substitutions));
                holder.req_layout_for_substitution.setVisibility(View.VISIBLE);
                holder.red_arrow.setVisibility(View.VISIBLE);
                break;
            default:
                holder.mTimeLineView.setMarker(ContextCompat.getDrawable(context,R.drawable.time_default));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}

class TimeLineViewHolder extends RecyclerView.ViewHolder{

    TimelineView mTimeLineView;
    TextView time,playername,playerjersey,playername2,playerjersey2,teamresponsible;
    LinearLayout req_layout_for_substitution;
    ImageView red_arrow;
    TimeLineViewHolder(View view,int viewType){
        super(view);
        mTimeLineView = (TimelineView)view.findViewById(R.id.mTimeLineView);
        time = (TextView)view.findViewById(R.id.time);
        playerjersey = (TextView)view.findViewById(R.id.playerjersey);
        playername = (TextView)view.findViewById(R.id.playername);
        playername2 = (TextView)view.findViewById(R.id.playername2);
        playerjersey2 = (TextView)view.findViewById(R.id.playerjersey2);
        req_layout_for_substitution = (LinearLayout)view.findViewById(R.id.req_layout_for_substitution);
        red_arrow = (ImageView)view.findViewById(R.id.red_arrow);
        teamresponsible = (TextView)view.findViewById(R.id.teamresponsible);
        mTimeLineView.initLine(viewType);
    }
}

class LineupAdapter extends RecyclerView.Adapter<LineupViewHolder>{

    Context context;
    ArrayList<PlayerDetails> playerDetails;
    LineupAdapter(Context context,ArrayList<PlayerDetails> playerDetails){

        this.context = context;
        this.playerDetails = playerDetails;
    }

    @Override
    public LineupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.player_description_row,parent,false);
        return new LineupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LineupViewHolder holder, int position) {
        PlayerDetails temp = playerDetails.get(position);
        holder.specific_team_player_name.setText(temp.PlayerName);
        holder.specific_team_player_jersey_number.setText(temp.JerseyNumber);
    }

    @Override
    public int getItemCount() {
        return playerDetails.size();
    }
}

class LineupViewHolder extends RecyclerView.ViewHolder{

    TextView specific_team_player_jersey_number,specific_team_player_name;
    public LineupViewHolder(View view) {
        super(view);
        specific_team_player_jersey_number = (TextView)view.findViewById(R.id.specific_team_player_jersey_number);
        specific_team_player_name = (TextView)view.findViewById(R.id.specific_team_player_name);
    }
}