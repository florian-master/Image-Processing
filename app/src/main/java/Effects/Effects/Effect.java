package Effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;

public abstract class Effect {
    protected float value;
    protected float [] mask1;
    protected float [] mask2;
    protected float [][] kernel1;
    protected float [][] kernel2;
    protected ProgressBar progressBar;
    protected Context context;

    public Effect (){
    }

    /**
     * @return the value of the effect
     */
    public float value() {
        return value;
    }

    /**
     * @return the progress bar to set
     */
    public ProgressBar progressBar() {
        return progressBar;
    }

    /**
     * Get the progress bar
     * @return the progress bar
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Restrain a value to a range
     * @param val the pixel to restrain
     * @param min the minimum value
     * @param max the maximum value
     * @return the new value if is out of range else the old
     */
    public int checkInRange(float val, int min, int max) {
        if (val < min) {
            Log.w("Value", "value given is wrong");
            return min;
        } else if (val > max) {
            Log.w("Value", "value given is wrong");
            return max;
        } else {
            return (int) val;
        }
    }

    /**
     * @return Activity context
     */
    public Context context() {
        return context;
    }

     int getGrey(int pixel) {
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);

        return (int) (0.30 * r + 0.59 * g + 0.11 * b);
    }

    @NonNull
    public String toString() {
        return this.getClass().getName();
    }

    /**
     * This method applies the effect with the RenderScript version algorithm
     * @param bitmap the bitmap on which to apply the effect
     * @return the bitmap modified
     */
    public abstract Bitmap apply(Bitmap bitmap);

    /**
     * This method applies the effect with the java version algorithm
     * @param bitmap the bitmap on which to apply the effect
     * @return the bitmap modified
     */
    public abstract Bitmap applyJava(Bitmap bitmap);
}
