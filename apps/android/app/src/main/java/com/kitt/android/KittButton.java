package com.kitt.android;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
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
        init(null);
    }
    
    public KittButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    public KittButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Configure text paint with smaller size for better fit
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(18); // Reduced from 48 for better fit
        textPaint.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        
        // Configure border paint
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setColor(Color.WHITE);
        
        // Read XML attributes
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.text});
            try {
                String xmlText = a.getString(0);
                if (xmlText != null) {
                    text = xmlText;
                }
            } finally {
                a.recycle();
            }
        }
        
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
        int padding = 3; // Reduced padding
        
        // Draw button background
        backgroundPaint.setColor(buttonColor);
        
        if (isGlowing || isPressed) {
            float shadowRadius = 6 * glowIntensity; // Reduced shadow
            backgroundPaint.setShadowLayer(shadowRadius, 0, 0, buttonColor);
            backgroundPaint.setAlpha((int) (255 * glowIntensity));
        } else {
            backgroundPaint.setShadowLayer(3, 0, 0, buttonColor); // Reduced shadow
            backgroundPaint.setAlpha(180);
        }
        
        RectF rect = new RectF(padding, padding, width - padding, height - padding);
        canvas.drawRoundRect(rect, 6, 6, backgroundPaint); // Reduced corner radius
        
        // Draw border
        if (isPressed) {
            borderPaint.setColor(Color.YELLOW);
            borderPaint.setStrokeWidth(3);
        } else {
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStrokeWidth(2);
        }
        canvas.drawRoundRect(rect, 6, 6, borderPaint);
        
        // Draw text if it exists
        if (text != null && !text.isEmpty()) {
            // Dynamically adjust text size based on button dimensions
            float maxTextSize = Math.min(height * 0.5f, width * 0.2f);
            float finalTextSize = Math.min(18, maxTextSize);
            textPaint.setTextSize(finalTextSize);
            
            float textY = height / 2f + textPaint.getTextSize() / 3f;
            canvas.drawText(text, width / 2f, textY, textPaint);
        }
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
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Set reasonable minimum dimensions for the button
        int minWidth = 50;  // Reduced from 200
        int minHeight = 30; // Reduced from 80
        
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width, height;
        
        // Handle width measurement
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(minWidth, widthSize);
        } else {
            width = minWidth;
        }
        
        // Handle height measurement
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(minHeight, heightSize);
        } else {
            height = minHeight;
        }
        
        setMeasuredDimension(width, height);
    }
    
    public void setText(String text) {
        this.text = text;
        invalidate();
    }
    
    public String getText() {
        return text;
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
