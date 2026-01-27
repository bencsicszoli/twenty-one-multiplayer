package com.codecool.twentyone.service;

import com.codecool.twentyone.exception_handler.custom_exception.EmailAddressAlreadyExistsException;
import com.codecool.twentyone.exception_handler.custom_exception.NotAllowedOperationException;
import com.codecool.twentyone.exception_handler.custom_exception.PlayerNameAlreadyExistsException;
import com.codecool.twentyone.model.dto.restdto.*;
import com.codecool.twentyone.model.entities.Player;
import com.codecool.twentyone.model.entities.Role;
import com.codecool.twentyone.repository.PlayerRepository;
import com.codecool.twentyone.security.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private PlayerService playerService;

    @Test
    public void createPlayer_shouldRegisterNewPlayer_when_playerNameAndEmailAreNotTaken() {
        RegisterDTO request = new RegisterDTO("John", "secretPassword", "john@john.com");
        when(playerRepository.existsByPlayerName(request.playerName())).thenReturn(false);
        when(playerRepository.existsByEmail(request.email())).thenReturn(false);
        when(encoder.encode(request.password())).thenReturn("encodedPassword");

        Map<String, String> result = playerService.createPlayer(request);

        assertEquals("Player 'John' created successfully", result.get("message"));

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals("John", capturedPlayer.getPlayerName());
        assertEquals("encodedPassword", capturedPlayer.getPassword());
        assertEquals("john@john.com", capturedPlayer.getEmail());
    }

    @Test
    public void createPlayer_shouldThrowPlayerNameAlreadyExistsException_when_playerNameAlreadyExists() {
        RegisterDTO request = new RegisterDTO("John", "secretPassword", "john@john.com");
        when(playerRepository.existsByPlayerName(request.playerName())).thenReturn(true);

        PlayerNameAlreadyExistsException exception = assertThrows(PlayerNameAlreadyExistsException.class, () -> playerService.createPlayer(request));
        assertEquals("Player with name John already exists", exception.getMessage());
    }

    @Test
    public void createPlayer_shouldThrowEmailAlreadyExistsException_when_emailIsAlreadyTaken() {
        RegisterDTO request = new RegisterDTO("John", "secretPassword", "john@john.com");
        when(playerRepository.existsByPlayerName(request.playerName())).thenReturn(false);
        when(playerRepository.existsByEmail(request.email())).thenReturn(true);

        EmailAddressAlreadyExistsException exception = assertThrows(EmailAddressAlreadyExistsException.class, () -> playerService.createPlayer(request));
        assertEquals("Email address already exists: john@john.com", exception.getMessage());
    }

    @Test
    public void loginPlayer_shouldReturnLoginDTO_when_credentialsAreValid() {
        LoginDTO request = new LoginDTO("John", "secretPassword");
        User userDetails = new User("John", "secretPassword", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fakeToken");

        JwtResponseDTO result = playerService.loginPlayer(request);

        assertEquals(new JwtResponseDTO("fakeToken", "John", List.of("ROLE_USER")), result);
    }

    @Test
    public void getMe_shouldReturnPlayerDTO_when_playerExists() {
        User springUser = new User("John", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Player player = new Player();
        player.setPlayerName("John");
        player.setGames(10);
        player.setWins(7);
        player.setLosses(3);
        player.setBalance(93);
        when(playerRepository.findByPlayerName(springUser.getUsername())).thenReturn(Optional.of(player));

        PlayerDTO result = playerService.getMe();

        assertEquals(new PlayerDTO(player.getPlayerName(), player.getGames(), player.getWins(), player.getLosses(), player.getBalance()), result);
    }

    @Test
    public void getMe_shouldThrowNoSuchElementException_when_playerDoesNotExist() {
        User springUser = new User("John", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(playerRepository.findByPlayerName(springUser.getUsername())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> playerService.getMe());
        assertEquals("Player 'John' not found", exception.getMessage());
    }

    @Test
    public void deleteMe_shouldDeletePlayerAndCreateResponse_when_playerExists() {
        User springUser = new User("John", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Player player = new Player();
        player.setBalance(37);
        player.setGames(10);
        when(playerRepository.findByPlayerName(springUser.getUsername())).thenReturn(Optional.of(player));

        Map<String, String> result = playerService.deleteMe();

        assertEquals("Player 'John' has been deleted", result.get("message"));
        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).delete(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals(37, capturedPlayer.getBalance());
        assertEquals(10, capturedPlayer.getGames());
    }

    @Test
    public void editCredentials_shouldReturnResponse_when_allCredentialsHaveChangedAndPasswordIsValid() {
        EditCredentialsDTO request = new EditCredentialsDTO("Johnny", "johnny@johnny.com", "oldPassword", "newPassword");
        User springUser = new User("John", "oldPassword", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Player player = new Player();
        player.setPassword("encodedPassword");
        when(playerRepository.findByPlayerName(springUser.getUsername())).thenReturn(Optional.of(player));
        when(playerRepository.existsByPlayerName(request.playerName())).thenReturn(false);
        when(encoder.matches(request.password(), player.getPassword())).thenReturn(true);
        when(encoder.encode(request.newPassword())).thenReturn("encodedNewPassword");

        Map<String, String> result = playerService.editCredentials(request);

        assertEquals("User 'John' edited successfully. The new username is 'Johnny'.", result.get("message"));
        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals("encodedNewPassword", capturedPlayer.getPassword());
        assertEquals("Johnny", capturedPlayer.getPlayerName());
        assertEquals("johnny@johnny.com", capturedPlayer.getEmail());
    }

    @Test
    public void editCredentials_shouldReturnResponse_when_usernameDoesNotChangedAndPasswordIsValid() {
        EditCredentialsDTO request = new EditCredentialsDTO("John", "johnny@johnny.com", "oldPassword", "newPassword");
        User springUser = new User("John", "oldPassword", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Player player = new Player();
        player.setPassword("encodedPassword");
        when(playerRepository.findByPlayerName(springUser.getUsername())).thenReturn(Optional.of(player));
        when(encoder.matches(request.password(), player.getPassword())).thenReturn(true);
        when(encoder.encode(request.newPassword())).thenReturn("encodedNewPassword");

        Map<String, String> result = playerService.editCredentials(request);

        assertEquals("User 'John' edited successfully", result.get("message"));
        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals("encodedNewPassword", capturedPlayer.getPassword());
        assertEquals("johnny@johnny.com", capturedPlayer.getEmail());
    }

    @Test
    public void editCredentials_shouldReturnResponse_when_emailAndPasswordDoNotChange() {
        EditCredentialsDTO request = new EditCredentialsDTO("Johnny", "", "password", "");
        User springUser = new User("John", "oldPassword", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Player player = new Player();
        player.setPassword("encodedPassword");
        when(playerRepository.findByPlayerName(springUser.getUsername())).thenReturn(Optional.of(player));
        when(playerRepository.existsByPlayerName(request.playerName())).thenReturn(false);
        when(encoder.matches(request.password(), player.getPassword())).thenReturn(true);

        Map<String, String> result = playerService.editCredentials(request);

        assertEquals("User 'John' edited successfully. The new username is 'Johnny'.", result.get("message"));
        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals("Johnny", capturedPlayer.getPlayerName());
    }

    @Test
    public void editCredentials_shouldThrowPlayerNameAlreadyExistsException_when_newUsernameIsAlreadyExists() {
        EditCredentialsDTO request = new EditCredentialsDTO("Johnny", "johnny@johnny.com", "oldPassword", "newPassword");
        User springUser = new User("John", "oldPassword", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Player player = new Player();
        when(playerRepository.findByPlayerName(springUser.getUsername())).thenReturn(Optional.of(player));
        when(playerRepository.existsByPlayerName(request.playerName())).thenReturn(true);

        PlayerNameAlreadyExistsException exception = assertThrows(PlayerNameAlreadyExistsException.class, () -> playerService.editCredentials(request));
        assertEquals("Player with name Johnny already exists", exception.getMessage());
    }

    @Test
    public void editCredentials_shouldThrowNotAllowedOperationException_when_passwordIsNotValid() {
        EditCredentialsDTO request = new EditCredentialsDTO("Johnny", "johnny@johnny.com", "invalidPassword", "newPassword");
        User springUser = new User("John", "invalidPassword", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Player player = new Player();
        player.setPassword("encodedPassword");
        when(playerRepository.findByPlayerName(springUser.getUsername())).thenReturn(Optional.of(player));
        when(playerRepository.existsByPlayerName(request.playerName())).thenReturn(false);
        when(encoder.matches(request.password(), player.getPassword())).thenReturn(false);

        NotAllowedOperationException exception = assertThrows(NotAllowedOperationException.class, () -> playerService.editCredentials(request));
        assertEquals("Invalid password for user 'John'", exception.getMessage());
    }
}
