package com.quantasnet.gitshark.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public long count() {
    	return roleRepository.count();
    }
    
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findUserRole() {
        return roleRepository.findByRoleName(Role.USER);
    }

    public Role findAdminRole() {
        return roleRepository.findByRoleName(Role.ADMIN);
    }

    public Role save(final Role role) {
        return roleRepository.save(role);
    }

    @Transactional
    public Role save(final String roleName) {
        final Role role = new Role();
        role.setRoleName(roleName);
        return roleRepository.save(role);
    }
}
