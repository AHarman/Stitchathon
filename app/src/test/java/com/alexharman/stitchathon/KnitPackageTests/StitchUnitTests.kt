package com.alexharman.stitchathon.KnitPackageTests

import com.alexharman.stitchathon.KnitPackage.Stitch
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class StitchUnitTests {

    @Test
    fun ifStitchContainsSlash_thenSplitStitch() {
        val stitch = Stitch("Foo/Bar")

        assert(stitch.isSplit)
    }

    @Test
    fun ifStitchDoesntContainSlash_thenNotSplitStitch() {
        val stitch = Stitch("FooBar")

        assertFalse(stitch.isSplit)
    }

    @Test
    fun ifSplitStitch_thenMadeOfCorrect() {
        val stitch = Stitch("Foo/Bar")
        val expected = arrayOf("Foo", "Bar").map { Stitch(it) }.toTypedArray()

        assert(expected.contentEquals(stitch.madeOf))
    }
}