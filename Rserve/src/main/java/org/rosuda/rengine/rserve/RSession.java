package org.rosuda.rengine.rserve;

import org.rosuda.rengine.rserve.protocol.RConnectionException;
import org.rosuda.rengine.rserve.protocol.RPacket;
import org.rosuda.rengine.rserve.protocol.RTalk;

public class RSession implements java.io.Serializable {
    // serial version UID should only change if method signatures change
    // significantly enough that previous versions cannot be used with
    // current versions
    private static final long serialVersionUID = -7048099825974875604L;

    String host;
    int port;
    byte[] key;

    private transient RPacket attachPacket = null; // response on session attach
    int rsrvVersion;

    protected RSession() {
        // default no-args constructor for serialization
    }

    RSession(RConnection c, RPacket p) throws RserveException {
        this.host = c.getHost();
        this.rsrvVersion = c.rsrvVersion;
        byte[] ct = p.getCont();
        if (ct == null || ct.length != 32 + 3 * 4) {
            throw new RserveException(c, "Invalid response to session detach request.");
        }
        this.port = RTalk.getInt(ct, 4);
        this.key = new byte[32];
        System.arraycopy(ct, 12, this.key, 0, 32);
    }

    /** attach/resume this session */
    public RConnection attach() throws RserveException {
        RConnection c = new RConnection(this);
        try {
            attachPacket = c.getRTalk().request(-1);
        } catch (RConnectionException e) {
            throw new RserveException(c, "Cannot attach", e);
        }
        return c;
    }
}
