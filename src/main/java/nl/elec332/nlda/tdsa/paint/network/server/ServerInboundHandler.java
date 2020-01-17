package nl.elec332.nlda.tdsa.paint.network.server;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import nl.elec332.lib.netty.packet.IPacket;
import nl.elec332.nlda.tdsa.paint.mode.ServerProgramMode;
import nl.elec332.nlda.tdsa.paint.network.packets.SPacketHandshake;
import nl.elec332.nlda.tdsa.paint.network.packets.SPacketSetLayer;
import nl.elec332.nlda.tdsa.paint.network.packets.SPacketUpdateView;

import java.util.List;

/**
 * Created by Elec332 on 15-1-2020.
 */
@ChannelHandler.Sharable
public class ServerInboundHandler extends SimpleChannelInboundHandler<IPacket<ServerPlayer>> {

    ServerInboundHandler(ServerProgramMode.SharedLayer server) {
        this.server = server;
        this.players = Lists.newArrayList();
    }

    private final ServerProgramMode.SharedLayer server;
    final List<ServerPlayer> players;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ServerPlayer player = new ServerPlayer((SocketChannel) ctx.channel(), server);
        player.sendPacket(new SPacketHandshake());
        player.sendPacket(new SPacketSetLayer(server.getLayerId(), server.getServerName()));
        players.add(player);
        player.sendPacket(new SPacketUpdateView(server.getPaintComponents()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        for (ServerPlayer p : players) {
            if (p.getChannel().equals(channel)) {
                players.remove(p);
                return;
            }
        }
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPacket<ServerPlayer> msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        for (ServerPlayer p : players) {
            if (p.getChannel().equals(channel)) {
                msg.processPacket(p);
                return;
            }
        }
        throw new RuntimeException();
    }

}
