package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_invert;

public class Invert extends Effect{

    /**
     * Invert Effect
     */
    public Invert(Context context) {
        super();
        this.context = context;
    }

    /**
     * This method applies the effect with the Renderscript version algorithm
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
        Allocation input =  Allocation.createFromBitmap (rs , bitmap);
        //Create allocation with the same type
        Allocation output = Allocation.createTyped (rs , input.getType ());

        // Create the script
        ScriptC_invert invert = new ScriptC_invert(rs);
        // Call script for output allocation
        invert.forEach_invert(input,output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        invert.destroy();
        rs.destroy();

        // Compute the run time of the process
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
            r = 255 - Color.red(pxTab[i]);
            g = 255 - Color.green(pxTab[i]);
            b = 255 - Color.blue(pxTab[i]);
            pxTab[i] = Color.rgb (r,g,b);
        }
        // Setting the bitmap's pixels
        outBmp.setPixels(pxTab,0,w,0,0,w,h);

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");
        return outBmp;
    }
}
