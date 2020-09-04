package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_keep_color;

public class KeepOneColor extends Effect {

    /**
     * Filter one color Effect
     * @param hue the hue value (color) to keep
     * @param context the activity context
     */
    public KeepOneColor(float hue, Context context) {
        super();
        // Checking of overage
        this.value = checkInRange(hue, 0, 359);
        this.context = context;
    }

    /**
     * keep the same color with hue value and apply a grey tone to the others pixels of the Bitmap
     * @param bitmap the bitmap on which to apply the effect
     * @return the bitmap modified
     */
    @Override
    public Bitmap apply(Bitmap bitmap) {
        // taking the current time to get the execution time
        long time = System.currentTimeMillis();

        // test if the bitmap is valid
        if(bitmap == null){
            Log.e("Bitmap", "Null Bitmap");
            return null;
        }
        // Copy the bitmap
        Bitmap outBmp = bitmap.copy(Bitmap.Config.ARGB_8888,true);

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        // Create allocation from Bitmap
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        //Create allocation with the same type
        Allocation output = Allocation.createTyped(rs, input.getType());

        //Create allocation with the same type
        ScriptC_keep_color keep_color_script = new ScriptC_keep_color(rs);

        //Set hue value (0-359)
        keep_color_script.set_hue(value);

        // Call script for output allocation
        keep_color_script.forEach_keep_color(input, output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        keep_color_script.destroy();
        rs.destroy();

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }

    /**
     * This method applies the effect with the java version algorithm
     *
     * @param bitmap the bitmap on which to apply the effect
     * @return the bitmap modified
     */
    @Override
    public Bitmap applyJava(Bitmap bitmap) {
        // taking the current time to get the execution time
        long time = System.currentTimeMillis();

        // test if the bitmap is valid
        if(bitmap == null){
            Log.e("Bitmap", "Null Bitmap");
            return null;
        }
        // Copy the bitmap
        Bitmap outBmp = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        // Getting size
        int w, h, size;
        w = bitmap.getWidth();
        h = bitmap.getHeight();
        size = w*h;

        int[] pxTab = new int[size];

        // Put the pixels of the bitmap in the array pxTabIn
        bitmap.getPixels(pxTab, 0, w, 0, 0, w, h);

        for (int i = 0; i < size; i++) {
            // Checking if the hue is red
            float[] hsl = new float[3];
            Color.colorToHSV(pxTab[i], hsl);
            if (hsl[0] > 10 && hsl[0] < 345) {
                // Get the grey color
                int gray = getGrey(pxTab[i]);
                pxTab[i] = Color.rgb(gray, gray, gray);
            }
        }
        // Putting the result in the bitmap from the array
        outBmp.setPixels(pxTab, 0, w, 0, 0, w, h);

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");
        return outBmp;
    }
}
