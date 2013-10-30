package org.noahtye.androidscheme;

import java.util.HashMap;

public class Env {
    private HashMap<String, Sexpr> envHash;
    private Env parent;
    public Env() {
        envHash = new HashMap<String, Sexpr>();
    }
    public Env(Env parent) {
        this();
        this.parent = parent;
    }
    public Sexpr get(String key) {
        if (envHash.containsKey(key)) {
            return envHash.get(key);
        } else if (parent != null) {
            return parent.get(key);
        }
        return null;
    }
    public void set(String key, Sexpr val) {
        envHash.put(key, val);
    }
    public void set(Symbol key, Sexpr val) {
        envHash.put(key.get(), val);
    }
}

