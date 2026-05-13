package dev.endoy.helpers.spring.subdomain;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SubdomainAutoConfiguration
{

    @Bean
    public SubdomainWebMvcRegistrations subdomainWebMvcRegistrations()
    {
        return new SubdomainWebMvcRegistrations();
    }
}
