package com.codecool.twentyone.service;

import com.codecool.twentyone.exception_handler.custom_exception.EmailAddressAlreadyExistsException;
import com.codecool.twentyone.exception_handler.custom_exception.NotAllowedOperationException;
import com.codecool.twentyone.exception_handler.custom_exception.PlayerNameAlreadyExistsException;
import com.codecool.twentyone.model.dto.restdto.*;
import com.codecool.twentyone.model.entities.Player;
import com.codecool.twentyone.model.entities.Role;
import com.codecool.twentyone.repository.PlayerRepository;
import com.codecool.twentyone.security.jwt.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public PlayerService(PlayerRepository playerRepository, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.playerRepository = playerRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public Map<String, String> createPlayer(RegisterDTO request) {
        if (playerRepository.existsByPlayerName(request.playerName())) {
            throw new PlayerNameAlreadyExistsException(request.playerName());
        }
        if (playerRepository.existsByEmail(request.email())) {
            throw new EmailAddressAlreadyExistsException(request.email());
        }
        Player player = new Player();
        player.setPlayerName(request.playerName());
        player.setPassword(encoder.encode(request.password()));
        player.setEmail(request.email());
        player.setRoles(EnumSet.of(Role.ROLE_USER));
        playerRepository.save(player);
        Map<String, String> result = new HashMap<>();
        result.put("message", String.format("Player '%s' created successfully", request.playerName()));
        return result;
    }

    public JwtResponseDTO loginPlayer(LoginDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.playerName(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return new JwtResponseDTO(jwt, userDetails.getUsername(), roles);
    }

    public PlayerDTO getMe() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Player currentPlayer = playerRepository.findByPlayerName(user.getUsername())
                .orElseThrow(() -> new NoSuchElementException(String.format("Player '%s' not found", user.getUsername())));
        return new PlayerDTO(currentPlayer.getPlayerName(), currentPlayer.getGames(), currentPlayer.getWins(), currentPlayer.getLosses(), currentPlayer.getBalance());
    }

    public Map<String, String> deleteMe() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Player currentPlayer = playerRepository.findByPlayerName(user.getUsername())
                .orElseThrow(() -> new NoSuchElementException(String.format("User '%s' not found", user.getUsername())));
        playerRepository.delete(currentPlayer);
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Player '%s' has been deleted", user.getUsername()));
        return response;
    }

    public Map<String, String> editCredentials(EditCredentialsDTO request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Player currentPlayer = playerRepository.findByPlayerName(user.getUsername()).orElseThrow(() -> new NoSuchElementException(String.format("User '%s' not found", user.getUsername())));
        Map<String, String> result = new HashMap<>();
        if (!request.playerName().equals(user.getUsername()) && playerRepository.existsByPlayerName(request.playerName())) {
            throw new PlayerNameAlreadyExistsException(request.playerName());
        }
        if(!encoder.matches(request.password(), currentPlayer.getPassword())) {
            throw new NotAllowedOperationException(String.format("Invalid password for user '%s'", user.getUsername()));
        }
        currentPlayer.setPlayerName(request.playerName());
        if (!request.newPassword().isEmpty()) {
            currentPlayer.setPassword(encoder.encode(request.newPassword()));
        }
        if (!request.email().isEmpty()) {
            currentPlayer.setEmail(request.email());
        }
        playerRepository.save(currentPlayer);

        if (request.playerName().equals(user.getUsername())) {
            result.put("message",String.format("User '%s' edited successfully", user.getUsername()));
        } else {
            result.put("message", String.format("User '%s' edited successfully. The new username is '%s'.", user.getUsername(), request.playerName()));
        }
        return result;
    }
}
