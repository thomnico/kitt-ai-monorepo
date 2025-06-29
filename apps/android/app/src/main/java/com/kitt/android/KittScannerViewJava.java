package com.kitt.android;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * KITT Scanner View - Oscillating red scanner like KITT's voice box
 */
public class KittScannerViewJava extends View {
    private Paint ledPaint;
    private float scannerPosition = 0;
    private ValueAnimator scannerAnimator;
    private int numLeds = 24;
    private float ledWidth;
    private float ledHeight;
    private boolean isScanning = false;
    
    public KittScannerViewJava(Context context) {
        super(context);
        init();
    }
    
    public KittScannerViewJava(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public KittScannerViewJava(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        ledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setBackgroundColor(Color.parseColor("#0a0a0a"));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setupScannerAnimation();
    }
    
    private void setupScannerAnimation() {
        scannerAnimator = ValueAnimator.ofFloat(0, numLeds - 1);
        scannerAnimator.setDuration(1200);
        scannerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scannerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scannerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scannerAnimator.addUpdateListener(animation -> {
            scannerPosition = (Float) animation.getAnimatedValue();
            invalidate();
        });
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ledWidth = (float) (w - 20) / numLeds;
        ledHeight = h - 20;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isScanning) return;
        
        int centerY = getHeight() / 2;
        
        for (int i = 0; i < numLeds; i++) {
            float x = 10 + i * ledWidth + ledWidth / 2;
            float distance = Math.abs(i - scannerPosition);
            
            // Calculate LED intensity based on distance from scanner
            float intensity = Math.max(0, 1.0f - distance / 4.0f);
            
            if (intensity > 0) {
                int red = (int) (255 * intensity);
                int color = Color.rgb(red, 0, 0);
                ledPaint.setColor(color);
                
                // Add glow effect
                float shadowRadius = 12 * intensity;
                ledPaint.setShadowLayer(shadowRadius, 0, 0, color);
                
                RectF ledRect = new RectF(
                    x - ledWidth / 3,
                    centerY - ledHeight / 3,
                    x + ledWidth / 3,
                    centerY + ledHeight / 3
                );
                canvas.drawRoundRect(ledRect, 6, 6, ledPaint);
            }
        }
    }
    
    public void startScanning() {
        if (!isScanning) {
            isScanning = true;
            scannerAnimator.start();
        }
    }
    
    public void stopScanning() {
        if (isScanning) {
            isScanning = false;
            scannerAnimator.cancel();
            invalidate();
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 60; // Fixed height for scanner
        setMeasuredDimension(width, height);
    }
}
