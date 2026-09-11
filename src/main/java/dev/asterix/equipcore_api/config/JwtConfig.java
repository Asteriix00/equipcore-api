package dev.asterix.equipcore_api.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "application.security.jwt")
@Validated
@Getter
@Setter
public class JwtConfig {

    @NotBlank(message = "JWT secret is required")
    private String secretKey;

    @Min(value = 60, message = "JWT expiration must be at least 1 minute")
    private long expiration;

}
