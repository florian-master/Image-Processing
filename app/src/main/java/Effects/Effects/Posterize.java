package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_posterization;

public class Posterize extends Effect {

    /**
     * Posterize Effect
     */
    public Posterize(float value, Context context) {
        super();

        // Checking of overage
        this.value = checkInRange(value, 0, 100);
        this.context = context;
    }

    /**
     * This method applies the effect with the RenderScript version algorithm
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

        //Create allocation from Bitmap
        Allocation input = Allocation.createFromBitmap(rs, bitmap);

        //Create allocation with the same type
        Allocation output = Allocation.createTyped(rs, input.getType ());

        ScriptC_posterization conv_script = new ScriptC_posterization(rs);

        //Set the value to apply
        conv_script.set_value(value/100);
        conv_script.forEach_posterize(input, output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        conv_script.destroy();
        rs.destroy();

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }

    /**
     * This method applies the effect with the Java version algorithm
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

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int size = w * h;
        int[] pxTabIn = new int[size];
        int[] pxTabOut = new int[size];
        bitmap.getPixels(pxTabIn,0,w,0,0,w,h);

        for(int i=0 ; i<size ; i++){
            // Getting pixel channels
            int px = pxTabIn[i];
            int red = Color.red(px);
            int green = Color.green(px);
            int blue = Color.blue(px);

            // Check if every channel is between 0-255
            red  = checkInRange((int) (red-(red%value)), 0, 255);
            green= checkInRange((int) (green-(green%value)), 0, 255);
            blue = checkInRange((int) (blue-(blue%value)), 0, 255);


            pxTabOut[i] = Color.rgb (red,green,blue);
        }
        outBmp.setPixels(pxTabOut,0,w,0,0,w,h);

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }
}

