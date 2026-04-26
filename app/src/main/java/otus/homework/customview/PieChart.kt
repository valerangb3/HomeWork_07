package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
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
        amount = 1841,
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
    private var centerX = 0f
    private var centerY = 0f
    private var totalAmount: Long = 0
    private var availableAngle = -1f
    private val minAngle = 5f
    private val minGap = .5f
    private var commonRadius = 0f
    private var maxStrokeWidth: Float = -1f
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = defaultStrokeWidthPx
    }
    private val slices = mutableMapOf<String, Slice>()
    private val minViewSize = resources.getDimensionPixelSize(
        R.dimen.pieChartVewMinSize
    )
    private val categories = mutableMapOf<String, PieItemView>()

    init {
        setData(pieSectors)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (categories.isEmpty()) return
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = measuredWidth / 2f
        centerY = measuredHeight / 2f
        setSlices()
    }

    override fun onDraw(canvas: Canvas) {
        if (slices.isEmpty()) return
        //super.onDraw(canvas)
        slices.forEach { (_, slice) ->
            paint.strokeWidth = slice.strokeWidth
            paint.color = slice.color
            canvas.drawArc(slice.rectF, slice.startAngle, slice.sweepAngle, false, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x - centerX
            val y = event.y - centerY
            val distance = sqrt(x * x + y * y)
            val outerRadius = commonRadius + maxStrokeWidth / 2
            val innerRadius = commonRadius - maxStrokeWidth / 2

            if (distance in innerRadius..outerRadius) {
                //Log.d("PieChart", "maxStrokeWidth = $maxStrokeWidth")
                //Log.d("PieChart", "innerRadius radius = $innerRadius")
                //Log.d("PieChart", "outerRadius radius = $outerRadius")
                //Log.d("PieChart", "distance = $distance")
                var touchAngle = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
                if (touchAngle < 0) touchAngle += 360f
                touchAngle = (touchAngle + 90f) % 360f
                findSector(touchAngle)
                Log.d("PieChart", "touchAngle = $touchAngle")
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findSector(touchAngle: Float): PieSector? {
        if (slices.isEmpty()) return null
        slices.forEach { (category, slice) ->
            val startAngle = slice.normalizedStartAngle
            val endAngle = slice.normalizedEndAngle
            if (touchAngle in startAngle..endAngle) {
                Log.d("PieChart", "Ты нажмал на `${categories[category]?.category ?: "wtf?!"}`")
            }
        }
        return null
    }

    fun setData(sectors: List<PieSector>) {
        if (sectors.isEmpty()) return
        categories.clear()
        slices.clear()
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
        availableAngle = FULL_ROTATION_DEG - (categories.size * gap)
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
            val radius = commonRadius - (maxStrokeWidth - strokeWidth) / 2

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
            slices[category] = Slice(
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                strokeWidth = strokeWidth,
                normalizedStartAngle = normalizedStartAngle,
                normalizedEndAngle = normalizedEndAngle,
                color = color,
                rectF = RectF(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius
                ).apply {
                   // inset(70f, 70f)
                },
            )
            normalizedStartAngle = normalizedEndAngle + minGap
            startAngle = endAngle + minGap
        }
    }

    private class Slice(
        val startAngle: Float,
        val normalizedStartAngle: Float,
        val normalizedEndAngle: Float,
        val sweepAngle: Float,
        val strokeWidth: Float,
        val rectF: RectF,
        val color: Int
    )

    private companion object {
        const val FULL_ROTATION_DEG = 360f
        const val START_ANGLE = -90f
        const val GOLDEN_RATIO_CONJUGATE = .618034f
        const val INITIAL_HUE = .33f // green
        const val SATURATION = .7f
        const val BRIGHTNESS = .85f
    }
}