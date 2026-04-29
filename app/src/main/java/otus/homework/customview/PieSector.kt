package otus.homework.customview

import android.os.Parcel
import android.os.Parcelable

data class PieSector(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Int,
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeInt(amount)
        dest.writeString(category)
        dest.writeInt(time)
    }

    companion object CREATOR: Parcelable.Creator<PieSector> {
        override fun createFromParcel(parcel: Parcel): PieSector {
            return PieSector(parcel)
        }

        override fun newArray(size: Int): Array<out PieSector?> {
            return arrayOfNulls(size)
        }
    }
}

internal data class PieItemView(
    val category: String,
    var attitude: Float,
    var amount: Long,
)