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
import kotlin.run


private val pieSectors = listOf(
    PieSector(
        id = 1,
        name = "Азбука Вкуса",
        amount = 1580,
        category = "Продукты",
        time = 1623318531
    ),
    PieSector(
        id = 2,
        name = "Ригла",
        amount = 499,
        category = "Здоровье",
        time = 1623322251
    ),
    PieSector(
        id = 3,
        name = "Пятерочка",
        amount = 129,
        category = "Продукты",
        time = 1623322371
    ),
    PieSector(
        id = 4,
        name = "Truffo",
        amount = 4541,
        category = "Кафе и рестораны",
        time = 1623326031
    ),
    PieSector(
        id = 5,
        name = "Simple Wine",
        amount = 1600,
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
        amount = 369,
        category = "Транспорт",
        time = 1623416031
    ),
    PieSector(
        id = 8,
        name = "Метро",
        amount = 100,
        category = "Транспорт",
        time = 1623416211
    ),
    PieSector(
        id = 9,
        name = "Стоматология",
        amount = 8000,
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
        amount = 1000,
        category = "Спорт",
        time = 1623419934
    ),
    PieSector(
        id = 12,
        name = "Uber",
        amount = 389,
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

    private var totalAmount: Long = 0

    private var strokeWidthPx = 100f

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = strokeWidthPx
    }

    private val rectF = RectF()

    private val minViewSize = resources.getDimensionPixelSize(
        R.dimen.pieChartVewMinSize
    )

    private val categories = mutableMapOf<String, PieItemView>()

    init {
        setSectors(pieSectors)
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
    }

    override fun onDraw(canvas: Canvas) {
        if (categories.isEmpty()) return
        super.onDraw(canvas)
    }

    fun setSectors(sectors: List<PieSector>) {
        if (sectors.isEmpty()) return
        categories.clear()
        sectors.forEach { totalAmount += it.amount }
        sectors.forEach { category ->
            val cat = categories[category.category]
            cat?.let { cCat ->
                cCat.amount += category.amount
                cCat.percent = (cCat.amount.toFloat() / totalAmount.toFloat()) * 100f
            } ?: run {
                categories[category.category] = PieItemView(
                    category = category.category,
                    percent = (category.amount.toFloat() / totalAmount.toFloat()) * 100f,
                    amount = category.amount.toLong()
                )
            }
        }
    }
}