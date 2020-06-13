package com.alexharman.stitchathon

import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragmentView<V: BaseView<P>, P: BasePresenter<V>>:
        DialogFragment(),
        BaseView<P> {
    protected abstract val view: V

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }
}