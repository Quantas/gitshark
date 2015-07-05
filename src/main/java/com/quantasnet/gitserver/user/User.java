package com.quantasnet.gitserver.user;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class User implements UserDetails, CredentialsContainer, Serializable {

    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=\\S+$).{8,255}$";

    private static final long serialVersionUID = 5923607403864940973L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true, length = 255)
    private String userName;

    @Column(name = "user_password", nullable = true, length = 255)
    private String password;

    @Column(name = "user_first_name", nullable = true)
    private String firstName;

    @Column(name = "user_last_name", nullable = true)
    private String lastName;

    @Column(name = "user_email", nullable = true, unique = true)
    private String email;

    @Column(name = "user_image_url", length = 255)
    private String imageUrl;

    @Column(name = "user_active", nullable = false)
    private boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;
    
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    // UserDetails methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public String getUsername() {
        return getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }

    public boolean isAdmin() {
        if (null != roles && !roles.isEmpty()) {
            for (final Role role : roles) {
                if (role.getRoleName().equals(Role.ADMIN)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().isAssignableFrom(this.getClass())) {
            return ((User) obj).getUserName().equals(this.userName);
        } else {
            return false;
        }
    }

}
