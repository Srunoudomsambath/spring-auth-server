package co.istad.authserver.init;

import co.istad.authserver.feature.client.JpaRegisteredClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class ClientInitializer implements CommandLineRunner {

    private final JpaRegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientInitializer(JpaRegisteredClientRepository registeredClientRepository,
                             PasswordEncoder passwordEncoder) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // Check if client already exists
        if (registeredClientRepository.findByClientId("pkce-client") != null) return;

        RegisteredClient pkceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("pkce-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // PKCE, no secret
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:3000/api/auth/callback/spring-oauth")
                .scope("openid")
                .scope("email")
                .scope("profile")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true) // PKCE required
                        .requireAuthorizationConsent(true)
                        .build()
                )
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(3))
                        .reuseRefreshTokens(false) // one-time use
                        .build()
                )
                .build();

        registeredClientRepository.save(pkceClient);
    }
}
