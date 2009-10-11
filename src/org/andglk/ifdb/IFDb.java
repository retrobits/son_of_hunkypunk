package org.andglk.ifdb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.andglk.hunkypunk.HunkyPunk.Games;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IFDb {
	public class MalformedIFIDException extends Exception {
		private static final long serialVersionUID = -4210235667358229476L;

		public MalformedIFIDException(String ifid) {
			super(ifid);
		}
	}
	
	public class IFictionHandler extends DefaultHandler {
		private static final String BIBLIOGRAPHIC_TAG = "bibliographic";
		private static final String AUTHOR_TAG = "author";
		private static final String TITLE_TAG = "title";
		private static final String LANGUAGE_TAG = "language";
		private static final String HEADLINE_TAG = "headline";
		private static final String FIRSTPUBLISHED_TAG = "firstpublished";
		private static final String GENRE_TAG = "genre";
		private static final String GROUP_TAG = "group";
		private static final String DESCRIPTION_TAG = "description";
		private static final String SERIES_TAG = "series";
		private static final String SERIESNUMBER_TAG = "seriesnumber";
		private static final String FORGIVENESS_TAG = "forgiveness";
		
		private static final int NOTHING_INTERESTING = -1;
		private static final int BIBLIOGRAPHIC = 0;
		private static final int TITLE = 1;
		private static final int AUTHOR = 2;
		private static final int LANGUAGE = 3;
		private static final int HEADLINE = 4;
		private static final int FIRSTPUBLISHED = 5;
		private static final int GENRE = 6;
		private static final int GROUP = 7;
		private static final int DESCRIPTION = 8;
		private static final int SERIES = 9;
		private static final int SERIESNUMBER = 10;
		private static final int FORGIVENESS = 11;
		
		private int mElement = NOTHING_INTERESTING;
		private ContentValues mValues = new ContentValues();

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			final String trim = localName.trim();
			if (mElement == BIBLIOGRAPHIC) {
				if (trim.equals(TITLE_TAG))
					mElement = TITLE;
				else if (trim.equals(AUTHOR_TAG))
					mElement = AUTHOR;
				else if (trim.equals(LANGUAGE_TAG))
					mElement = LANGUAGE;
				else if (trim.equals(HEADLINE_TAG))
					mElement = HEADLINE;
				else if (trim.equals(FIRSTPUBLISHED_TAG))
					mElement = FIRSTPUBLISHED;
				else if (trim.equals(GENRE_TAG))
					mElement = GENRE;
				else if (trim.equals(GROUP_TAG))
					mElement = GROUP;
				else if (trim.equals(DESCRIPTION_TAG))
					mElement = DESCRIPTION;
				else if (trim.equals(SERIES_TAG))
					mElement = SERIES;
				else if (trim.equals(SERIESNUMBER_TAG))
					mElement = SERIESNUMBER;
				else if (trim.equals(FORGIVENESS_TAG))
					mElement = FORGIVENESS;
			} else if (trim.equals(BIBLIOGRAPHIC_TAG))
				mElement = BIBLIOGRAPHIC;
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String str = new String(ch, start, length);
			switch (mElement) {
			case TITLE:
				mValues.put(Games.TITLE, str);
				break;
			case AUTHOR:
				mValues.put(Games.AUTHOR, str);
				break;
			case LANGUAGE:
				mValues.put(Games.LANGUAGE, str);
				break;
			case HEADLINE:
				mValues.put(Games.HEADLINE, str);
				break;
			case FIRSTPUBLISHED:
				mValues.put(Games.FIRSTPUBLISHED, str);
				break;
			case GENRE:
				mValues.put(Games.GENRE, str);
				break;
			case GROUP:
				mValues.put(Games.GROUP, str);
				break;
			case DESCRIPTION:
				mValues.put(Games.DESCRIPTION, str);
				break;
			case SERIES:
				mValues.put(Games.SERIES, str);
				break;
			case SERIESNUMBER:
				mValues.put(Games.SERIESNUMBER, Integer.valueOf(str));
				break;
			case FORGIVENESS:
				mValues.put(Games.FORGIVENESS, str);
				break;
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			/* we assume well-formed IFiction here */
			switch (mElement) {
			case TITLE:
			case AUTHOR:
			case LANGUAGE:
			case HEADLINE:
			case FIRSTPUBLISHED:
			case GENRE:
			case GROUP:
			case DESCRIPTION:
			case SERIES:
			case SERIESNUMBER:
				mElement = BIBLIOGRAPHIC;
				break;
			case BIBLIOGRAPHIC:
				mElement = NOTHING_INTERESTING;
				break;
			}
		}
		
		public ContentValues getValues() {
			return mValues;
		}
	}

	private static final String[] PROJECTION = { Games._ID, Games.IFID };
	private static final int IFID = 1;

	private static final String BASE_URL = "http://ifdb.tads.org/viewgame?ifiction&ifid=";

	private static final String TAG = "IFDb";
	
	private static IFDb sInstance;
	private final ContentResolver mContentResolver;

	private IFDb(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
		
	}

	public static IFDb getInstance(ContentResolver contentResolver) {
		if (sInstance == null)
			sInstance = new IFDb(contentResolver);
		else
			assert(contentResolver.equals(sInstance.mContentResolver));

		return sInstance;
	}

	public void startLookup(final Handler errorHandler) {
		new Thread() {
			@Override
			public void run() {
				try {
					lookupGames();
				} catch (IOException e) {
					Message.obtain(errorHandler).sendToTarget();
				}
			}
		}.start();
	}

	protected void lookupGames() throws IOException {
		Cursor query = mContentResolver.query(Games.CONTENT_URI, PROJECTION, 
				Games.LOOKED_UP + " IS NULL", null, null);
		
		while (query.moveToNext())
			try {
				lookup(query.getString(IFID));
			} catch (MalformedIFIDException e) {
				Log.e(TAG, "malformed ifid", e);
			} catch (IOException e) {
				Log.e(TAG, "can't connect, giving up on others", e);
				throw e;
			}
	}
	
	private static SAXParserFactory factory = SAXParserFactory.newInstance(); 

	private void lookup(String ifid) throws MalformedIFIDException, IOException {
		URL url;
		try {
			url = urlOfIfid(ifid);
		} catch (MalformedURLException e) {
			throw new MalformedIFIDException(ifid);
		}

		SAXParser sp;
		XMLReader xr;
		try {
			sp = factory.newSAXParser();
			xr = sp.getXMLReader();
		} catch (Exception e) {
			throw new RuntimeException("Unexpected failure while creating SAX parser", e);
		}
		
		IFictionHandler handler = new IFictionHandler();
		xr.setContentHandler(handler);
		try {
			xr.parse(new InputSource(url.openStream()));
		} catch (IOException e) {
			Log.w(TAG, "IO exception while fetching record on " + ifid + " from IFDb, possibly doesn't exist", e);
			return;
		} catch (SAXException e) {
			Log.e(TAG, "SAX exception while parsing record on " + ifid, e);
			return;
		}
		
		ContentValues values = handler.getValues();
		values.put(Games.LOOKED_UP, true);
		mContentResolver.update(Games.uriOfIfid(ifid), values, null, null);
	}

	private static URL urlOfIfid(String ifid) throws MalformedURLException {
		return new URL(BASE_URL + ifid);
	}
}
