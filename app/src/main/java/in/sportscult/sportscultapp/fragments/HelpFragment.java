package in.sportscult.sportscultapp.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import in.sportscult.sportscultapp.R;

public class HelpFragment  extends Fragment {

    private static EditText name,number;
    private static Button requestcall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.content_help,null);

        name = (EditText)view.findViewById(R.id.name);
        number = (EditText)view.findViewById(R.id.number);
        requestcall = (Button)view.findViewById(R.id.requestcall);
        requestcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request_a_call(view);
            }
        });
        return view;

    }

    public void request_a_call(View view1){

        final View view = view1;
        String Name = name.getText().toString();
        String Number = number.getText().toString();

        if(Name.length()<3){
            name.setError("Name Too Short");
            name.requestFocus();
            return;
        }
        if(Number.length()<6 || Number.length()>13){
            number.setError("Invalid Contact Number");
            number.requestFocus();
            return;
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Call Requests");
        Map<String,String> map = new HashMap<String,String>();
        map.put("Name",Name.trim().toUpperCase());
        map.put("Contact Number",Number);
        databaseReference.push().setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(),"Your Request Has Been Posted",Toast.LENGTH_LONG).show();
                name.setText("");
                number.setText("");
                ((LinearLayout)view.findViewById(R.id.linearLayoutDetails)).setVisibility(View.GONE);
            }
        });
    }

}
