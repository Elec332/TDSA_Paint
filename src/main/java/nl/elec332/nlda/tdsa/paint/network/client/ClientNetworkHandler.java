package nl.elec332.nlda.tdsa.paint.network.client;

import com.google.common.base.Strings;
import io.netty.channel.ChannelHandlerContext;
import nl.elec332.lib.netty.IDefaultStartable;
import nl.elec332.lib.netty.NettyReceiverThread;
import nl.elec332.lib.netty.packet.IPacket;
import nl.elec332.nlda.tdsa.paint.mode.ClientProgramMode;
import nl.elec332.nlda.tdsa.paint.network.EnumPackets;
import nl.elec332.nlda.tdsa.paint.util.ThreadedPopupMessage;

import javax.swing.*;
import java.util.function.Function;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class ClientNetworkHandler implements IDefaultStartable<ClientPacketHandler> {

    public ClientNetworkHandler(ClientProgramMode mode, String ip, int port) {
        this.client = mode;
        this.ip = ip;
        this.port = port;
    }

    private final ClientProgramMode client;
    private final String ip;
    private final int port;

    private boolean connected, shutdown;
    private NettyReceiverThread thread;
    private ClientPacketHandler packetHandler;

    @Override
    public void start() {
        if (thread != null) {
            return;
        }
        thread = new NettyReceiverThread(ip, port, this);
        thread.setName("BKE Client Receiver");
        if (connected) {
            throw new IllegalStateException();
        }
        thread.start();
        ThreadedPopupMessage.showMessage(client.getWindow().getWindow(), "Conecting...", consumer -> {
            while (!connected && !shutdown) {
                Thread.sleep(0);
            }
            shutdown = false;
        });
        if (!connected) {
            throw new RuntimeException();
        }
    }

    @Override
    public Function<ChannelHandlerContext, ClientPacketHandler> getNetworkHandlerFactory() {
        return channelHandlerContext -> new ClientPacketHandler(channelHandlerContext, client);
    }

    @Override
    public void setNetworkHandler(ClientPacketHandler networkHandler) {
        this.packetHandler = networkHandler;
    }

    @Override
    public IPacket<?> createPacketFromType(int type) {
        try {
            return EnumPackets.getPacket(type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPacketType(IPacket<?> packet) {
        return EnumPackets.getType(packet);
    }

    @Override
    public void onNettyThreadConnected() {
        connected = true;
    }

    @Override
    public void onNettyExceptionCaught(Exception e) {
        shutdown = true;
        JOptionPane.showMessageDialog(client.getWindow().getWindow(), "Caught exception: " + e.getMessage());
    }

    @Override
    public void onNettyThreadShutdown() {
        connected = false;
        shutdown = true;
        this.thread = null;
        packetHandler.clear();
        packetHandler.onDisconnected();
        client.removeConnection(this);
    }

    public void disconnect() {
        packetHandler.disconnect();
    }

    @Override
    public String toString() {
        String s = packetHandler == null ? null : packetHandler.toString();
        return Strings.isNullOrEmpty(s) ? super.toString() : s;
    }

}
