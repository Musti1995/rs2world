package be.nfm.rs2.config;

import be.nfm.rs2.client.Client;
import be.nfm.rs2.util.ArrayWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

/**
 * @author Musa Kapan
 */

@Configuration
public class BeanConfig {

    @Bean
    public SecureRandom rng() {
        return new SecureRandom();
    }

    @Bean
    public ArrayWrapper<Client> activeClients(@Value("${${rs2.client.capacity}}") int capacity) {
        return ArrayWrapper.wrap(new Client[capacity]);
    }
}
