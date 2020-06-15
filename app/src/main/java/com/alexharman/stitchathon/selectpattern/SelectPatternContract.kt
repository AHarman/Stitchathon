
import android.graphics.Bitmap
import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView

interface SelectPatternContract {
    interface View: BaseView<Presenter> {

        // TODO: Probably shouldn't have a reference to Bitmap class here...
        fun setAvailablePatterns(patterns: Array<Pair<String, Bitmap?>>)

        fun removePatterns(patterns: Collection<String>)
    }

    interface Presenter: BasePresenter<View> {
        fun selectPattern(patternName: String)

        fun deletePatterns(patternNames: Collection<String>)
    }
}