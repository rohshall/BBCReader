package com.salquestfl.bbcreader;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Bundle;
import android.util.Log;
import android.net.Uri;
import android.os.AsyncTask;


class RssReaderTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

    private static final String TAG = "BBCReader";

    private Activity activity;
    private ProgressDialog progressDialog;

    public RssReaderTask(Activity activity) {
        this.activity = activity;
    }

   @Override
    protected void onPreExecute() {
      super.onPreExecute();
      progressDialog = new ProgressDialog(activity);
      progressDialog.setCancelable(false);
      progressDialog.setMessage("Downloading articles, please wait...");
      progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      progressDialog.setProgress(0);
      progressDialog.show();
    }

    // This executes in non-UI thread. No UI calls from here (including Toast)
    @Override
    protected ArrayList<HashMap<String, String>> doInBackground(String... urls) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urls[0]);
            conn = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            return new RssReader().read(in);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return null;
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // This executes in UI thread
    @Override
    protected void onPostExecute(final ArrayList<HashMap<String, String>> articles) {
        super.onPostExecute(articles);
        progressDialog.dismiss();
        if (articles == null) {
            String msg = "Could not connect to the server. Please try again after some time.";
            Log.w(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        } else {
            final BaseAdapter adapter = new ArticleAdapter(activity, articles);
            final ListView l = (ListView) activity.findViewById(android.R.id.list);
            l.setAdapter(adapter);
            l.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    final HashMap<String, String> article = articles.get(position);
                    String url = article.get("link");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    activity.startActivity(intent);
                }
            });
        }
    }
}
 

/**
 * Main Activity
 *
 */
public class BBCReaderActivity extends Activity {

    private static final String TAG = "BBCReader";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Get the RSS feed asynchronously
        String url = "http://feeds.bbci.co.uk/news/world/asia/rss.xml";
        new RssReaderTask(this).execute(url);
    }
}
