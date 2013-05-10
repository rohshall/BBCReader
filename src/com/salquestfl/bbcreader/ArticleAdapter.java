package com.salquestfl.bbcreader;

import java.lang.ref.WeakReference;
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
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.util.Log;
import android.net.Uri;
import android.os.AsyncTask;


class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;

    public BitmapWorkerTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Download image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        String thumbnailUri = params[0];
        try {
            return BitmapFactory.decodeStream(new URL(thumbnailUri).openConnection().getInputStream());
        } catch (Exception e) {
            return null;
        }
    }
    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}

public class ArticleAdapter extends BaseAdapter { 

    private static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
        ImageView thumbnailImage;
    }

    private final Context context;
    private final ArrayList<HashMap<String, String>> articles;
    private final LayoutInflater inflater;

    public ArticleAdapter(Context context, ArrayList<HashMap<String, String>> articles) {
        this.context = context;
        this.articles = articles;
        inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
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
        return position;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.article, null);
            holder = new ViewHolder();
            holder.titleText = (TextView) convertView.findViewById(R.id.title);
            holder.descriptionText = (TextView) convertView.findViewById(R.id.description);
            holder.thumbnailImage = (ImageView) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        final HashMap<String, String> article = (HashMap<String, String>) getItem(position);
        String title = article.get("title");
        holder.titleText.setText(title);
        String description = article.get("description");
        holder.descriptionText.setText(description);
        String thumbnailUri = article.get("thumbnail");
        if (thumbnailUri != null) {
            new BitmapWorkerTask(holder.thumbnailImage).execute(thumbnailUri);
        } else {
            holder.thumbnailImage.setImageBitmap(null);
        }
        return convertView;
    }
}
