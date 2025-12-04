package co.istad.authserver.init;

import co.istad.authserver.domain.Role;
import co.istad.authserver.feature.client.JpaRegisteredClientRepository;
import co.istad.authserver.feature.role.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitDefault {

    private final RoleRepository roleRepository;
    private final JpaRegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        Logger log = LoggerFactory.getLogger(InitDefault.class);

        // Initialize Role
        if (roleRepository.count() == 0) {
            Role role = Role.builder()
                    .uuid(UUID.randomUUID().toString())
                    .role("USER")
                    .build();
            roleRepository.save(role);
            log.info("INITIALIZED ROLE : {}", role);
        }

        // Initialize Client Credentials Client
        if (registeredClientRepository.findByClientId("oidc-client") == null) {
            TokenSettings ccTokenSettings = TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(30))
                    .build();

            RegisteredClient clientCredentialsClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("oidc-client")
                    .clientSecret(passwordEncoder.encode("secret"))
                    .clientName("Default OIDC Client")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .tokenSettings(ccTokenSettings)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .build())
                    .build();

            registeredClientRepository.save(clientCredentialsClient);
            log.info("INITIALIZED CLIENT CREDENTIALS CLIENT: {}", clientCredentialsClient.getClientId());
        }

        if (registeredClientRepository.findByClientId("pkce-client") == null) {
            TokenSettings pkceTokenSettings = TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(30))
                    .refreshTokenTimeToLive(Duration.ofDays(3))
                    .reuseRefreshTokens(false)
                    .build();

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
                            .build())
                    .tokenSettings(pkceTokenSettings)
                    .build();

            registeredClientRepository.save(pkceClient);
            log.info("INITIALIZED PKCE CLIENT: {}", pkceClient.getClientId());
        }
    }
}
