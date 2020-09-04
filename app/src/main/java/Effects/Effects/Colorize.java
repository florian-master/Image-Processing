package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_color;

public class Colorize extends Effect{

    /**
     * Colorize Effect
     */
    public Colorize(float hue, Context context) {
        super();
        // Check if every channel is between 0-255
        this.value = checkInRange(hue, 0, 359);
        this.context = context;
    }

    /**
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
        RenderScript rs = RenderScript.create (context);

        // Create allocation from Bitmap
        Allocation input = Allocation.createFromBitmap (rs , bitmap);
        //Create allocation with the same type
        Allocation output = Allocation.createTyped (rs , input.getType ());

        // Create the script
        ScriptC_color color_script = new ScriptC_color(rs);

        //Set hue value (0-359)
        color_script.set_hue(value);

        // Call script for output allocation
        color_script.forEach_color(input, output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        color_script.destroy();
        rs.destroy();

        // Compute and show the execution time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }

    /**
     * This method applies the effect with the java version algorithm
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

        int[] pxTabIn = new int[size];
        int[] pxTabOut = new int[size];
        float[] hsb = new float[3];

        // Put the pixels of the bitmap in the array pxTabIn
        bitmap.getPixels(pxTabIn, 0, w, 0, 0, w, h);

        for (int i = 0; i < size; i++) {
            // Converting the pixel from RGB color to HSL color
            Color.colorToHSV(pxTabIn[i], hsb);
            // changing the hue value (set hsb[0] between 50 - 70 for yellow effect)
            hsb[0] = value;
            hsb[1] = 50;
            pxTabOut[i] = Color.HSVToColor(hsb);
        }
        // Putting the result in the bitmap from the array
        outBmp.setPixels(pxTabOut, 0, w, 0, 0, w, h);

        // Compute and show the execution time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");
        return outBmp;
    }
}
