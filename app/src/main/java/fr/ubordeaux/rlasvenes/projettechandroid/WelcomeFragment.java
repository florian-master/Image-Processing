package fr.ubordeaux.rlasvenes.projettechandroid;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class WelcomeFragment extends Fragment {
    private static int SPLASH_TIME = 1000;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
         View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);

         //Code to start timer and take action after the timer ends
         new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity)getActivity()).goToChoosePicFragment();
            }
        }, SPLASH_TIME);
        return rootView;
     }
}
