package com.dobrosav.matches.api.model.res;

import java.io.Serializable;

public class SuccessResult implements Serializable {
    private Boolean result;

    public SuccessResult() {
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "SuccessResult{" +
                "result=" + result +
                '}';
    }
}
