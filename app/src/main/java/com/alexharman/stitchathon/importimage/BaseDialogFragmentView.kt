package com.alexharman.stitchathon.importimage

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView

abstract class BaseDialogFragmentView<V: BaseView<P>, P: BasePresenter<V>>: DialogFragment(), BaseView<P> {
    protected abstract val view: V

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.view = this.view
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }
}