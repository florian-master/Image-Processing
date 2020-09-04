package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_cartoon;

public class Cartoon extends Effect{

    /**
     * Cartoon Effect with Renderscript
     */
    public Cartoon(Context context) {
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

        // Create allocation from Bitmap
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        //Create allocation with the same type
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Create the script
        ScriptC_cartoon cartoon_script = new ScriptC_cartoon(rs);

        // Call script for output allocation
        cartoon_script.forEach_cartoonize(input, output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        cartoon_script.destroy();
        rs.destroy();

        // Apply a second filter
        outBmp = new Convolution(KernelMaker.AverageBlurKernel(3), context).apply(outBmp);

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
        return null;
    }
}
