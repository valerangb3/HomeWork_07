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


private val pieSectors = listOf(
    PieSector(
        id = 1,
        name = "Азбука Вкуса",
        amount = 1580111,
        category = "Продукты",
        time = 1623318531
    ),
    PieSector(
        id = 2,
        name = "Ригла",
        amount = 4912129,
        category = "Здоровье",
        time = 1623322251
    ),
    PieSector(
        id = 3,
        name = "Пятерочка",
        amount = 32333,
        category = "Продукты",
        time = 1623322371
    ),
    PieSector(
        id = 4,
        name = "Truffo",
        amount = 312333,
        category = "Кафе и рестораны",
        time = 1623326031
    ),
    PieSector(
        id = 5,
        name = "Simple Wine",
        amount = 14243124,
        category = "Алкоголь",
        time = 1623329631
    ),
    PieSector(
        id = 6,
        name = "Азбука Вкуса Экспресс",
        amount = 1841322,
        category = "Доставка еды",
        time = 1623322371
    ),
    PieSector(
        id = 7,
        name = "Uber",
        amount = 36129,
        category = "Транспорт",
        time = 1623416031
    ),
    PieSector(
        id = 8,
        name = "Метро",
        amount = 102220,
        category = "Транспорт",
        time = 1623416211
    ),
    PieSector(
        id = 9,
        name = "Стоматология",
        amount = 80100,
        category = "Здоровье",
        time = 1623419811
    ),
    PieSector(
        id = 10,
        name = "Пятерочка",
        amount = 809,
        category = "Продукты",
        time = 1623419934
    ),
    PieSector(
        id = 11,
        name = "Бассейн",
        amount = 1011100,
        category = "Спорт",
        time = 1623419934
    ),
    PieSector(
        id = 12,
        name = "Uber",
        amount = 3322189,
        category = "Транспорт",
        time = 1623419934
    ),
)

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private val gap: Long = 2 //TODO - можно доделатиь, что прокинуть из параметра
    private val defaultStrokeWidthPx = 70f //TODO - можно доделатиь, что прокинуть из параметра
    private var inset = 70f //TODO - можно доделатиь, что прокинуть из параметра (отступ)
    private val minAngle = 15f //TODO - можно доделатиь, что прокинуть из параметра (нормализация угла)
    private var centerX = 0f
    private var centerY = 0f
    private var totalAmount: Long = 0
    private val minGap = .5f
    private var commonRadius = 0f
    private var maxStrokeWidth = -1f
    private val paintArc = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = defaultStrokeWidthPx
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
    private val categories = mutableMapOf<String, PieItemView>()
    private val slices = mutableMapOf<String, Slice>()
    private var sectors: MutableList<PieSector>? = null
    private var downTouch = false
    private var lastClickX = -1f
    private var lastClickY = -1f

    init {
        // setData(pieSectors)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("ViewLifeCycle", "onMeasure")
        //if (categories.isEmpty()) return
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
        Log.d("ViewLifeCycle", "onMeasure: size = $size")
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d("ViewLifeCycle", "onSizeChanged")
        if (categories.isEmpty()) return
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = measuredWidth / 2f
        centerY = measuredHeight / 2f
        setSlices()

    }

    override fun onDraw(canvas: Canvas) {
        Log.d("ViewLifeCycle", "onDraw")
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
        val outerRadius = commonRadius - inset + maxStrokeWidth / 2
        val innerRadius = commonRadius - inset - maxStrokeWidth / 2

        if (distance in innerRadius..outerRadius) {
            var touchAngle = Math.toDegrees(atan2(lastClickY, lastClickX).toDouble()).toFloat()
            if (touchAngle < 0) touchAngle += FULL_ROTATION_DEG
            touchAngle = (touchAngle + 90f) % FULL_ROTATION_DEG
            findSector(touchAngle)
            Log.d("PieChart", "touchAngle = $touchAngle")
        }
        return true
    }

    private fun findSector(touchAngle: Float): PieItemView? {
        if (slices.isEmpty()) return null
        slices.forEach { (category, slice) ->
            val startAngle = slice.normalizedStartAngle
            val endAngle = slice.normalizedEndAngle
            if (touchAngle in startAngle..endAngle) {
                Log.d("PieChart", "Ты нажмал на `${categories[category]?.category ?: "wtf?!"}`")
                return categories[category]
            }
        }
        return null
    }

    fun setCategories() {
        categories.clear()
        sectors?.let { sectors ->
            sectors.forEach { totalAmount += it.amount }
            sectors.forEach { category ->
                val cat = categories[category.category]
                cat?.let { cCat ->
                    cCat.amount += category.amount
                    cCat.attitude = cCat.amount.toFloat() / totalAmount.toFloat() // attitude - отношение
                } ?: run {
                    categories[category.category] = PieItemView(
                        category = category.category,
                        attitude = category.amount.toFloat() / totalAmount.toFloat(), // attitude - отношение
                        amount = category.amount.toLong()
                    )
                }
            }
        }
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

    private fun setSlices() {
        if (categories.isEmpty()) return
        categories.forEach { (_, view) ->
            maxStrokeWidth = max(
                defaultStrokeWidthPx + (defaultStrokeWidthPx * view.attitude),
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
            val strokeWidth = defaultStrokeWidthPx + (defaultStrokeWidthPx * view.attitude)
            val radius = (commonRadius - (maxStrokeWidth - strokeWidth) / 2) - 20f //todo padding (20f)

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
            val textRadius = commonRadius + (paintArc.strokeWidth / 2) - 20f
            val x = centerX + textRadius * cos(Math.toRadians(middleAngle.toDouble())).toFloat()
            val y = centerY + textRadius * sin(Math.toRadians(middleAngle.toDouble())).toFloat()

            val sliceText = "%.2f%%".format(view.attitude * 100)

            val textBounds = Rect()
            paintText.getTextBounds(sliceText, 0, sliceText.length, textBounds)

            slices[category] = Slice(
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

    override fun onSaveInstanceState(): Parcelable {
        Log.d("ViewLifeCycle", "onSaveInstanceState")
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.pieChartData = sectors
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        Log.d("ViewLifeCycle", "onRestoreInstanceState")
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            //sectors = state.pieChartData?.toMutableList() ?: emptyList()
            sectors = state.pieChartData?.toMutableList() ?: emptyList()
            slices.clear()
            setCategories()
            setSlices()
            //categories.clear()
            //setData(items)
        } else super.onRestoreInstanceState(state)
    }

    private class Slice(
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

    private companion object {
        const val FULL_ROTATION_DEG = 360f
        const val START_ANGLE = -90f
        const val GOLDEN_RATIO_CONJUGATE = .618034f
        const val INITIAL_HUE = .33f // green
        const val SATURATION = .7f
        const val BRIGHTNESS = .85f
    }
}