package com.alexharman.stitchathon

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragmentView<V: BaseView<P>, P: BasePresenter<V>>:
        DialogFragment(),
        BaseView<P> {
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