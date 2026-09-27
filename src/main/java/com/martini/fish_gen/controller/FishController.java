package com.martini.fish_gen.controller;

import com.martini.fish_gen.model.dto.CreateFishesFromPromptDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fishes")
public class FishController {

    @PostMapping("/with-prompt")
    public void createWithPrompt(@RequestBody CreateFishesFromPromptDto dto) {
        
    }
}
