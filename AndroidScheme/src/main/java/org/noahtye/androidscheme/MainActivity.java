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
import java.util.Stack;

public class MainActivity extends Activity implements Scheme {

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
            return Satom.parseToken(token);
        }
    }

    private static Symbol NIL = new Symbol("nil");
    private static Symbol TRUE = new Symbol("true");
    private static Symbol FALSE = new Symbol("false");

    public Sexpr eval(Sexpr sexpr, Env env) {
        if (sexpr instanceof Symbol) {
            return env.get(((Symbol) sexpr).get());
        }
        if (sexpr instanceof Satom) {
            return sexpr;
        } else if (sexpr instanceof Slist) {
            Slist slist = (Slist) sexpr;
            Symbol sym = (slist.head() instanceof Symbol) ? ((Symbol) slist.head()) : null;
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
                    return ((Proc) evalledList.get(0)).apply(evalledList.rest(), env, this);
                } else {
                    return new Sexpr().evalError("Evaluation error: couldn't find proc " +
                                                 slist.head() + " from " + sexpr.toString());
                }
            }
        } else {
            return sexpr; // todo: not sure what to do here.
        }
    }

    private class Add extends Proc {
        public Sexpr apply(Slist args, Env env, Scheme scheme) {
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
        public Sexpr apply(Slist args, Env env, Scheme scheme) {
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
        public Sexpr apply(Slist args, Env env, Scheme scheme) {
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
        public Sexpr apply(Slist args, Env env, Scheme scheme) {
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
        public Sexpr apply(Slist args, Env env, Scheme scheme) {
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
