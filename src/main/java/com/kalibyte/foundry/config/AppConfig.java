package com.kalibyte.foundry.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppConfig {

    /**
     * The state where the company is located. 
     * Used for GST calculation (CGST/SGST vs IGST).
     */
    private String companyState = "Maharashtra";

    private DefaultAdmin defaultAdmin;

    @Getter
    @Setter
    public static class DefaultAdmin {
        private String email;
        private String password;
    }
}
