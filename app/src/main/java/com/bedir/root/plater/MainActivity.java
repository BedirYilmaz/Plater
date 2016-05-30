package com.bedir.root.plater;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import data.DBPolHelper;
import data.Plate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    List<Plate> plateList;
    DBPolHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        db = new DBPolHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.data_downlad, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    downloadAllPlates();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showPlates();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showPlates() {
        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        /*
        db.createPlate("43 ADSF 23", "Wei Meng Lee");
        db.createPlate("12 QEWR 993", "Bill Phillips and Brian Hardy");
        db.createPlate("06 URNU 324", "Wallace Jackson");
        */

        plateList = db.searchPlate(searchView.getQuery().toString());
        Cursor plateCursor = db.searchPlateWithCursor(searchView.getQuery().toString());
        final ListView l = (ListView) findViewById(R.id.listView1);

        String[] from = new String[]{
                DBPolHelper.KEY_PLATE,
                DBPolHelper.KEY_RECORD
        };

        int[] to = new int[]{
                R.id.list_text_plate,
                R.id.list_text_record
        };

        final SimpleCursorAdapter plateAdapter = new SimpleCursorAdapter(this, R.layout.list_item, plateCursor, from, to);

        l.setAdapter(plateAdapter);
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Runnable run = new Runnable() {
                public void run() {
                    //reload content
                    plateAdapter.changeCursor(db.searchPlateWithCursor(searchView.getQuery().toString()));
                    plateAdapter.notifyDataSetChanged();
                    plateAdapter.notifyDataSetInvalidated();
                    plateList.clear();
                    plateList.addAll(db.searchPlate(searchView.getQuery().toString()));
                    l.invalidateViews();
                    l.refreshDrawableState();
                    Log.d("UI", "PLATE ADAPTER RENEWED");
                }
            };

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                AlertDialog.Builder dialogBuilder =
                        new AlertDialog.Builder(MainActivity.this);
                final Plate selected = plateList.get(position);
                dialogBuilder.setMessage(selected.getPlate() + " " + selected.getRecord())
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getString(R.string.erase_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Deletion code
                        db.deletePlate(selected);
                        runOnUiThread(run);
                        dialog.dismiss();
                    }
                }).setNeutralButton(getString(R.string.update_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Update page code

                    }
                });
                dialogBuilder.create().show();

            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        showPlates();
        return false;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    public class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {

                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();

                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String xmlBoxedJson = buffer.toString();
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(new StringBufferInputStream(xmlBoxedJson));

                //optional, but recommended
                //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();
                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                NodeList nList = doc.getElementsByTagName("string");
                System.out.println("----------------------------");
                Node nNode = nList.item(0);

                System.out.println("\nCurrent Element :" + nNode.getNodeName());
                System.out.println("\nElement Value:" + nNode.getTextContent());

                String json = nNode.getTextContent();
                JSONArray jsonArray = new JSONArray(json);

                int i = 0;
                for (; i < jsonArray.length(); i++) {
                    JSONObject childObject = jsonArray.getJSONObject(i);
                    db.createPlate(childObject.getString("PlateCode"), childObject.getString("Record"));
                }

                System.out.print("Total number of Plates added is " + i);
                return nNode.getTextContent();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            Toast.makeText(getApplicationContext(), res, Toast.LENGTH_LONG).show();
        }
    }

    public List<Plate> downloadAllPlates() throws JSONException {
        JsonTask j = new JsonTask();
        j.execute("http://bediryilmaz.com/PlateService.asmx/GetEmployessJSON");
        return null;
    }

}
