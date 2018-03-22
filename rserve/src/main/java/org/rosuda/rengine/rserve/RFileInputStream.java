package org.rosuda.rengine.rserve;

// JRclient library - client interface to Rserve, see http://www.rosuda.org/Rserve/
// Copyright (C) 2004 Simon Urbanek
// --- for licensing information see LICENSE file in the original JRclient distribution ---

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.rosuda.rengine.rserve.protocol.RConnectionException;
import org.rosuda.rengine.rserve.protocol.RPacket;
import org.rosuda.rengine.rserve.protocol.RTalk;

/**
 * <b>RFileInputStream</b> is an {@link InputStream} to transfer files
 * from <b>Rserve</b> server to the client. It is used very much like
 * a {@link FileInputStream}. Currently mark and seek is not supported.
 * The current implementation is also "one-shot" only, that means the file
 * can be read only once.
 * @version $Id$
 */
public class RFileInputStream extends InputStream {
    /** RTalk class to use for communication with the Rserve */
    private final RTalk rt;
   
    /** set to <code>true</code> when {@link #close} was called.
     Any subsequent read requests on closed stream  result in an
     {@link IOException} or error result */
    private boolean closed;
    /** set to <code>true</code> once EOF is reached - or more specifically
     the first time remore fread returns OK and 0 bytes */
    private boolean eof;

    /** tries to open file on the R server, using specified {@link RTalk} object
     and filename. Be aware that the filename has to be specified in host
     format (which is usually unix). In general you should not use directories
     since Rserve provides an own directory for every connection. Future Rserve
     servers may even strip all directory navigation characters for security
     purposes. Therefore only filenames without path specification are considered
     valid, the behavior in respect to absolute paths in filenames is undefined. */
    RFileInputStream(RTalk rti, String fn) throws IOException {
        rt = rti;
        RPacket rp = null;
        try {
            rp = rt.request(RTalk.CMD_openFile, fn);
            if (rp == null || !rp.isOk()) {
                throw new IOException((rp == null) ? RFileStreamUtils.CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE
                                                   : ("Request return code: " + rp.getStat()));
            }
        } catch (RConnectionException e) {
            throw new IOException(RFileStreamUtils.CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE, e);
        }
        closed = false;
        eof = false;
    }

    /** reads one byte from the file. This function should be avoided, since
     {@link RFileInputStream} provides no buffering. This means that each
     call to this function leads to a complete packet exchange between
     the server and the client. Use {@link #read(byte[], int, int)} instead
     whenever possible. In fact this function calls <code>#read(b,0,1)</code>.
     @return -1 on any failure, or the acquired byte (0..255) on success */
    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b, 0, 1) < 1) {
            return -1;
        }
        return b[0];
    }

    /** Reads specified number of bytes (or less) from the remote file.
     @param b buffer to store the read bytes
     @param off offset where to strat filling the buffer
     @param len maximal number of bytes to read
     @return number of bytes read or -1 if EOF reached
     */
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("File is not open");
        }
        if (eof) {
            return -1;
        }
        RPacket rp = null;
        try {
            rp = rt.request(RTalk.CMD_readFile, len);
            if (rp == null || !rp.isOk()) {
                throw new IOException((rp == null) ? RFileStreamUtils.CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE
                                                   : ("Request return code: " + rp.getStat()));
            }
        } catch (RConnectionException e) {
            throw new IOException(RFileStreamUtils.CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE, e);
        }
        byte[] rd = rp.getCont();
        if (rd == null) {
            eof = true;
            return -1;
        }
        int i = 0;
        while (i < rd.length) {
            b[off + i] = rd[i];
            i++;
        }
        return rd.length;
    }

    /** close stream - is not related to the actual RConnection, calling
     close does not close the RConnection
     */
    public void close() throws IOException {
        RFileStreamUtils.close(rt);
        closed = true;
    }
}
