package com.salquestfl.bbcreader;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Bundle;
import android.util.Log;
import android.net.Uri;
import android.os.AsyncTask;


interface ICallback {
    void taskComplete(ArrayList<HashMap<String, String>> result);
}
/**
 * Main Activity
 *
 */
public class BBCReaderActivity extends Activity implements ICallback {

    private static final String TAG = "BBCReader";
    static private int POOLSIZE = 10;
    private final ExecutorService pool = Executors.newFixedThreadPool(POOLSIZE);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Get the RSS feed asynchronously
        String url = "http://feeds.bbci.co.uk/news/world/asia/rss.xml";
        new RssReaderTask(this, this).execute(url);
    }

    @Override
    public void onDestroy() {
        pool.shutdownNow();
    }
    
    
    public void taskComplete(final ArrayList<HashMap<String, String>> articles) {
        try {
            if (articles == null) {
                throw new Exception("Could not connect to the server. Please try again after some time.");
            }
            final BaseAdapter adapter = new ArticleAdapter(this, articles, pool);
            final ListView l = (ListView) findViewById(android.R.id.list);
            l.setAdapter(adapter);
            l.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    final HashMap<String, String> article = articles.get(position);
                    String url = article.get("link");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
