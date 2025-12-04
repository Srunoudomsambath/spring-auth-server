package co.istad.authserver.feature.role;

import co.istad.authserver.feature.role.dto.RoleRequest;
import co.istad.authserver.feature.role.dto.RoleResponse;

public interface RoleService {

    RoleResponse createRole(RoleRequest roleRequest);
}
