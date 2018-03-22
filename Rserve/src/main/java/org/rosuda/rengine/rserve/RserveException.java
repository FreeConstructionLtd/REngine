// JRclient library - client interface to Rserve, see http://www.rosuda.org/Rserve/
// Copyright (C) 2004 Simon Urbanek
// --- for licensing information see LICENSE file in the original JRclient distribution ---
//
//  RserveException.java
//
//  Created by Simon Urbanek on Mon Aug 18 2003.
//
//  $Id$
//

package org.rosuda.rengine.rserve;

import org.rosuda.rengine.REngineException;
import org.rosuda.rengine.rserve.protocol.RPacket;
import org.rosuda.rengine.rserve.protocol.RTalk;

class RserveException extends REngineException {
    private final int reqReturnCode;

    public RserveException(RConnection c, String msg) {
        this(c, msg, -1);
    }

    public RserveException(RConnection connection, String msg, Throwable cause) {
        super(connection, msg, cause);
        reqReturnCode = -1;
        if (connection != null) {
            connection.lastError = getMessage();
        }
    }

    public RserveException(RConnection connection, String msg, int requestReturnCode) {
        super(connection, msg);
        reqReturnCode = requestReturnCode;
        if (connection != null) {
            connection.lastError = getMessage();
        }
    }

    public RserveException(RConnection connection, String msg, Throwable cause, int requestReturnCode) {
        super(connection, msg, cause);
        reqReturnCode = requestReturnCode;
        if (connection != null) {
            connection.lastError = getMessage();
        }
    }

    public RserveException(RConnection c, String msg, RPacket p) {
        this(c, msg, (p == null) ? -1 : p.getStat());
    }

    private String getRequestErrorDescription() {
        return getRequestErrorDescription(reqReturnCode);
    }

    private String getRequestErrorDescription(int code) {
        switch (code) {
            case 0:
                return "no error";
            case 2:
                return "R parser: input incomplete";
            case 3:
                return "R parser: syntax error";
            case RTalk.ERR_auth_failed:
                return "authorization failed";
            case RTalk.ERR_conn_broken:
                return "connection broken";
            case RTalk.ERR_inv_cmd:
                return "invalid command";
            case RTalk.ERR_inv_par:
                return "invalid parameter";
            case RTalk.ERR_IOerror:
                return "I/O error on the server";
            case RTalk.ERR_not_open:
                return "connection is not open";
            case RTalk.ERR_access_denied:
                return "access denied (local to the server)";
            case RTalk.ERR_unsupported_cmd:
                return "unsupported command";
            case RTalk.ERR_unknown_cmd:
                return "unknown command";
            case RTalk.ERR_data_overflow:
                return "data overflow, incoming data too big";
            case RTalk.ERR_object_too_big:
                return "evaluation successful, but returned object is too big to transport";
            case RTalk.ERR_out_of_mem:
                return "FATAL: Rserve ran out of memory, closing connection";
            case RTalk.ERR_session_busy:
                return "session is busy";
            case RTalk.ERR_detach_failed:
                return "session detach failed";
            case RTalk.ERR_ctrl_closed:
                return "control pipe to master process is closed/broken";
        }
        return "error code: " + code;
    }

    public String getMessage() {
        return super.getMessage() + ((reqReturnCode != -1) ? ", request status: " + getRequestErrorDescription() : "");
    }

    public int getRequestReturnCode() {
        return reqReturnCode;
    }
}