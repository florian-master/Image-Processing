package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

public class GaussianBlurIntr extends Effect {

    /**
     * Gaussian Blur Effect with ScriptIntrinsicBlur (RS)
     * @param radius Set the radius of the Blur. Supported range 0 < radius <= 25
     * @param context activity context
     */
    public GaussianBlurIntr(float radius, Context context) {
        super();

        // Checking of overage
        this.value = checkInRange(radius, 1, 25);
        this.context = context;
    }

    /**
     * Apply a gaussian blur effect with the ScriptIntrinsicBlur script
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
        Allocation blurredAllocation = Allocation.createTyped(rs, input.getType ());

        //Create script
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        //Set blur radius (maximum 25.0)
        blurScript.setRadius(value);
        //Set input for script
        blurScript.setInput(input);
        //Call script for output allocation
        blurScript.forEach(blurredAllocation);

        //Copy script result into bitmap
        blurredAllocation.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        blurredAllocation.destroy();
        blurScript.destroy();

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
        return null;
    }
}
