package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexharman.stitchathon.database.AppDatabase;
import com.alexharman.stitchathon.database.KnitPatternDao;

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
        setUpGridView();

        new GetNamesAndImagesTask(AppDatabase.Companion.getAppDatabase(getApplicationContext()), this).execute();
    }

    private void setUpGridView() {
        gridView = findViewById(R.id.pattern_select_grid);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("patternName", (String) gridView.getAdapter().getItem(position));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                gridView.getChildAt(position).setActivated(checked);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                Log.d("Foo", "onCreateActionMode");
                mode.getMenuInflater().inflate(R.menu.delete_button, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                Log.d("Foo", "onPrepareActionMode");
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete_button) {
                    SparseBooleanArray checked = gridView.getCheckedItemPositions();
                    ArrayList<String> toBeDeleted = new ArrayList<>();
                    for (int i = 0; i < checked.size(); i++) {
                        if (checked.get(checked.keyAt(i))) {
                            toBeDeleted.add((String) gridView.getAdapter().getItem(checked.keyAt(i)));
                            ((MyAdaptor)gridView.getAdapter()).removeItem(checked.keyAt(i));
                        }
                    }
                    new DeletePatternAsyncTask(OpenPatternActivity.this).execute(toBeDeleted.toArray(new String[]{}));
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.d("Foo", "onDestroyActionMode");
            }
        });
    }

    private void fillGrid(HashMap<String, Bitmap> thumbs) {
        gridView.setAdapter(new MyAdaptor(this, thumbs));
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

    private static class DeletePatternAsyncTask extends AsyncTask<String, Void, Void> {
        WeakReference<OpenPatternActivity> context;

        DeletePatternAsyncTask(OpenPatternActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... strings) {
            KnitPatternDao dao = AppDatabase.Companion.getAppDatabase(context.get()).knitPatternDao();
            for (String name: strings) {
                dao.deletePattern(name, context.get());
            }
            return null;
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

        void removeItem(int position) {
            patternNames.remove(position);
            bitmaps.remove(position);
            notifyDataSetChanged();
        }
    }
}
