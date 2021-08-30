package be.nfm.rs2.packets;

import be.nfm.rs2.client.ClientEvent;
import be.nfm.rs2.client.ClientState;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * @author Musa Kapan
 */
@Component
public class PacketDecoderService {

    private final SecureRandom rng;

    public PacketDecoderService(SecureRandom rng) {
        this.rng = rng;
    }

    public ClientEvent decodePacket(ClientState state, ByteBuffer buffer) {
        return null;
    }

}
