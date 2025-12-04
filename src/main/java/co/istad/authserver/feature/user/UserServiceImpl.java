package co.istad.authserver.feature.user;

import co.istad.authserver.domain.Role;
import co.istad.authserver.domain.User;
import co.istad.authserver.feature.role.RoleRepository;
import co.istad.authserver.feature.user.dto.UserRequest;
import co.istad.authserver.feature.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if(userRepository.existsByUsername(userRequest.username()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        if(userRepository.existsByEmail(userRequest.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        List<Role> roles = new ArrayList<>();

        userRequest.roles().forEach(role -> {
            roles.add(
                    roleRepository.findByRole(role).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found")
                    )
            );
        });

        User user = User.builder()
                .uuid(UUID.randomUUID().toString())
                .password(passwordEncoder.encode(userRequest.password()))
                .email(userRequest.email())
                .username(userRequest.username())
                .familyName(userRequest.familyName())
                .givenName(userRequest.givenName())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(false)
                .roles(roles)
                .build();


        user = userRepository.save(user);

        return new UserResponse(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.getFamilyName(),
                user.getGivenName(),
                user.getEnabled(),
                user.getRoles().stream().map(Role::getRole).toList()
        );
    }

    @Override
    public void disableUser(String uuid) {
        User user = userRepository.findByUuid(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void enableUser(String uuid) {
        User user = userRepository.findByUuid(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );
        user.setEnabled(true);
        userRepository.save(user);
    }
}
