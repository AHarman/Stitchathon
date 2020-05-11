package com.alexharman.stitchathon;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.alexharman.stitchathon.KnitPackage.KnitPattern;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.stream.IntStream;

@RunWith(RobolectricTestRunner.class)
public class KnitPatternDrawerUnitTests {
    private KnitPattern testPattern;

    @Mock
    private SharedPreferences prefs;

    @Before
    public void setUpKnitPattern() {
        int testPatternHeight = 10;
        int testPatternWidth = 20;
        String[][] testStitches = new String[testPatternHeight][testPatternWidth];
        IntStream.range(0, testStitches.length)
                .forEach(row -> IntStream.range(0, testStitches[row].length)
                        .forEach( col -> testStitches[row][col] = String.valueOf((row + col) % 3)));
        testPattern = new KnitPattern("", testStitches, true);
    }

    @Before
    public void setUpPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application.getApplicationContext());
        prefs
                .edit()
                .putInt(PreferenceKeys.STITCH_COLOUR_1, 0xFFFF0000)
                .putInt(PreferenceKeys.STITCH_COLOUR_2, 0xFF00FF00)
                .putInt(PreferenceKeys.STITCH_COLOUR_3, 0xFF0000FF)
                .putInt(PreferenceKeys.STITCH_COLOUR_3, 0xFF0000FF)
                .putString(PreferenceKeys.STITCH_SIZE, "10")
                .putString(PreferenceKeys.STITCH_PAD, "2")
                .commit();
    }

//    @Test
//    public void givenPatternLargerThanView_ifScrollWithinBounds_thenScrollAmountProvided() {
//        int width = 50;
//        int height = 50;
//        int xScroll = 20;
//        int yScroll = 30;
//        KnitPatternDrawer drawer = new KnitPatternDrawer(testPattern, width, height, prefs);
//
//        drawer.scroll(xScroll, yScroll);
//
//        assert(drawer.getViewport().left == xScroll && drawer.getViewport().top == yScroll);
//    }
//
//    @Test
//    public void givenPatternLargerThanView_ifScrollOutsideBounds_thenViewSizeUnchanged() {
//        int width = 50;
//        int height = 50;
//        int xScroll = -20;
//        int yScroll = -30;
//        KnitPatternDrawer drawer = new KnitPatternDrawer(testPattern, width, height, prefs);
//
//        drawer.scroll(xScroll, yScroll);
//
//        assert(drawer.getViewport().width() == width && drawer.getViewport().height() == height);
//    }
//
//    @Test
//    public void givenPatternLargerThanView_ifScrollLeftOfBounds_thenStopAtLeft() {
//        int width = 50;
//        int height = 50;
//        int xScroll = -20;
//        int yScroll = 0;
//        KnitPatternDrawer drawer = new KnitPatternDrawer(testPattern, width, height, prefs);
//
//        drawer.scroll(xScroll, yScroll);
//
//        assert(drawer.getViewport().left == 0);
//    }
//
//    @Test
//    public void givenPatternLargerThanView_ifScrollRightOfBounds_thenStopAtRight() {
//        int width = 50;
//        int height = 50;
//        int xScroll = 2000;
//        int yScroll = 0;
//        KnitPatternDrawer drawer = new KnitPatternDrawer(testPattern, width, height, prefs);
//
//        drawer.scroll(xScroll, yScroll);
//
//        assert(drawer.getViewport().right == drawer.getOverallWidth());
//    }
//
//    @Test
//    public void givenPatternLargerThanView_ifScrollAboveBounds_thenStopAtTop() {
//        int width = 50;
//        int height = 50;
//        int xScroll = 0;
//        int yScroll = -30;
//        KnitPatternDrawer drawer = new KnitPatternDrawer(testPattern, width, height, prefs);
//
//        drawer.scroll(xScroll, yScroll);
//
//        assert(drawer.getViewport().top == 0);
//    }
//
//    @Test
//    public void givenPatternLargerThanView_ifScrollBelowBounds_thenStopAtBottom() {
//        int width = 50;
//        int height = 50;
//        int xScroll = 0;
//        int yScroll = 3000;
//        KnitPatternDrawer drawer = new KnitPatternDrawer(testPattern, width, height, prefs);
//
//        drawer.scroll(xScroll, yScroll);
//
//        assert(drawer.getViewport().bottom == drawer.getOverallHeight());
//    }
//
//    @Test
//    public void givenPatternNarrowerThanView_ifScrollHorizontal_thenXUnchanged() {
//        int width = 5000;
//        int height = 50;
//        int xScroll = 20;
//        int yScroll = 0;
//        KnitPatternDrawer drawer = new KnitPatternDrawer(testPattern, width, height, prefs);
//        int expected = drawer.getViewport().left;
//
//                drawer.scroll(xScroll, yScroll);
//
//        assertEquals(expected, drawer.getViewport().left);
//    }
//
//    @Test
//    public void givenPatternShorterThanView_ifScrollVertical_thenYUnchanged() {
//        int width = 50;
//        int height = 5000;
//        int xScroll = 0;
//        int yScroll = 20;
//        KnitPatternDrawer drawer = new KnitPatternDrawer(testPattern, width, height, prefs);
//        int expected = drawer.getViewport().bottom;
//
//        drawer.scroll(xScroll, yScroll);
//
//        assertEquals(expected, drawer.getViewport().bottom);
//    }

}