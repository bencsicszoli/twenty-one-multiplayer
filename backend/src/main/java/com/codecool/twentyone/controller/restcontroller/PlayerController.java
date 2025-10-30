package com.codecool.twentyone.controller.restcontroller;

import com.codecool.twentyone.model.dto.*;
import com.codecool.twentyone.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String , String>> addNewPlayer(@RequestBody RegisterDTO request) {
        Map<String , String> response = playerService.createPlayer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public JwtResponseDTO authenticateUser(@RequestBody LoginDTO request) {
        return playerService.loginPlayer(request);
    }

    @GetMapping("/me")
    public PlayerDTO me() {
        return playerService.getMe();
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Map<String, String> deleteMe() {
        return playerService.deleteMe();
    }

    @PatchMapping("/me")
    public Map<String, String> editCredentials(@RequestBody EditCredentialsDTO request) {
        return playerService.editCredentials(request);
    }
}
