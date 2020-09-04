package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_saturation;


public class Saturation extends Effect {

    /**
     * Saturation Effect
     * @param saturation the value to set
     * @param context the activity context
     */
    public Saturation(float saturation, Context context) {
        // Checking of overage
        this.value = checkInRange(saturation, 0, 200);
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

        // Convert the value
        float saturation = value/100;

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        Allocation allocIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allocOut = Allocation.createTyped(rs, allocIn.getType());

        ScriptC_saturation scriptSat = new ScriptC_saturation(rs);

        scriptSat.set_saturation(saturation);

        scriptSat.forEach_root(allocIn, allocOut);
        allocOut.copyTo(outBmp);

        allocIn.destroy();
        allocOut.destroy();
        scriptSat.destroy();
        rs.destroy();

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }

    /**
     * Modify the saturation value to all the pixels of the Bitmap
     * @return edited bitmap
     */
    private Bitmap saturationV1(Bitmap bitmap) {

        // test if the bitmap is valid
        if(bitmap == null){
            Log.e("Bitmap", "Null Bitmap");
            return null;
        }
        // Copy the bitmap
        Bitmap outBmp = bitmap.copy(Bitmap.Config.ARGB_8888,true);

        // Convert the value
        float saturation = (value-100)/100;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int size = w * h;
        int[] pxTabIn = new int[size];
        int[] pxTabOut = new int[size];
        float[] hsv = new float[3];
        bitmap.getPixels(pxTabIn,0,w,0,0,w,h);

        for(int i=0 ; i<size ; i++){
            Color.colorToHSV(pxTabIn[i],hsv);
            // Change the saturation channel
            hsv[1]+=saturation;

            int px = Color.HSVToColor(hsv);
            // Check channels overage
            int red = checkInRange(Color.red(px), 0, 255);
            int green = checkInRange(Color.green(px), 0, 255);
            int blue = checkInRange(Color.blue(px), 0, 255);

            pxTabOut[i] = Color.rgb (red,green,blue);
        }
        outBmp.setPixels(pxTabOut,0,w,0,0,w,h);

        return outBmp;
    }

    /**
     * Modify the saturation value to all the pixels of the Bitmap
     * @return edited bitmap
     */
    private Bitmap saturationV2(Bitmap bitmap) {

        // test if the bitmap is valid
        if(bitmap == null){
            Log.e("Bitmap", "Null Bitmap");
            return null;
        }
        // Copy the bitmap
        Bitmap outBmp = bitmap.copy(Bitmap.Config.ARGB_8888,true);

        // Convert the value
        float saturation = value/100.0f;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int size = w * h;
        int[] pxTabIn = new int[size];
        int[] pxTabOut = new int[size];
        float Pr = 0.299f;
        float Pg = 0.587f;
        float Pb = 0.114f;
        bitmap.getPixels(pxTabIn,0,w,0,0,w,h);

        for(int i=0 ; i<size ; i++){
            int px = pxTabIn[i];
            int r = Color.red(px);
            int g = Color.green(px);
            int b = Color.blue(px);

            // Compute the chroma = lum * saturation
            float pur = (float) Math.sqrt(r*r*Pr + g*g*Pg+ b*b*Pb);
            int red = checkInRange(pur+((Color.red(px)-pur)*saturation), 0, 255);
            int green = checkInRange(pur+((Color.green(px)-pur)*saturation), 0, 255);
            int blue = checkInRange(pur+((Color.blue(px)-pur)*saturation), 0, 255);

            pxTabOut[i] = Color.rgb (red,green,blue);
        }
        outBmp.setPixels(pxTabOut,0,w,0,0,w,h);

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

        bitmap = saturationV2(bitmap);

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");
        return bitmap;
    }
}

