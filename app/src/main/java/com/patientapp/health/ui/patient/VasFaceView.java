package com.patientapp.health.ui.patient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class VasFaceView extends View {

    private int level = 0;
    private int faceColor = 0xFF4CAF50;
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public VasFaceView(Context context) {
        super(context);
        init();
    }

    public VasFaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VasFaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        eyePaint.setColor(0xFF333333);
        eyePaint.setStyle(Paint.Style.FILL);
        mouthPaint.setColor(0xFF333333);
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeWidth(2.5f);
        mouthPaint.setStrokeCap(Paint.Cap.ROUND);
        tearPaint.setColor(0xFF42A5F5);
        tearPaint.setStyle(Paint.Style.FILL);
    }

    public void setLevel(int level, int color) {
        this.level = level;
        this.faceColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(w, h) / 2f - 2f;

        fillPaint.setColor(faceColor);
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius, fillPaint);

        fillPaint.setColor(0x4DFFFFFF);
        canvas.drawCircle(cx, cy, radius, fillPaint);

        strokePaint.setColor(faceColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2f);
        canvas.drawCircle(cx, cy, radius, strokePaint);

        float eyeY = cy - radius * 0.2f;
        float eyeLeftX = cx - radius * 0.3f;
        float eyeRightX = cx + radius * 0.3f;
        float eyeRadius = radius * 0.08f;
        canvas.drawCircle(eyeLeftX, eyeY, eyeRadius, eyePaint);
        canvas.drawCircle(eyeRightX, eyeY, eyeRadius, eyePaint);

        drawMouth(canvas, cx, cy, radius);
    }

    private void drawMouth(Canvas canvas, float cx, float cy, float radius) {
        float mouthY = cy + radius * 0.35f;
        float mouthWidth = radius * 0.5f;

        if (level <= 1) {
            RectF oval = new RectF(cx - mouthWidth, mouthY - mouthWidth * 0.5f,
                    cx + mouthWidth, mouthY + mouthWidth * 0.5f);
            canvas.drawArc(oval, 0f, 180f, false, mouthPaint);
        } else if (level <= 3) {
            RectF oval = new RectF(cx - mouthWidth, mouthY - mouthWidth * 0.2f,
                    cx + mouthWidth, mouthY + mouthWidth * 0.4f);
            canvas.drawArc(oval, 10f, 160f, false, mouthPaint);
        } else if (level <= 5) {
            canvas.drawLine(cx - mouthWidth, mouthY, cx + mouthWidth, mouthY, mouthPaint);
        } else if (level <= 7) {
            RectF oval = new RectF(cx - mouthWidth, mouthY - mouthWidth * 0.1f,
                    cx + mouthWidth, mouthY + mouthWidth * 0.5f);
            canvas.drawArc(oval, 190f, 160f, false, mouthPaint);
        } else {
            RectF oval = new RectF(cx - mouthWidth, mouthY,
                    cx + mouthWidth, mouthY + mouthWidth);
            canvas.drawArc(oval, 190f, 160f, false, mouthPaint);
            if (level >= 10) {
                float tearX = cx + radius * 0.45f;
                float tearY = cy - radius * 0.05f;
                float dropRadius = radius * 0.12f;
                canvas.drawCircle(tearX, tearY + dropRadius, dropRadius, tearPaint);
                Path path = new Path();
                path.moveTo(tearX, tearY - dropRadius * 0.5f);
                path.lineTo(tearX - dropRadius, tearY + dropRadius);
                path.lineTo(tearX + dropRadius, tearY + dropRadius);
                path.close();
                canvas.drawPath(path, tearPaint);
            }
        }
    }
}
