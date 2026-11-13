package com.iappyx.generated.placeholder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Simple original 2D Byte robot artwork drawn natively; no image or network dependency. */
public final class ByteRobotView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF shape = new RectF();

    public ByteRobotView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setContentDescription("Byte floating robot. Tap to open the companion panel; drag to move.");
    }

    private void fill(Canvas c, int color) { paint.setColor(color); paint.setStyle(Paint.Style.FILL); }
    private void stroke(Canvas c, int color, float width) {
        paint.setColor(color); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(width);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float s = Math.min(getWidth(), getHeight()) / 100f;
        canvas.save();
        canvas.scale(s, s);

        // Soft shadow and dark rounded backing.
        fill(canvas, 0x5533BBDD);
        canvas.drawCircle(50, 51, 45, paint);
        fill(canvas, Color.rgb(14, 29, 48));
        canvas.drawCircle(50, 48, 44, paint);
        stroke(canvas, Color.rgb(79, 195, 247), 2.2f);
        canvas.drawCircle(50, 48, 42.5f, paint);

        // Antenna.
        stroke(canvas, Color.rgb(210, 224, 238), 4f);
        canvas.drawLine(50, 10, 50, 19, paint);
        fill(canvas, Color.rgb(79, 220, 220));
        canvas.drawCircle(50, 8, 5.5f, paint);

        // Ears.
        fill(canvas, Color.rgb(105, 190, 211));
        shape.set(17, 39, 27, 61); canvas.drawRoundRect(shape, 5, 5, paint);
        shape.set(73, 39, 83, 61); canvas.drawRoundRect(shape, 5, 5, paint);

        // Robot head.
        fill(canvas, Color.rgb(224, 235, 244));
        shape.set(25, 20, 75, 73); canvas.drawRoundRect(shape, 15, 15, paint);
        stroke(canvas, Color.rgb(79, 195, 247), 2f);
        canvas.drawRoundRect(shape, 15, 15, paint);

        // Face screen.
        fill(canvas, Color.rgb(18, 42, 66));
        shape.set(31, 29, 69, 62); canvas.drawRoundRect(shape, 10, 10, paint);

        // Expressive cyan eyes.
        fill(canvas, Color.rgb(94, 226, 238));
        canvas.drawRoundRect(37, 39, 44, 49, 3, 3, paint);
        canvas.drawRoundRect(56, 39, 63, 49, 3, 3, paint);

        // Friendly smile.
        stroke(canvas, Color.rgb(94, 226, 238), 2.5f);
        canvas.drawArc(43, 44, 57, 56, 15, 150, false, paint);

        // Small lower body detail.
        fill(canvas, Color.rgb(79, 195, 247));
        canvas.drawCircle(50, 68, 2.2f, paint);
        canvas.restore();
    }
}