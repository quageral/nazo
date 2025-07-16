package com.nazo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {

    @GetMapping("/nazo/tetris")
    public String tetris() {
        return "tetris";
    }
}