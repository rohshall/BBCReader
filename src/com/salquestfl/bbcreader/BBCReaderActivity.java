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



class ArticleAdapter extends BaseAdapter { 

    private static class ViewHolder {
        TextView title_text;
        TextView description_text;
        ImageView thumbnail_image;
    }

    private Context context;
    private ArrayList<HashMap<String, String>> articles;
    private final ImageDownloader imageDownloader = new ImageDownloader();

    public ArticleAdapter(final Context context, ArrayList<HashMap<String, String>> articles) {
        this.context = context;
        this.articles = articles;
    }

    @Override
    public int getCount() {
        return articles.size();
    }

    @Override
    public Object getItem(int position) {
        return articles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return articles.indexOf(getItem(position));
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater)
                        context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.article, null);
            holder = new ViewHolder();
            holder.title_text = (TextView) convertView.findViewById(R.id.title);
            holder.description_text = (TextView) convertView.findViewById(R.id.description);
            holder.thumbnail_image = (ImageView) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        final HashMap<String, String> article = (HashMap<String, String>) getItem(position);
        String title = article.get("title");
        holder.title_text.setText(title);
        String description = article.get("description");
        holder.description_text.setText(description);
        String thumbnail_uri = article.get("thumbnail");
        if (thumbnail_uri != null) {
            imageDownloader.download(thumbnail_uri, holder.thumbnail_image);
        } else {
            holder.thumbnail_image.setImageBitmap(null);
        }
        return convertView;
    }
}



/**
 * Main Activity
 *
 */
public class BBCReaderActivity extends Activity {

    private static final String TAG = "BBCReader";

    private class RssFeedTask extends AsyncTask<String, Void, String> {

        private Context context;

        public RssFeedTask(Context context) {
            this.context = context;
        }

        private String readData(BufferedReader in) throws IOException {
            StringBuilder chars = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                chars.append(line);
            }
            return chars.toString();
        }

        // This executes in non-UI thread. No UI calls from here (including Toast)
        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urls[0]);
                conn = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String data = readData(in);
                return data;
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
        protected void onPostExecute(String data) {
            try {
                if (data == null) {
                    throw new Exception("Could not connect to the server. Please try again after some time.");
                }
                final ArrayList<HashMap<String, String>> articles = RssReader.read(new StringReader(data));
                final BaseAdapter adapter = new ArticleAdapter(context, articles);
                final ListView l = (ListView) findViewById(android.R.id.list);
                l.setAdapter(adapter);
                l.setOnItemClickListener( new OnItemClickListener() {
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
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
     }
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Get the RSS feed asynchronously
        String url = "http://feeds.bbci.co.uk/news/world/asia/rss.xml";
        new RssFeedTask(this).execute(url);
    }
}
