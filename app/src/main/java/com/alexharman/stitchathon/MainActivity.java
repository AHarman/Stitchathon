package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Context;
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
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView stitchCount;
    private TextView rowCount;
    private TextView completeCount;
    private KnitPattern knitPattern;
    private KnitPatternView patternView;

    private static final int READ_EXTERNAL_IMAGE = 42;
    private static final int READ_EXTERNAL_JSON_PATTERN = 55;
    private static final int OPEN_INTERNAL_PATTERN = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        patternView = (KnitPatternView) findViewById(R.id.knitView);
        stitchCount = (TextView) findViewById(R.id.stitch_counter);
        rowCount = (TextView) findViewById(R.id.row_counter);
        completeCount = (TextView) findViewById(R.id.complete_counter);

        Button incrementRowButton = (Button) findViewById(R.id.increment_row_button);
        incrementRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patternView.incrementRow();
            }
        });
        Button undoButton = (Button) findViewById(R.id.undo_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patternView.undo();
            }
        });

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String knitPatternFP = sharedPreferences.getString("pattern", null);
        String imageFP = sharedPreferences.getString("image", null);
        if (knitPatternFP != null) {
            openPattern(Uri.fromFile(new File(knitPatternFP)), Uri.fromFile(new File(imageFP)));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            Intent intent = new Intent(this, OpenPattern.class);
            startActivityForResult(intent, OPEN_INTERNAL_PATTERN);
        } else if (id == R.id.nav_import_pattern) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            startActivityForResult(intent, READ_EXTERNAL_JSON_PATTERN);
        } else if (id == R.id.nav_import_image) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, READ_EXTERNAL_IMAGE);
        }

        item.setChecked(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            savePatternToFile();
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
        editor.putString("pattern", getFilesDir() + "/" + knitPattern.name + ".json");
        editor.putString("image", getFilesDir() + "/" + knitPattern.name + ".png");
        editor.apply();
    }

    private void importImage(Uri imageUri) {
        new ImportImageTask().execute(imageUri);
    }

    private void openPattern(Uri patternUri, @Nullable Uri imageUri) {
        new OpenPatternTask().execute(patternUri, imageUri);
    }

    private void importFile(Uri uri) {
        new ImportPatternTask().execute(uri);
    }

    private void savePatternToFile() {
        new SavePatternTask().execute(knitPattern);
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
            if (resultData != null) {
                importImage(resultData.getData());
            }
        }
        if (requestCode == OPEN_INTERNAL_PATTERN && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                openPattern((Uri) resultData.getParcelableExtra("pattern"), (Uri) resultData.getParcelableExtra("image"));
            }
        }
    }

    private class SavePatternTask extends AsyncTask<KnitPattern, Void, Void> {
        @Override
        protected Void doInBackground(KnitPattern... knitPatterns) {
            Log.d("SavePatternTask", "In doInBackground");
            KnitPattern knitPattern = knitPatterns[0];
            Gson gson = new Gson();
            FileOutputStream outputStream;
            String gsonOutput = gson.toJson(knitPattern);
            Bitmap bm = patternView.patternBitmap;

            try {
                outputStream = openFileOutput(knitPattern.name + ".json", Context.MODE_PRIVATE);
                Log.d("SavePatternTask", "GSON gave us: " + gsonOutput);
                Log.d("SavePatternTask", "Length: " + gsonOutput.length());
                outputStream.write(gsonOutput.getBytes());
                outputStream.close();
                Log.d("SavePatternTask", "Finished saving pattern");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                outputStream = openFileOutput(knitPattern.name + ".png", Context.MODE_PRIVATE);
                bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
                Log.d("SavePatternTask", "Finished saving image");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class OpenPatternTask extends AsyncTask<Uri, Void, KnitPattern> {
        Bitmap patternBitmap = null;
        ProgressbarDialog progressbarDialog;

        @Override
        protected void onPreExecute() {
            Log.d("OpenPatternTask", "in onPreExecute");
            progressbarDialog = ProgressbarDialog.newInstance(getString(R.string.progress_dialog_load_title), true, getString(R.string.progress_bar_loading_pattern));
            progressbarDialog.show(getSupportFragmentManager(), "Opening");
        }

        @Override
        protected KnitPattern doInBackground(Uri... uris) {
            Uri patternUri = uris[0];
            Uri imageUri = uris[1];
            Gson gson = new Gson();
            Log.d("OpenPatternTask", "in doInBackground()");
            Log.d("OpenPatternTask", "patternFP = " + patternUri.getPath());

            patternBitmap = readImageFile(imageUri);

            String knitPatternGSON = readTextFile(patternUri);
            if (knitPatternGSON.length() == 0) {
                Log.d("OpenPatternTask", "Didn't load in string properly");
            } else {
                KnitPattern pattern = gson.fromJson(knitPatternGSON, KnitPattern.class);
                Log.d("OpenPatternTask", "GSON json is: " + knitPatternGSON);
                return pattern;
            }
            return null;
        }

        @Override
        protected void onPostExecute(KnitPattern knitPattern) {
            super.onPostExecute(knitPattern);
            Log.d("OpenPatternTask", "In onPostExecute");
            if (knitPattern != null) {
                setKnitPattern(knitPattern, patternBitmap);
            } else {
                Log.d("OpenPatternTask", "Pattern did not load from json");
            }
            progressbarDialog.dismiss();
        }
    }

    private class ImportPatternTask extends AsyncTask<Uri, String, KnitPattern> {
        ProgressbarDialog progressbarDialog;
        Bitmap patternBitmap;

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
                savePatternToFile();
            }
            progressbarDialog.dismiss();
        }
    }

    private class ImportImageTask extends AsyncTask<Uri, Void, String> {
        ProgressbarDialog progressbarDialog;
        @Override
        protected void onPreExecute() {
            Log.d("ImportImageTask", "In onPreExecute");
            progressbarDialog = ProgressbarDialog.newInstance(getString(R.string.progress_dialog_import_title), true, getString(R.string.progress_bar_processing_bitmap));
            progressbarDialog.show(getSupportFragmentManager(), "Importing image");
        }

        @Override
        protected String doInBackground(Uri... uris) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressbarDialog.dismiss();
        }
    }
}
