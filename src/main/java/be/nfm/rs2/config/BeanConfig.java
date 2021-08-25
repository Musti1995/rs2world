package be.nfm.rs2.config;

import be.nfm.rs2.server.Server;
import be.nfm.rs2.server.ServerContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Musa Kapan
 */

@Configuration
public class BeanConfig {

    @Bean
    public ServerContext context(Server server) {
        return server.context();
    }

}
