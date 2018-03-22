package org.rosuda.rengine.rserve;

import java.io.IOException;

import org.rosuda.rengine.rserve.protocol.RConnectionException;
import org.rosuda.rengine.rserve.protocol.RPacket;
import org.rosuda.rengine.rserve.protocol.RTalk;

class RFileStreamUtils {
    public static final String CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE = "Connection to Rserve failed";

    public static void close(RTalk rt) throws IOException {
        try {
            RPacket rp = rt.request(RTalk.CMD_closeFile, (byte[]) null);
            if (rp == null || !rp.isOk()) {
                throw new IOException((rp == null) ? CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE : ("Request return code: " + rp.getStat()));
            }
        } catch (RConnectionException e) {
            throw new IOException(CONNECTION_TO_RSERVE_FAILED_ERROR_MESSAGE, e);
        }
    }
}
