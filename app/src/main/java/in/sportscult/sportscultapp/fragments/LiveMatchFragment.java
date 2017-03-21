package in.sportscult.sportscultapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

public class LiveMatchFragment extends Fragment {

    private ListView Live_Matches_List;
    private ArrayList<LiveMatch> liveMatchArrayList;
    private Map<String,String> team_profile_pic_download_urls;
    private LiveMatchAdapter liveMatchAdapter;
    private ProgressDialog progressDialog;

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
        Live_Matches_List = (ListView)view.findViewById(R.id.Live_Matches_List);
        team_profile_pic_download_urls = new HashMap<String, String>();
        liveMatchArrayList = new ArrayList<LiveMatch>();

        Fetch_Live_Matches_From_Firebase();

        return view;
    }

    public void Fetch_Live_Matches_From_Firebase(){

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data....");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Live Matches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                liveMatchArrayList = new ArrayList<LiveMatch>();
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    liveMatchAdapter = new LiveMatchAdapter(getActivity(),liveMatchArrayList,team_profile_pic_download_urls);
                    Live_Matches_List.setAdapter(liveMatchAdapter);
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();
                    final LiveMatch liveMatch = new LiveMatch(map.get("Team A"),map.get("Team B"),map.get("Team A Goals"),
                            map.get("Team B Goals"),map.get("Venue"),map.get("Start Time"),map.get("Age Group"));
                    liveMatchArrayList.add(liveMatch);

                    databaseReference.child(liveMatch.AgeGroup).child("Team Names").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()) {
                                Map<String, String> urlmap = (Map<String, String>) ChildSnapshot.getValue();
                                team_profile_pic_download_urls.put(liveMatch.AgeGroup + ChildSnapshot.getKey(), urlmap.get("Team Profile Pic Thumbnail Url"));
                            }
                            progressDialog.dismiss();
                            liveMatchAdapter = new LiveMatchAdapter(getActivity(),liveMatchArrayList,team_profile_pic_download_urls);
                            Live_Matches_List.setAdapter(liveMatchAdapter);
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
}

class LiveMatch{
    String TeamA,TeamB,TeamAGoals,TeamBGoals,Venue,StartTime,AgeGroup;
    LiveMatch(String TeamA,String TeamB,String TeamAGoals,String TeamBGoals,String Venue,String StartTime,String AgeGroup){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.TeamAGoals = TeamAGoals;
        this.TeamBGoals = TeamBGoals;
        this.Venue = Venue;
        this.StartTime = StartTime;
        this.AgeGroup = AgeGroup;
    }
}

class LiveMatchAdapter extends ArrayAdapter<LiveMatch>{

    ArrayList<LiveMatch> arrayListForLiveMatch;
    Map<String,String> urlMap;
    Context context;

    LiveMatchAdapter(Context context,ArrayList<LiveMatch> arrayListForLiveMatch,Map<String,String> urlMap){
        super(context,R.layout.live_match_card,arrayListForLiveMatch);
        this.arrayListForLiveMatch = arrayListForLiveMatch;
        this.context = context;
        this.urlMap = urlMap;
    }

    class Viewholder3{
        TextView teamA_name,teamB_name,live_match_score_A,live_match_score_B,live_match_start_time,live_match_venue;
        ImageView teamA_image,teamB_image;
        Viewholder3(View view){
            teamA_name = (TextView)view.findViewById(R.id.teamA_name);
            teamB_name = (TextView)view.findViewById(R.id.teamB_name);
            live_match_score_A = (TextView)view.findViewById(R.id.live_match_score_A);
            live_match_score_B = (TextView)view.findViewById(R.id.live_match_score_B);
            live_match_start_time = (TextView)view.findViewById(R.id.live_match_start_time);
            live_match_venue = (TextView)view.findViewById(R.id.live_match_venue);
            teamA_image = (ImageView)view.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)view.findViewById(R.id.teamB_image);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        Viewholder3 viewHolder = null;
        if(row==null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.live_match_card,parent,false);
            viewHolder = new Viewholder3(row);
            row.setTag(viewHolder);
        }
        else{
            viewHolder = (Viewholder3) row.getTag();
        }

        LiveMatch data = arrayListForLiveMatch.get(position);
        viewHolder.teamA_name.setText(data.TeamA);
        viewHolder.teamB_name.setText(data.TeamB);
        viewHolder.live_match_score_A.setText(data.TeamAGoals);
        viewHolder.live_match_score_B.setText(data.TeamBGoals);
        viewHolder.live_match_start_time.setText("Start Time : " + data.StartTime);
        viewHolder.live_match_venue.setText("Venue : " + data.Venue);

        final ImageView tempImageViewA = viewHolder.teamA_image;
        final ImageView tempImageViewB = viewHolder.teamB_image;
        final String urlA = urlMap.get(data.AgeGroup + data.TeamA);
        final String urlB = urlMap.get(data.AgeGroup + data.TeamB);
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
/*
package in.sportscult.sportscultapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

public class LiveMatchFragment extends Fragment {

    private ListView Live_Matches_List;
    private ArrayList<LiveMatch> liveMatchArrayList;
    private Map<String,String> team_profile_pic_download_urls;
    private LiveMatchAdapter liveMatchAdapter;
    private ProgressDialog progressDialog;

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
        Live_Matches_List = (ListView)view.findViewById(R.id.Live_Matches_List);
        team_profile_pic_download_urls = new HashMap<String, String>();
        liveMatchArrayList = new ArrayList<LiveMatch>();

        Fetch_Live_Matches_From_Firebase();

        return view;
    }

    public void Fetch_Live_Matches_From_Firebase(){

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data....");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Live Matches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                liveMatchArrayList = new ArrayList<LiveMatch>();
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    liveMatchAdapter = new LiveMatchAdapter(getActivity(),liveMatchArrayList,team_profile_pic_download_urls);
                    Live_Matches_List.setAdapter(liveMatchAdapter);
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();
                    final LiveMatch liveMatch = new LiveMatch(map.get("Team A"),map.get("Team B"),map.get("Team A Goals"),
                            map.get("Team B Goals"),map.get("Venue"),map.get("Start Time"),map.get("Age Group"));
                    liveMatchArrayList.add(liveMatch);

                    databaseReference.child(liveMatch.AgeGroup).child("Team Names").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()) {
                                Map<String, String> urlmap = (Map<String, String>) ChildSnapshot.getValue();
                                team_profile_pic_download_urls.put(liveMatch.AgeGroup + ChildSnapshot.getKey(), urlmap.get("Team Profile Pic Thumbnail Url"));
                            }
                            progressDialog.dismiss();
                            liveMatchAdapter = new LiveMatchAdapter(getActivity(),liveMatchArrayList,team_profile_pic_download_urls);
                            Live_Matches_List.setAdapter(liveMatchAdapter);
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
}

class LiveMatch{
    String TeamA,TeamB,TeamAGoals,TeamBGoals,Venue,StartTime,AgeGroup;
    LiveMatch(String TeamA,String TeamB,String TeamAGoals,String TeamBGoals,String Venue,String StartTime,String AgeGroup){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.TeamAGoals = TeamAGoals;
        this.TeamBGoals = TeamBGoals;
        this.Venue = Venue;
        this.StartTime = StartTime;
        this.AgeGroup = AgeGroup;
    }
}

class LiveMatchAdapter extends ArrayAdapter<LiveMatch>{

    ArrayList<LiveMatch> arrayListForLiveMatch;
    Map<String,String> urlMap;
    Context context;

    LiveMatchAdapter(Context context,ArrayList<LiveMatch> arrayListForLiveMatch,Map<String,String> urlMap){
        super(context,R.layout.live_match_card,arrayListForLiveMatch);
        this.arrayListForLiveMatch = arrayListForLiveMatch;
        this.context = context;
        this.urlMap = urlMap;
    }

    class Viewholder3{
        TextView teamA_name,teamB_name,live_match_score_A,live_match_score_B,live_match_start_time,live_match_venue;
        ImageView teamA_image,teamB_image;
        Viewholder3(View view){
            teamA_name = (TextView)view.findViewById(R.id.teamA_name);
            teamB_name = (TextView)view.findViewById(R.id.teamB_name);
            live_match_score_A = (TextView)view.findViewById(R.id.live_match_score_A);
            live_match_score_B = (TextView)view.findViewById(R.id.live_match_score_B);
            live_match_start_time = (TextView)view.findViewById(R.id.live_match_start_time);
            live_match_venue = (TextView)view.findViewById(R.id.live_match_venue);
            teamA_image = (ImageView)view.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)view.findViewById(R.id.teamB_image);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        Viewholder3 viewHolder = null;
        if(row==null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.live_match_card,parent,false);
            viewHolder = new Viewholder3(row);
            row.setTag(viewHolder);
        }
        else{
            viewHolder = (Viewholder3) row.getTag();
        }

        LiveMatch data = arrayListForLiveMatch.get(position);
        viewHolder.teamA_name.setText(data.TeamA);
        viewHolder.teamB_name.setText(data.TeamB);
        viewHolder.live_match_score_A.setText(data.TeamAGoals);
        viewHolder.live_match_score_B.setText(data.TeamBGoals);
        viewHolder.live_match_start_time.setText("Start Time : " + data.StartTime);
        viewHolder.live_match_venue.setText("Venue : " + data.Venue);

        final ImageView tempImageViewA = viewHolder.teamA_image;
        final ImageView tempImageViewB = viewHolder.teamB_image;
        final String urlA = urlMap.get(data.AgeGroup + data.TeamA);
        final String urlB = urlMap.get(data.AgeGroup + data.TeamB);
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
*/