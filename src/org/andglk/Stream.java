package org.andglk;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import android.util.Log;

public abstract class Stream extends CPointed {
	protected int mWritten;
	private int mRead;
	private List<Window.Stream> mEchoedWindows = new LinkedList<Window.Stream>();
	protected static List<Stream> _streams = new LinkedList<Stream>();
	private static Iterator<Stream> _iterator;
	private static Stream _last;
	protected Glk mGlk;

	protected Stream(int rock) {
		super(rock);
		
		_streams.add(this);
		mGlk = Glk.getInstance();
	}
	
	static public Stream iterate(Stream s) {
		if (s == null)
			_iterator = _streams.iterator();
		else if (_last != s) {
			_iterator = _streams.iterator();
			while (_iterator.next() != s);
		}
		_last = _iterator.next();
		return _last;
	}

	/** Opens a stream which reads from or writes to a disk file.
	 * 
	 * <p>The file may be read or written in text or binary mode; this is determined by the {@code fileref} argument. 
	 * Similarly, platform-dependent attributes such as file type are determined by {@code fileref}.
	 * 
	 * <p>When writing in binary mode, Unicode values (characters greater than 255) cannot be written to the file. 
	 * If you try, they will be stored as 0x3F ("?") characters. In text mode, Unicode values are stored in UTF-8 
	 * (but 8-bit characters are written raw).
	 * 
	 * @param fileref Indicates which file will be opened.
	 * @param fmode Can be any of {@link FileRef#FILEMODE_READ}, {@link FileRef#FILEMODE_READWRITE}, 
	 * {@link FileRef#FILEMODE_READ}, {@link FileRef#FILEMODE_WRITE}, {@link FileRef#FILEMODE_WRITEAPPEND}.
	 * <p>If fmode is {@link FileRef#FILEMODE_READ}, the file must already exist; 
	 * for the other modes, an empty file is created if none exists.
	 * <p>If fmode is {@link FileRef#FILEMODE_WRITE}, and the file already exists, it is truncated down to zero length 
	 * (an empty file). 
	 * <p>If fmode is {@link FileRef#FILEMODE_WRITEAPPEND}, the file mark is set to the end of the file.
	 * @param rock Rock value to embed in the stream.
	 * @return C pointer to the reference to the stream.
	 */
	static int openFile(FileRef fileref, int fmode, int rock) {
		return (new FileStream(fileref, fmode, rock)).getPointer();
	}
	
	public void putChar(char c) {
		try {
			doPutChar(c);
			mWritten++;
		} catch (IOException e) {
			Log.e("Glk/Stream", "I/O error in putChar", e);
		}
	}
	
	abstract protected void doPutChar(char c) throws IOException;

	/** Close the stream.
	 * 
	 * @return Total number of bytes read[0] and written[1] to the stream.
	 */
	int[] close() {
		try {
			doClose();
		} catch (IOException e) {
			Log.e("Glk/Stream", "I/O error in close", e);
		} 
		
		release();
		_streams.remove(this);
		if (mGlk.getCurrentStream() == this)
			mGlk.setCurrentStream(null);
		
		Iterator<Window.Stream> it = mEchoedWindows.iterator();
		while (it.hasNext())
			it.next().echoOff();
			
		return new int[] { mRead, mWritten };
	}
	
	protected abstract void doClose() throws IOException;

	/** Reads one character from the stream.
	 * 
	 * @return The read character (0..255) or -1 if at the end of stream.
	 */
	int getChar() {
		try {
			int res = doGetChar();
			mRead++;
			return res;
		} catch (IOException e) {
			Log.e("Glk/Stream", "I/O error in getChar", e);
			return -1;
		}
	}

	abstract protected int doGetChar() throws IOException;

	/** Write a string to the stream.
	 * 
	 * @param str The string to write.
	 */
	public void putString(String str) {
		try {
			doPutString(str);
			mWritten += str.length();
		} catch (IOException e) {
			Log.e("Glk/Stream", "I/O error in putString", e);
		}
	}

	protected abstract void doPutString(String str) throws IOException;

	public abstract void setStyle(long styl);

	public void echoOff(Window.Stream stream) {
		mEchoedWindows.remove(stream);
	}

	public void echoOn(Window.Stream stream) {
		mEchoedWindows.add(stream);
	}
	
	public static void setCurrent(Stream stream) {
		Glk.getInstance().setCurrentStream(stream);
	}
	
	public static Stream getCurrent() {
		return Glk.getInstance().getCurrentStream();
	}
}
