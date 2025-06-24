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
    private boolean isLighted = false; // Added state variable
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
        
        // Configure text paint with larger size for better visibility
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(36); // 2x larger text for better readability
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        
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
        
        if (isLighted) {
            // "Lighted on" state: bright and solid
            backgroundPaint.setShadowLayer(0, 0, 0, buttonColor);
            backgroundPaint.setAlpha(255);
        } else {
            // "Lighted off" state: very dim
            backgroundPaint.setShadowLayer(0, 0, 0, buttonColor);
            backgroundPaint.setAlpha(30); // Very dim when off
        }

        if (isPressed) {
            // Pressed state: slightly brighter
            backgroundPaint.setShadowLayer(0, 0, 0, buttonColor);
            backgroundPaint.setAlpha(200);
        }

        RectF rect = new RectF(padding, padding, width - padding, height - padding);
        canvas.drawRoundRect(rect, 6, 6, backgroundPaint);
        
        // Draw subtle border only when lighted
        if (isLighted || isPressed) {
            borderPaint.setColor(Color.parseColor("#FFFFFF"));
            borderPaint.setStrokeWidth(1);
            canvas.drawRoundRect(rect, 6, 6, borderPaint);
        }
        
        // Draw text if it exists
        if (text != null && !text.isEmpty()) {
            // Set text color based on state
            if (isLighted) {
                textPaint.setColor(Color.WHITE);
            } else {
                textPaint.setColor(Color.parseColor("#AAAAAA"));
            }
            
            // Handle multi-line text
            String[] lines = text.split("\n");
            float lineHeight = textPaint.getTextSize() + 2;
            float totalTextHeight = lines.length * lineHeight;
            float startY = (height - totalTextHeight) / 2 + textPaint.getTextSize();
            
            for (int i = 0; i < lines.length; i++) {
                float y = startY + (i * lineHeight);
                canvas.drawText(lines[i], width / 2f, y, textPaint);
            }
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
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width, height;
        
        // Use exact dimensions if specified, otherwise use larger defaults
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = 80; // Larger default width
        }
        
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = 50; // Larger default height
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

    public void setLighted(boolean lighted) {
        isLighted = lighted;
        invalidate();
    }

    public boolean isLighted() {
        return isLighted;
    }
}
