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
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
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



public class ArticleAdapter extends BaseAdapter { 

    private static class ViewHolder {
        TextView title_text;
        TextView description_text;
        ImageView thumbnail_image;
    }

    private final Context context;
    private final ArrayList<HashMap<String, String>> articles;
    private final HashMap<String, Future<Bitmap>> bitmapFutures;
    private final ExecutorService pool;

    public ArticleAdapter(final Context context, ArrayList<HashMap<String, String>> articles, ExecutorService pool) {
        this.context = context;
        this.articles = articles;
        this.pool = pool;
        bitmapFutures = new HashMap<String, Future<Bitmap>>();
        for (final HashMap<String, String> article : articles) {
            final String thumbnail_uri = article.get("thumbnail");
            if (thumbnail_uri != null) {
                 Callable callable = new Callable() {
                     public Bitmap call() {
                         try {
                             return BitmapFactory.decodeStream(new URL(thumbnail_uri).openConnection().getInputStream());
                         } catch (Exception e) {
                             // Just absorb the image download error
                             return null;
                         }
                     }
                 };
                 Future<Bitmap> future = pool.submit(callable);
                 bitmapFutures.put(thumbnail_uri, future);
            }
        }
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
        Bitmap thumbnail = null;
        if (thumbnail_uri != null) {
            Future<Bitmap> future = bitmapFutures.get(thumbnail_uri);
            try {
                thumbnail = future.get();
            } catch (Exception e) {
            }
        }
        holder.thumbnail_image.setImageBitmap(thumbnail);
        return convertView;
    }
}
