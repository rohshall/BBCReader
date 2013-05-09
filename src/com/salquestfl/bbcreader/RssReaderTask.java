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

public class RssReaderTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

    private static final String TAG = "BBCReader";

    private Context context;
    private ICallback callback;

    public RssReaderTask(Context context, ICallback callback) {
        this.context = context;
        this.callback = callback;
    }

    // This executes in non-UI thread. No UI calls from here (including Toast)
    @Override
    protected ArrayList<HashMap<String, String>> doInBackground(String... urls) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urls[0]);
            conn = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            return RssReader.read(in);
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
    protected void onPostExecute(ArrayList<HashMap<String, String>> data) {
        callback.taskComplete(data);
    }
}
 

