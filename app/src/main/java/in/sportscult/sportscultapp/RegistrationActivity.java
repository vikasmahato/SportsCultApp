package in.sportscult.sportscultapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText reg_team_name,reg_coach_name,reg_coach_contact,reg_coach_email,reg_password,reg_confirm_password;
    private String team_name,coach_name,coach_contact,coach_email,age_group,location,password,confirm_password;
    private static ListView player_list;
    private static final int GAlLERY_ACCESS = 1000;
    private static Uri id_proof_scan_uri;
    private static ArrayList<String> names_of_players,contact_of_players;
    private static ArrayList<Uri> uri_of_players;
    private static View AlertDialogView;
    private static Player_List_Adapter player_list_adapter;
    static View focus;
    static boolean verification_success;
    private static ProgressDialog progressDialog;
    //Provide the minimum number of permissable team members in a team
    private static final int minimum_number_of_players = 2;

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReference1;
    private StorageReference storageReference;

    //Add Team Profile Pic, Group Jersey,Password Field
    //Dropdown Menu for Location, Age Group
    //Under the player description, add jersey number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        reg_team_name = (EditText)findViewById(R.id.reg_team_name);
        reg_coach_name = (EditText)findViewById(R.id.reg_coach_name);
        reg_coach_email = (EditText)findViewById(R.id.reg_coach_email);
        reg_coach_contact = (EditText)findViewById(R.id.reg_coach_contact);
        reg_password = (EditText)findViewById(R.id.reg_password);
        reg_confirm_password = (EditText)findViewById(R.id.reg_confirm_password);
        player_list = (ListView) findViewById(R.id.player_list);
        names_of_players = new ArrayList<String>();
        contact_of_players = new ArrayList<String>();
        uri_of_players = new ArrayList<Uri>();

        //Comment this section out after adding additional functionality for them
        age_group = "Group - A";
        location = "Rohini";
        //Till here

        test();
    }

    public void test(){

        //player_list = (ListView) findViewById(R.id.player_list);
//        int len = names_of_players.size();
//        String namearray[] = new String[len];
//        String contactsarray[] = new String[len];
//        Uri uriarray[] = new Uri[len];
//        for(int i=0;i<len;i++) {
//            namearray[i] = names_of_players.get(i);
//            contactsarray[i] = contact_of_players.get(i);
//            uriarray[i] = uri_of_players.get(i);
//        }
//
//        player_list_adapter = new Player_List_Adapter(this,namearray,contactsarray,uriarray);
//        player_list.setAdapter(player_list_adapter);
        player_list_adapter = new Player_List_Adapter(this,names_of_players,contact_of_players,uri_of_players);
        player_list.setAdapter(player_list_adapter);

    }

    //Add a new player
    public void add_new_player(View view){

        id_proof_scan_uri = null;
        AlertDialogView = LayoutInflater.from(RegistrationActivity.this).inflate(R.layout.add_new_player,null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegistrationActivity.this);
        alertDialog.setView(AlertDialogView);
        alertDialog.setTitle("Enter The Player Details");
        alertDialog.setCancelable(false);
        final EditText player_name = (EditText)AlertDialogView.findViewById(R.id.player_name);
        final EditText player_contact = (EditText)AlertDialogView.findViewById(R.id.player_contact);
        final Button upload_id_proof_scan = (Button)AlertDialogView.findViewById(R.id.upload_id_proof_scan);

        upload_id_proof_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,GAlLERY_ACCESS);
            }
        });

        alertDialog.setPositiveButton("Add Player", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String playername = player_name.getText().toString();
                String playercontact = player_contact.getText().toString();

                //Image uri is already present in the variable id_proof_scan_uri

                if(player_contact.length()<6 || player_name.length()==0 || id_proof_scan_uri==null){
                }
                else{

                    //Now add all the data to the list view data set
                    names_of_players.add(playername);
                    contact_of_players.add(playercontact);
                    uri_of_players.add(id_proof_scan_uri);
                    test();
                    //player_list_adapter.notifyDataSetChanged();
                }

                dialog.cancel();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GAlLERY_ACCESS && resultCode==RESULT_OK){
            ImageView id_proof_scan = (ImageView) AlertDialogView.findViewById(R.id.id_proof_scan);
            id_proof_scan_uri = data.getData();
            id_proof_scan.setVisibility(View.VISIBLE);
            id_proof_scan.setImageURI(id_proof_scan_uri);
        }
    }

    //TIll here Add A New Player



    //Register Your Team
    public void register_team(View view){

        progressDialog = new ProgressDialog(RegistrationActivity.this);
        progressDialog.setMessage("Registering Your Team...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //Get the contents from fields
        team_name = reg_team_name.getText().toString().trim().toUpperCase();
        coach_name = reg_coach_name.getText().toString().trim().toUpperCase();
        coach_contact = reg_coach_contact.getText().toString().trim();
        coach_email = reg_coach_email.getText().toString().trim();
        password = reg_password.getText().toString().trim();
        confirm_password = reg_confirm_password.getText().toString().trim();

        //Verify if the details were entered correctly
        boolean correct = verify_details();

        if(!correct) {
            progressDialog.dismiss();
            return;
        }

        //Verify if a team name with the same name in that age group already exists
//        correct = verify_team_name();
//        if(!correct){
//            progressDialog.dismiss();
//            return;
//        }

        //Add Team Name along with payment status and password
        databaseReference1 = FirebaseDatabase.getInstance().getReference().child(age_group).child("Team Names").child(team_name);
        databaseReference1.child("Payment Status").setValue("No");
        databaseReference1.child("Password").setValue(password);

        //Add Team Details
        storageReference = FirebaseStorage.getInstance().getReference().child(age_group).child(team_name);
        //Add Team Profile Pic
        //storageReference.child("Team Profile Pic").putFile(uri_of_team_profile_pic);
        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group).child(team_name);
        databaseReference.child("Coach Name").setValue(coach_name);
        databaseReference.child("Contact Number").setValue(coach_contact);
        databaseReference.child("Email").setValue(coach_email);
        databaseReference.child("Location").setValue(location);

        //Adding information about players
        storageReference = storageReference.child("Player ID Scans");
        databaseReference.child("Number Of Players").setValue(names_of_players.size());
        databaseReference = databaseReference.child("Players");
        for(int i=0;i<names_of_players.size();i++){
            //Remove this player code after creating a proper one
            String player_code = ""+((char)(i+65));
            //Proper player code
            //name_of_player+jersey_number_of_player
            storageReference.child(player_code).putFile(uri_of_players.get(i));
            DatabaseReference tempreference = databaseReference.child(player_code);
            tempreference.child("Name").setValue(names_of_players.get(i));
            tempreference.child("Contact").setValue(contact_of_players.get(i));
        }

        progressDialog.dismiss();
        //Start a new Activity here which take to payment page
    }

    boolean verify_details(){

        focus = null;
        verification_success = true;
        if(password.length()<6){
            reg_confirm_password.setText("");
            reg_password.setError("Password Must Atleast 6 characters");
            focus = reg_password;
            verification_success = false;
        }
        else if(!password.equals(confirm_password)){
            reg_confirm_password.setError("Passwords Don't Match");
            focus = reg_confirm_password;
            verification_success = false;
        }
        if(coach_email.length()==0){
            reg_coach_email.setError(getString(R.string.empty_field));
            focus = reg_coach_email;
            verification_success = false;
        }
        else if(!(coach_email.contains("@") && coach_email.contains(".com"))){
            reg_coach_email.setError("Invalid Email");
            focus = reg_coach_email;
            verification_success = false;
        }
        if(coach_contact.length()==0){
            reg_coach_contact.setError(getString(R.string.empty_field));
            focus = reg_coach_contact;
            verification_success = false;
        }
        else if(coach_contact.length()<6){
            reg_coach_contact.setError("Invalid Contact Number");
            focus = reg_coach_contact;
            verification_success = false;
        }
        if(coach_name.length()==0) {
            reg_coach_name.setError(getString(R.string.empty_field));
            focus = reg_coach_name;
            verification_success = false;
        }
        if(team_name.length()==0){
            reg_team_name.setError(getString(R.string.empty_field));
            focus = reg_team_name;
            verification_success = false;
        }
        if(names_of_players.size()<minimum_number_of_players){
            Toast.makeText(RegistrationActivity.this,("You need to add atleast "+(minimum_number_of_players-names_of_players.size())+" players."),Toast.LENGTH_LONG).show();
            verification_success = false;
            if(focus==null)
                focus = reg_team_name;
        }

        if(!verification_success)
            focus.requestFocus();
        return verification_success;
    }

    boolean verify_team_name(){
        verification_success = true;
        DatabaseReference tempDatabaseReference = FirebaseDatabase.getInstance().getReference().child(age_group).child("Team Names");
        tempDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String,String> map = (Map<String,String>)dataSnapshot.getValue();
                    if(map.get(team_name)!=null){
                        reg_team_name.setError("Team Name Already Exists");
                        reg_team_name.requestFocus();
                        verification_success = false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        return verification_success;
    }

    //Till here Register Your Team


    //Adapter for displaying player details in listview
    class Player_List_Adapter extends ArrayAdapter<String>{

        ArrayList<String> names,contacts;
        ArrayList<Uri> uris;
        Context context;

        public Player_List_Adapter(Context context,ArrayList<String> names,ArrayList<String> contacts,ArrayList<Uri> uris) {
            super(context, R.layout.player_row,names);
            this.context = context;
            this.names = names;
            this.contacts = contacts;
            this.uris = uris;
        }

        public class ViewHolder{
            TextView individual_name,individual_contact;
            ImageView individual_id_proof;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

//            if(convertView==null){
//                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                convertView = layoutInflater.inflate(R.layout.player_row,null);
//            }
//
//            ViewHolder viewHolder = new ViewHolder();
//            viewHolder.individual_name = (TextView)convertView.findViewById(R.id.individual_name);
//            viewHolder.individual_contact = (TextView)convertView.findViewById(R.id.individual_contact);
//            viewHolder.individual_id_proof = (ImageView)convertView.findViewById(R.id.individual_id_proof);
//
//            viewHolder.individual_name.setText(names[position]);
//            viewHolder.individual_contact.setText(contacts[position]);
//            viewHolder.individual_id_proof.setImageURI(uris[position]);
//
//            return convertView;

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View current_view = layoutInflater.inflate(R.layout.player_row,parent,false);

            TextView individual_name = (TextView) current_view.findViewById(R.id.individual_name);
            TextView individual_contact = (TextView) current_view.findViewById(R.id.individual_contact);
            ImageView individual_id_proof = (ImageView) current_view.findViewById(R.id.individual_id_proof);

            individual_name.setText(names.get(position));
            individual_contact.setText(contacts.get(position));
            individual_id_proof.setImageURI(uris.get(position));

            return current_view;
        }
    }


}
