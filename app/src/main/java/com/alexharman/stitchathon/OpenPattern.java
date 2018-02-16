package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.alexharman.stitchathon.database.AppDatabase;

import java.io.IOException;

public class OpenPattern extends AppCompatActivity {
    String[] patternNames;
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pattern);
        gridView = findViewById(R.id.pattern_select_grid);
        getNamesAndImages();
    }
    private void fillGrid() {
        gridView.setAdapter(new myAdaptor(this));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("patternName", patternNames[position]);
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

    // TODO: Get images. Not yet implemented with Room
    private void getNamesAndImages() {
        Log.d("Opening", "in getNamesAndImages");
        new GetNamesAndImagesTask(AppDatabase.Companion.getAppDatabase(getApplicationContext())).execute();
    }

    @Nullable
    private Bitmap createThumbnail(String filename) {
        Log.d("Opening", "In createThumbnail");
        Log.d("Opening", "Filename: " + filename);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;
        BitmapRegionDecoder bitmapRegionDecoder;
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(getFilesDir() + "/" + filename, false);
            return bitmapRegionDecoder.decodeRegion(new Rect(0, 0, 500, 500), opts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_4444);
    }

    private class GetNamesAndImagesTask extends AsyncTask<Void, Void, String[]> {
        private AppDatabase db;

        GetNamesAndImagesTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            return db.knitPatternDao().getPatternNames();
        }

        @Override
        protected void onPostExecute(String[] strings) {
            patternNames = strings;
            fillGrid();
        }
    }

    private class myAdaptor extends BaseAdapter {
        private Context context;

        myAdaptor(Context context) {
            this.context = context;
        }
        @Override
        public int getCount() {
            return patternNames.length;
        }

        @Override
        public String getItem(int position) {
            return patternNames[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View gridItem;

            if (convertView == null) {
                gridItem = inflater.inflate(R.layout.grid_item, null);
            } else {
                gridItem = convertView;
            }
            TextView textView = gridItem.findViewById(R.id.grid_item_text);
            textView.setText(patternNames[position]);

            return gridItem;
        }
    }
}
