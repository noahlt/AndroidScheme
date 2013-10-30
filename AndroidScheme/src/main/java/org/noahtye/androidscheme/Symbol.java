package org.noahtye.androidscheme;

public class Symbol extends Satom {
    String name;
    public Symbol(String s) { name = s; }
    public String get() { return name; }
    public String toString() { return name; }
    public boolean equals(Symbol sym) { return (sym != null) && name.equals(sym.get()); }
    public boolean equals(String str) { return name.equals(str); }
}
