package fr.ubordeaux.rlasvenes.projettechandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import Effects.*;
import zoom.PhotoView;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static fr.ubordeaux.rlasvenes.projettechandroid.R.id.picture;


public class WorkFragment extends Fragment {

    private Bitmap origBmp, tmp, inBmp, outBmp;

    // Stack Effect function's macro
    private EffectStack stack;
    private static final int UNDO = -1;
    private static final int SAVE = 0;
    private static final int REDO = 1;
    private static final int QUIT = 2;

    // Interaction variables
    private ProgressBar progress_bar;
    private ProgressDialog popup;
    private int matrixSize = 0;
    private final CharSequence[] typeList={"Average Blur" ,"Gaussian Blur", "Gaussian Blur Intrinsic",
                    "Sobel", "Prewitt", "Laplace cx4", "Laplace cx8", "Emboss", "Cartoon", "Pencil"};
    private final CharSequence[] sizeList={"3x3" ,"5x5", "7x7", "11x11", "25x25"};

    private SeekBar seek_bar;
    private ImageView img_view, hue_gradient;
    private View layout;
    private TextView indicator;

    // if true run java version else renderscript
    private boolean version = true;

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstance) {
        View rootView = inflater.inflate(R.layout.fragment_work, container, false);
        Bundle bundle = getArguments();
        Uri selectedImageUri;
        if (bundle != null) {
            selectedImageUri = Uri.parse(bundle.getString("uri"));
            version = bundle.getBoolean("switch");
        } else {
            String msg = "Unable to get the image URI";
            Toast.makeText(getActivity(),msg, Toast.LENGTH_SHORT).show();
            Log.e("Image", msg);
            return rootView;
        }

        // Change the color of the layout
        if (version){
            View view = rootView.findViewById(R.id.effect_menu);
            view.setBackgroundColor(Color.rgb(216,96,96));
        }

        try {
            // Creating the bitmap from the URI
            origBmp = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
            tmp = origBmp.copy(origBmp.getConfig(), true);
        } catch (IOException e) {
            Log.e("Error", "Unable to Open the Image");
            e.printStackTrace();
        }

        // Checking the size
        int scale = 1;
        int width = origBmp.getWidth();
        int height = origBmp.getHeight();
        int origSize = (width*height)/1000000;

        // Computing the scale ratio
        if(origSize >= 1)
            scale = (int) ((Math.log(origSize)/Math.log(2))+1);

        // Scale the image
        inBmp = Bitmap.createScaledBitmap(origBmp, width/scale, height/scale,true);

        Log.i("Image Resized to", inBmp.getWidth() + " x " + inBmp.getHeight());
        // Copying the original bitmap for the output bitmap
        outBmp = inBmp.copy(Bitmap.Config.ARGB_8888,true);

        // Get the preview of the effects
        effectPreview(rootView, inBmp);

        // Print the picture
        img_view = rootView.findViewById(picture);
        PhotoView mPhotoView = rootView.findViewById(picture);
        mPhotoView.setImageBitmap(inBmp);

        // Initialisation of the seek bar
        indicator =  rootView.findViewById(R.id.textView);
        layout = rootView.findViewById(R.id.seek_bar_layout);
        seek_bar = rootView.findViewById(R.id.seekBar);
        seek_bar.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                layout.setVisibility(View.INVISIBLE);
            }
        });

        // Initialisation of the hue seek bar
        hue_gradient =  rootView.findViewById(R.id.hue_bar);

        // Initialisation of the progress bar
        progress_bar = rootView.findViewById(R.id.progressBar);

        popup = new ProgressDialog(getContext());
        popup.setTitle("Computing ...");
        popup.setMessage("Please Wait, it may take a moment ...");
        popup.setMax(100);
        popup.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        popup.hide();

        // Initialize the effect stack (we gonna use this to undo changes)
        stack = new EffectStack(inBmp, version, popup, getActivity());

        // initialisation of buttons instances
        Button button_back = rootView.findViewById(R.id.button_back);
        button_back.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){exitConfirmation();}
        });

        Button button_save = rootView.findViewById(R.id.button_save);
        button_save.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){new CallEffStackMeth().execute(SAVE);}
        });

        ImageView button_undo = rootView.findViewById(R.id.button_undo);
        button_undo.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                if(!stack.undoIsEmpty()) {
                    new CallEffStackMeth().execute(UNDO);
                } else
                    Toast.makeText(getActivity(),"No effect applied yet", Toast.LENGTH_SHORT).show();
                img_view.setImageBitmap(outBmp);
            }
        });

        ImageView button_reset = rootView.findViewById(R.id.button_reset);
        button_reset.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                stack.clear();
                outBmp = inBmp.copy(inBmp.getConfig(), true);
                img_view.setImageBitmap(outBmp);
                Toast.makeText(getActivity(),"Clear All Effects", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView button_redo = rootView.findViewById(R.id.button_redo);
        button_redo.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                if(!stack.redoIsEmpty()) {
                    new CallEffStackMeth().execute(REDO);
                } else
                    Toast.makeText(getActivity(),"No effect to restore", Toast.LENGTH_SHORT).show();
                img_view.setImageBitmap(outBmp);
            }
        });

        ImageButton button_hist_eq = rootView.findViewById(R.id.button_HistEq);
        button_hist_eq.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                HistogramEq eff = new HistogramEq(getActivity());
                if(version)
                    outBmp = eff.applyJava(outBmp);
                else
                    outBmp = eff.apply(outBmp);

                img_view.setImageBitmap(outBmp);
                stack.addEffect(eff);
            }
        });

        ImageButton button_eye_detection = rootView.findViewById(R.id.button_eye_detection);
        button_eye_detection.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                EyeDetection eff = new EyeDetection(getActivity(), getResources(), R.drawable.green_eye);
                if(version) {
                    outBmp = eff.applyJava(outBmp);
                    stack.addEffect(eff);
                }else
                    Toast.makeText(getActivity(),"Sorry, RenderScript Version unavailable!",
                            Toast.LENGTH_SHORT).show();

                img_view.setImageBitmap(outBmp);
            }
        });

        ImageButton button_posterization = rootView.findViewById(R.id.button_posterize);
        button_posterization.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){

                // Showing the seek bar on calling the effect
                layout.setVisibility(View.VISIBLE);
                hue_gradient.setVisibility(View.INVISIBLE);
                // Set the seek bar properties
                seek_bar = seekBarProp(seek_bar,1, 1, 100);
                indicator.setText("Posterize 0 %");

                seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    int value = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // update the progress indicator
                        indicator.setText("Posterize " + progress + " %");
                        value = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Apply the effect
                        Posterize eff = new Posterize(value, getActivity());
                        if(version)
                            outBmp = eff.applyJava(outBmp);
                        else
                            outBmp = eff.apply(outBmp);

                        img_view.setImageBitmap(outBmp);
                        stack.addEffect(eff);
                        // hide the seek bar when the seeker released
                        layout.setVisibility(View.INVISIBLE);
                        hue_gradient.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        ImageButton button_brightness = rootView.findViewById(R.id.button_brightness);
        button_brightness.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){

                // Showing the seek bar on calling the effect
                layout.setVisibility(View.VISIBLE);
                hue_gradient.setVisibility(View.INVISIBLE);
                // Set the seek bar properties
                seek_bar = seekBarProp(seek_bar,0, -100, 100);
                indicator.setText("Brightness " + 0 + " %");

                seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    int value = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // update the progress indicator
                        indicator.setText("Brightness " + progress + " %");
                        value = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Apply the effect
                        Brightness eff = new Brightness(value, getActivity());

                        if(version)
                            outBmp = eff.applyJava(outBmp);
                        else
                            outBmp = eff.apply(outBmp);

                        img_view.setImageBitmap(outBmp);
                        stack.addEffect(eff);
                        // hide the seek bar when the seeker released
                        layout.setVisibility(View.INVISIBLE);
                        hue_gradient.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        ImageButton button_contrast = rootView.findViewById(R.id.button_contrast);
        button_contrast.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){

                // Showing the seek bar on calling the effect
                layout.setVisibility(View.VISIBLE);
                hue_gradient.setVisibility(View.INVISIBLE);
                // Set the seek bar properties
                seek_bar = seekBarProp(seek_bar,0, -100, 100);
                indicator.setText("Contrast " + 0 + " %");

                seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    int value = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // update the progress indicator
                        indicator.setText("Contrast " + progress + " %");
                        value = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {  }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Apply the effect
                        Contrast eff = new Contrast(value, getActivity());
                        if(version)
                            outBmp = eff.apply(outBmp);
                        else
                            outBmp = eff.applyJava(outBmp);

                        stack.addEffect(eff);

                        img_view.setImageBitmap(outBmp);
                        // hide the seek bar when the seeker released
                        layout.setVisibility(View.INVISIBLE);
                        hue_gradient.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        ImageButton button_black_white = rootView.findViewById(R.id.button_black_white);
        button_black_white.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                ToGrey eff = new ToGrey(getActivity());

                if(version)
                    outBmp = eff.applyJava(outBmp);
                else
                    outBmp = eff.apply(outBmp);

                stack.addEffect(eff);
                img_view.setImageBitmap(outBmp);
            }
        });

        ImageButton button_invert = rootView.findViewById(R.id.button_invert);
        button_invert.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                Invert eff = new Invert(getActivity());

                if(version)
                    outBmp = eff.applyJava(outBmp);
                else
                    outBmp = eff.apply(outBmp);

                stack.addEffect(eff);
                img_view.setImageBitmap(outBmp);
            }
        });

        ImageButton button_saturation = rootView.findViewById(R.id.button_saturation);
        button_saturation.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){

                // Showing the seek bar on calling the effect
                layout.setVisibility(View.VISIBLE);
                hue_gradient.setVisibility(View.INVISIBLE);
                // Set the seek bar properties
                seek_bar = seekBarProp(seek_bar,100, 0, 200);
                indicator.setText("saturation : " + 100 );

                seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    float value = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // update the progress indicator
                        indicator.setText("saturation : " + progress);
                        value = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Apply the effect
                        Effect eff = new Saturation(value, getActivity());

                        if(version)
                            outBmp = eff.applyJava(outBmp);
                        else
                            outBmp = eff.apply(outBmp);

                        img_view.setImageBitmap(outBmp);
                        stack.addEffect(eff);
                        // hide the seek bar when the seeker released
                        layout.setVisibility(View.INVISIBLE);
                        hue_gradient.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        ImageButton button_colorize = rootView.findViewById(R.id.button_colorize);
        button_colorize.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){

                // Showing the seek bar on calling the effect
                layout.setVisibility(View.VISIBLE);
                // Set the seek bar properties
                seek_bar = seekBarProp(seek_bar,0, 0, 360);
                indicator.setText("hue : " + 0 );

                seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    float value = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // update the progress indicator
                        indicator.setText("hue : " + progress );
                        value = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Apply the effect
                        Colorize eff = new Colorize(value,getActivity());

                        if(version)
                            outBmp = eff.applyJava(outBmp);
                        else
                            outBmp = eff.apply(outBmp);

                        img_view.setImageBitmap(outBmp);
                        stack.addEffect(eff);
                        // hide the seek bar when the seeker released
                        layout.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        ImageButton button_kp_one_color = rootView.findViewById(R.id.button_kp_one_color);
        button_kp_one_color.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                // Showing the seek bar on calling the effect
                layout.setVisibility(View.VISIBLE);
                // Set the seek bar properties
                seek_bar = seekBarProp(seek_bar,0, 0, 360);
                indicator.setText("hue : " + 0 );

                seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    float value = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // update the progress indicator
                        indicator.setText("hue : " + progress );
                        value = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Apply the effect
                        KeepOneColor eff = new KeepOneColor(value, getActivity());

                        if(version)
                            outBmp = eff.applyJava(outBmp);
                        else
                            outBmp = eff.apply(outBmp);

                        img_view.setImageBitmap(outBmp);
                        stack.addEffect(eff);
                        // hide the seek bar when the seeker released
                        layout.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        ImageButton button_conv = rootView.findViewById(R.id.button_conv);
        button_conv.setOnClickListener(new Button.OnClickListener(){
            // Applied actions on click
            public void onClick(View v){
                convolution_popup();
            }
        });

        return rootView;
    }

    /**
     * Show a preview of every effect on the image uploaded
     * @param view the view to print in
     * @param bitmap the bitmap of the image uploaded
     */
    private void effectPreview(View view, Bitmap bitmap) {

        // Copying the original bitmap for the output bitmap
        Bitmap orig = Bitmap.createScaledBitmap(bitmap, 250, 250,true);
        Bitmap thumb;

        // Apply effect on the thumbnail and print it on every button
        ImageButton button;
        button = view.findViewById(R.id.button_HistEq);
        thumb = new HistogramEq(getActivity()).apply(orig);
        button.setImageBitmap(thumb);

        button = view.findViewById(R.id.button_posterize);
        thumb = new Posterize(20,getActivity()).apply(orig);
        button.setImageBitmap(thumb);

        button = view.findViewById(R.id.button_brightness);
        thumb = new Brightness(100, getActivity()).apply(orig);
        button.setImageBitmap(thumb);

        button = view.findViewById(R.id.button_contrast);
        thumb = new Contrast(-100,getActivity()).apply(orig);
        button.setImageBitmap(thumb);

        button = view.findViewById(R.id.button_black_white);
        thumb = new ToGrey(getActivity()).apply(orig);
        button.setImageBitmap(thumb);

        button = view.findViewById(R.id.button_invert);
        thumb = new Invert(getActivity()).apply(orig);
        button.setImageBitmap(thumb);

        button = view.findViewById(R.id.button_saturation);
        thumb = new Saturation(200,getActivity()).apply(orig);
        button.setImageBitmap(thumb);

        button = view.findViewById(R.id.button_colorize);
        thumb = new Colorize(new Random().nextInt(360),getActivity()).apply(orig);
        button.setImageBitmap(thumb);

        button = view.findViewById(R.id.button_kp_one_color);
        thumb = new KeepOneColor(new Random().nextInt(360) ,getActivity()).apply(orig);
        button.setImageBitmap(thumb);
    }

    /**
     * Modify the seek bar properties
     * @param seek the seek bar
     * @param progress the progression of the seek bar
     * @param min the minimum of the seek bar
     * @param max the maximum of the seek bar
     * @return the seek bar modified
     */
    private SeekBar seekBarProp (SeekBar seek, int progress, int min, int max) {
        seek.setProgress(progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seek.setMin(min);
        }
        seek.setMax(max);
        return seek;
    }

    /**
     * Initialise the Convolution type popup to choose the effect we want to apply
     */
    private void convolution_popup(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Filter Type");

        builder.setItems(typeList, new DialogInterface.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(DialogInterface dialogInterface, int selected_effect) {
                // If
                if(typeList[selected_effect].equals("Average Blur"))
                    kernel_popup(selected_effect);
                else if(typeList[selected_effect].equals("Gaussian Blur"))
                    kernel_popup(selected_effect);
                else
                    new ApplyConvolution().execute(selected_effect);
            }
        });
        builder.show();

    }

    /**
     * Allow the user to choose the size of the kernel convolution
     * @param selected_effect the selected convolution effect
     */
    private void kernel_popup(final int selected_effect){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Matrix Size");

        builder.setItems(sizeList, new DialogInterface.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(DialogInterface dialogInterface, int selected_size) {
                if(sizeList[selected_size].equals("3x3"))
                    matrixSize = 3;
                else if(sizeList[selected_size].equals("5x5"))
                    matrixSize = 5;
                else if(sizeList[selected_size].equals("7x7"))
                    matrixSize = 7;
                else if(sizeList[selected_size].equals("11x11"))
                    matrixSize = 11;
                else if(sizeList[selected_size].equals("25x25"))
                    matrixSize = 25;
                else
                    Toast.makeText(getActivity(),"An error occurred in convolution", Toast.LENGTH_SHORT).show();

                // If there is no error occurred
                if(matrixSize>0)
                    new ApplyConvolution().execute(selected_effect);

            }
        });
        builder.show();

    }

    /**
     * Ask for saving file before exiting the workspace
     */
    public void exitConfirmation () {
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to save your work before to Exit?")
                .setTitle("Are Sure You Want To Exit")
                .setIcon(R.drawable.exit)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CallEffStackMeth().execute(QUIT);
                        Log.i("info", "Back to choose menu");
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("info", "Back to choose menu");
                        ((MainActivity)getActivity()).goToChoosePicFragment();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Check for the convolution type chosen and create a kernel for computing the convolution filter
     */
    private class ApplyConvolution extends AsyncTask<Integer, Integer, Bitmap> {
        Effect conv = null;
        @Override
        protected void onPreExecute() {
            popup.show();
            progress_bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Integer... params){
            float [] kernel, kernel1, kernel2;
            int radius = 3;
            float sigma = 1.3f;
            int selectedType = params[0];

            if (typeList[selectedType].equals("Average Blur")) {
                // Get the right size and type of a kernel
                if (version) {
                    kernel = KernelMaker.AverageBlurKernel(matrixSize);
                    conv = new Convolution(kernel, progress_bar);
                    outBmp =conv.applyJava(outBmp);
                } else {
                    kernel = KernelMaker.AverageBlurKernel(matrixSize);
                    conv = new Convolution(kernel, getActivity());
                    outBmp = conv.apply(outBmp);
                }
            }  else if (typeList[selectedType].equals("Gaussian Blur")) {
                // Get the right size and type of a kernel
                if(version){
                    kernel = KernelMaker.GaussianBlurKernel(matrixSize, sigma);
                    conv = new Convolution(kernel, progress_bar);
                    outBmp =conv.applyJava(outBmp);
                } else {
                    kernel = KernelMaker.GaussianBlurKernel(matrixSize, sigma);
                    conv = new Convolution(kernel, getActivity());
                    outBmp =conv.apply(outBmp);
                }
            }  else if (typeList[selectedType].equals("Gaussian Blur Intrinsic")) {
                if(version) {
                    conv = null;
                } else {
                    conv = new GaussianBlurIntr(radius, getActivity());
                    outBmp = conv.apply(outBmp);
                }
            } else if (typeList[selectedType].equals("Sobel")) {
                // Get the right size and type of a kernel
                if(version){
                    kernel1 = KernelMaker.SobelKernel()[0];
                    kernel2 = KernelMaker.SobelKernel()[1];
                    conv = new Convolution2K(kernel1, kernel2, progress_bar);
                    outBmp =conv.applyJava(outBmp);
            } else {
                kernel1 = KernelMaker.SobelKernel()[0];
                kernel2 = KernelMaker.SobelKernel()[1];
                conv = new Convolution2K(kernel1, kernel2, getActivity());
                outBmp = conv.apply(outBmp);
            }
        } else if (typeList[selectedType].equals("Prewitt")) {
            // Get the right size and type of a kernel
            if (version) {
                kernel1 = KernelMaker.PrewittKernel()[0];
                kernel2 = KernelMaker.PrewittKernel()[1];
                conv = new Convolution2K(kernel1, kernel2, progress_bar);
                outBmp =conv.applyJava(outBmp);
            } else {
                kernel1 = KernelMaker.PrewittKernel()[0];
                kernel2 = KernelMaker.PrewittKernel()[1];
                conv = new Convolution2K(kernel1, kernel2, getActivity());
                outBmp = conv.apply(outBmp);
            }
        } else if (typeList[selectedType].equals("Laplace cx4")) {
            // Get the right size and type of a kernel
            if (version) {
                kernel = KernelMaker.LaplaceKernel_cx4();
                conv = new Convolution(kernel, progress_bar);
                outBmp = conv.applyJava(outBmp);
            } else {
                kernel = KernelMaker.LaplaceKernel_cx4();
                conv = new Convolution(kernel, getActivity());
                outBmp = conv.apply(outBmp);
            }
        } else if (typeList[selectedType].equals("Laplace cx8")) {
            // Get the right size and type of a kernel
            if (version) {
                kernel = KernelMaker.LaplaceKernel_cx8();
                conv = new Convolution(kernel, progress_bar);
                outBmp = conv.applyJava(outBmp);
            } else {
                kernel = KernelMaker.LaplaceKernel_cx8();
                conv = new Convolution(kernel, getActivity());
                outBmp = conv.apply(outBmp);
            }
        } else if (typeList[selectedType].equals("Emboss")) {
            // Get the right size and type of a kernel
            if (version){
                kernel = KernelMaker.EmbossKernel();
                conv = new Convolution(kernel, progress_bar);
                outBmp =conv.applyJava(outBmp);
            } else {
                kernel = KernelMaker.EmbossKernel();
                conv = new Convolution(kernel, getActivity());
                outBmp = conv.apply(outBmp);
            }
            } else if (typeList[selectedType].equals("Cartoon")) {
                if(version) {
                    conv = null;
                } else {
                    conv = new Cartoon(getActivity());
                    outBmp = conv.apply(outBmp);
                }
            }else if (typeList[selectedType].equals("Pencil")) {
                if(version) {
                    conv = null;
                } else {
                    conv = new PencilDraw(getActivity());
                    outBmp = conv.apply(outBmp);
                }
            } else {
                outBmp = inBmp;
                Toast.makeText(getActivity(),"An error occurred in convolution", Toast.LENGTH_SHORT).show();
            }

            // Push the effect on the stack
            if (outBmp != null && conv != null)
                stack.addEffect(conv);
            else
                Log.w("Convolution", "No effect selected!");

            return outBmp;
        }

        @Override
        protected void onPostExecute(Bitmap output) {
            img_view.setImageBitmap(output);
            popup.hide();
            progress_bar.setVisibility(View.GONE);
            matrixSize = 0;
            if(conv == null && version)
                Toast.makeText(getActivity(), "Sorry, Java Version unavailable!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Call the selected method in the Effect in the effect stack class
     */
    public class CallEffStackMeth extends AsyncTask<Integer, Void, Bitmap> {
        boolean save_request = false;
        boolean quit_request = false;
        @Override
        protected void onPreExecute() {
            popup.show();
            progress_bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Integer... func) {
            switch (func[0]) {
                case SAVE:
                    tmp = stack.applyEffects(origBmp);
                    // Request for the write permission before to save the image file
                    if(Build.VERSION.SDK_INT>=23) {
                        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                    save_request = true;
                    break;
                case QUIT:
                    tmp = stack.applyEffects(origBmp);
                    // Request for the write permission before to save the image file
                    if(Build.VERSION.SDK_INT>=23) {
                        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                    quit_request = true;
                    save_request = true;
                    break;
                case REDO:
                    outBmp = stack.redoEffect();
                    break;
                case UNDO:
                    outBmp = stack.undoEffect();
                    break;
                default:
                    Toast.makeText(getActivity(),"Error on calling Stack effect method"
                                                , Toast.LENGTH_SHORT).show();
                    break;
            }
            return outBmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (save_request && tmp != null) {
                // Set directory where to save the picture
                File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                // Create an image file name from the current date
                String date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
                File imageFile = new File(storageDir, "IMG_ProjeTech_" + date + ".jpg");

                // Write the image
                try {
                    FileOutputStream out = new FileOutputStream(imageFile);
                    tmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Toast.makeText(getActivity(), "Image Saved Successfully", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Unable to save Image", Toast.LENGTH_LONG).show();
                    Log.e("Save", "Unable to save Image", e);
                }
            }

            // Must wait the process
            if(quit_request){
                ((MainActivity)getActivity()).goToChoosePicFragment();
            }

            img_view.setImageBitmap(bitmap);
            popup.hide();
            progress_bar.setVisibility(View.GONE);

            save_request = false;
            quit_request = false;
        }
    }
}