package org.noahtye.androidscheme;

public abstract class Proc extends Sexpr {
    public abstract Sexpr apply(Slist args, Env env, Scheme scheme);
    public String toString() {
        return "<proc>";
    }
}

