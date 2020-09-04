package fr.ubordeaux.rlasvenes.projettechandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_FRAGMENT = "fragment_save";
    private static final String TAG = "MainActivity";

    private long time;
    private Toast toast;

    private static String mFragment;
    private final WelcomeFragment welcome_frag = new WelcomeFragment();
    private final ChoosePictureFragment choosePic_frag =new ChoosePictureFragment();
    private final WorkFragment work_frag = new WorkFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Started.");

        if(savedInstanceState != null)
            mFragment = savedInstanceState.getString(KEY_FRAGMENT);
        else
            mFragment = getIntent().getStringExtra(KEY_FRAGMENT);

        if (mFragment != null) {
            if(mFragment.equals(((Object) welcome_frag).getClass().getSimpleName()))
                showFragment(this.welcome_frag);
            else if(mFragment.equals(((Object) choosePic_frag).getClass().getSimpleName()))
                showFragment(this.choosePic_frag);
            else if(mFragment.equals(((Object) work_frag).getClass().getSimpleName()))
                showFragment(this.work_frag);
        } else
            showFragment(this.welcome_frag);


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_FRAGMENT, mFragment != null ? mFragment: "");
        super.onSaveInstanceState(outState);
    }

    private void showFragment(final Fragment fragment) {
        if (fragment == null)
            return;

        // Update current fragment
        mFragment = ((Object) fragment).getClass().getSimpleName();
        // Begin a fragment transaction
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        // animate the changing fragment
        // Replace current fragment by a new one
        ft.replace(R.id.containter, fragment);
        ft.addToBackStack(null);
        //commit changes
        ft.commit();
    }

    public void goToChoosePicFragment(){
        showFragment(this.choosePic_frag);
    }

    public void goToWorkFragment(String uri, boolean checkedSwitch){
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        bundle.putBoolean("switch", checkedSwitch);
        work_frag.setArguments(bundle);

        Log.d(TAG, uri);

        showFragment(this.work_frag);
    }

    @Override
    public void onBackPressed() {
        Log.i("TAG", "BACK PRESSED");

        if(time+2000 > System.currentTimeMillis()){
            toast.cancel();
            Log.i("TAG", "Quit");
            finish();
            return;
        } else {
            toast = Toast.makeText(getApplicationContext(),"Press 2 times to exit completely the App!",
                    Toast.LENGTH_LONG);
            toast.show();
        }

        time = System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("TAG", "APPS STARTED");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("TAG", "APPS STOPPED");
    }

    @Override
    protected void onDestroy() {
        Log.i("TAG", "APPS DESTROYED");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("TAG", "APPS PAUSED");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TAG", "APPS RESUMED");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("TAG", "APPS RESTARTED");
    }



}
