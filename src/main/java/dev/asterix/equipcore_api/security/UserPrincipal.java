package dev.asterix.equipcore_api.security;

import dev.asterix.equipcore_api.model.User;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    public @NonNull String getPassword() {

        return user.getPassword();
    }

    @Override
    public @NonNull String getUsername() {

        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {

        return user.isEnabled();
    }
}
