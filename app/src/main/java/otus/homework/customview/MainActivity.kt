package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private suspend fun loadJson(): List<PieSector> {
        val jsonData = withContext(Dispatchers.IO) {
            try {
                resources.openRawResource(R.raw.payload)
                    .bufferedReader()
                    .use { it.readText() }
            } catch (_: Exception) {
                null
            }
        }
        val pieSectors = mutableListOf<PieSector>()
        if (jsonData != null) {
            val items = JSONArray(jsonData)
            for (i in 0 until items.length()) {
                val sectorItem = items.getJSONObject(i)
                val id = sectorItem.getInt("id")
                val name = sectorItem.getString("name")
                val amount = sectorItem.getInt("amount")
                val category = sectorItem.getString("category")
                val time = sectorItem.getInt("time")
                pieSectors.add(
                    PieSector(
                        id,
                        name,
                        amount,
                        category,
                        time
                    )
                )
            }

        }
        return pieSectors
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<PieChart>(R.id.pieChart)

        lifecycleScope.launch {
            val pieSectors = loadJson()
            if (pieSectors.isNotEmpty()) {
                pieChart.setData(pieSectors)
            }
        }

        val appContext = this.applicationContext

        pieChart.setOnSliceClickListener { slice ->
            Toast.makeText(appContext, "Категория: ${slice.category}", Toast.LENGTH_SHORT).show()
        }
    }
}