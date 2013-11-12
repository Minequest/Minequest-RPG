package com.theminequest.MQCoreRPG.entity.network;

import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;
import net.minecraft.server.v1_6_R2.Connection;
import net.minecraft.server.v1_6_R2.IConsoleLogManager;
import net.minecraft.server.v1_6_R2.NetworkManager;
import net.minecraft.server.v1_6_R2.Packet;

/**
 *
 * Borrowed from Citizens ;)
 * @author fullwall
 */
public class EmptyNetworkManager extends NetworkManager
{

    public EmptyNetworkManager(IConsoleLogManager logManager, Socket socket, String string, Connection conn, PrivateKey key) throws IOException
    {
        super(logManager, socket, string, conn, key);
        try
        {
            java.lang.reflect.Field nm = NetworkManager.class.getDeclaredField("n");
            nm.setAccessible(true);
            nm.set(this, false);
        } catch (Exception ex)
        {
            // Swallow the exception
        }
    }

    @Override
    public void a()
    {
    }

    @Override
    public void a(Connection conn)
    {
    }

    @Override
    public void a(String s, Object... objects)
    {
    }

    @Override
    public void b()
    {
    }

    @Override
    public void d()
    {
    }

    @Override
    public int e()
    {
        return 0;
    }

    @Override
    public void queue(Packet packet)
    {
    }
}
