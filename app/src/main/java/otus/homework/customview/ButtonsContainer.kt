package otus.homework.customview

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.setPadding

internal class ButtonsContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = R.attr.buttonContainerStyle,
    @StyleRes defStyleRes: Int = R.style.DefaultButtonsContainerStyle,
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    init {
        isInEditMode
        LayoutInflater.from(context).inflate(R.layout.buttons_container, this, true)

        orientation = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> VERTICAL
            else -> HORIZONTAL
        }
        setPadding(resources.getDimensionPixelSize(R.dimen.dp16))

        val topButton = findViewById<Button>(R.id.top_button)
        val bottomButton = findViewById<Button>(R.id.bottom_button)

        context.obtainStyledAttributes(
            attrs,
            R.styleable.ButtonsContainer,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                val topButtonText = getString(R.styleable.ButtonsContainer_topButtonText)
                val bottomButtonText = getString(R.styleable.ButtonsContainer_bottomButtonText)
                val topButtonColor = getColor(R.styleable.ButtonsContainer_topButtonBackgroundColor, 0)
                val bottomButtonColor = getColor(R.styleable.ButtonsContainer_bottomButtonBackgroundColor, 0)

                topButton.text = topButtonText
                bottomButton.text = bottomButtonText

                topButton.setBackgroundColor(topButtonColor)
                bottomButton.setBackgroundColor(bottomButtonColor )
            } finally {
                recycle()
            }
        }
    }
}