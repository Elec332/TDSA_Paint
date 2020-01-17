package nl.elec332.nlda.tdsa.paint.network.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import nl.elec332.lib.java.network.MulticastHelper;
import nl.elec332.lib.netty.IDefaultStartable;
import nl.elec332.lib.netty.packet.DefaultChannelInitializer;
import nl.elec332.lib.netty.packet.IPacket;
import nl.elec332.nlda.tdsa.paint.mode.ServerProgramMode;
import nl.elec332.nlda.tdsa.paint.network.EnumPackets;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class ServerNetworkHandler implements IDefaultStartable<Object> {

    public ServerNetworkHandler(int port, String name, ServerProgramMode.SharedLayer mode) {
        this.port = port;
        this.inboundHandler = new ServerInboundHandler(mode);
        this.name = name;
        this.uuid = mode.getLayerId();
    }

    public static final String ident = "ArnieServerIdent-";

    private final String name;
    private final UUID uuid;
    private final ServerInboundHandler inboundHandler;
    private final int port;

    private boolean stopping;
    private EventLoopGroup bossGroup, workerGroup;

    public Collection<ServerPlayer> getPlayers() {
        return inboundHandler.players;
    }

    @Override
    public void start() throws Exception {
        MulticastHelper.startMulticastSender(4263, (ident + name + "_" + port + "_" + uuid.toString()).getBytes(), () -> !stopping);
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new DefaultChannelInitializer(this));
            b.bind(port).sync().channel().closeFuture().sync();
        } finally {
            closeServer();
        }
    }

    @Override
    public IPacket<?> createPacketFromType(int i) {
        try {
            return EnumPackets.getPacket(i);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPacketType(IPacket<?> iPacket) {
        return EnumPackets.getType(iPacket);
    }

    @Override
    public Function<ChannelHandlerContext, Object> getNetworkHandlerFactory() {
        return null;
    }

    @Override
    public SimpleChannelInboundHandler<?> createInboundHandler(DefaultChannelInitializer packetInitializer) {
        return inboundHandler;
    }

    public void closeServer() {
        stopping = true;
        for (ServerPlayer player : inboundHandler.players) {
            player.getChannel().flush().disconnect().channel().close();
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

}
