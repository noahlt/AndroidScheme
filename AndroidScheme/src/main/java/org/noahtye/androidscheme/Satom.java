package org.noahtye.androidscheme;

public class Satom extends Sexpr {
    public static Satom parseToken(String atomToken) {
        try {
            return new Sint(Integer.parseInt(atomToken));
        } catch (NumberFormatException e1) {
            try {
                return new Sfloat(Float.parseFloat(atomToken));
            } catch (NumberFormatException e2) {
                return new Symbol(atomToken);
            }
        }
    }
}
