package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class OpenPattern extends AppCompatActivity {
    ArrayList<String> jsonFilenames = new ArrayList<>();
    ArrayList<String> imageFilenames = new ArrayList<>();
    ArrayList<Bitmap> thumbnails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pattern);

        getNamesAndImages();
        GridView gridview = (GridView) findViewById(R.id.pattern_select_grid);
        gridview.setAdapter(new myAdaptor(this));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("pattern", Uri.fromFile(new File(getFilesDir() + "/" + jsonFilenames.get(position))));
                intent.putExtra("image", Uri.fromFile(new File(getFilesDir() + "/" + imageFilenames.get(position))));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void getNamesAndImages() {
        ArrayList<String> filenames = new ArrayList<>(Arrays.asList(fileList()));
        for (String string : filenames) {
            Log.d("Foo", string);
            if (string.endsWith(".json")) {
                jsonFilenames.add(string);
            } else if (string.endsWith(".png")) {
                imageFilenames.add(string);
                thumbnails.add(createThumbnail(string));
            }
        }
    }

    private Bitmap createThumbnail(String filename) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;
        BitmapRegionDecoder bitmapRegionDecoder = null;
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(getFilesDir() + "/" + filename, false);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return bitmapRegionDecoder.decodeRegion(new Rect(0, 0, 500, 500), opts);
    }

    private class myAdaptor extends BaseAdapter {
        Context context;

        public myAdaptor(Context context) {
            this.context = context;
        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return jsonFilenames.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return null;
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View gridItem;

            if (convertView == null) {
                gridItem = inflater.inflate(R.layout.grid_item, null);
                ImageView imageView = (ImageView) gridItem.findViewById(R.id.grid_item_image);
                imageView.setImageBitmap(thumbnails.get(position));
                TextView textView = (TextView) gridItem.findViewById(R.id.grid_item_text);
                textView.setText(jsonFilenames.get(position).split("\\.")[0]);
            } else {
                gridItem = convertView;
            }

            return gridItem;
        }
    }
}
