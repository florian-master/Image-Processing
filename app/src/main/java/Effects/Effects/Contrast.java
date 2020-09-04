package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.widget.Toast;

import fr.ubordeaux.rlasvenes.projettechandroid.ScriptC_contrast_lde;

public class Contrast extends Effect {
    private int min_red;
    private int max_red;
    private int min_green;
    private int max_green;
    private int min_blue;
    private int max_blue;
    private int min_light;
    private int max_light;

    private int[] red;
    private int[] green;
    private int[] blue;

    /**
     * Contrast Effect with Renderscript
     */
    public Contrast(int shift, Context context) {
        super();
        // Checking of overage
        this.value = checkInRange(shift, -100, 100);
        this.context = context;
    }

    /**
     * Compute the extremes of the array of pixels
     *
     * @param pixels array of pixels provided by getPixels()
     * @param size size of the bitmap (width*height)
     */
    private void  Extremums(int[] pixels, int size) {
        int r, g, b, l;
        min_red = 255;
        max_red = 0;
        min_green = 255;
        max_green = 0;
        min_blue = 255;
        max_blue = 0;
        min_light = 255;
        max_light = 0;
        for (int i = 0; i < size; i++) {
            r = Color.red(pixels[i]);
            g = Color.green(pixels[i]);
            b = Color.blue(pixels[i]);
            l = (int) (0.3 * r + 0.59 * g + 0.11 * b);
            if (r < min_red)
                min_red = r;
            if (r > max_red)
                max_red = r;
            if (g < min_green)
                min_green = g;
            if (g > max_green)
                max_green = g;
            if (b < min_blue)
                min_blue = b;
            if (b > max_blue)
                max_blue = b;
            if (l < min_light)
                min_light = l;
            if (l > max_light)
                max_light = l;
        }
    }
    
    /**
     * Compute the LUTs tables with the old and new minimums and maximums
     *
     * @param new_min   new minimum for the histograms
     * @param new_max   new maximum for the histograms
     * @param min_red   old red signal minimum
     * @param max_red   old red signal maximum
     * @param min_green old green signal minimum
     * @param max_green old green signal maximum
     * @param min_blue  old blue signal minimum
     * @param max_blue  old blue signal maximum
     */
    private void luts(int new_min, int new_max, int min_red, int max_red, int min_green, int max_green, int min_blue, int max_blue) {
        red = new int[256];
        green = new int[256];
        blue = new int[256];
        for (int i = 0; i < 256; i++) {
            red[i] = (new_max * (i - min_red) + new_min * (max_red - i)) / (max_red - min_red);
            green[i] = (new_max * (i - min_green) + new_min * (max_green - i)) / (max_green - min_green);
            blue[i] = (new_max * (i - min_blue) + new_min * (max_blue - i)) / (max_blue - min_blue);
        }
    }

    /**
     * Compute the rgb LUT tables
     * @param pixels array of pixels provided by getPixels()
     * @param size size of the bitmap (width*height)
     */
    private void lutsCalculus(int[] pixels, int size) {
        Extremums(pixels, size);

        int new_min = (int) (min_light - (value / 2));
        int new_max = (int) (max_light + (value / 2));
        if (new_min < 0)
            new_min = 0;
        if (new_max > 255)
            new_max = 255;
        if (new_min > new_max) {
            new_min = (min_light + max_light) / 2;
            new_max = new_min;
        }
        luts(new_min, new_max, min_red, max_red, min_green, max_green, min_blue, max_blue);
    }

    /**
     * Increase or decrease the contrast of the Bitmap by the LDE algorithm
     * @param bitmap the bitmap on which to apply the effect
     * @return the bitmap modified
     */
    @Override
    public Bitmap apply(Bitmap bitmap) {
        // taking the current time to get the execution time
        long time = System.currentTimeMillis();

        // Checking if the bitmap is not null
        if (bitmap == null) {
            Toast.makeText(context,"An error occurred, bitmap = null!",Toast.LENGTH_LONG).show();
            return null;
        }
        // Copy the bitmap
        Bitmap outBmp = bitmap.copy(Bitmap.Config.ARGB_8888,true);

        if (value == 0) {
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int[] pixels = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        lutsCalculus(pixels, size);

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        // Create allocation from Bitmap
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        //Create allocation with the same type
        Allocation output = Allocation.createTyped(rs, input.getType());

        //Create allocation with the same type
        ScriptC_contrast_lde contrast_script = new ScriptC_contrast_lde(rs);

        // Set the lut values
        contrast_script.set_lut_red(red);
        contrast_script.set_lut_green(green);
        contrast_script.set_lut_blue(blue);

        // Call script for output allocation
        contrast_script.forEach_dynamic_extension(input, output);

        //Copy script result into bitmap
        output.copyTo(outBmp);

        //Destroy everything to free memory
        input.destroy();
        output.destroy();
        contrast_script.destroy();
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

        // Checking if the bitmap is not null
        if (bitmap == null) {
            Toast.makeText(context,"An error occurred, bitmap = null!",Toast.LENGTH_LONG).show();
            return null;
        }
        // Copy the bitmap
        Bitmap outBmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (value == 0) {
            return bitmap;
        }

        int r, g, b, a;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int[] pixels = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        // Compute the lut tables
        lutsCalculus(pixels, size);
        for (int i = 0; i < size; i++) {
            r = red[Color.red(pixels[i])];
            g = green[Color.green(pixels[i])];
            b = blue[Color.blue(pixels[i])];
            a = Color.alpha(pixels[i]);
            pixels[i] = Color.argb(a, r, g, b);
        }
        outBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        // Compute and show the execution time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }
}
