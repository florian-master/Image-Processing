package Effects;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_convolution2k;

public class Convolution2K extends Effect {

    /**
     * Convolution with 2 kernels algorithm with renderscript
     */
    public Convolution2K(float[] mask1, float[] mask2, ProgressBar progressBar) {
        super();
        this.mask1 = mask1;
        this.mask2 = mask2;
        this.progressBar = progressBar;
    }

    /**
     * Convolution with 2 kernels algorithm with java
     */
    public Convolution2K(float[] mask1, float[] mask2, Context context) {
        super();
        this.mask1 = mask1;
        this.mask2 = mask2;
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

        // Calculate the sum of the kernel (mask)
        int ker_length1 = mask1.length, ker_length2 = mask2.length;

        // Check the kernels length
        if(ker_length1 != ker_length2 || ker_length1%2 == 0 || ker_length1 < 9){
            Log.e("Kernels", "kernels length doesn't matching");
            Toast.makeText(context, "An error occurred on the kernels filter!", Toast.LENGTH_LONG).show();
            return null;
        }
        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation input = Allocation.createFromBitmap(rs, bitmap);

        //Create allocation with the same type
        Allocation output = Allocation.createTyped(rs, input.getType ());

        ScriptC_convolution2k conv_script = new ScriptC_convolution2k (rs);
        conv_script.set_input(input);
        conv_script.set_width(bitmap.getWidth());
        conv_script.set_height(bitmap.getHeight());

        //Set the mask to apply
        Allocation v1 = Allocation.createSized(rs, Element.F32(rs),ker_length1);
        v1.copyFrom(mask1);
        conv_script.bind_kmatrix1(v1);
        Allocation v2 = Allocation.createSized(rs, Element.F32(rs),ker_length2);
        v2.copyFrom(mask2);
        conv_script.bind_kmatrix2(v2);

        // Check and Compute the size of the masks
        int size1 = (int) Math.sqrt(ker_length1);
        int size2 = (int) Math.sqrt(ker_length2);

        if(size1 != size2)
            return null;

        conv_script.set_ksize(size1);

        // Get the divisor
        float total = 0.f;
        for(float f : mask1){
            total += f;
        }

        conv_script.set_kdiv(total/2);
        conv_script.forEach_apply_convolution2k(input, output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        conv_script.destroy();

        // Compute and show the execution time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+getClass().getName(), time+ " ms");

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
        int h = bitmap.getHeight();
        int w = bitmap.getWidth();

        // Calculate the sum of the kernel (mask)
        int ker_length = mask1.length, divisor=0;

        // Check the kernels length
        if(ker_length != mask2.length || ker_length%2 == 0 || ker_length < 3){
            Log.e("Kernels", "kernels length doesn't matching");
            Toast.makeText(context, "An error occurred on the kernels filter!", Toast.LENGTH_LONG).show();
            return null;
        }

        for (int i = 0; i < ker_length; ++i)
                divisor += Math.abs(mask1[i]);

        // Prevent a division by zero
        if (divisor <= 0) {
            Log.d("Division", "division by zero detected");
            divisor = 1;
        }
        divisor /= 2;

        // Creating a pixel array from the bitmap
        int[] pxTab = new int[w*h];
        bitmap.getPixels(pxTab,0,w,0,0,w,h);
        int[] pxTabOut = pxTab.clone();

        int ker_mid = (int) Math.sqrt(ker_length);
        int newR, newG, newB, counter = 0;
        int sumR1, sumG1, sumB1, sumR2, sumG2, sumB2;

        // Apply transformation (without borders)
        for (int y = 0; y < h - ker_length; ++y) {
            counter++;
            for (int x = 0; x < w - ker_length; ++x) {

                // Get sum of RGB multiplied by kernel
                sumR1 = sumG1 = sumB1 = sumR2 = sumG2 = sumB2 = 0;
                for (int i = 0; i < ker_mid; ++i) {
                    for (int j = 0; j < ker_mid; ++j) {
                        // Extract the pixel from the array
                        int color = pxTab[((y + j) * w) + x + i];
                        float ker1 = mask1[(j*ker_mid)+i];
                        float ker2 = mask2[(j*ker_mid)+i];

                        // compute the sum
                        sumR1 += Color.red(color) * ker1;
                        sumR2 += Color.red(color) * ker2;
                        sumG1 += Color.green(color) * ker1;
                        sumG2 += Color.green(color) * ker2;
                        sumB1 += Color.blue(color) * ker1;
                        sumB2 += Color.blue(color) * ker2;
                    }
                }

                // Check if every channel is between 0-255
                newR = checkInRange((int) Math.hypot(sumR1, sumR2)/divisor, 0, 255);
                newG = checkInRange((int) Math.hypot(sumG1, sumG2)/divisor,0, 255);
                newB = checkInRange((int) Math.hypot(sumB1, sumB2)/divisor, 0, 255);

                // Write new pixel
                pxTabOut[((y + ker_mid)*w) + x + ker_mid] = Color.rgb(newR, newG, newB);
            }
            // Publishing the status of computation in percent
            progressBar.setProgress((counter*100)/h);
        }

        // Setting the bitmap's pixels
        outBmp.setPixels(pxTabOut,0,w,0,0,w,h);

        // Compute and show the execution time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+getClass().getName(), time+ " ms");
        return outBmp;
    }
}
