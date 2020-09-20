package com.mgrin.thau.sessions.authDto;

public class GoogleAuthDTO {
    private String code;
    private String redirectURI;

    public String getCode() {
        return code;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }
}