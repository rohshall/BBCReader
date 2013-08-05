package com.salquestfl.bbcreader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.widget.Toast;
import android.util.Log;


public class RssReader {
    private static final String TAG = "BBCReader";
    private static final String DESCRIPTION = "description";
    private static final String LINK = "link";
    private static final String TITLE = "title";
    private static final String ITEM = "item";
    private static final String THUMBNAIL = "thumbnail";
    private static final String CHANNEL = "channel";

    public ArrayList<HashMap<String, String>> read(Reader ir) throws XmlPullParserException, IOException {
	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	factory.setNamespaceAware(true);
	XmlPullParser xpp = factory.newPullParser();
	xpp.setInput(ir);

	ArrayList<HashMap<String, String>> rssItems = null;
	HashMap<String, String> rssItem = null;

	int eventType = xpp.getEventType();
	boolean done = false;
	while (eventType != XmlPullParser.END_DOCUMENT && !done) {
	  String name = null;
	  if(eventType == XmlPullParser.START_DOCUMENT) {
	      rssItems = new ArrayList<HashMap<String, String>>();
	  } else if(eventType == XmlPullParser.START_TAG) {
	      name = xpp.getName().toLowerCase();
	      if (name.equals(ITEM)) {
		  rssItem = new HashMap<String, String>();
	      } else if (rssItem != null) {
		  if (name.equals(LINK) || name.equals(DESCRIPTION) || name.equals(TITLE)) {
		      String field_val = xpp.nextText();
		      rssItem.put(name, field_val);
		  } else if (name.equals(THUMBNAIL)) {
		      String uri = xpp.getAttributeValue(null, "url");
		      rssItem.put(name, uri);
		  }
	      }
	  } else if(eventType == XmlPullParser.END_TAG) {
	      name = xpp.getName().toLowerCase();
	      if (name.equals(ITEM)) {
		rssItems.add(rssItem);
	      } else if (name.equals(CHANNEL)) {
		done = true;
	      }
	  }
	  eventType = xpp.next();
	}
	return rssItems;
    }
}
