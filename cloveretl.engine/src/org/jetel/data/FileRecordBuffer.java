/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.jetel.exception.JetelRuntimeException;
import org.jetel.exception.TempFileCreationException;
import org.jetel.graph.ContextProvider;
import org.jetel.graph.runtime.IAuthorityProxy;
import org.jetel.util.bytes.CloverBuffer;

/**
 *  Class implementing RecordBuffer backed by temporary file - i.e. unlimited
 *  size<br>
 *  Implements FIFO: push & shift operations can be interleaved, however it
 *  deteriorates performance.<br>
 *  
 *
 *@author     dpavlis <david.pavlis@javlin.eu>
 *@created    May 21, 2003
 *@since      December 09, 2002
 *@updated	  November 15, 2010 - removed size limitation/changed position pointers to long
 */
public class FileRecordBuffer {

	private FileChannel tmpFileChannel;
	private File tmpFile;

	private CloverBuffer dataBuffer;

	protected long readPosition;
	protected long writePosition;
	private long mapPosition;

	private boolean hasFile;
	private boolean isDirty;
	private boolean isClosed;
	// indicates whether buffer contains unwritten data

	// data
	private final static int DEFAULT_BUFFER_SIZE = Defaults.Record.RECORDS_BUFFER_SIZE;
	// size of BUFFER - used for push & shift operations
	private final static int LEN_SIZE_SPECIFIER = 4;
	// size of integer variable used to keep record length

	private final static String TMP_FILE_PREFIX = "fbuffrb";
	// prefix of temporary file generated by system
	private final static String TMP_FILE_SUFFIX = ".tmp";
	// suffix of temporary file generated by system
	private final static String TMP_FILE_MODE = "rw";



	// methods
	/**
	 *  Constructor for the FileRecordBuffer object
	 *
	 *@param  dataBufferSize  The size of internal in memory buffer. If smaller
	 *      than DEFAULT_BUFFER_SIZE, then default is used
	 */
	public FileRecordBuffer(int dataBufferSize) {
		readPosition = 0;
		writePosition = 0;
		mapPosition = 0;
		isDirty = false;
		hasFile = false;
		isClosed=false;
		dataBuffer = CloverBuffer.allocateDirect(dataBufferSize > DEFAULT_BUFFER_SIZE ? dataBufferSize : DEFAULT_BUFFER_SIZE);
	}


	/**
	 *  Constructor for the FileRecordBuffer object
	 */
	public FileRecordBuffer() {
		readPosition = 0;
		writePosition = 0;
		mapPosition = 0;
		isDirty = false;
		hasFile = false;
		isClosed=false;
		dataBuffer = CloverBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
	}


	/**
	 *  Opens buffer, creates temporary file.
	 *
	 *@exception  IOException  Description of Exception
	 *@since                   September 17, 2002
	 */
	@SuppressWarnings("resource")
	private void openTmpFile() throws IOException {
		
		try {
			IAuthorityProxy authorityProxy = IAuthorityProxy.getAuthorityProxy(ContextProvider.getGraph());
			tmpFile = authorityProxy.newTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX, -1);
		} catch (TempFileCreationException e) {
			throw new IOException("Failed to create temp file.", e);
		}
		// we want the temp file be deleted on exit
		tmpFileChannel = new RandomAccessFile(tmpFile, TMP_FILE_MODE).getChannel();
		hasFile=true;
	}


	/**
	 *  Closes buffer, removes temporary file (is exists)
	 *
	 *@exception  IOException  Description of Exception
	 *@since                   September 17, 2002
	 */
	public void close() throws IOException {
		isClosed=true;
		if (hasFile) {
			tmpFileChannel.close();
			if (!tmpFile.delete()) {
				throw new IOException("Can't delete TMP file: " + tmpFile.getAbsoluteFile());
			}
		}
		hasFile = false;
	}


	/**
	 *  Rewinds the buffer. Next shift operation returns first record stored.
	 *
	 *@since    September 19, 2002
	 */
	public void rewind() {
		readPosition = 0;
	}


	/**
	 *  Clears the buffer. Temp file (if it was created) remains
	 * unchanged size-wise
	 */
	public void clear() {
		readPosition = 0;
		writePosition = 0;
		mapPosition = 0;
		isDirty = false;
		dataBuffer.clear();
	}


	/**
	 *  Stores one data record into buffer.
	 *
	 *@param  data             ByteBuffer containing record's data
	 *@exception  IOException  In case of IO failure
	 *@since                   September 17, 2002
	 */
	public void push(CloverBuffer data) throws IOException {
		if(isClosed){
			throw new IOException("Buffer has been closed !");
		}
		
		int recordSize = data.remaining();

		secureBuffer(writePosition, recordSize + LEN_SIZE_SPECIFIER);
		try {
			dataBuffer.position((int)(writePosition - mapPosition));
			dataBuffer.putInt(recordSize);
			dataBuffer.put(data);
			writePosition += (recordSize + LEN_SIZE_SPECIFIER);
			isDirty = true;
		} catch (BufferOverflowException ex) {
			throw new IOException("Input Buffer is not big enough to accomodate data record !");
		}
	}

	/**
	 * @deprecated use {@link #push(CloverBuffer)} instead
	 */
	@Deprecated
	public void push(ByteBuffer data) throws IOException {
		CloverBuffer wrappedBuffer = CloverBuffer.wrap(data);
		push(wrappedBuffer);
		if (wrappedBuffer.buf() != data) {
			throw new JetelRuntimeException("Deprecated method invocation failed. Please use CloverBuffer instead of ByteBuffer.");
		}
	}

	/**
	 *  Checks whether in memory buffer has to be reloaded/flushed
	 *
	 *@param  position       Description of the Parameter
	 *@param  requestedSize  Description of the Parameter
	 *@return                Description of the Return Value
	 */
	private final boolean needRemap(long position, int requestedSize) {
        final int remaining=dataBuffer.remaining();
		if (position < mapPosition || position > (mapPosition + remaining) ||
                (position - mapPosition + requestedSize > remaining)) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 *  Secures that in memory buffer is "mapped" from proper location and
	 *  populated with data from TMP file (is needed)
	 *
	 *@param  position         Description of the Parameter
	 *@param  requestedSize    Description of the Parameter
	 *@exception  IOException  Description of the Exception
	 */
	private final void secureBuffer(long position, int requestedSize) throws IOException {
		if (needRemap(position, requestedSize)) {
			flushBuffer();
			boolean reloadNeed = (position < writePosition);
			mapBuffer(position, requestedSize, reloadNeed);
		}
	}


	/**
	 *  Returns next record from the buffer - FIFO order.
	 *
	 *@param  data             ByteBuffer into which store data
	 *@return                  ByteBuffer populated with record's data or NULL if
	 *      no more record can be retrieved
	 *@exception  IOException  Description of Exception
	 *@since                   September 17, 2002
	 */
	public CloverBuffer shift(CloverBuffer data) throws IOException {
		int recordSize;
		if(isClosed){
			throw new IOException("Buffer has been closed !");
		}
		if (readPosition >= writePosition) {
			return null;
		}
		secureBuffer(readPosition, LEN_SIZE_SPECIFIER);
		dataBuffer.position((int)(readPosition - mapPosition));
		recordSize = dataBuffer.getInt();
		readPosition += LEN_SIZE_SPECIFIER;
		secureBuffer(readPosition, recordSize);
		int oldLimit = dataBuffer.limit();
		dataBuffer.limit(dataBuffer.position() + recordSize);
		data.put(dataBuffer);
		dataBuffer.limit(oldLimit);
		readPosition += recordSize;
		return data;
	}

	/**
	 * @deprecated use {@link #shift(CloverBuffer)} instead
	 */
	@Deprecated
	public ByteBuffer shift(ByteBuffer data) throws IOException {
		CloverBuffer wrappedBuffer = CloverBuffer.wrap(data);
		CloverBuffer result = shift(wrappedBuffer);
		if (wrappedBuffer.buf() != data) {
			throw new JetelRuntimeException("Deprecated method invocation failed. Please use CloverBuffer instead of ByteBuffer.");
		}
		return result.buf();
	}
	
	/**
	 *  Reads next record from the buffer but leaves the record there - FIFO order.
	 *  Subsequent calls to this method returns the same record.	
	 *
	 *@param  data             ByteBuffer into which store data
	 *@return                  ByteBuffer populated with record's data or NULL if
	 *      no more record can be retrieved
	 *@exception  IOException  Description of the Exception
	 */
	public CloverBuffer get(CloverBuffer data) throws IOException {
		int recordSize;
		if(isClosed){
			throw new IOException("Buffer has been closed !");
		}
		if (readPosition >= writePosition) {
			return null;
		}
		secureBuffer(readPosition, LEN_SIZE_SPECIFIER);
		dataBuffer.mark();
		dataBuffer.position((int)(readPosition - mapPosition));
		recordSize = dataBuffer.getInt();
		readPosition += LEN_SIZE_SPECIFIER;
		secureBuffer(readPosition, recordSize);
		int oldLimit = dataBuffer.limit();
		dataBuffer.limit(dataBuffer.position() + recordSize);
		data.put(dataBuffer);
		dataBuffer.limit(oldLimit);
		dataBuffer.reset();
		return data;
	}


	/**
	 *  Flushes in memory buffer into TMP file
	 *
	 *@exception  IOException  Description of Exception
	 *@since                   September 17, 2002
	 */
	private void flushBuffer() throws IOException {
		if (isDirty) {
			dataBuffer.flip();
			if (!hasFile) {
				openTmpFile();
			}
			tmpFileChannel.write(dataBuffer.buf(), mapPosition);
		}
		dataBuffer.clear();
		isDirty = false;
	}

	public boolean isEmpty() {
		return readPosition >= writePosition;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  fromPosition     Description of the Parameter
	 *@param  reload           Description of the Parameter
	 *@exception  IOException  Description of Exception
	 *@since                   September 17, 2002
	 */
	private void mapBuffer(long fromPosition, int requestedSize, boolean reload) throws IOException {
		dataBuffer.clear();
		//ensure that the buffer is big enough to bear 'requestedSize' bytes
		if (dataBuffer.capacity() < requestedSize) {
			dataBuffer.expand(requestedSize);
		}
		mapPosition = fromPosition;
		if (reload) {
			if (hasFile) {
				tmpFileChannel.read(dataBuffer.buf(), mapPosition);
				dataBuffer.flip();
			} else {
				throw new RuntimeException("Can't remap buffer TMP file doesn't exist");
			}
		}
		isDirty = false;
	}

}

