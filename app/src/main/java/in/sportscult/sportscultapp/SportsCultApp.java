package in.sportscult.sportscultapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Vishal on 31-Mar-17.
 */

public class SportsCultApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
