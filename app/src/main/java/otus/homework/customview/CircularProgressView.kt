package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import kotlin.math.min

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val strokeWidthPx = 20f
    private val maxProgress = 100f
    private var currentProgress = 35f

    private val rectF = RectF()

    private var centerX = 0f
    private var centerY = 0f

    private var sweepAngle = 0f
    private var radius = 0f

    private val trackPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.GRAY
        strokeWidth = strokeWidthPx
    }

    private val progressPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = strokeWidthPx
    }

    private val minViewSize = resources.getDimensionPixelSize(
        R.dimen.circularProgressViewMinSize
    )

    private fun Canvas.drawTrack() {
        drawCircle(centerX, centerY, radius, trackPaint)
    }

    private fun Canvas.drawProgress() {
        drawArc(rectF, -90f, sweepAngle, false, progressPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sweepAngle = (currentProgress / maxProgress) * 360
        centerX = measuredWidth / 2f
        centerY = measuredHeight / 2f
        radius = (measuredWidth - strokeWidthPx) / 2f

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val curWidth = MeasureSpec.getSize(widthMeasureSpec)
        val curHeight = MeasureSpec.getSize(heightMeasureSpec)

        val contentWidth = when (val wMode = MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST -> curWidth
            MeasureSpec.EXACTLY -> curWidth
            MeasureSpec.UNSPECIFIED -> minViewSize
            else -> error("Неизвестный режим ширины ($wMode)")
        }

        val contentHeight = when (val hMode = MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.AT_MOST -> curHeight
            MeasureSpec.EXACTLY -> curHeight
            MeasureSpec.UNSPECIFIED -> minViewSize
            else -> error("Неизвестный режим ширины ($hMode)")
        }

        val size = min(contentWidth, contentHeight)

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawTrack()
        canvas.drawProgress()
    }

    fun setCurrentProgress(newProgress: Float) {
        if (newProgress !in 0f..100f) return
        currentProgress = newProgress
        invalidate()
    }
}