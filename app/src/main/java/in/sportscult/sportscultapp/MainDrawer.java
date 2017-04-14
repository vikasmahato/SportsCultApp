package in.sportscult.sportscultapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.Stack;

import in.sportscult.sportscultapp.Utils.ExpandAndCollapseViewUtil;
import in.sportscult.sportscultapp.fragments.AboutSFLFragment;
import in.sportscult.sportscultapp.fragments.AboutUsFragment;
import in.sportscult.sportscultapp.fragments.HelpFragment;
import in.sportscult.sportscultapp.fragments.ListOfTeamsFragment;
import in.sportscult.sportscultapp.fragments.ResultsFragment;
import in.sportscult.sportscultapp.fragments.RulesFragment;
import in.sportscult.sportscultapp.fragments.SettingsFragment;
import in.sportscult.sportscultapp.fragments.TabFragment;

public class MainDrawer extends AppCompatActivity {
    public static final String SETTINGS = "settings" ;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    ViewGroup linearLayoutDetails;
    ImageView imageViewExpand;


    private static final int DURATION = 250;

    @Override
    public void onBackPressed() {
        String FRAGTAG = (getVisibleFragment().toString());
        FRAGTAG = FRAGTAG.substring(0,FRAGTAG.indexOf('{'));
        if(FRAGTAG.equals("TabFragment")){

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Are you sure you want to exit the application");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    dialog.dismiss();
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });
            alertDialog.show();
        }
        else{

            Fragment newFragment = null;
            String tag = null;
            newFragment = new TabFragment();
            tag = newFragment.toString();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.containerView, newFragment, newFragment.toString());
            //transaction.addToBackStack(tag);
            transaction.commit();
            Log.d("FRAGMENT CHECK",tag);
        }
    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainDrawer.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        FirebaseMessaging.getInstance().subscribeToTopic("news");
       // Toast.makeText(this, "news", Toast.LENGTH_SHORT).show();

        readSettings();
/**
 *Setup the DrawerLayout and NavigationView
 */
             mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
             mNavigationView = (NavigationView) findViewById(R.id.shitstuff) ;

        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */

             mFragmentManager = getSupportFragmentManager();
             mFragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = new TabFragment();
        String tag = fragment.toString();
        mFragmentTransaction.add(R.id.containerView, fragment,tag);
        mFragmentTransaction.addToBackStack(tag);
        mFragmentTransaction.commit();
        Log.d("FRAGMENT CHECK",tag);
            // mFragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();
        /**
         * Setup click events on the Navigation View Items.
         */

             mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
             @Override
             public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                 Fragment newFragment = null;
                 String tag = null;
                 int id = menuItem.getItemId();

                 if (id == R.id.nav_teams) {

                     newFragment = new ListOfTeamsFragment();
                     tag = newFragment.toString();
//
//                     Intent teamsIntent = new Intent(getBaseContext(), ListOfTeams.class);
//                     startActivity(teamsIntent);

                 }else if (id == R.id.nav_home) {
                     newFragment = new TabFragment();
                     tag = newFragment.toString();
                 }  else if (id == R.id.nav_registration) {
                     newFragment = new RegistrationFragment();
                     tag = newFragment.toString();
                 } else if (id == R.id.nav_about) {
                     newFragment = new AboutUsFragment();
                     tag = newFragment.toString();
                 } else if (id == R.id.nav_rules) {
                     newFragment = new RulesFragment();
                     tag = newFragment.toString();
                 } else if (id == R.id.nav_about_sfl) {
                     newFragment = new AboutSFLFragment();
                     tag = newFragment.toString();
                 } else if (id == R.id.nav_help) {
                     newFragment = new HelpFragment();
                     tag = newFragment.toString();
                 }
                    else if(id == R.id.nav_settings){
                     newFragment = new SettingsFragment();
                     tag = newFragment.toString();

                 } else if (id == R.id.nav_match_details) {
                     newFragment = new ResultsFragment();
                     tag = newFragment.toString();
                }

                if(newFragment!=null && tag != null) {
                    FragmentTransaction transaction = mFragmentManager.beginTransaction();
                    transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    transaction.replace(R.id.containerView, newFragment, newFragment.toString());
                    transaction.addToBackStack(tag);
                    transaction.commit();
                    Log.d("FRAGMENT CHECK",tag);
                }
                 return false;
            }

        });



                android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
                ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

                mDrawerLayout.setDrawerListener(mDrawerToggle);

                mDrawerToggle.syncState();

    }

    /**
     * reads settings from shared preferences and subscribes to topics for notification
     */
    private void readSettings() {
        SharedPreferences sharedPref = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        boolean live_match = sharedPref.getBoolean(getString(R.string.live_match),  true);
        boolean live_score = sharedPref.getBoolean(getString(R.string.live_score),  true);

        if(live_match) FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.live_match));
        if(live_score) FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.live_score));


    }

    /**
     * Launches E-mail intent to send email using Installed email client
     * Called from HelpFragment
     * @param v The view that launches the intent
     */
    public void sendEmail(View v){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","sportscultprototype@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SFL Enquiry");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    /**
     * Expands and collapses the Request call card in help  fragment
     * @param view
     */
    public void toggleDetails(View view) {

        linearLayoutDetails = (ViewGroup) findViewById(R.id.linearLayoutDetails);
        imageViewExpand = (ImageView) findViewById(R.id.imageViewExpand);

        if (linearLayoutDetails.getVisibility() == View.GONE) {
            ExpandAndCollapseViewUtil.expand(linearLayoutDetails, DURATION);
            imageViewExpand.setImageResource(R.drawable.ic_expand_more_black_24dp);
            rotate(-180.0f);
        } else {
            ExpandAndCollapseViewUtil.collapse(linearLayoutDetails, DURATION);
            imageViewExpand.setImageResource(R.drawable.ic_expand_less_black_24dp);
            rotate(180.0f);
        }
    }

    /**
     * Animates the arrow button in Request a call card in HelpFragment
     * @param angle
     */
    private void rotate(float angle) {
        Animation animation = new RotateAnimation(0.0f, angle, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(DURATION);
        imageViewExpand.startAnimation(animation);
    }

    /**
     * Launches the google navigation app to show directions to qHub Football field
     * @param view
     */
    public void getDirections(View view){

        Uri gmmIntentUri = Uri.parse("google.navigation:q=qhub+by+Quantum+Sports");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");


        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }else {
            Toast.makeText(this, "No Application to View maps", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Launches google maps to pinpoint qHub football field on a map
     * @param view
     */
    public void viewLocation(View view){
        Uri gmmIntentUri = Uri.parse("geo:28.4219738,77.1348673");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }else {
            Toast.makeText(this, "No Application to View maps", Toast.LENGTH_SHORT).show();
        }
    }

    public void setSettings(){
        SharedPreferences sharedpreferences;
        sharedpreferences = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putBoolean("live_match", true);
        editor.putBoolean("score", true);
        editor.commit();
    }

    public static void DefaultProfilePic(String TeamName,TextView textView){
        textView.setVisibility(View.VISIBLE);
        textView.setText(shortenName(TeamName));
    }

    private static String shortenName(String TeamName){
        String name = "";
        String s[] = TeamName.split(" ");
        if(s.length==1)
            name+=(TeamName.charAt(0));
        else if(s.length>1){
            for(int i=0;i<2;i++)
                name+=s[i].charAt(0);
        }
        return name;
    }
}