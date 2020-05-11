package com.alexharman.stitchathon;

import android.graphics.Rect
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ViewPortTests {

    @Test
    fun givenViewPortConstructed_whenNoMethodsCalled_thenCurrentViewIsCorrect() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        Assert.assertEquals(Rect(0, 0, 10, 10), viewPort.currentView)
    }

    @Test
    fun givenOffsetWithinBounds_whenOffsetMade_thenViewPortIsOffset() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offset(5, 5)

        Assert.assertEquals(Rect(5, 5, 15, 15), viewPort.toRect())
    }

    @Test
    fun givenOffsetYPutsAboveBound_whenOffsetMade_thenViewPortIsAtTopOfBounds() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offset(0, -1)

        Assert.assertEquals(Rect(0, 0, 10, 10), viewPort.toRect())
    }

    @Test
    fun givenOffsetYPutsBelowBound_whenOffsetMade_thenViewPortIsAtBottomOfBounds() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offset(0, 15)

        Assert.assertEquals(Rect(0, 10, 10, 20), viewPort.toRect())
    }

    @Test
    fun givenOffsetXPutsRightOfBound_whenOffsetMade_thenViewPortIsAtRightOfBounds() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offset(15, 0)

        Assert.assertEquals(Rect(10, 0, 20, 10), viewPort.toRect())
    }

    @Test
    fun givenOffsetXPutsLeftOfBound_whenOffsetMade_thenViewPortIsAtLeftOfBounds() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offset(-5, 0)

        Assert.assertEquals(Rect(0, 0, 10, 10), viewPort.toRect())
    }

    @Test
    fun givenOffsetToWithinBounds_whenOffsetToMade_thenViewPortIsOffset() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offsetTo(5, 5)

        Assert.assertEquals(Rect(5, 5, 15, 15), viewPort.toRect())
    }

    @Test
    fun givenOffsetToPutsLeftOfBound_whenOffsetToMade_thenViewPortIsAtLeftOfBounds() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offsetTo(-1, 0)

        Assert.assertEquals(Rect(0, 0, 10, 10), viewPort.toRect())
    }

    @Test
    fun givenOffsetToPutsRightOfBound_whenOffsetToMade_thenViewPortIsAtRightOfBounds() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offsetTo(15, 0)

        Assert.assertEquals(Rect(10, 0, 20, 10), viewPort.toRect())
    }

    @Test
    fun givenOffsetToPutsAboveBound_whenOffsetToMade_thenViewPortIsAtTopOfBounds() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offsetTo(0, -1)

        Assert.assertEquals(Rect(0, 0, 10, 10), viewPort.toRect())
    }

    @Test
    fun givenOffsetToPutsBelowBound_whenOffsetToMade_thenViewPortIsAtBottomOfBounds() {
        val viewPort = BoundedViewPort(Rect(0, 0, 10, 10), Rect(0, 0, 20, 20))

        viewPort.offsetTo(0, 15)

        Assert.assertEquals(Rect(0, 10, 10, 20), viewPort.toRect())
    }
}