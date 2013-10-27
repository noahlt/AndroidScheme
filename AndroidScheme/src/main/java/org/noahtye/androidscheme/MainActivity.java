package org.noahtye.androidscheme;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public class MainActivity extends Activity {

    private Sexpr parse(String expr) {
        return readFrom(tokenize(expr));
    }

    private Stack<String> tokenize(String expr) {
        String[] tokenArray = expr.replace("(", " ( ").replace(")", " ) ").trim().split("[ \\n]+");
        Stack<String> tokens = new Stack<String>();
        for (int i = tokenArray.length - 1; i >= 0; i--) {
            tokens.push(tokenArray[i]);
        }
        return tokens;
    }

    private Sexpr readFrom(Stack<String> tokens) {
        if (tokens.isEmpty()) {
            return new Sexpr().parseError("unexpected EOF while reading");
        }
        String token = tokens.pop();
        if (token.equals("(")) {
            Slist slist = new Slist();
            while (!tokens.isEmpty() && !tokens.peek().equals(")")) {
                slist.append(readFrom(tokens));
            }
            if (tokens.isEmpty()) {
                // TODO: for all of these, pass through the original string to print it with error message
                return new Sexpr().parseError("missing ')'");
            } else {
                tokens.pop(); // pop off ')'
                return slist;
            }
        } else if (token.equals(")")) {
            return new Sexpr().parseError("unexpected ')' while reading");
        } else {
            return parseAtom(token);
        }
    }

    private class Sexpr {
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
    private class Slist extends Sexpr {
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

    /*private class Satom extends Sexpr {
        public static Satom fromToken(String token) {
            try {
                return Sint(Integer.parseInt(token));
            } catch (NumberFormatException e1) {
                try {
                    return Sfloat(Float.parseFloat(token));
                } catch (NumberFormatException e2) {
                    return Symbol(token);
                }
            }
        }
    }*/

    private class Satom extends Sexpr {}

    private Satom parseAtom(String atomToken) {
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

    private class Sint extends Satom {
        int value;
        public Sint(int i) { value = i; }
        public int get() { return value; }
        public String toString() { return String.valueOf(value); }
        public Sfloat toSfloat() { return new Sfloat((float) value); }
    }

    private class Sfloat extends Satom {
        float value;
        public Sfloat(float f) { value = f; }
        public String toString() { return String.valueOf(value); }
    }

    private class Symbol extends Satom {
        String name;
        public Symbol(String s) { name = s; }
        public String get() { return name; }
        public String toString() { return name; }
        public boolean equals(Symbol sym) { return (sym != null) && name.equals(sym.get()); }
        public boolean equals(String str) { return name.equals(str); }
    }

    // TODO all these static
    private Symbol NIL = new Symbol("nil");
    private Symbol TRUE = new Symbol("true");
    private Symbol FALSE = new Symbol("false");

    private Sexpr eval(Sexpr sexpr, Env env) {
        Log.d("noah", "eval: " + sexpr.toString());
        if (sexpr instanceof Symbol) {
            return env.get(((Symbol) sexpr).get());
        }
        if (sexpr instanceof Satom) {
            return sexpr;
        } else if (sexpr instanceof Slist) {
            Slist slist = (Slist) sexpr;
            Symbol sym = (slist.head() instanceof Symbol) ? ((Symbol) slist.head()) : null;
            Log.d("noah", "sym != null? " + (sym != null));
            if (sym != null) { Log.d("noah", "sym: " + sym.get()); }
            if (sym != null && sym.equals("quote")) {
                if (2 == slist.size()) {
                    return slist.get(1);
                } else {
                    return new Sexpr().evalError("quote needs 1 arg, got " + (slist.size() - 1));
                }
            } else if (sym != null && sym.equals("if")) {
                if (4 == slist.size()) {
                    Sexpr predicate = eval(slist.get(1), env);
                    if (predicate.equals(TRUE)) {
                        return eval(slist.get(2), env);
                    } else {
                        return eval(slist.get(3), env);
                    }
                } else {
                    return new Sexpr().evalError("if needs 3 args, got " + (slist.size()-2));
                }
            } else if (sym != null && sym.equals("set!")) {
                env.set((Symbol) slist.get(1), eval(slist.get(2), env));
                // TODO test wrong type (arg1 must be symbol)
                // TODO test wrong # args
                return NIL;
            } else if (sym != null && sym.equals("define")) {
                // TODO function syntax
                env.set((Symbol) slist.get(1), eval(slist.get(2), env));
                return NIL;
            } else if (sym != null && sym.equals("lambda")) {
                if (slist.get(1) instanceof Slist) {
                    // TODO verify that every arg name is just a symbol, not a slist
                    return new UserProc((Slist) slist.get(1), slist.get(2));
                } else {
                    return new Sexpr().evalError("lambda's first argument must be a list");
                }
            } else if (sym != null && sym.equals("begin")) {
                if (slist.size() > 1) {
                    for (int i = 0; i < slist.size() - 1; i++) {
                        eval(slist.get(i), env);
                    }
                    return eval(slist.get(slist.size()-1), env);
                } else {
                    return NIL;
                }
            } else {
                Slist evalledList = new Slist();
                for (Sexpr x : slist.raw()) {
                    evalledList.append(eval(x, env));
                }
                if (evalledList.head() instanceof Proc) {
                    return ((Proc) evalledList.get(0)).apply(evalledList.rest(), env);
                } else {
                    return new Sexpr().evalError("Evaluation error: couldn't find proc " +
                                                 slist.head() + " from " + sexpr.toString());
                }
            }
        } else {
            return sexpr; // todo: not sure what to do here.
        }
    }

    private class Env {
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

    private abstract class Proc extends Sexpr {
        public abstract Sexpr apply(Slist args, Env env);
        public String toString() {
            return "<proc>";
        }
    }

    private class UserProc extends Proc {
        private Sexpr src;
        private Slist argNames;
        public UserProc(Slist argNames, Sexpr src) {
            this.src = src;
            this.argNames = argNames;
        }
        public Sexpr apply(Slist args, Env enclosingEnv) {
            if (args.size() == argNames.size()) {
                Env env = new Env(enclosingEnv);
                for (int i = 0; i < argNames.size(); i++) {
                    env.set((Symbol) argNames.get(i), args.get(i));
                }
                return eval(src, env);
            } else {
                return new Sexpr().evalError("function expects " + (argNames.size()-1) + " arguments; received " + (args.size() - 1));
            }
        }
    }

    private class Add extends Proc {
        public Sexpr apply(Slist args, Env env) {
            ArrayList<Sint> sints = args.allSint();
            if (sints != null) {
                int r = 0;
                for (Sint sint : sints) {
                    r += sint.get();
                }
                return new Sint(r);
            }
            ArrayList<Sfloat> sfloats = args.rest().allSfloat();
            if (sfloats != null) {
                throw new UnsupportedOperationException();
            }
            return new Sexpr().evalError("Type error applying :+ to " + args.toString());
        }
    }

    private class Multiply extends Proc {
        public Sexpr apply(Slist args, Env env) {
            ArrayList<Sint> sints = args.allSint();
            if (sints != null) {
                int r = 1;
                for (Sint sint : sints) {
                    r *= sint.get();
                }
                return new Sint(r);
            }
            ArrayList<Sfloat> sfloats = args.rest().allSfloat();
            if (sfloats != null) {
                throw new UnsupportedOperationException();
            }
            return new Sexpr().evalError("Type error applying :* to " + args.toString());
        }
    }

    private class Subtract extends Proc {
        public Sexpr apply(Slist args, Env env) {
            ArrayList<Sint> sints = args.allSint();
            if (sints != null && sints.size() > 1) {
                int r = sints.get(0).get();
                for (int i = 1; i < sints.size(); i++) {
                    r -= sints.get(i).get();
                }
                return new Sint(r);
            } else if (sints != null && sints.size() == 1) {
                return new Sint(-sints.get(0).get());
            }
            ArrayList<Sfloat> sfloats = args.rest().allSfloat();
            if (sfloats != null) {
                throw new UnsupportedOperationException();
            }
            return new Sexpr().evalError("Type error applying :- to " + args.toString());
        }
    }

    private class And extends Proc {
        // TODO short-circuiting and can't be implemented as a regular function
        public Sexpr apply(Slist args, Env env) {
            ArrayList<Symbol> sbools = args.allSymbol();
            if (sbools != null && sbools.size() > 0) {
                for (int i = 1; i < sbools.size(); i++) {
                    if (!sbools.get(i).equals(TRUE)) {
                        return FALSE;
                    }
                }
                return TRUE;
            }
            return new Sexpr().evalError("Type error applying :and to " + args.toString());
        }
    }

    private class LessThan extends Proc {
        public Sexpr apply(Slist args, Env env) {
            ArrayList<Sint> sints = args.allSint();
            if (sints != null && sints.size() > 0) {
                for (int i = 1; i < sints.size(); i++) {
                    if (sints.get(i).get() <= sints.get(i - 1).get()) {
                        return FALSE;
                    }
                }
                return TRUE;
            }
            return new Sexpr().evalError("Type error appyling :< to "+ args.toString());
        }
    }

    private Env initGlobals() {
        // TODO these should be symbols, not strings, and symbols should be interned only once.
        Env globals = new Env();
        globals.set("+", new Add());
        globals.set("-", new Subtract());
        globals.set("*", new Multiply());
        globals.set("and", new And());
        globals.set("<", new LessThan());
        globals.set("nil", NIL);
        globals.set("true", TRUE);
        globals.set("false", FALSE);
        return globals;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Env globals = initGlobals();

        final EditText editor = (EditText) findViewById(R.id.editor);
        final Button eval = (Button) findViewById(R.id.eval);
        final TextView results = (TextView) findViewById(R.id.results);

        eval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                results.setText(eval(parse("(begin " + editor.getText().toString() + ")"), globals).toString());
            }
        });
    }

    private void demoEval(Env env, String expr) {
        Log.d("scheme", expr);
        Log.d("scheme", " → " +eval(parse(expr), env).toString());
    }

    protected void onResume() {
        super.onResume();
        initGlobals();

        Env globals = initGlobals();
        demoEval(globals, "(+ 1 2)");
        demoEval(globals, "(+ 1 2 (+ 3 4) (- 5 6))");
        demoEval(globals, "(- 6)");
        demoEval(globals, "(* 2 3 4)");
        demoEval(globals, "1");
        demoEval(globals, "true");
        demoEval(globals, "(and true false)");
        demoEval(globals, "(and true true)");
        demoEval(globals, "(if true 1 2)");
        demoEval(globals, "(if false 1 2)");
        demoEval(globals, "(quote hello)");
        demoEval(globals, "(define x 3)");
        demoEval(globals, "x");
        demoEval(globals, "(* x 2)");
        demoEval(globals, "((lambda (a) (* a a)) 10)");
        demoEval(globals, "(define square (lambda (a) (* a a)))");
        demoEval(globals, "(square 10)");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
