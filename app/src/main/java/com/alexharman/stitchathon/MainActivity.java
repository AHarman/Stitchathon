package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ImportImageDialog.ImportImageDialogListener{

    private TextView stitchCount;
    private TextView rowCount;
    private TextView completeCount;
    private KnitPattern knitPattern;
    private KnitPatternView patternView;
    private ImportImageDialog importImageDialog;
    private AppDatabase db;

    static final int READ_EXTERNAL_IMAGE = 42;
    static final int READ_EXTERNAL_JSON_PATTERN = 55;
    static final int OPEN_INTERNAL_PATTERN = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpUI();

        db = AppDatabase.Companion.getAppDatabase(getApplicationContext());

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String patternName = sharedPreferences.getString("pattern", null);
        if (patternName != null) {
            openPattern(patternName);
        }

//        String imageFP = sharedPreferences.getString("image", null);

//        if (knitPatternFP != null) {
//            openPattern(Uri.fromFile(new File(knitPatternFP)), Uri.fromFile(new File(imageFP)));
//        }
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
        }

        item.setChecked(false);
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

    void selectExternalFile(String type, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        startActivityForResult(intent, requestCode);
    }

    public void updateStitchCounter() {
        String s = getString(R.string.stitch_counter) + knitPattern.getStitchesDoneInRow();
        stitchCount.setText(s);
        s = getString(R.string.row_counter) + (knitPattern.getCurrentRow() + 1);
        rowCount.setText(s);
        s = getString(R.string.complete_counter) + (100 * knitPattern.getTotalStitchesDone() / knitPattern.getTotalStitches()) + "%";
        completeCount.setText(s);
    }

    private String readTextFile(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
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

    private Bitmap readImageFile(Uri uri) {
        Bitmap bitmap = null;
        if (uri != null) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inMutable = true;
                InputStream inputStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Lifecycle", "in onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LifeCycle", "in onPause");
        if (knitPattern != null) {
            savePattern();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Lifecycle", "in onStop");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Lifecycle", "in onSaveInstanceState");
    }

    private void setKnitPattern(KnitPattern knitPattern) {
        setKnitPattern(knitPattern, null);
    }

    private void setKnitPattern(@NonNull KnitPattern knitPattern, @Nullable Bitmap image) {
        this.knitPattern = knitPattern;
        patternView.setPattern(knitPattern, image);
        updateStitchCounter();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("pattern", knitPattern.getName());
//        editor.putString("image", getFilesDir() + "/" + knitPattern.getName() + ".png");
        editor.apply();
    }

    private void importImage() {
        importImageDialog = new ImportImageDialog();
        importImageDialog.show(getSupportFragmentManager(), "Importing image");
    }

    private void openPattern(String patternName) {
        new OpenPatternTask().execute(patternName);
    }

    private void importFile(Uri uri) {
        new ImportPatternTask().execute(uri);
    }

    private void saveNewPattern() {
        new SavePatternTask(true).execute(knitPattern);
    }

    private void savePattern() {
        new SavePatternTask(false).execute(knitPattern);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Log.d("Lifecycle", "In onActivityResult");
        if (requestCode == READ_EXTERNAL_JSON_PATTERN && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                importFile(resultData.getData());
            }
        }
        if (requestCode == READ_EXTERNAL_IMAGE && resultCode == Activity.RESULT_OK) {
            if (resultData != null && resultData.getData() != null) {
                importImageDialog.setUri(resultData.getData());
            }
        }
        if (requestCode == OPEN_INTERNAL_PATTERN && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                openPattern(resultData.getStringExtra("pattern"));
            }
        }
    }

    @Override
    public void onImportImageDialogOK(@NonNull Uri uri, @NonNull String name, int width, int height, int numColours) {
        Log.d("onImportImageDialogOK", uri.getPath());
        Log.d("onImportImageDialogOK", name);
        Log.d("onImportImageDialogOK", ""+width);
        Log.d("onImportImageDialogOK", ""+height);
        Log.d("onImportImageDialogOK", ""+numColours);
        new ImportImageTask(uri, name, width, height, numColours).execute();
    }

    private class SavePatternTask extends AsyncTask<KnitPattern, Void, Void> {
        boolean storeStitches;

        SavePatternTask(boolean storeStitches) {
            this.storeStitches = storeStitches;
        }

        @Override
        protected Void doInBackground(KnitPattern... knitPatterns) {
            Log.d("SavePatternTask", "In doInBackground");
            KnitPattern knitPattern = knitPatterns[0];

            if(storeStitches) {
                db.knitPatternDao().saveNewPattern(knitPattern);
            } else {
                db.knitPatternDao().savePattern(knitPattern);
            }

//            Gson gson = new Gson();
//            FileOutputStream outputStream;
//            String gsonOutput = gson.toJson(knitPattern);
//            Bitmap bm = patternView.patternBitmap;
//            try {
//                outputStream = openFileOutput(knitPattern.getName() + ".png", Context.MODE_PRIVATE);
//                bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//                outputStream.close();
//                Log.d("SavePatternTask", "Finished saving image");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return null;
        }
    }

    private class OpenPatternTask extends AsyncTask<String, Void, KnitPattern> {
        ProgressbarDialog progressbarDialog;

        @Override
        protected void onPreExecute() {
            Log.d("OpenPatternTask", "in onPreExecute");
            progressbarDialog = ProgressbarDialog.newInstance(getString(R.string.progress_dialog_load_title), true, getString(R.string.progress_bar_loading_pattern));
            progressbarDialog.show(getSupportFragmentManager(), "Opening");
        }

        @Override
        protected KnitPattern doInBackground(String... strings) {
            Log.d("OpenPatternTask", "in doInBackground()");
            Log.d("OpenPatternTask", "patternName = " + strings[0]);

            return db.knitPatternDao().getKnitPattern(strings[0]);
        }

        @Override
        protected void onPostExecute(KnitPattern knitPattern) {
            super.onPostExecute(knitPattern);
            Log.d("OpenPatternTask", "In onPostExecute");
            if (knitPattern != null) {
                setKnitPattern(knitPattern);
            } else {
                Log.d("OpenPatternTask", "Pattern did not load from database");
            }
            progressbarDialog.dismiss();
        }
    }

    private class ImportPatternTask extends AsyncTask<Uri, String, KnitPattern> {
        private ProgressbarDialog progressbarDialog;
        private Bitmap patternBitmap;

        @Override
        protected void onPreExecute() {
            Log.d("ImportPatternTask", "In onPreExecute");
            Log.d("ImportPatternTask", "Thread: " + Thread.currentThread().getName());
            progressbarDialog = ProgressbarDialog.newInstance(getString(R.string.progress_dialog_import_title), true, getString(R.string.progress_bar_importing_pattern));
            progressbarDialog.show(getSupportFragmentManager(), "Importing pattern");
        }

        @Override
        protected KnitPattern doInBackground(Uri... uris) {
            Uri uri = uris[0];
            Log.d("ImportPatternTask", "In doInBackground");
            Log.d("ImportPatternTask", "Thread: " + Thread.currentThread().getName());
            KnitPattern knitPattern = null;
            try {
                knitPattern = (KnitPatternParser.createKnitPattern(readTextFile(uri)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (knitPattern != null) {
                publishProgress(getString(R.string.progress_bar_creating_bitmap));
                patternBitmap = patternView.createPatternBitmap(knitPattern);
            }

            return knitPattern;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d("ImportPatternTask", "In onProgressUpdate");
            Log.d("ImportPatternTask", "Thread: " + Thread.currentThread().getName());
            progressbarDialog.updateText(values[0]);
        }

        @Override
        protected void onPostExecute(KnitPattern knitPattern) {
            super.onPostExecute(knitPattern);
            Log.d("ImportPatternTask", "In onPostExecute");
            Log.d("ImportPatternTask", "Thread: " + Thread.currentThread().getName());
            if (knitPattern != null) {
                Log.d("ImportPatternTask", "Imported, going to save and set");
                setKnitPattern(knitPattern, patternBitmap);
                saveNewPattern();
            }
            progressbarDialog.dismiss();
        }
    }

    private class ImportImageTask extends AsyncTask<Void, String, KnitPattern> {
        private Uri imageUri;
        private String patternName;
        private int width;
        private int height;
        private int numColours;
        Bitmap bitmap;
        Bitmap patternBitmap;

        ProgressbarDialog progressbarDialog;

        ImportImageTask(Uri uri, String name, int width, int height, int numColours) {
            this.imageUri = uri;
            this.patternName = name;
            this.width = width;
            this.height = height;
            this.numColours = numColours;
        }

        @Override
        protected void onPreExecute() {
            Log.d("ImportImageTask", "In onPreExecute");
            progressbarDialog = ProgressbarDialog.newInstance(getString(R.string.progress_dialog_import_title), true, getString(R.string.progress_bar_processing_bitmap));
            progressbarDialog.show(getSupportFragmentManager(), "Importing image");
        }

        @Override
        protected KnitPattern doInBackground(Void... voids) {
            bitmap = readImageFile(imageUri);
            if (bitmap == null) {
                return null;
            }

            ImageReader imageReader = new ImageReader();
            KnitPattern pattern = imageReader.readImage(bitmap, patternName, width, height, numColours);
            if (knitPattern != null) {
                publishProgress(getString(R.string.progress_bar_creating_bitmap));
                patternBitmap = patternView.createPatternBitmap(knitPattern);
            }
            return pattern;
        }

        protected void onProgressUpdate(String... values) {
            progressbarDialog.updateText(values[0]);
        }

        @Override
        protected void onPostExecute(KnitPattern pattern) {
            super.onPostExecute(pattern);
            if (pattern != null) {
                Log.d("ImportPatternTask", "Imported, going to save and set");
                setKnitPattern(pattern, patternBitmap);
                saveNewPattern();
            }
            progressbarDialog.dismiss();
        }
    }
}
