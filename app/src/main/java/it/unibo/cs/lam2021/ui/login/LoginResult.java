package it.unibo.cs.lam2021.ui.login;

class LoginResult {
    private final boolean result;
    private String message;

    LoginResult() {
        this.result = true;
    }

    LoginResult(String msg) {
        this.result = false;
        this.message = msg;
    }

    public boolean getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}