// JRclient library - client interface to Rserve, see http://www.rosuda.org/Rserve/
// Copyright (C) 2003 Simon Urbanek
// --- for licensing information see LICENSE file in the original JRclient distribution ---
//
//  RFileOutputStream.java
//
//  Created by Simon Urbanek on Wed Oct 22 2003.
//

package org.rosuda.rengine.rserve;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.rosuda.rengine.rserve.protocol.RConnectionException;
import org.rosuda.rengine.rserve.protocol.RPacket;
import org.rosuda.rengine.rserve.protocol.RTalk;


/** <b>RFileOutputStream</b> is an {@link OutputStream} to transfer files
 from the client to <b>Rserve</b> server. It is used very much like
 a {@link FileOutputStream}. Currently mark and seek is not supported.
 The current implementation is also "one-shot" only, that means the file
 can be written only once.
 @version $Id$
 */

public class RFileOutputStream extends OutputStream {
    /**
     * RTalk class to use for communication with the Rserve */
    private final RTalk rt;

    /**
     * Set to <code>true</code> when {@link #close} was called.
     * Any subsequent read requests on closed stream  result in an
     * {@link IOException} or error result
     */
    private boolean closed;

    /**
     * Tries to create a file on the R server, using specified {@link RTalk} object
     * and filename. Be aware that the filename has to be specified in host
     * format (which is usually unix). In general you should not use directories
     * since Rserve provides an own directory for every connection. Future Rserve
     * servers may even strip all directory navigation characters for security
     * purposes. Therefore only filenames without path specification are considered
     * valid, the behavior in respect to absolute paths in filenames is undefined.
     * @param rti RTalk object for communication with Rserve
     * @param fn filename of the file to create (existing file will be overwritten)
     */
    RFileOutputStream(RTalk rti, String fn) throws IOException {
        rt = rti;
        try {
            RPacket rp = rt.request(RTalk.CMD_createFile, fn);
            if (rp == null || !rp.isOk()) {
                throw new IOException((rp == null) ? RFileStreamUtils.CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE
                                                   : ("Request return code: " + rp.getStat()));
            }
        } catch (RConnectionException e) {
            throw new IOException(RFileStreamUtils.CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE, e);
        }
        closed = false;
    }

    /**
     * Writes one byte to the file. This function should be avoided, since
     * {@link RFileOutputStream} provides no buffering. This means that each
     * call to this function leads to a complete packet exchange between
     * the server and the client. Use {@link #write(byte[])} instead
     * whenever possible. In fact this function calls <code>write(b,0,1)</code>.
     * @param b byte to write
     */
    public void write(int b) throws IOException {
        byte[] ba = new byte[1];
        write(ba, 0, 1);
    }

    /**
     * Writes the content of b into the file. This methods is equivalent to calling <code>write(b,0,b.length)</code>.
     * @param b content to write
     */
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes specified number of bytes to the remote file.
     * @param b buffer containing the bytes to write
     * @param off offset where to start
     * @param len number of bytes to write
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("File is not open");
        }
        if (len < 0) {
            len = 0;
        }
        byte[] hdr = RTalk.newHdr(RTalk.DT_BYTESTREAM, len);
        try {
            RPacket rp = rt.request(RTalk.CMD_writeFile, hdr, b, off, len);
            if (rp == null || !rp.isOk()) {
                throw new IOException((rp == null) ? RFileStreamUtils.CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE
                                                   : ("Request return code: " + rp.getStat()));
            }
        } catch (RConnectionException e) {
            throw new IOException(RFileStreamUtils.CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE, e);
        }
    }

    /**
     * Close stream - is not related to the actual RConnection, calling
     * close does not close the RConnection.
     */
    public void close() throws IOException {
        RFileStreamUtils.close(rt);
        closed = true;
    }

    /**
     * Currently (Rserve 0.3) there is no way to force flush on the remote side, hence this function is noop. Future
     * versions of Rserve may support this feature though. At any rate, it is safe to call it.
     */
    public void flush() {
    }
}
