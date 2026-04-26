package otus.homework.customview

data class PieSector(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Int,
)

internal data class PieItemView(
    val category: String,
    var attitude: Float,
    var amount: Long,
)