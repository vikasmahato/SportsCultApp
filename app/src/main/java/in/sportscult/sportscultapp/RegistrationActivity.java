package in.sportscult.sportscultapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText reg_team_name,reg_coach_name,reg_coach_contact,reg_coach_email,reg_password,reg_confirm_password;
    private String team_name,coach_name,coach_contact,coach_email,age_group,location,password,confirm_password;
    static boolean unique_team_name = true;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference databaseReference1;

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

        //Comment this section out after adding additional functionality for them
        age_group = "A";
        location = "Rohini";
        //Till here

    }

    public void register_team(View view){

        //Get the contents from fields
        team_name = reg_team_name.toString().trim().toUpperCase();
        coach_name = reg_coach_name.toString().trim().toUpperCase();
        coach_contact = reg_coach_contact.toString().trim();
        coach_email = reg_coach_email.toString().trim();
        password = reg_password.toString().trim();
        confirm_password = reg_confirm_password.toString().trim();

        //Verify if the details were entered correctly
        boolean correct = verify_details();

        if(!correct)
            return;

        databaseReference1 = databaseReference.child(age_group).child("Team Names");

        //Verify if a team name with the same name in that age group already exists
        correct = verify_team_name();
        if(!correct)
            return;

        //Add Team Name along with payment status
        databaseReference1.child(team_name).setValue("No");

        databaseReference = databaseReference.child(age_group).child(team_name);
        databaseReference.child("Coach Name").setValue(coach_name);
        databaseReference.child("Contact Number").setValue(coach_contact);
        databaseReference.child("Email").setValue(coach_email);
        databaseReference.child("Location").setValue(location);

    }

    boolean verify_details(){

        View focus = null;
        boolean verification_success = true;
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

        if(!verification_success)
            focus.requestFocus();
        return verification_success;
    }

    boolean verify_team_name(){

        View focus = null;
        boolean verification_success = true;
            databaseReference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String,String> map = (Map<String,String>)dataSnapshot.getValue();
                    if(map.get(team_name)!=null)
                        unique_team_name = false;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            if(!unique_team_name){
                reg_team_name.setError("Team Name Already Exists");
                focus = reg_team_name;
                verification_success = false;
            }

        if(!verification_success)
            focus.requestFocus();
        return verification_success;
    }
}
