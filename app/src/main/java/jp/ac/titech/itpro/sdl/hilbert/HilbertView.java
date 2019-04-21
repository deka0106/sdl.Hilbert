package jp.ac.titech.itpro.sdl.hilbert;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.*;

public class HilbertView extends View {

    public final static int MIN_ORDER = 1;
    public final static int MAX_ORDER = 9;

    private int order = 1;
    private final Paint paint = new Paint();
    private SparseArray<Future<Bitmap>> caches = new SparseArray<>();

    public HilbertView(Context context) {
        this(context, null);
    }

    public HilbertView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HilbertView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        preCache(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            canvas.drawBitmap(caches.get(order).get(), 0, 0, paint);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        preCache(w, h);
    }

    public void setOrder(int n) {
        order = n;
        invalidate();
    }

    private void preCache(final int w, final int h) {
        final ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = MIN_ORDER; i <= MAX_ORDER; i++) {
            final int order = i;
            caches.put(order, exec.submit(new Callable<Bitmap>() {
                @Override
                public Bitmap call() {
                    return createBitmap(w, h, order);
                }
            }));
        }
    }

    private Bitmap createBitmap(int w, int h, int order) {
        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(0, 0, w, h, paint);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        final int size = Math.min(w, h);
        final double step = (double) size / (1 << order);
        final HilbertTurtle turtle = new HilbertTurtle(new Turtle.Drawer() {
            @Override
            public void drawLine(double x0, double y0, double x1, double y1) {
                canvas.drawLine((float) x0, (float) y0, (float) x1, (float) y1, paint);
            }
        });
        turtle.setPos((w - size + step) / 2, (h + size - step) / 2);
        turtle.setDir(HilbertTurtle.E);
        turtle.draw(order, step, HilbertTurtle.R);
        return bitmap;
    }

}
