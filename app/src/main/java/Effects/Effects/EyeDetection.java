package Effects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

public class EyeDetection extends Effect {
    private Context context;
    private Resources resources;
    private int image;

    public EyeDetection(Context context, Resources resources, int image) {
        this.context = context;
        this.resources = resources;
        this.image = image;
    }

    @Override
    public Bitmap apply(Bitmap bitmap) {
        return null;
    }

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

        Paint myPaints = new Paint();
        myPaints.setStrokeWidth(5);
        myPaints.setColor(Color.RED);
        myPaints.setStyle(Paint.Style.STROKE);

        Canvas tempCanvas = new Canvas(outBmp);
        tempCanvas.drawBitmap(bitmap, 0, 0, null);

        final FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        if(!detector.isOperational()){
            Toast.makeText(context,"Could not set up the face detector!",Toast.LENGTH_LONG).show();
            return null;
        }

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        Bitmap toIncrust = BitmapFactory.decodeResource(resources, image);

        for(int i=0; i<faces.size(); i++) {
            Face thisFace = faces.valueAt(i);

            for (Landmark land : thisFace.getLandmarks()) {
                if (land.getType() == Landmark.LEFT_EYE || land.getType() == Landmark.RIGHT_EYE) {
                    float x1l = land.getPosition().x;
                    float y1l = land.getPosition().y;

                    toIncrust = Bitmap.createScaledBitmap(toIncrust, (int) (thisFace.getWidth() / 4.03), (int) (thisFace.getWidth() / 4.03), true);
                    tempCanvas.drawBitmap(toIncrust, x1l - toIncrust.getWidth()/2, y1l - toIncrust.getWidth()/2, myPaints);
                }
            }
        }

        // Compute the run time of the process
        time = System.currentTimeMillis() - time;
        Log.i("RunTime for "+toString(), time+ " ms");

        return outBmp;
    }
}
