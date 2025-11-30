package by.yayauheny.tutochkatgbot.config;

import org.koin.core.Koin;
import org.koin.core.context.GlobalContext;
import org.koin.core.context.startKoin;
import org.koin.core.module.Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yayauheny.by.di.databaseConfigModule;
import yayauheny.by.di.serviceModule;
import yayauheny.by.service.RestroomService;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for backend services integration.
 * Bridges Koin (backend DI) with Spring (bot DI).
 */
@Configuration
public class BackendServiceConfig {

    @Bean
    public RestroomService restroomService() {
        // Initialize Koin if not already initialized
        Koin koinInstance = GlobalContext.INSTANCE.getOrNull();
        if (koinInstance == null) {
            List<Module> modules = Arrays.asList(
                databaseConfigModule,
                serviceModule
            );
            startKoin(modules);
        }
        
        // Get service from Koin
        Koin koin = GlobalContext.INSTANCE.get();
        return koin.get(RestroomService.class);
    }
}
