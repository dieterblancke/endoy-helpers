package dev.endoy.helpers.spring.subdomain;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;

public class SubdomainWebMvcRegistrations implements WebMvcRegistrations
{

    @Override
    public SubdomainRequestMappingHandlerMapping getRequestMappingHandlerMapping()
    {
        return new SubdomainRequestMappingHandlerMapping();
    }
}
