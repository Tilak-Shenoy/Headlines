package com.example.tilak.headlines;


import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity  implements LoaderCallbacks<List<Article>>{

    private static String REQUEST_URL="https://newsapi.org/v2/top-headlines?sources=";
    private static final String API_KEY="apiKey=f249014a7ef8447db1ce2edf2dad7e5b";

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int Article_LOADER_ID = 1;

    private ArticleAdapter adapter;


    TextView mEmptyStateTextView,preference;
    Spinner prefList;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=(ListView) findViewById(R.id.container);
        prefList=(Spinner)findViewById(R.id.prefList);
        preference=(TextView)findViewById(R.id.PreferenceView);
        mEmptyStateTextView=(TextView) findViewById(R.id.empty_view);

        /*
        *The below block of code is to generate the url so that data from the api can be fetched
         */
        ArrayList source=  getIntent().getStringArrayListExtra("sourceString");
        for (Object x: source){
            REQUEST_URL=REQUEST_URL+x+",";
        }
        REQUEST_URL=REQUEST_URL.substring(0,REQUEST_URL.length()-1);
        REQUEST_URL+="&";
        REQUEST_URL=REQUEST_URL+API_KEY;

        //TODO: Give the user to change the preference any time..in the mean time let the app use the spinner as filter


        preference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Editing the string in oreder to make the url valid
                 */
               REQUEST_URL= REQUEST_URL.substring(0,REQUEST_URL.indexOf("sources="));
               REQUEST_URL+="sources=";

                startActivity(new Intent(MainActivity.this,Preference.class));
            }
        });

        /*
        Populates the ListView with the respones fetched via API
         */
        adapter=new ArticleAdapter(this,new ArrayList<Article>());

        listView.setAdapter(adapter);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(Article_LOADER_ID, null, this);


        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);


        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url=adapter.contentUrl;
                Intent i = new Intent(MainActivity.this,Web.class);
                i.putExtra("url",url);
//                i.setData(Uri.parse(url));
                startActivity(i);

            }

        });


    }

    @Override
    public void onBackPressed(){
        /*
                Editing the string in oreder to make the url valid
                 */
        REQUEST_URL= REQUEST_URL.substring(0,REQUEST_URL.indexOf("sources="));
        REQUEST_URL+="sources=";

        startActivity(new Intent(MainActivity.this,Preference.class));
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        return new ArticleLoader(this, REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> Articles) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No Articles found."
        if (Articles==null)
        mEmptyStateTextView.setText(R.string.no_Articles);

        // Clear the adapter of previous Article data
        adapter.clear();

        // If there is a valid list of {@link Article}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (Articles != null && !Articles.isEmpty()) {
            adapter.addAll(Articles);
        }
        else
            mEmptyStateTextView.setText("No Articles found");
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        // Loader reset, so we can clear out our existing data.
        adapter.clear();


    }
}
