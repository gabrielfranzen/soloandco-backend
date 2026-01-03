package model.dto;

public class RefreshRequest {
    
    private String refreshToken;

    public RefreshRequest() {
        // Construtor padrão necessário para JSON Binding
    }

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
