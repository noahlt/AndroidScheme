package org.noahtye.androidscheme;

public interface Scheme {
    public Sexpr eval(Sexpr sexpr, Env env);
}
