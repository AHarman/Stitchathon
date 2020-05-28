package com.alexharman.stitchathon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View

abstract class BaseFragmentView<V: BaseView<P>, P: BasePresenter<V>>: Fragment(), BaseView<P> {
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