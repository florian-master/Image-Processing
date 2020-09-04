package fr.ubordeaux.rlasvenes.projettechandroid;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class ChoosePictureFragment extends Fragment {
    private Integer SELECT_IMAGE=1;
    private File imageFile;
    Switch javaRsSwitch;
    View rootView;
    boolean checkedSwitch;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        rootView = inflater.inflate(R.layout.fragment_choose_picture, container, false);
        javaRsSwitch = rootView.findViewById(R.id.javaRs_Switch);
        javaRsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheched) {
                if (isCheched){
                    Toast.makeText(getActivity(),"Java",Toast.LENGTH_LONG).show();
                    rootView.setBackgroundColor(Color.rgb(216,96,96));
                }else {
                    Toast.makeText(getActivity(), "RenderScript", Toast.LENGTH_LONG).show();
                    rootView.setBackgroundColor(Color.WHITE);
                }
                checkedSwitch = isCheched;
            }
        });

        Button camera_btn = rootView.findViewById(R.id.open_camera);
        Button gallery_btn = rootView.findViewById(R.id.open_gallery);
        ImageView camera_pic = rootView.findViewById(R.id.camera_icon);
        ImageView gallery_pic = rootView.findViewById(R.id.gallery_icon);

        // Button listener for the camera and gallery
        camera_btn.setOnClickListener(camera);
        gallery_btn.setOnClickListener(gallery);
        camera_pic.setOnClickListener(camera);
        gallery_pic.setOnClickListener(gallery);

        return rootView;

    }

    private View.OnClickListener camera = new View.OnClickListener() {
        public void onClick(View v) {
            String[] permissions = {android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if(Build.VERSION.SDK_INT>=23) {
                requestPermissions(permissions, 2);
            }
            Log.i("TAG", "Calling camera");
            take_pic();
        }
    };

    private View.OnClickListener gallery = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i("TAG", "Calling gallery");
            open_gallery();

        }
    };
    /**
     * Call the camera service
     */
    public void take_pic() {
        // Creating an explicit intent for calling the camera service
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create an image file name from the current date
        String date =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir= getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            imageFile = File.createTempFile("Camera_"+date, ".jpg", storageDir);

            // Get the image uri
            Uri tempUri = FileProvider.getUriForFile(this.getContext(),
                    "fr.ubordeaux.rlasvenes.projettechandroid.provider", imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
            // Open the intent
            startActivityForResult(intent, 0);
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Unable to create a file", Toast.LENGTH_SHORT).show();
            Log.e("Error","Unable to create a file");
            e.printStackTrace();
        }
    }

    /**
     * Call the gallery service
     */
    public void open_gallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_IMAGE);
    }

    /**
     * Get the picture (the result) from the camera activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode, data);

        int orientation = getActivity().getResources().getConfiguration().orientation;

        if(resultCode== Activity.RESULT_OK){
            String uri;
            Integer SELECT_CAMERA = 0;
            if(requestCode== SELECT_CAMERA){
                // Get the uri
                uri =  Uri.fromFile(imageFile).toString();
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ((MainActivity) getActivity()).goToWorkFragment(uri,checkedSwitch);
                }else{
                    ((MainActivity) getActivity()).goToWorkFragment(uri,checkedSwitch);
                }

            }else if(requestCode==SELECT_IMAGE) {
                // Get the uri
                uri = data.getData().toString();
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ((MainActivity) getActivity()).goToWorkFragment(uri,checkedSwitch);
                } else{
                    ((MainActivity) getActivity()).goToWorkFragment(uri,checkedSwitch);
                }

            } else {
                // An error occurred
                Toast.makeText(getActivity(), "Unable to pick up a picture", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getActivity(), "Unable to pick up a picture", Toast.LENGTH_SHORT).show();
    }

}
