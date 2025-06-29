package com.kitt.android

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.abs

class KittScannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ledCount = 6
    private val ledSpacing = 40f
    private val ledRadius = 15f
    private var animationProgress = 0f
    private var animator: ValueAnimator? = null
    private var isTalkingMode = false
    
    private val redColor = Color.RED
    private val darkRedColor = Color.argb(80, 255, 0, 0)
    private val blackColor = Color.argb(40, 0, 0, 0)

    init {
        startAnimation()
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = if (isTalkingMode) 500 else 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                animationProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val totalWidth = (ledCount - 1) * ledSpacing
        val startX = centerX - totalWidth / 2f
        
        if (isTalkingMode) {
            // Pulsing animation for talking
            val pulseFactor = (animationProgress * 2 * Math.PI).toFloat().let { Math.sin(it.toDouble()).toFloat() }
            val brightness = (pulseFactor + 1) / 2 // Normalize to 0-1 range
            
            for (i in 0 until ledCount) {
                val ledX = startX + i * ledSpacing
                val ledY = centerY
                
                val alpha = (255 * brightness).toInt().coerceIn(0, 255)
                val color = when {
                    brightness > 0.8f -> redColor
                    brightness > 0.4f -> Color.argb(alpha, 255, 100, 0)
                    brightness > 0.2f -> darkRedColor
                    else -> blackColor
                }
                
                paint.color = color
                canvas.drawCircle(ledX, ledY, ledRadius, paint)
                
                if (brightness > 0.4f) {
                    paint.color = Color.argb((alpha * 0.3f).toInt(), 255, 0, 0)
                    canvas.drawCircle(ledX, ledY, ledRadius * (1 + brightness * 0.5f), paint)
                }
            }
        } else {
            // Left-right movement for thinking
            val activePosition = animationProgress * (ledCount - 1)
            
            for (i in 0 until ledCount) {
                val ledX = startX + i * ledSpacing
                val ledY = centerY
                
                // Calculate distance from active position to determine brightness
                val distance = abs(i - activePosition)
                val brightness = when {
                    distance < 0.5f -> 1.0f // Main bright LED
                    distance < 1.5f -> 0.6f // Adjacent LEDs with medium brightness
                    distance < 2.5f -> 0.3f // Further LEDs with low brightness
                    else -> 0.1f // Background LEDs
                }
                
                // Set color based on brightness
                val alpha = (255 * brightness).toInt().coerceIn(0, 255)
                val color = when {
                    brightness > 0.8f -> redColor
                    brightness > 0.4f -> Color.argb(alpha, 255, 100, 0) // Orange-red
                    brightness > 0.2f -> darkRedColor
                    else -> blackColor
                }
                
                paint.color = color
                canvas.drawCircle(ledX, ledY, ledRadius, paint)
                
                // Add glow effect for brighter LEDs
                if (brightness > 0.4f) {
                    paint.color = Color.argb((alpha * 0.3f).toInt(), 255, 0, 0)
                    canvas.drawCircle(ledX, ledY, ledRadius * 1.5f, paint)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = ((ledCount - 1) * ledSpacing + ledRadius * 4).toInt()
        val desiredHeight = (ledRadius * 4).toInt()
        
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        
        setMeasuredDimension(width, height)
    }

    fun stopAnimation() {
        animator?.cancel()
    }

    fun resumeAnimation() {
        if (animator?.isRunning != true) {
            startAnimation()
        }
    }

    fun setTalkingMode(isTalking: Boolean) {
        if (isTalkingMode != isTalking) {
            isTalkingMode = isTalking
            startAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}
