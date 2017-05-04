/*Acknowledgement to nichhk for reworking SimpleMjpeg Frame to be divided into classes more efficiently*/

package calebice.twoaxisrccar.mjpeg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import calebice.twoaxisrccar.GuiFunctions.OverlayView;

/**
 * Drawing class connected to OverlayView that paints incoming Mjpegs onto the OverlayView frame
 * once a MjpegInputStream is validated
 */
public class MjpegPlayer implements SurfaceHolder.Callback{

    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;
    private boolean mRun = false;
    private Paint overlayPaint;

    private boolean surfaceDone;

    /**
     * Checks to ensure there is a Layout that the SurfaceView is monitoring
     * @param holder Created SurfaceHolder to monitor and paint onto SurfaceView
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("TAG", "a surface was created!");
        surfaceDone=true;
    }

    /*Unused methods in interface implementation*/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}
    /*Unused interface methods*/

    /**
     * Thread that manages and paints the MjpegInputStream onto the OverlayView's SurfaceHolder
     * Which is displayed on the layout for StreamControllerActivity
     * This is the framework for reading the incoming stream of Mjpegs
     */
    private class MjpegViewThread extends Thread {
        private SurfaceView surface;

        /**
         * Instantiates the Mjpeg Thread with the passed in Overlay object for rendering
         * @param surface the Overlay object's view for drawing in StreamControllerActivity
         */
        private MjpegViewThread(SurfaceView surface) { this.surface = surface; }

        /**
         * Loops over the available MJpegs using stream and then draws each frame onto
         * the surfaceHolder from OverlayView
         */
        public void run() {

            final Paint p = new Paint();
            while (mRun) {
                if (surfaceDone) {
                    try {
                        final Bitmap bm = mIn.readMjpegFrame();
                        Bitmap scaled = Bitmap.createScaledBitmap(bm, surface.getWidth(), surface.getHeight(), false);
                            SurfaceHolder surfaceH = surface.getHolder();
                                Canvas c = surfaceH.lockCanvas();
                                c.drawColor(Color.BLACK);
                                //Pains the full Bitmap onto a canvas
                                c.drawBitmap(scaled, 0, 0, p);
                                //Posts this drawn image onto the Surface
                                surfaceH.unlockCanvasAndPost(c);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Initializes the Paint object to draw onto the Thread that has the SurfaceView from OverlayView
     * @param holder the object to draw onto (From OverlayView)
     */
    private void init(SurfaceView holder) {
        thread = new MjpegViewThread(holder);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
    }

    /**
     * Called once a MjpegInputStream is established (source) begins the MjpegViewThread which loops
     * over frames from the stream
     */
    private void startPlayback() {
        if(mIn != null) {
            mRun = true;
            thread.start();
        }
    }

    /**
     * Sets up MjpegViewThread to draw onto the OverlayView frame
     * @param cov the Surface view to begin painting on
     */
    public MjpegPlayer(OverlayView cov) {
        init(cov.getSurfaceView());
        cov.setCallback(this);
    }

    /**
     * Called from ReadInputStream object, establishes a connection to MjpegInputStream and then
     * begins thread that reads the stream from source
     * @param source the MjpegInputStream returned from the user specified IP address
     */
    public void setSource(MjpegInputStream source) {
        mIn = source;
        startPlayback();
    }
}
