/*
 * Copyright 2006-2009 Opensys TM by Javlin, a.s. All rights reserved.
 * Opensys TM by Javlin PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Opensys TM by Javlin a.s.; Kremencova 18; Prague; Czech Republic
 * www.cloveretl.com; info@cloveretl.com
 *
 */

package org.jetel.graph;

import java.io.IOException;
import java.io.OutputStream;

import org.jetel.data.DataRecord;
import org.jetel.data.formatter.BinaryDataFormatter;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.graph.Edge;
import org.jetel.graph.EdgeBase;
import org.jetel.graph.runtime.IAuthorityProxy;
import org.jetel.util.bytes.CloverBuffer;

/**
 * This edge represents the "left" side of a remote edge, used by graph running splitted on cluster.
 * An artificial consumer {@link RemoteEdgeDataTransmitter} is attached to ensure parallel initialisation
 * of IO streams for remote data transmission. The data are not read by the consumer, but the data records
 * are send directly to the prepared output stream by the producer thread.
 * 
 * All incoming data are serialised by {@link BinaryDataFormatter} to an output stream
 * provided by {@link RemoteEdgeDataTransmitter}, which was created in pre-execution phase using
 * {@link IAuthorityProxy#getRemoteEdgeOutputStream(String)} method.
 * 
 * @author Kokon (info@cloveretl.com)
 *         (c) Opensys TM by Javlin, a.s. (www.cloveretl.com)
 *
 * @created 8.11.2012
 */
public class LRemoteEdge extends EdgeBase {
	
	private BinaryDataFormatter dataFormatter;
	private long outputRecordsCounter;
	private long outputBytesCounter;
	private boolean eof;
	
	/**
	 * @param proxy
	 * @param index 
	 * @param remoteNodeId 
	 */
	public LRemoteEdge(Edge proxy) {
		super(proxy);
	}

	@Override
	public void init() throws IOException, InterruptedException {
		dataFormatter = new BinaryDataFormatter();
		try {
			dataFormatter.init(proxy.getMetadata());
		} catch (ComponentNotReadyException e) {
			throw new JetelRuntimeException(e);
		}
	}

	@Override
	public void preExecute() {
		super.preExecute();

		outputRecordsCounter = 0;
		outputBytesCounter = 0;
		eof = false;
	}

	@Override
	public void postExecute() {
		super.postExecute();
		
		//closing is performed on eof(), which is safer, because eof() is performed in execute() phase
		//which is interruptable, but postExecute() phase is not interruptable
		//
		//this close() invocation is blocking, waiting for close of opposite side of remote piped stream 
//		dataFormatter.close();
	}
	
	@Override
	public void writeRecord(DataRecord record) throws IOException, InterruptedException {
		outputBytesCounter += dataFormatter.write(record);
		outputRecordsCounter++;
	}

	@Override
	public void writeRecordDirect(CloverBuffer record) throws IOException, InterruptedException {
		outputBytesCounter += dataFormatter.write(record);
		outputRecordsCounter++;
	}

	@Override
	public DataRecord readRecord(DataRecord record) throws IOException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean readRecordDirect(CloverBuffer record) throws IOException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getOutputRecordCounter() {
		return outputRecordsCounter;
	}

	@Override
	public long getInputRecordCounter() {
		return outputRecordsCounter;
	}

	@Override
	public long getOutputByteCounter() {
		return outputBytesCounter;
	}

	@Override
	public long getInputByteCounter() {
		return outputBytesCounter;
	}

	@Override
	public int getBufferedRecords() {
		return 0;
	}

	@Override
	public int getUsedMemory() {
		return 0;
	}

	@Override
	public void eof() throws IOException, InterruptedException {
		eof = true;
		dataFormatter.close();
	}

	@Override
	public boolean isEOF() {
		return eof;
	}

	@Override
	public void free() {
		
	}

	@Override
	public boolean hasData() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the output stream where the incoming records are written.
	 * @param outputStream
	 * @see RemoteEdgeDataTransmitter#preExecute()
	 */
	public void setOutputStream(OutputStream outputStream) {
		dataFormatter.setDataTarget(outputStream);
	}

}
