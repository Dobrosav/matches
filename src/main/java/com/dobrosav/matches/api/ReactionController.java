package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.ReactionRequest;
import com.dobrosav.matches.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reactions")
public class ReactionController {

    private final ReactionService reactionService;

    @Autowired
    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping
    public ResponseEntity<Void> react(@RequestBody ReactionRequest reactionRequest) {
        reactionService.processReaction(
                reactionRequest.getFromUserEmail(),
                reactionRequest.getToUserEmail(),
                reactionRequest.getReaction()
        );
        return ResponseEntity.ok().build();
    }
}
