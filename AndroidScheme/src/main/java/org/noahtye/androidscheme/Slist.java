package org.noahtye.androidscheme;

import java.util.ArrayList;

public class Slist extends Sexpr {
    private ArrayList<Sexpr> list;
    public Slist() {
        list = new ArrayList<Sexpr>();
    }
    public Slist(ArrayList<Sexpr> existingList) {
        list = existingList;
    }
    public void append(Sexpr sexpr) {
        list.add(sexpr);
    }
    public Sexpr head() {
        return list.get(0);
    }
    public Sexpr get(int n) {
        return list.get(n);
    }
    public Slist rest() {
        return new Slist(rawRest());
    }
    public ArrayList<Sexpr> rawRest() {
        return new ArrayList<Sexpr>(list.subList(1, list.size()));
    }
    public ArrayList<Sexpr> raw() {
        return list;
    }
    public int size() {
        return list.size();
    }
    public ArrayList<Sint> allSint() {
        ArrayList<Sint> r = new ArrayList<Sint>(list.size());
        for (Sexpr x : list) {
            if (x instanceof Sint) {
                r.add((Sint) x);
            } else {
                return null;
            }
        }
        return r;
    }
    public ArrayList<Sfloat> allSfloat() {
        ArrayList<Sfloat> r = new ArrayList<Sfloat>(list.size());
        for (Sexpr x : list) {
            if (x instanceof Sfloat) {
                r.add((Sfloat) x);
            } else if (x instanceof Sint) {
                r.add(((Sint) x).toSfloat());
            } else {
                return null;
            }
        }
        return r;
    }
    public ArrayList<Symbol> allSymbol() {
        ArrayList<Symbol> r = new ArrayList<Symbol>(list.size());
        for (Sexpr x : list) {
            if (x instanceof Symbol) {
                r.add((Symbol) x);
            } else {
                return null;
            }
        }
        return r;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i < list.size() - 1; i++) {
            sb.append(list.get(i).toString());
            sb.append(' ');
        }
        sb.append(list.get(list.size() - 1)); // last token doesn't get a space before ')'
        sb.append(')');
        return sb.toString();
    }
}
