package com.kitt.android;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom KITT-style button with glow effects and retro-tech styling
 */
public class KittButton extends View {
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint textPaint;
    private String text = "";
    private int buttonColor = Color.RED;
    private boolean isPressed = false;
    private boolean isGlowing = false;
    private ValueAnimator glowAnimator;
    private float glowIntensity = 0.3f;
    
    public KittButton(Context context) {
        super(context);
        init();
    }
    
    public KittButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public KittButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Configure text paint
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(20);
        textPaint.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        
        // Configure border paint
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.WHITE);
        
        setupGlowAnimation();
        setClickable(true);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // Enable shadow layer
    }
    
    private void setupGlowAnimation() {
        glowAnimator = ValueAnimator.ofFloat(0.3f, 1.0f);
        glowAnimator.setDuration(800);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.addUpdateListener(animation -> {
            glowIntensity = (Float) animation.getAnimatedValue();
            invalidate();
        });
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int padding = 8;
        
        // Draw button background
        backgroundPaint.setColor(buttonColor);
        
        if (isGlowing || isPressed) {
            float shadowRadius = 15 * glowIntensity;
            backgroundPaint.setShadowLayer(shadowRadius, 0, 0, buttonColor);
            backgroundPaint.setAlpha((int) (255 * glowIntensity));
        } else {
            backgroundPaint.setShadowLayer(8, 0, 0, buttonColor);
            backgroundPaint.setAlpha(180);
        }
        
        RectF rect = new RectF(padding, padding, width - padding, height - padding);
        canvas.drawRoundRect(rect, 12, 12, backgroundPaint);
        
        // Draw border
        if (isPressed) {
            borderPaint.setColor(Color.YELLOW);
            borderPaint.setStrokeWidth(4);
        } else {
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStrokeWidth(2);
        }
        canvas.drawRoundRect(rect, 12, 12, borderPaint);
        
        // Draw text
        float textY = height / 2f + textPaint.getTextSize() / 3f;
        canvas.drawText(text, width / 2f, textY, textPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                invalidate();
                performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
    
    public void setText(String text) {
        this.text = text;
        invalidate();
    }
    
    public void setButtonColor(int color) {
        this.buttonColor = color;
        invalidate();
    }
    
    public void startGlow() {
        isGlowing = true;
        glowAnimator.start();
    }
    
    public void stopGlow() {
        isGlowing = false;
        glowAnimator.cancel();
        glowIntensity = 0.3f;
        invalidate();
    }
}
