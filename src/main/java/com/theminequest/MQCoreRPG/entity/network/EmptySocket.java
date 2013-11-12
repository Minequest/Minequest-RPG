package com.theminequest.MQCoreRPG.entity.network;

import java.net.Socket;

/**
 *
 * Borrowed from Citizens ;)
 * @author fullwall
 */
public class EmptySocket extends Socket
{

    @Override
    public java.io.InputStream getInputStream()
    {
        return new java.io.ByteArrayInputStream(EMPTY);
    }

    @Override
    public java.io.OutputStream getOutputStream()
    {
        return new java.io.ByteArrayOutputStream(1);
    }
    private final byte[] EMPTY = new byte[0];
}
