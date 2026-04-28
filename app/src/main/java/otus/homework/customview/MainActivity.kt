package otus.homework.customview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val pieSectors = listOf(
        /*PieSector(
            id = 1,
            name = "Азбука Вкуса",
            amount = 1580111,
            category = "Продукты",
            time = 1623318531
        ),*/
        PieSector(
            id = 2,
            name = "Ригла",
            amount = 4912129,
            category = "Здоровье",
            time = 1623322251
        ),
        /*PieSector(
            id = 3,
            name = "Пятерочка",
            amount = 32333,
            category = "Продукты",
            time = 1623322371
        ),*/
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
        /*PieSector(
            id = 10,
            name = "Пятерочка",
            amount = 809,
            category = "Продукты",
            time = 1623419934
        ),*/
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<PieChart>(R.id.pieChart)

        findViewById<Button>(R.id.top_button).setOnClickListener {
            pieChart.setData(pieSectors)
            //val intent = Intent(this, SecondActivity::class.java)
            //startActivity(intent)
        }
    }
}