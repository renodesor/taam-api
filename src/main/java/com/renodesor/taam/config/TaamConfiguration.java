package com.renodesor.taam.config;

import com.renodesor.taam.entity.TaamUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaamConfiguration {

    @Bean
    public TaamUser taamUser() {
        return new TaamUser();
    }
}
