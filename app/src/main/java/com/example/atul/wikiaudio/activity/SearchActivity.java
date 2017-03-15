package com.example.atul.wikiaudio.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.atul.wikiaudio.R;
import com.example.atul.wikiaudio.rest.MediaWikiClient;
import com.example.atul.wikiaudio.rest.ServiceGenerator;
import com.example.atul.wikiaudio.ui.EndlessScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private ListView resultList;

    private ArrayList<String> wiktionaryTitleArrayList;
    private ArrayAdapter<String> resultListAdapter;
    private EndlessScrollListener endlessScrollListener;

    private String queryString;
    private Integer nextOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchView searchBar = (SearchView) findViewById(R.id.search_bar);
        searchBar.requestFocus();
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                queryString = s;
                nextOffset = 0;
                wiktionaryTitleArrayList.clear();
                resultListAdapter.notifyDataSetChanged();
                progressDialog = ProgressDialog.show(SearchActivity.this, "Search", "Fetching results...", true);
                search(queryString);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        wiktionaryTitleArrayList = new ArrayList<>();
        resultListAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, wiktionaryTitleArrayList
        );

        endlessScrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                if (nextOffset != null) {
                    search(queryString);
                    // or loadNextDataFromApi(totalItemsCount);
                    return true; // ONLY if more data is actually being loaded; false otherwise.
                } else {
                    return false;
                }
            }
        };

        resultList = (ListView) findViewById(R.id.search_result_list);
        resultList.setAdapter(resultListAdapter);
        resultList.setOnScrollListener(endlessScrollListener);
        resultList.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        //  Write to shared preferences
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.pref_file_key),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.pref_is_logged_in), false);
        editor.apply();

        ServiceGenerator.clearCookies();

        Intent intent = new Intent(getApplicationContext(),
                LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void search(String query) {
        MediaWikiClient mediaWikiClient = ServiceGenerator.createService(MediaWikiClient.class,
                getApplicationContext());
        Call<ResponseBody> call = mediaWikiClient.search("query", "search", query,
                nextOffset, true);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        try {
                            processSearchResult(responseStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            searchFailed("Server misbehaved! Please try again later.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        searchFailed("Please check your connection!");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                searchFailed("Please check your connection!");
            }
        });
    }

    private void searchFailed(String msg) {
        if (progressDialog != null)
            progressDialog.dismiss();
        endlessScrollListener.setLoading(false);
        Toast.makeText(this, "Search failed!\n" + msg, Toast.LENGTH_LONG).show();
    }

    private void processSearchResult(String responseStr) throws JSONException {
        JSONObject reader = new JSONObject(responseStr);

        JSONArray searchResults = reader.getJSONObject("query").optJSONArray("search");
        for (int ii = 0; ii < searchResults.length(); ii++) {
            wiktionaryTitleArrayList.add(
                    searchResults.getJSONObject(ii).getString("title")
            );
        }
        if (reader.has("continue"))
            nextOffset = reader.getJSONObject("continue").getInt("sroffset");
        else
            nextOffset = null;

        resultListAdapter.notifyDataSetChanged();
        endlessScrollListener.setLoading(false);

        if (progressDialog != null)
            progressDialog.dismiss();

        ImageView wiktionaryLogo = (ImageView) findViewById(R.id.wiktionary_logo);
        wiktionaryLogo.setVisibility(View.GONE);
        resultList.setVisibility(View.VISIBLE);
    }
}

