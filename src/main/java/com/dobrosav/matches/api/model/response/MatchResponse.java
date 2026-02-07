package com.dobrosav.matches.api.model.response;

import java.io.Serializable;

public class MatchResponse implements Serializable {

    private Integer matchId;
    private UserResponse otherUser;

    public MatchResponse() {
    }

    public MatchResponse(Integer matchId, UserResponse otherUser) {
        this.matchId = matchId;
        this.otherUser = otherUser;
    }

    public Integer getMatchId() {
        return matchId;
    }

    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    public UserResponse getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(UserResponse otherUser) {
        this.otherUser = otherUser;
    }
}
