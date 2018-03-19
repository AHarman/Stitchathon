package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexharman.stitchathon.database.AppDatabase;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpenPatternActivity extends AppCompatActivity {
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pattern);
        gridView = findViewById(R.id.pattern_select_grid);
        new GetNamesAndImagesTask(AppDatabase.Companion.getAppDatabase(getApplicationContext()), this).execute();
    }

    private void fillGrid(HashMap<String, Bitmap> thumbs) {
        gridView.setAdapter(new MyAdaptor(this, thumbs));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("patternName", (String) gridView.getAdapter().getItem(position));
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

    private static class GetNamesAndImagesTask extends AsyncTask<Void, Void, HashMap<String, Bitmap>> {
        private AppDatabase db;
        private WeakReference<OpenPatternActivity> context;

        GetNamesAndImagesTask(AppDatabase db, OpenPatternActivity context) {
            this.db = db;
            this.context = new WeakReference<>(context);
        }

        @Override
        protected HashMap<String, Bitmap> doInBackground(Void... voids) {
            return db.knitPatternDao().getThumbnails(context.get());
        }

        @Override
        protected void onPostExecute(HashMap<String, Bitmap> thumbs) {
            context.get().fillGrid(thumbs);
        }
    }

    private class MyAdaptor extends BaseAdapter {
        private Context context;
        private ArrayList<String> patternNames;
        private ArrayList<Bitmap> bitmaps;

        MyAdaptor(Context context, HashMap<String, Bitmap> thumbs) {
            this.context = context;
            patternNames = new ArrayList<>();
            bitmaps = new ArrayList<>();

            for (Map.Entry<String, Bitmap> entry: thumbs.entrySet()) {
                patternNames.add(entry.getKey());
                bitmaps.add(entry.getValue());
            }
        }

        @Override
        public int getCount() {
            return patternNames.size();
        }

        @Override
        public String getItem(int position) {
            return patternNames.get(position);
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
            ImageView imageView = gridItem.findViewById(R.id.grid_item_image);
            imageView.setImageBitmap(bitmaps.get(position));
            TextView textView = gridItem.findViewById(R.id.grid_item_text);
            textView.setText(patternNames.get(position));

            return gridItem;
        }
    }
}
