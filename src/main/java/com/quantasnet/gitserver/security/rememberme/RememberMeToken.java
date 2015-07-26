package com.quantasnet.gitserver.security.rememberme;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "remember_me_token")
public class RememberMeToken implements Serializable {

    private static final long serialVersionUID = -5228741667258598180L;

    @Id
    @Column(name = "series")
    private String series;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "last_used", nullable = false)
    private Date lastUsed;

    public String getSeries() {
        return series;
    }

    public void setSeries(final String series) {
        this.series = series;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(final Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}
