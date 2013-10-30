package org.noahtye.androidscheme;

public class Sint extends Satom {
    int value;
    public Sint(int i) { value = i; }
    public int get() { return value; }
    public String toString() { return String.valueOf(value); }
    public Sfloat toSfloat() { return new Sfloat((float) value); }
}
