package dev.asterix.equipcore_api.security;

import dev.asterix.equipcore_api.enumeration.UserRole;
import dev.asterix.equipcore_api.model.User;
import dev.asterix.equipcore_api.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserDetailsServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    private User user;

    @BeforeEach
    public void setUp() {

        user = User
                .builder()
                .firstName("Amine")
                .lastName("CHAKHAR")
                .email("amine.chakhar@gmail.com")
                .password("$2a$12$85NwZwUWcg9ve9Ry7MCmoefueK0fNTGyD2n5W7RdmAGqbGA9YoiU2")
                .role(UserRole.ADMIN)
                .isEnabled(true)
                .build();

        userRepository.save(user);
    }

    @Test
    public void loadUserByUsername_userExists() {

        UserDetails result = userDetailsServiceImpl.loadUserByUsername(user.getEmail());

        Assertions.assertEquals(user.getEmail(), result.getUsername());
        Assertions.assertEquals(user.getPassword(), result.getPassword());
        Assertions.assertEquals(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), result.getAuthorities());
        Assertions.assertTrue(result.isEnabled());
    }

    @Test
    public void loadUserByUsername_userNotFound() {

        String email = "amine@gmail.com";

        UsernameNotFoundException exception = Assertions.assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsServiceImpl.loadUserByUsername(email)
        );

        Assertions.assertEquals("User with email : " + email + " not found", exception.getMessage());
    }
}