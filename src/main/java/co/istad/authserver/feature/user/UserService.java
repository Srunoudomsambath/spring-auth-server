package co.istad.authserver.feature.user;

import co.istad.authserver.feature.user.dto.UserRequest;
import co.istad.authserver.feature.user.dto.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);
    void disableUser(String uuid);
    void enableUser(String uuid);

}
