package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alexharman.stitchathon.KnitPackage.KnitPattern;
import com.alexharman.stitchathon.KnitPackage.KnitPatternParser;
import com.alexharman.stitchathon.database.AppDatabase;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ImportImageDialog.ImportImageDialogListener {

    private TextView stitchCount;
    private TextView rowCount;
    private TextView completeCount;
    private KnitPattern knitPattern;
    private KnitPatternView patternView;
    private ImportImageDialog importImageDialog;
    private static AppDatabase db;
    private OnSharedPreferenceChangeListener preferenceListener = new MySharedPreferenceListener();


    static final int READ_EXTERNAL_IMAGE = 42;
    static final int READ_EXTERNAL_JSON_PATTERN = 55;
    static final int OPEN_INTERNAL_PATTERN = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpUI();

        db = AppDatabase.Companion.getAppDatabase(getApplicationContext());

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener);
        String patternName = sharedPreferences.getString("pattern", null);
        if (patternName != null) {
            openPattern(patternName);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_open) {
            selectInternalPattern();
        } else if (id == R.id.nav_import_pattern) {
            selectExternalFile("application/json", READ_EXTERNAL_JSON_PATTERN);
        } else if (id == R.id.nav_import_image) {
            importImage();
        } else if (id == R.id.nav_about_app) {
            new AppInfoDialog().show(getSupportFragmentManager(), "App info");
        } else if (id == R.id.nav_settings) {
            openOptions();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void setUpUI() {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        patternView = findViewById(R.id.knitView);
        stitchCount = findViewById(R.id.stitch_counter);
        rowCount = findViewById(R.id.row_counter);
        completeCount = findViewById(R.id.complete_counter);

        Button incrementRowButton = findViewById(R.id.increment_row_button);
        incrementRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patternView.incrementRow();
            }
        });
        Button undoButton = findViewById(R.id.undo_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patternView.undo();
            }
        });
    }

    void selectInternalPattern() {
        Intent intent = new Intent(this, OpenPattern.class);
        startActivityForResult(intent, OPEN_INTERNAL_PATTERN);
    }

    void openOptions() {
        Intent intent = new Intent(this, AppOptionsActivity.class);
        startActivity(intent);
    }

    void selectExternalFile(String type, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        startActivityForResult(intent, requestCode);
    }

    public void updateStitchCounter() {
        if (knitPattern == null) {
            stitchCount.setText("");
            rowCount.setText("");
            completeCount.setText("");
            return;
        }
        String s = getString(R.string.stitch_counter) + knitPattern.getStitchesDoneInRow();
        stitchCount.setText(s);
        s = getString(R.string.row_counter) + (knitPattern.getCurrentRow() + 1);
        rowCount.setText(s);
        s = getString(R.string.complete_counter) + (100 * knitPattern.getTotalStitchesDone() / knitPattern.getTotalStitches()) + "%";
        completeCount.setText(s);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (knitPattern != null) {
            savePattern();
        }
    }

    private void setKnitPattern(@NonNull KnitPattern knitPattern) {
        setKnitPattern(knitPattern, new KnitPatternDrawer(knitPattern));
    }

    private void setKnitPattern(@NonNull KnitPattern knitPattern, @NonNull KnitPatternDrawer knitPatternDrawer) {
        this.knitPattern = knitPattern;
        patternView.setPattern(knitPatternDrawer);
        updateStitchCounter();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("pattern", knitPattern.getName());
        editor.apply();
    }

    private void clearKnitPattern() {
        this.knitPattern = null;
        patternView.clearPattern();
        updateStitchCounter();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.remove("pattern");
        editor.apply();
    }

    private void importImage() {
        importImageDialog = new ImportImageDialog();
        importImageDialog.show(getSupportFragmentManager(), "Importing image");
    }

    private void openPattern(@NonNull String patternName) {
        new OpenPatternTask(this).execute(patternName);
    }

    private void importJson(Uri uri) {
        new ImportJsonTask(this).execute(uri);
    }

    private void savePattern() {
        new SavePatternChangesTask().execute(knitPattern);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_EXTERNAL_JSON_PATTERN && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                importJson(resultData.getData());
            }
        }
        if (requestCode == READ_EXTERNAL_IMAGE && resultCode == Activity.RESULT_OK) {
            if (resultData != null && resultData.getData() != null) {
                importImageDialog.setUri(resultData.getData());
            }
        }
        if (requestCode == OPEN_INTERNAL_PATTERN && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                openPattern(resultData.getStringExtra("patternName"));
            }
        }
    }

    @Override
    public void onImportImageDialogOK(@NonNull Uri uri, @NonNull String name, int width, int height, int numColours) {
        new ImportImageTask(this, uri, name, width, height, numColours).execute();
    }

    private static class SavePatternChangesTask extends AsyncTask<KnitPattern, Void, Void> {
        @Override
        protected Void doInBackground(KnitPattern... knitPatterns) {
            KnitPattern knitPattern = knitPatterns[0];
            db.knitPatternDao().savePatternChanges(knitPattern);
            return null;
        }
    }

    private static class OpenPatternTask extends AsyncTask<String, String, KnitPattern> {
        private ProgressbarDialog progressbarDialog;
        private KnitPatternDrawer knitPatternDrawer;
        private WeakReference<MainActivity> context;

        OpenPatternTask(MainActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            progressbarDialog = ProgressbarDialog.newInstance(context.get().getString(R.string.progress_dialog_load_title), true, context.get().getString(R.string.progress_bar_loading_pattern));
            progressbarDialog.show(context.get().getSupportFragmentManager(), "Opening");
        }

        @Override
        protected KnitPattern doInBackground(String... strings) {
            KnitPattern knitPattern = db.knitPatternDao().getKnitPattern(strings[0], context.get().getApplicationContext());
            publishProgress(context.get().getString(R.string.progress_bar_creating_bitmap));
            knitPatternDrawer = new KnitPatternDrawer(knitPattern);
            return knitPattern;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressbarDialog.updateText(values[0]);
        }

        @Override
        protected void onPostExecute(KnitPattern knitPattern) {
            super.onPostExecute(knitPattern);
            if (knitPattern != null) {
                context.get().setKnitPattern(knitPattern, knitPatternDrawer);
            }
            progressbarDialog.dismiss();
        }
    }

    private static class ImportJsonTask extends ImportPatternTask<Uri> {
        ImportJsonTask(MainActivity context) {
            super(context);
        }

        @Override
        protected KnitPattern doInBackground(Uri... uris) {
            Uri uri = uris[0];
            KnitPattern knitPattern = null;
            try {
                knitPattern = (KnitPatternParser.createKnitPattern(readTextFile(uri)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (knitPattern != null) {
                saveNewPattern(knitPattern);
            }

            return knitPattern;
        }

        private String readTextFile(Uri uri) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                InputStream inputStream = context.get().getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }
    }

    private static class ImportImageTask extends ImportPatternTask<Void> {
        private Uri imageUri;
        private String patternName;
        private int width;
        private int height;
        private int numColours;
        private Bitmap sourceImg;

        ImportImageTask(MainActivity context, Uri uri, String name, int width, int height, int numColours) {
            super(context);
            this.imageUri = uri;
            this.patternName = name;
            this.width = width;
            this.height = height;
            this.numColours = numColours;
        }

        @Override
        protected KnitPattern doInBackground(Void... voids) {
            sourceImg = readImageFile(imageUri);
            if (sourceImg == null) {
                return null;
            }
            KnitPattern knitPattern = new ImageReader().readImage(sourceImg, patternName, width, height, numColours);
            saveNewPattern(knitPattern);
            return knitPattern;
        }

        private Bitmap readImageFile(@NonNull Uri uri) {
            Bitmap bitmap = null;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inMutable = true;
            try {
                InputStream inputStream = context.get().getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    private static abstract class ImportPatternTask<V> extends AsyncTask<V, String, KnitPattern> {
        ProgressbarDialog progressbarDialog;
        private KnitPatternDrawer knitPatternDrawer;
        WeakReference<MainActivity> context;

        ImportPatternTask(MainActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            progressbarDialog = ProgressbarDialog.newInstance(context.get().getString(R.string.progress_dialog_import_title), true, context.get().getString(R.string.progress_bar_importing_pattern));
            progressbarDialog.show(context.get().getSupportFragmentManager(), "Importing image");
        }

        void saveNewPattern(@NonNull KnitPattern knitPattern) {
            publishProgress(context.get().getString(R.string.progress_bar_creating_bitmap));
            knitPatternDrawer = new KnitPatternDrawer(knitPattern);
            publishProgress(context.get().getString(R.string.progress_bar_saving_pattern));
            db.knitPatternDao().saveNewPattern(knitPattern, ThumbnailUtils.extractThumbnail(knitPatternDrawer.getPatternBitmap(), 200, 200), context.get().getApplicationContext());
        }

        protected void onProgressUpdate(String... values) {
            progressbarDialog.updateText(values[0]);
        }

        @Override
        protected void onPostExecute(KnitPattern pattern) {
            super.onPostExecute(pattern);
            if (pattern != null) {
                context.get().setKnitPattern(pattern, knitPatternDrawer);
            }
            progressbarDialog.dismiss();
        }
    }

    private class MySharedPreferenceListener implements OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pattern") && sharedPreferences.contains("pattern")) {
                setKnitPattern(knitPattern);
            } else if(key.equals("pattern")) {
                clearKnitPattern();
            }
        }
    }
}
