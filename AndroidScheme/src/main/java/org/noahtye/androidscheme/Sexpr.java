package org.noahtye.androidscheme;

public class Sexpr {
    public String parseError = null;
    public String evalError = null;
    public Sexpr parseError(String errorMessage) {
        parseError = errorMessage;
        return this;
    }
    public Sexpr evalError(String errorMessage) {
        evalError = errorMessage;
        return this;
    }
    public String toString() {
        if (parseError != null) {
            return parseError;
        } else if (evalError != null) {
            return evalError;
        } else {
            return "what a boring sexpr";
        }
    }
}
