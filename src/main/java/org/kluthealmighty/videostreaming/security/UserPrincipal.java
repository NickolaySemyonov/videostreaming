package org.kluthealmighty.videostreaming.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private final Long userId;
    private final String email;
    private final String password;
    private final String channelTag;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String email, String password, String channelTag) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.channelTag = channelTag;
        this.authorities = List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getUserId(){return userId;}

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getChannelTag() {
        return channelTag;
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
        return true;
    }
}
