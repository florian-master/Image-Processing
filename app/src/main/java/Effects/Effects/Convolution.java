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

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_convolution;

public class Convolution extends Effect{
    private ProgressBar progressBar;

    /**
     * Convolution algorithm with java
     */
    public Convolution(float[] kernel, ProgressBar progressBar) {
        this.mask1 = kernel;
        this.progressBar = progressBar;
    }

    /**
     * Convolution algorithm with RenderScript
     */
    public Convolution(float[] mask, Context context) {
        super();
        this.mask1 = mask;
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

        // Calculate the sum of the kernel (mask)
        int ker_length1 = mask1.length;

        // Check the kernels length
        if(ker_length1%2 == 0 || ker_length1 < 9){
            Log.e("Kernel", "kernel length is invalid");
            Toast.makeText(context, "An error occurred on the kernel filter!", Toast.LENGTH_LONG).show();
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

        ScriptC_convolution conv_script = new ScriptC_convolution (rs);
        conv_script.set_input(input);
        conv_script.set_width(bitmap.getWidth());
        conv_script.set_height(bitmap.getHeight());

        //Set the mask to apply
        int size = (int) Math.sqrt(mask1.length);
        conv_script.set_ksize(size);
        Allocation v = Allocation.createSized(rs, Element.F32(rs),mask1.length);
        v.copyFrom(mask1);
        conv_script.bind_kmatrix(v);
        float total = 0.f;
        for(float f : mask1){
            total += f;
        }
        conv_script.set_kdiv(total);
        conv_script.forEach_apply_convolution(input, output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        conv_script.destroy();

        // Compute and show the execution time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }

    /**
     * Get the progress bar
     * @return the progress bar
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * This method applies the effect with the java version algorithm
     * @param bitmap the bitmap on which to apply the effect
     * @return the bitmap modified
     */
    public Bitmap applyJava (Bitmap bitmap) {
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
        if(ker_length%2 == 0 || ker_length < 3){
            Log.e("Kernel", "kernel length is valid");
            Toast.makeText(context, "An error occurred on the kernel filter!", Toast.LENGTH_LONG).show();
            return null;
        }

        float[] kernel = mask1;

        // Calculate the sum of the kernel (mask)
        for (int i = 0; i < ker_length; ++i)
                divisor+= kernel[i];

        // Prevent a division by zero
        if (divisor <= 0) {
            Log.d("Division", "division by zero detected");
            divisor = 1;
        }

        // Creating a pixel array from the bitmap
        int[] pxTab = new int[w*h];
        bitmap.getPixels(pxTab,0,w,0,0,w,h);
        int[] pxTabOut = pxTab.clone();

        // Apply transformation (without borders)
        int ker_mid = (int) Math.sqrt(ker_length);
        int counter = 0, newR, newG, newB, sumR, sumG, sumB;

        for (int y = 0; y < h - ker_length - 1; ++y) {
            counter++;
            for (int x = 0; x < w - ker_length - 1; ++x) {

                // Get sum of RGB multiplied by kernel
                sumR = sumG = sumB = 0;
                for (int i = 0; i < ker_mid; ++i) {
                    for (int j = 0; j < ker_mid; ++j) {
                        // Extract the pixel from the array
                        int color = pxTab[((y + j) * w) + x + i];
                        float ker = kernel[(j*ker_mid)+i];

                        // compute the sum
                        sumR += Color.red(color) * ker;
                        sumG += Color.green(color) * ker;
                        sumB += Color.blue(color) * ker;
                    }
                }
                // Check if every channel is between 0-255
                newR = checkInRange(Math.round(sumR / divisor), 0, 255);
                newG = checkInRange(Math.round(sumG / divisor), 0, 255);
                newB = checkInRange(Math.round(sumB / divisor), 0, 255);

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
        Log.i("RunTime for "+toString(), time+ " ms");
        return outBmp;
    }

}