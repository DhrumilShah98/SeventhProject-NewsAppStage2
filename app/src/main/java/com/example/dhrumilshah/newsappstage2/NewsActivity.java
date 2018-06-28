package com.example.dhrumilshah.newsappstage2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>{

    private static final int NEWS_LOADER_ID = 1;
    private static final String GUARDIANS_REQUEST_URL = "http://content.guardianapis.com/search";
    private static final String GUARDIANS_HOME_PAGE = "https://www.theguardian.com/international";
    private NewsArrayAdapter newsAdapter;
    private ListView newsListView;
    private TextView emptyStateTextView;
    private View loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        emptyStateTextView = findViewById(R.id.empty_state);
        loadingIndicator = findViewById(R.id.loading_indicator);
        newsListView = findViewById(R.id.list);

        newsAdapter = new NewsArrayAdapter(this, new ArrayList<News>());

        newsListView.setAdapter(newsAdapter);

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (cm != null) {
            networkInfo = cm.getActiveNetworkInfo();
        }
        if(networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        }else{
            loadingIndicator.setVisibility(View.GONE);
            emptyStateTextView.setText(getString(R.string.no_internet_connection));
            emptyStateTextView.setVisibility(View.VISIBLE);
        }

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                News currentNews = newsAdapter.getItem(position);
                if (currentNews != null) {
                    String webUrl = currentNews.getWebUrl();
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    if(webUrl != null){
                        webIntent.setData(Uri.parse(webUrl));
                    }else{
                        webIntent.setData(Uri.parse(GUARDIANS_HOME_PAGE));
                    }
                    startActivity(webIntent);
                }
            }
        });
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
        String entries = sharedPrefs.getString(
                getString(R.string.settings_display_entries_key),
                getString(R.string.settings_display_entries_default));

        int enteriesCheck ;
        try {
            enteriesCheck = Integer.parseInt(entries);
            if(enteriesCheck < 5 || enteriesCheck >50){
                entries = getString(R.string.settings_display_entries_default);
            }
        }catch (NumberFormatException e){
            entries = getString(R.string.settings_display_entries_default);
        }

        String filterBy  = sharedPrefs.getString(
                getString(R.string.settings_filter_key),
                getString(R.string.settings_filter_default)
        );

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(GUARDIANS_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter(getString(R.string.parameter_section), filterBy);
        uriBuilder.appendQueryParameter(getString(R.string.parameter_showtags), getString(R.string.value_showtags));
        uriBuilder.appendQueryParameter(getString(R.string.parameter_format), getString(R.string.value_format));
        uriBuilder.appendQueryParameter(getString(R.string.parameter_lang), getString(R.string.value_lang));
        uriBuilder.appendQueryParameter(getString(R.string.parameter_orderby), getString(R.string.value_orderby));
        uriBuilder.appendQueryParameter(getString(R.string.parameter_showfields), getString(R.string.value_showfields));
        uriBuilder.appendQueryParameter(getString(R.string.parameter_pagesize), entries);
        uriBuilder.appendQueryParameter(getString(R.string.parameter_apikey), getString(R.string.value_apikey));
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {

        loadingIndicator.setVisibility(View.GONE);
        emptyStateTextView.setText(getString(R.string.no_news_found));
        newsListView.setEmptyView(emptyStateTextView);
        newsAdapter.clear();
        if(news != null && !news.isEmpty()){
            newsAdapter.addAll(news);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.clear();
    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
