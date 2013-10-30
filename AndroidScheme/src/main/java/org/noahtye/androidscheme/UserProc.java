package org.noahtye.androidscheme;

public class UserProc extends Proc {
    private Sexpr src;
    private Slist argNames;
    public UserProc(Slist argNames, Sexpr src) {
        this.src = src;
        this.argNames = argNames;
    }
    public Sexpr apply(Slist args, Env enclosingEnv, Scheme scheme) {
        if (args.size() == argNames.size()) {
            Env env = new Env(enclosingEnv);
            for (int i = 0; i < argNames.size(); i++) {
                env.set((Symbol) argNames.get(i), args.get(i));
            }
            return scheme.eval(src, env);
        } else {
            return new Sexpr().evalError("function expects " + (argNames.size()-1) + " arguments; received " + (args.size() - 1));
        }
    }
}

