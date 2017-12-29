package com.alexharman.stitchathon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

    private static final int READ_EXTERNAL_FILE = 55;
    private static final int READ_INTERNAL_FILE = 1234;

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
            startActivityForResult(intent,  READ_INTERNAL_FILE);
        } else if (id == R.id.nav_import) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            startActivityForResult(intent, READ_EXTERNAL_FILE);
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
        Log.d("Pers", "in readTextFile()");
        InputStream inputStream;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            inputStream = getContentResolver().openInputStream(uri);
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
        Log.d("Pers", "Reading text got us this: " + stringBuilder.toString());
        Log.d("Pers", "Length: " + stringBuilder.toString().length());
        return stringBuilder.toString();
    }

    private void importFile(Uri uri) {
        Log.d("Pers", "In importFile");
        KnitPattern knitPattern = null;
        try {
            knitPattern = (KnitPatternParser.createKnitPattern(readTextFile(uri)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (knitPattern != null) {
            Log.d("Pers", "Imported, going to save and set");
            setKnitPattern(knitPattern);
            savePatternToFile();
            savePatternImage();
        }
    }

    private void savePatternImage() {
        Log.d("Pers", "In savePatternImage");
        Bitmap bm = patternView.patternBitmap;
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(knitPattern.name + ".png", Context.MODE_PRIVATE);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Log.d("Pers", "Finished saving image");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePatternToFile() {
        Log.d("Pers", "In savePatternToFile");
        Gson gson = new Gson();
        FileOutputStream outputStream;
        String gsonOutput = gson.toJson(knitPattern);

        try {
            outputStream = openFileOutput(knitPattern.name + ".json", Context.MODE_PRIVATE);
            Log.d("Pers", "GSON gave us: " + gsonOutput);
            Log.d("Pers", "Length: " + gsonOutput.length());
            outputStream.write(gsonOutput.getBytes());
            outputStream.close();
            Log.d("Pers", "Finished saving pattern");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Pers", "in onStart");

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String knitPatternFP = sharedPreferences.getString("pattern", null);
        String imageFP = sharedPreferences.getString("image", null);
        if (knitPatternFP != null) {
            Log.d("Pers", "pattern filepath: " + knitPatternFP);
            openPattern(Uri.fromFile(new File(knitPatternFP)), Uri.fromFile(new File(imageFP)));
        } else {
            Log.d("Pers", "knitPatternFP is null");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (knitPattern != null) {
            savePatternToFile();
            savePatternImage();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Foo", "in onStop");
    }

    private void setKnitPattern(KnitPattern knitPattern) {
        setKnitPattern(knitPattern, null);
    }

    private void setKnitPattern(KnitPattern knitPattern, @Nullable Bitmap image) {
        Log.d("Pers", "In setKnitPattern()");
        Log.d("Pers", "Pattern null: " + (knitPattern==null));
        Log.d("Pers", "Image null: " + (image==null));
        this.knitPattern = knitPattern;
        patternView.setPattern(knitPattern, image);
        updateStitchCounter();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("pattern", getFilesDir() + "/" + knitPattern.name + ".json");
        editor.putString("image", getFilesDir() + "/" + knitPattern.name + ".png");
        editor.apply();
    }

    private void openPattern(Uri patternUri, @Nullable Uri imageUri) {
        Log.d("Pers", "in openPattern()");
        Log.d("Pers", "patternFP = " + patternUri.getPath());

        Gson gson = new Gson();
        Bitmap patternBitmap = null;

        if (imageUri != null) {
            Log.d("Pers", "Loading image from file");
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inMutable = true;
            patternBitmap = BitmapFactory.decodeFile(imageUri.getPath(), opts);
        }

        String knitPatternGSON = readTextFile(patternUri);
        if (knitPatternGSON == null || knitPatternGSON.length() == 0) {
            Log.d("Pers", "Didn't load in string properly");
        } else {
            KnitPattern pattern = gson.fromJson(knitPatternGSON, KnitPattern.class);
            Log.d("Pers", "GSON json is: " + knitPatternGSON);
            if (pattern != null) {
                setKnitPattern(pattern, patternBitmap);
            } else {
                Log.d("Pers", "Pattern did not load from json");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_EXTERNAL_FILE && resultCode == Activity.RESULT_OK) {
            Log.d("Pers", "Returned from the import file bit");
            if (resultData != null) {
                importFile(resultData.getData());
            }
        }
        if (requestCode == READ_INTERNAL_FILE && resultCode == Activity.RESULT_OK) {
            Log.d("Pers", "Returned from the open pattern bit");
            if (resultData != null) {
                openPattern((Uri) resultData.getParcelableExtra("pattern"), (Uri) resultData.getParcelableExtra("image"));
            }
        }
    }
}
