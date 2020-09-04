package Effects;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import java.util.Stack;

public class EffectStack {
    private boolean version;
    private Bitmap inBmp;
    private Stack<Effect>  undoStack, redoStack;
    private ProgressDialog popup;
    private Context context;

    /**
     * Constructor
     */
    public EffectStack(Bitmap bitmap, boolean version, ProgressDialog popup, Context context) {
        this.inBmp = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.version = version;
        this.popup = popup;
        this.context = context;
    }

    /**
     * Push an effect on the stack and clear the redo stack
     * @param effect to push on the top of the stack
     */
    public void addEffect (Effect effect) {
        undoStack.push(effect);
        redoStack.clear();
    }

    /**
     * Check if the undo stack is empty
     * @return true if the stack is empty false otherwise
     */
    public Boolean undoIsEmpty() { return undoStack.isEmpty();}

    /**
     * Check if the redo stack is empty
     * @return true if the stack is empty false otherwise
     */
    public Boolean redoIsEmpty() { return redoStack.isEmpty();}

    /**
     * Clear all the stacks effects
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * Remove the last effect applied on a bitmap to push it on the redo stack
     * @return the bitmap on which all the effects have been applied
     */
    public Bitmap undoEffect(){
        if (undoStack.isEmpty())
            return null;

        // Remove the last effect
        Effect eff = undoStack.pop();
        redoStack.push(eff);
        Log.i("Info", eff.toString()+" has been popped");

        return applyEffects(inBmp);
    }

    /**
     * Restore the last effect removed on a bitmap to push it in the undo stack
     * @return the bitmap on which all the effects have been applied
     */
    public Bitmap redoEffect () {
        if (redoStack.isEmpty())
            return null;

        Effect eff = redoStack.pop();
        undoStack.push(eff);
        Log.i("Info", eff.toString()+" has been restored");

        return applyEffects(inBmp);
    }

    /**
     * Apply all the effect in the undo stack
     * @return the bitmap on which all the effects have been applied
     */
    public Bitmap applyEffects (Bitmap bitmap) {
        // test if the bitmap is valid
        if(bitmap == null){
            Log.e("Bitmap", "Null Bitmap");
            return null;
        }
        // Copy the bitmap
        Bitmap outBmp = bitmap.copy(bitmap.getConfig(),true);
        Effect eff;
        // Apply the effects
        int count = undoStack.size();
        for (int k=0; k<count; k++) {
            eff = undoStack.get(k);
            popup.setProgress(k*100/count);

            if(eff!=null) {
                if(version)
                    outBmp = eff.applyJava(outBmp);
                else
                    outBmp = eff.apply(outBmp);
            } else {
                outBmp = new Brightness(0, context).apply(bitmap);
                Toast.makeText(context, "No effect applied yet", Toast.LENGTH_SHORT).show();
                Log.e("EffectStack", "Error apply");
            }
        }
        return outBmp;
    }

}
