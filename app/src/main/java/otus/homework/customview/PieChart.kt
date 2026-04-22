package otus.homework.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes



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

}