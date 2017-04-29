
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

public class MjpegPlayer implements SurfaceHolder.Callback{


    public final static int SIZE_STANDARD   = 1;
    public final static int SIZE_BEST_FIT   = 4;

    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;
    private boolean mRun = false;
    private Paint overlayPaint;

    private boolean surfaceDone;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("TAG", "a surface was created!");
        surfaceDone=true;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public class MjpegViewThread extends Thread {
        private SurfaceView surface;

        public MjpegViewThread(SurfaceView surface) { this.surface = surface; }

        public void run() {

            final Paint p = new Paint();
            while (mRun) {
                if (surfaceDone) {
                    try {
                        final Bitmap bm = mIn.readMjpegFrame();
                        Bitmap scaled = Bitmap.createScaledBitmap(bm, surface.getWidth(), surface.getHeight(), false);
                            SurfaceHolder surfaceH = surface.getHolder();
                            synchronized (surfaceH) {
                                Canvas c = surfaceH.lockCanvas();
                                c.drawColor(Color.BLACK);
                                c.drawBitmap(scaled, 0, 0, p);
                                surfaceH.unlockCanvasAndPost(c);
                            }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void init(SurfaceView holder) {
        thread = new MjpegViewThread(holder);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
    }

    public void startPlayback() {
        if(mIn != null) {
            mRun = true;
            thread.start();
        }
    }

    public MjpegPlayer(OverlayView cov) {
        init(cov.getSurfaceView());
        cov.setCallback(this);
    }

    public void setSource(MjpegInputStream source) {
        mIn = source;
        startPlayback();
    }
}
