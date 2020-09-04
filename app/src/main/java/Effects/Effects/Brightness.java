package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_brightness;

public class Brightness extends Effect {

    /**
     * Brightness Effect
     */
    public Brightness(int value, Context context) {
        super();
        // Checking of overage
        if (value < -100 ) {
            Log.d("Value", "value given is wrong");
            value = -100;
        }else if (value > 100) {
            Log.d("Value", "value given is wrong");
            value = 100;
        }

        this.context = context;
        this.value = value;
    }

    /**
     * Set the brightness of the bitmap
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
        ScriptC_brightness brightness = new ScriptC_brightness(rs);
        // Set the brightness value (-100 Min +100 Max)
        brightness.set_brightness_offset(value);

        // Call script for output allocation
        brightness.forEach_brightness(input, output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        brightness.destroy();
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

        // Creating a pixel array from the bitmap
        int[] pxTab = new int[size];
        bitmap.getPixels(pxTab,0,w,0,0,w,h);

        // Changing the value pixel by pixel
        int r, g, b;
        for (int i = 0 ; i < size ; i++) {
            r = checkInRange(value + Color.red(pxTab[i]), 0, 255);
            g = checkInRange(value + Color.green(pxTab[i]), 0, 255);
            b = checkInRange(value + Color.blue(pxTab[i]), 0, 255);
            pxTab[i] = Color.rgb (r,g,b);
        }
        // Setting the bitmap's pixels
        outBmp.setPixels(pxTab,0,w,0,0,w,h);

        // Compute and show the execution time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");
        return outBmp;
    }
}
