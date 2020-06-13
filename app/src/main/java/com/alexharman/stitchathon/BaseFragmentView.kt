package com.alexharman.stitchathon

import androidx.fragment.app.Fragment

abstract class BaseFragmentView<V: BaseView<P>, P: BasePresenter<V>>: Fragment(), BaseView<P> {
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