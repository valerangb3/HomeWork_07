package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import java.util.Collections.emptyList
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.run

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var strokeWidthPx: Float
    private var inset: Float
    private var minGap: Float
    private var minAngle: Float
    private var centerX = 0f
    private var centerY = 0f
    private var totalAmount: Long = 0
    private var commonRadius = 0f
    private var maxStrokeWidth = -1f
    private val paintArc = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLUE
    }
    private val paintText = Paint().apply {
        textSize = 20.0f
        style = Paint.Style.FILL
        color = Color.GRAY
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val paintBorderText = Paint().apply {
        textSize = 20.0f
        style = Paint.Style.STROKE
        color = Color.GRAY
    }
    private val minViewSize = resources.getDimensionPixelSize(
        R.dimen.pieChartVewMinSize
    )
    private val categories = mutableMapOf<String, Slice>()
    private val slices = mutableMapOf<String, PieItemView>()
    private var sectors: MutableList<PieSector>? = null
    private var downTouch = false
    private var lastClickX = -1f
    private var lastClickY = -1f
    private var onSectorClick: ((Slice) -> Unit)? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChart)
        strokeWidthPx = typedArray.getDimension(R.styleable.PieChart_strokeWidth, DEFAULT_STROKE_WIDTH)
        paintArc.strokeWidth = strokeWidthPx
        inset = typedArray.getDimension(R.styleable.PieChart_inset, DEFAULT_INSET)
        minGap = typedArray.getDimension(R.styleable.PieChart_gap, DEFAULT_GAP)
        minAngle = typedArray.getDimension(R.styleable.PieChart_normalize, DEFAULT_ANGLE)
        typedArray.recycle()
    }

    private fun findSector(touchAngle: Float): Slice? {
        if (slices.isEmpty()) return null
        slices.forEach { (category, slice) ->
            val startAngle = slice.normalizedStartAngle
            val endAngle = slice.normalizedEndAngle
            if (touchAngle in startAngle..endAngle) {
                return categories[category]
            }
        }
        return null
    }
    private fun setCategories() {
        categories.clear()
        sectors?.let { sectors ->
            sectors.forEach { totalAmount += it.amount }
            sectors.forEach { category ->
                val cat = categories[category.category]
                cat?.let { cCat ->
                    cCat.amount += category.amount
                    cCat.attitude = cCat.amount.toFloat() / totalAmount.toFloat() // attitude - отношение
                } ?: run {
                    categories[category.category] = Slice(
                        category = category.category,
                        attitude = category.amount.toFloat() / totalAmount.toFloat(), // attitude - отношение
                        amount = category.amount.toLong()
                    )
                }
            }
        }
    }
    private fun setSlices() {
        if (categories.isEmpty()) return
        categories.forEach { (_, view) ->
            maxStrokeWidth = max(
                strokeWidthPx + (strokeWidthPx * view.attitude),
                maxStrokeWidth
            )
        }
        val adjustedAngles = categories.mapValues { entry ->
            max((entry.value.amount.toFloat() / totalAmount.toFloat()) * FULL_ROTATION_DEG, minAngle)
        }
        val adjustedSum = adjustedAngles.values.sum()

        commonRadius = (measuredWidth - maxStrokeWidth) / 2f

        var startAngle = START_ANGLE
        var normalizedStartAngle = 0f

        categories.onEachIndexed { index, entry ->
            val (category, view) = entry
            val centerX = measuredWidth / 2
            val centerY = measuredHeight / 2
            val sweepAngle = (adjustedAngles[category]!! / adjustedSum) * FULL_ROTATION_DEG - minGap
            val strokeWidth = strokeWidthPx + (strokeWidthPx * view.attitude)
            val radius = (commonRadius - (maxStrokeWidth - strokeWidth) / 2) - inset //todo padding (20f)

            val hue = (INITIAL_HUE + index * GOLDEN_RATIO_CONJUGATE) % 1.0f
            val color = Color.HSVToColor(
                floatArrayOf(
                    hue * FULL_ROTATION_DEG,
                    SATURATION,
                    BRIGHTNESS
                )
            )
            val endAngle = startAngle + sweepAngle
            val normalizedEndAngle = normalizedStartAngle + sweepAngle

            val middleAngle = startAngle + sweepAngle / 2
            val textRadius = commonRadius + (paintArc.strokeWidth / 2) - inset
            val x = centerX + textRadius * cos(Math.toRadians(middleAngle.toDouble())).toFloat()
            val y = centerY + textRadius * sin(Math.toRadians(middleAngle.toDouble())).toFloat()

            val sliceText = "%.2f%%".format(view.attitude * 100)

            val textBounds = Rect()
            paintText.getTextBounds(sliceText, 0, sliceText.length, textBounds)

            slices[category] = PieItemView(
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                strokeWidth = strokeWidth,
                normalizedStartAngle = normalizedStartAngle,
                normalizedEndAngle = normalizedEndAngle,
                color = color,

                circleRectF = RectF(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius
                ).apply {
                    inset(inset, inset)
                },

                text = sliceText,
                textX = x,
                textY = y,
                textWidth = textBounds.width(),
                textHeight = textBounds.height()
            )
            normalizedStartAngle = normalizedEndAngle + minGap
            startAngle = endAngle + minGap
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //if (categories.isEmpty()) return
        val curWidth = MeasureSpec.getSize(widthMeasureSpec)
        val curHeight = MeasureSpec.getSize(heightMeasureSpec)

        val contentWidth = when (val wMode = MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST -> min(curWidth, minViewSize)
            MeasureSpec.EXACTLY -> min(curWidth, minViewSize)
            MeasureSpec.UNSPECIFIED -> minViewSize
            else -> error("Неизвестный режим ширины ($wMode)")
        }

        val contentHeight = when (val hMode = MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.AT_MOST -> min(curHeight, minViewSize)
            MeasureSpec.EXACTLY -> min(curHeight, minViewSize)
            MeasureSpec.UNSPECIFIED -> minViewSize
            else -> error("Неизвестный режим ширины ($hMode)")
        }

        val size = min(contentWidth, contentHeight)
        centerX = size / 2f
        centerY = size / 2f
        setMeasuredDimension(size, size)
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (categories.isEmpty()) return
        super.onSizeChanged(w, h, oldw, oldh)
        setSlices()

    }
    override fun onDraw(canvas: Canvas) {
        if (slices.isEmpty()) return
        //super.onDraw(canvas)
        slices.forEach { (_, slice) ->

            paintArc.strokeWidth = slice.strokeWidth
            paintArc.color = slice.color
            paintText.textAlign = Paint.Align.CENTER

            canvas.drawArc(slice.circleRectF, slice.startAngle, slice.sweepAngle, false, paintArc)
            canvas.drawText(slice.text, slice.textX, slice.textY, paintText)
            canvas.drawRect(
                slice.textX - 10f - slice.textWidth / 2f,
                slice.textY - slice.textHeight - 10f,
                slice.textX  + 10f + slice.textWidth / 2f,
                slice.textY + slice.textHeight,
                paintBorderText
            )
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downTouch = true
                true
            }
            MotionEvent.ACTION_UP -> {
                if (downTouch) {
                    downTouch = false
                    lastClickX = event.x - centerX
                    lastClickY = event.y - centerY
                    performClick()
                } else false
            }
            else -> false
        }
    }
    override fun performClick(): Boolean {
        super.performClick()
        val distance = sqrt(lastClickX * lastClickX + lastClickY * lastClickY)
        val outerRadius = commonRadius - inset - maxStrokeWidth / 2f
        val innerRadius = commonRadius - inset - maxStrokeWidth - maxStrokeWidth / 2f
        
        if (distance in innerRadius..outerRadius) {
            var touchAngle = Math.toDegrees(atan2(lastClickY, lastClickX).toDouble()).toFloat()
            if (touchAngle < 0) touchAngle += FULL_ROTATION_DEG
            touchAngle = (touchAngle + 90f) % FULL_ROTATION_DEG
            val slice = findSector(touchAngle)
            if (slice != null) {
                onSectorClick?.invoke(slice)
            }
        }
        return true
    }

    fun setData(items: List<PieSector>) {
        if (items.isEmpty()) return
        slices.clear()
        sectors?.clear()
        sectors?.apply {
            addAll(items)
        } ?: run {
            sectors = items.toMutableList()
        }
        setCategories()
        setSlices()
        invalidate()
    }

    fun setOnSliceClickListener(listener: (Slice) -> Unit) {
        onSectorClick = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.pieChartData = sectors
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            sectors = state.pieChartData?.toMutableList() ?: emptyList()
            slices.clear()
            setCategories()
            setSlices()
        } else super.onRestoreInstanceState(state)
    }

    class SavedState : BaseSavedState {
        var pieChartData: List<PieSector>? = null

        constructor(superState: Parcelable?): super(superState)
        constructor(parcel: Parcel): super(parcel) {
            pieChartData = parcel.createTypedArrayList(PieSector.CREATOR)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<out SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private class PieItemView(
        val text: String,
        val textX: Float,
        val textY: Float,
        val textWidth: Int,
        val textHeight: Int,

        val startAngle: Float,
        val normalizedStartAngle: Float,
        val normalizedEndAngle: Float,
        val sweepAngle: Float,
        val strokeWidth: Float,
        val circleRectF: RectF,
        val color: Int
    )

    data class Slice(
        val category: String,
        var attitude: Float,
        var amount: Long,
    )

    private companion object {
        const val FULL_ROTATION_DEG = 360f
        const val START_ANGLE = -90f
        const val GOLDEN_RATIO_CONJUGATE = .618034f
        const val INITIAL_HUE = .33f // green
        const val SATURATION = .7f
        const val BRIGHTNESS = .85f
        const val DEFAULT_STROKE_WIDTH = 70f
        const val DEFAULT_INSET = 70f
        const val DEFAULT_GAP = .5f
        const val DEFAULT_ANGLE = 15f
    }
}