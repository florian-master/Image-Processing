package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_hist_eq;

public class HistogramEq extends Effect{

    /**
     * Histogram Equalization Effect
     * @param context the activity context
     */
    public HistogramEq(Context context) {
        super();
        this.context = context;
    }

    /**
     * Increase the contrast of a colored Bitmap
     * by the Histogram Equalization algorithm
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
        Bitmap outBmp= bitmap.copy(Bitmap.Config.ARGB_8888,true);

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocationA = Allocation.createFromBitmap(rs, bitmap);

        //Create allocation with same type
        Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());

        //Create script from rs file.
        ScriptC_hist_eq histEqScript = new ScriptC_hist_eq(rs);

        //Set size in script
        histEqScript.set_size(bitmap.getHeight()*bitmap.getWidth());

        //Call the first kernel.
        histEqScript.forEach_root(allocationA, allocationB);

        //Call the rs method to compute the remap array
        histEqScript.invoke_createRemapArray();

        //Call the second kernel
        histEqScript.forEach_remaptoRGB(allocationB, allocationA);

        //Copy script outBmp result into bitmap
        allocationA.copyTo(outBmp);

        //Destroy everything to free memory
        allocationA.destroy();
        allocationB.destroy();
        histEqScript.destroy();
        rs.destroy();

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }

    /**
     * Compute Cumulative Histogram
     * @param hist the histogram to cumule
     * @return Cumulative Histogram
     */
    public int[] CumHistogram(int[] hist){
        int acc = 0;
        for(int i = 0 ; i<256 ; i++){
            acc +=  hist[i];
            hist[i] = acc;
        }
        return hist;
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

        int w, h, size;
        w = bitmap.getWidth();
        h = bitmap.getHeight();
        size = w*h;
        int[] pxTab = new int[size];
        int[] hist = new int [256];
        bitmap.getPixels(pxTab,0,w,0,0,w,h);

        // Computing the histogram
        for (int i = 0 ; i < size ; i++)
            hist[getGrey(pxTab[i])]++;

        // Computing the cumulative histogram
        int[] cumHist = CumHistogram(hist);

        // Histogram equalisation for each color
        for (int i = 0; i < size; i++) {
            int r = (cumHist[Color.red(pxTab[i])]*255) / size;
            int g = (cumHist[Color.green(pxTab[i])]*255) / size;
            int b = (cumHist[Color.blue(pxTab[i])]*255) / size;

            pxTab[i] = Color.rgb(r, g, b);
        }

        outBmp.setPixels(pxTab,0,w,0,0,w,h);

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");
        return outBmp;
    }
}
