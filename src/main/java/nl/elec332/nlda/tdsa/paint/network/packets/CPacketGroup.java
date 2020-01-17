package nl.elec332.nlda.tdsa.paint.network.packets;

import com.google.common.collect.Lists;
import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.netty.packet.AbstractDataPacket;
import nl.elec332.nlda.tdsa.paint.network.server.ServerPlayer;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by Elec332 on 15-1-2020
 */
public class CPacketGroup extends AbstractDataPacket<ServerPlayer> {

    public CPacketGroup(UUID newUuid, Collection<UUID> objects) {
        this.newUuid = newUuid;
        this.objects = objects;
    }

    public CPacketGroup() {
        objects = Lists.newArrayList();
    }

    private UUID newUuid;
    private Collection<UUID> objects;

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        stream.writeUUID(newUuid);
        stream.writeInt(objects.size());
        for (UUID id : objects) {
            stream.writeUUID(id);
        }
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        newUuid = stream.readUUID();
        int loop = stream.readInt();
        for (int i = 0; i < loop; i++) {
            objects.add(stream.readUUID());
        }
    }

    @Override
    public void processPacket(ServerPlayer handler) {
        handler.group(objects, newUuid);
    }

}
