package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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

import com.alexharman.stitchathon.databaseAccessAsyncTasks.DeletePatternsTask;
import com.alexharman.stitchathon.databaseAccessAsyncTasks.GetNamesAndImagesTask;
import com.alexharman.stitchathon.databaseAccessAsyncTasks.GetNamesAndImagesTask.GetNamesAndThumbnails;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpenPatternActivity extends AppCompatActivity implements GetNamesAndThumbnails {
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pattern);
        setUpGridView();

        new GetNamesAndImagesTask(this, this).execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onNamesAndThumbnailsReturn(@NotNull HashMap<String, Bitmap> map) {
        ((MyAdaptor) gridView.getAdapter()).addItems(map);
    }

    private void setUpGridView() {
        gridView = findViewById(R.id.pattern_select_grid);
        gridView.setAdapter(new MyAdaptor());
        gridView.setEmptyView(findViewById(R.id.empty_view));

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
                mode.getMenuInflater().inflate(R.menu.delete_button, menu);
                mode.setTitle(R.string.select_patterns);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete_button) {
                    MyAdaptor adaptor = (MyAdaptor)gridView.getAdapter();
                    ArrayList<String> toBeDeleted = new ArrayList<>();
                    for (int i = adaptor.getCount(); i >= 0; i--) {
                        if (gridView.isItemChecked(i)) {
                            toBeDeleted.add(adaptor.getItem(i));
                            adaptor.removeItem(i);
                        }
                    }
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(OpenPatternActivity.this);
                    if (toBeDeleted.contains(prefs.getString(PreferenceKeys.CURRENT_PATTERN_NAME, ""))) {
                        prefs.edit().remove(PreferenceKeys.CURRENT_PATTERN_NAME).apply();
                    }

                    new DeletePatternsTask(OpenPatternActivity.this).execute(toBeDeleted.toArray(new String[]{}));
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private class MyAdaptor extends BaseAdapter {
        private ArrayList<String> patternNames = new ArrayList<>();
        private ArrayList<Bitmap> bitmaps = new ArrayList<>();

        @Override
        public int getCount() {
            return patternNames.size();
        }

        @Override
        public String getItem(int position) {
            return patternNames.get(position);
        }

        // Don't use this, the items don't have numerical IDs
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) OpenPatternActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        void addItems(HashMap<String, Bitmap> thumbs) {
            for (Map.Entry<String, Bitmap> entry: thumbs.entrySet()) {
                patternNames.add(entry.getKey());
                bitmaps.add(entry.getValue());
            }
            notifyDataSetChanged();
        }
    }
}
