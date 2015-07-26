package com.quantasnet.gitserver.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "role")
public class Role implements GrantedAuthority, Serializable {

    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";

    private static final long serialVersionUID = 8361429697887374812L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "role_name", unique = true)
    private String roleName;

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String getAuthority() {
        return getRoleName();
    }
}
