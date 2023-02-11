package com.zuehlke.securesoftwaredevelopment.service;

import com.zuehlke.securesoftwaredevelopment.domain.Permission;
import com.zuehlke.securesoftwaredevelopment.domain.Role;
import com.zuehlke.securesoftwaredevelopment.repository.PermissionRepository;
import com.zuehlke.securesoftwaredevelopment.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionService.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public PermissionService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> get(int userId) {
        List<Permission> userPermissions = new ArrayList<>();
        if(userId <= 0){
            LOG.error("ID korisnika mora biti veci od 0!");
            return userPermissions;
        }else{
            List<Role> roles = roleRepository.findByUserId(userId);
            if(roles == null){
                LOG.error("Korisniku nije dodeljena nijedna rola!");
                return userPermissions;
            }else{
                for (Role role : roles) {
                    List<Permission> rolePermissions = permissionRepository.findByRoleId(role.getId());
                    userPermissions.addAll(rolePermissions);
                }
                return userPermissions;
            }
        }
    }
}
