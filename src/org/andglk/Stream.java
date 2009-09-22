package org.andglk;

public abstract class Stream extends CPointed {
	protected Stream(int rock) {
		super(rock);
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
	
	abstract void putChar(char c);
	
	/** Close the stream.
	 * 
	 * @return Total number of bytes read[0] and written[1] to the stream.
	 */
	abstract int[] close();
	
	/** Reads one character from the stream.
	 * 
	 * @return The read character (0..255) or -1 if at the end of stream.
	 */
	abstract int getChar();
}
