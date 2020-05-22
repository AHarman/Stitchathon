import android.graphics.Bitmap
import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView

interface SelectPatternContract {
    interface View: BaseView<Presenter> {
        fun setAvailablePatterns(patterns: Array<Pair<String, Bitmap?>>)

        fun removePatterns(patterns: Array<Pair<String, Bitmap?>>)
    }

    interface Presenter: BasePresenter<View> {
        fun selectPattern(patternName: String)

        fun deletePatterns(patternNames: Array<String>)
    }
}