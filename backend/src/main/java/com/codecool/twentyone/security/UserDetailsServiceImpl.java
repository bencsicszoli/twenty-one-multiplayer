package com.codecool.twentyone.security;

import com.codecool.twentyone.model.entities.Player;
import com.codecool.twentyone.model.entities.Role;
import com.codecool.twentyone.repository.PlayerRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PlayerRepository playerRepository;

    public UserDetailsServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String playerName) throws UsernameNotFoundException {
        Player player = playerRepository.findByPlayerName(playerName)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + playerName));

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : player.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.name()));
        }

        return new User(player.getPlayerName(), player.getPassword(), authorities);
    }
}
