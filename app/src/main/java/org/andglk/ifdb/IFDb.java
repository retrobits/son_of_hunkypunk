/*
	Copyright © 2009 Rafał Rzepecki <divided.mind@gmail.com>

	This file is part of Hunky Punk.

    Hunky Punk is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Hunky Punk is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Hunky Punk.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.andglk.ifdb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.andglk.glk.Utils;
import org.andglk.hunkypunk.HunkyPunk;
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
		private static final String IFDB_TAG = "ifdb";
		private static final String COVERART_TAG = "coverart";
		
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
		private static final int IFDB = 12;
		private static final int COVERART = 13;
		
		private int mElement = NOTHING_INTERESTING;
		private ContentValues mValues = new ContentValues();
		private String mCoverArt = "";

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
			} else if (mElement == IFDB && trim.equals(COVERART_TAG))
				mElement = COVERART;
			else if (trim.equals(BIBLIOGRAPHIC_TAG))
				mElement = BIBLIOGRAPHIC;
			else if (trim.equals(IFDB_TAG))
				mElement = IFDB;
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String str = new String(ch, start, length);
			switch (mElement) {
			case TITLE:
				String soFar = mValues.getAsString(Games.TITLE);
				mValues.put(Games.TITLE, soFar == null ? str : soFar + str);
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
			case DESCRIPTION: {
				String soFarD = mValues.getAsString(Games.DESCRIPTION);
				mValues.put(Games.DESCRIPTION, soFarD == null ? str : soFarD + str);
				break;
			}
			case SERIES:
				mValues.put(Games.SERIES, str);
				break;
			case SERIESNUMBER:
				mValues.put(Games.SERIESNUMBER, Integer.valueOf(str));
				break;
			case FORGIVENESS:
				mValues.put(Games.FORGIVENESS, str);
				break;
			case COVERART:
				mCoverArt += str;
				break;
			}
		}

		@Override
		public void endElement(String uri, String localName, String name) 
				throws SAXException { 
			String tag = null;
			switch (mElement) {
			case TITLE:
				tag = Games.TITLE;
				break;
			case AUTHOR:
				tag = Games.AUTHOR;
				break;
			case LANGUAGE:
				tag = Games.LANGUAGE;
				break;
			case HEADLINE:
				tag = Games.HEADLINE;
				break;
			case FIRSTPUBLISHED:
				tag = Games.FIRSTPUBLISHED;
				break;
			case GENRE:
				tag = Games.GENRE;
				break;
			case GROUP:
				tag = Games.GROUP;
				break;
			case DESCRIPTION:
				tag = Games.DESCRIPTION;
				break;
			case SERIES:
				tag = Games.SERIES;
				break;
			}
			if (tag != null) {
				String str = mValues.getAsString(tag);
				mValues.put(tag, StringHelper.unescapeHTML(str));
			}

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
			case FORGIVENESS:
				mElement = BIBLIOGRAPHIC;
				break;
			case BIBLIOGRAPHIC:
				mElement = NOTHING_INTERESTING;
			case IFDB:
				if (localName.trim().equals(IFDB_TAG))
					mElement = NOTHING_INTERESTING;
				break;
			case COVERART:
				if (localName.trim().equals(COVERART_TAG))
					mElement = IFDB;
				break;
			}
		}
		
		public ContentValues getValues() {
			return mValues;
		}
		
		public String getCoverArt() {
			return mCoverArt;
		}
	}

	private static final String[] PROJECTION = { Games._ID, Games.IFID };
	private static final int IFID = 1;

	private static final String BASE_URL = "http://ifdb.tads.org/viewgame?ifiction&ifid=";

	private static final String TAG = "IFDb";
	public static final int FAILURE = 0;
	public static final int SUCCESS = 1;
	
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

	public void lookupGames() throws IOException {
		Cursor query = mContentResolver.query(Games.CONTENT_URI, PROJECTION, 
				Games.LOOKED_UP + " IS NULL", null, null);
		
		try {
			while (query.moveToNext())
				try {
					lookup(query.getString(IFID));
				} catch (MalformedIFIDException e) {
					Log.e(TAG, "malformed ifid", e);
				} catch (IOException e) {
					Log.e(TAG, "can't connect, giving up on others", e);
					throw e;
				}
		} finally {
			query.close();
		}
	}
	
	private static SAXParserFactory factory = SAXParserFactory.newInstance(); 

	public void lookup(String ifid) throws MalformedIFIDException, IOException {
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
			//Log.v(TAG, "fetching IFDB info " + url.toString());
			xr.parse(new InputSource(url.openStream()));
		} catch (IOException e) {
			Log.w(TAG, "IO exception while fetching record on " + ifid + " from IFDb, possibly doesn't exist", e);
			return;
		} catch (SAXException e) {
			Log.e(TAG, "SAX exception while parsing record on " + ifid, e);
			// try to continue, IFDb currently sometimes serves broken XML
			// (and there is no easy way in java to massage it, #@%&#%$@)
		}
		
		final String coverArt = handler.getCoverArt();
		if (coverArt != null)
			try {
				fetchCover(ifid, coverArt);
			} catch (IOException e) {
				Log.e(TAG, "IO error when fetching cover for " + ifid, e);
			}
		
		ContentValues values = handler.getValues();
		values.put(Games.LOOKED_UP, true);
		mContentResolver.update(Games.uriOfIfid(ifid), values, null, null);
	}

	private static void fetchCover(String ifid, String coverArt) throws IOException {
		if (coverArt == null || coverArt.length() == 0) return;

		URL source = new URL(coverArt);
		File destination = HunkyPunk.getCover(ifid);

		FileOutputStream fos = new FileOutputStream(destination);
		Utils.copyStream(source.openStream(), fos);
		
		fos.close();
	}

	private static URL urlOfIfid(String ifid) throws MalformedURLException {

		//HACK: fixup various ifdb errors here:
		String hack_ifid = ifid;
		if (hack_ifid.compareTo("ZCODE-88-840726")==0)
			hack_ifid += "-A129";
		
		return new URL(BASE_URL + hack_ifid);
	}

	public void startLookup(final String ifid, final Handler lookupHandler) {
		new Thread() {
			@Override
			public void run() {
				try {
					lookup(ifid);
					Message.obtain(lookupHandler, SUCCESS).sendToTarget();
				} catch (Exception e) {
					Message.obtain(lookupHandler, FAILURE).sendToTarget();
					Log.e(TAG, "error while looking up " + ifid, e);
				}
			}
		}.start();
	}
}
