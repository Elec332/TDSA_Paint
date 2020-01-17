package nl.elec332.nlda.tdsa.paint.network;

import nl.elec332.lib.netty.packet.IPacket;
import nl.elec332.nlda.tdsa.paint.network.packets.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Elec332 on 15-1-2020.
 */
public enum EnumPackets {

    HANDSHAKE(new SPacketHandshake()),
    SET_LAYER(new SPacketSetLayer()),
    UPDATE_IMAGE(new SPacketUpdateView()),
    EDIT_PROPERTIES(new CPacketFigureProperties()),
    UNGROUP(new CPacketUngroup()),
    GROUP(new CPacketGroup()),
    ADD_FIGURE(new CPacketAddFigure()),
    REMOVE_FIGURE(new CPacketRemoveFigure()),
    END_CONNECTION(new SPacketEndConnection());

    EnumPackets(IPacket packet) {
        this.packet = packet;
    }

    private final IPacket packet;

    public static IPacket<?> getPacket(int type) throws Exception {
        if (type >= 0 && type < values().length) {
            return values()[type].packet.getClass().newInstance();
        }
        throw new IllegalArgumentException("" + type);
    }

    public static int getType(IPacket packet) {
        if (map.containsKey(packet.getClass())) {
            return map.get(packet.getClass());
        }
        throw new IllegalArgumentException(packet.toString());
    }

    private static final Map<Class<? extends IPacket>, Integer> map;

    static {
        map = new HashMap<>();
        for (EnumPackets pt : values()) {
            map.put(pt.packet.getClass(), pt.ordinal());
        }
    }

}
