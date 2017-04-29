package calebice.twoaxisrccar.GuiFunctions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Extension of LinearLayout that sets up the frame to view the incoming video
 */
public class OverlayView extends LinearLayout {
    private final OverlayFrameView mView;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(0, 0, 0, 0);

        mView = new OverlayFrameView(context, attrs);
        mView.setLayoutParams(params);
        addView(mView);

        setVisibility(View.VISIBLE);

    }

    public SurfaceView getSurfaceView(){
        return mView.getSurfaceView();

    }

    public void setCallback(SurfaceHolder.Callback cb){
        mView.imageView.getHolder().addCallback(cb);
    }

    /**
     * This is a helper class for OverlayView. Creates a SurfaceView object which draws
     * incoming images. This is passed into MjpegPlayer as the object to draw on
     */
    private class OverlayFrameView extends ViewGroup {
        private final SurfaceView imageView;

        /**
         * Constructor that
         * @param context
         * @param attrs
         */
        public OverlayFrameView(Context context, AttributeSet attrs) {
            super(context, attrs);
            imageView = new SurfaceView(context, attrs);
            addView(imageView);

        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // Width and height of this ViewGroup.
            final int width = right - left;
            final int height = bottom - top;

            // The size of the image, given as a fraction of the dimension as a ViewGroup. We multiply
            // both width and heading with this number to compute the image's bounding box. Inside the
            // box, the image is the horizontally and vertically centered.
            final float imageSize = 1.0f;

            // The fraction of this ViewGroup's height by which we shift the image off the ViewGroup's
            // center. Positive values shift downwards, negative values shift upwards.
            final float verticalImageOffset = -0.07f;

            // Layout ImageView
            float imageMargin = (1.0f - imageSize) / 2.0f;
            float leftMargin = (int) (width * (imageMargin));
            float topMargin = (int) (height * (imageMargin + verticalImageOffset));
            imageView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width * imageSize), (int) (topMargin + height * imageSize));
            imageView.setZOrderMediaOverlay(true);
        }

        public SurfaceView getSurfaceView(){
            return imageView;
        }
    }
}