package org.abcd.examples.ParLang.symbols;

import java.util.ArrayList;
import java.util.Stack;

public class SymbolTable {

    private Scope currentScope;
    final private Scope globalScope;
    final private Stack<Scope> scopeStack = new Stack<>();
    public ArrayList<String> declaredMethods = new ArrayList<>();
    public ArrayList<String> calledMethods = new ArrayList<>();


    public SymbolTable() {
        this.globalScope = new Scope("global");
        this.currentScope = this.globalScope;
    }

    public Scope getCurrentScope(){
        return this.currentScope;
    }

    //Creates a new scope and sets the currentScope as its parent if there is no scope with the same name
    //It then pushes the currentScope onto the stack and sets the new scope as the currentScope
    //This is done as the currentScope is not located on the stack
    public void addScope(String scopeName){
        if(lookUpScope(scopeName) == null){
            Scope scope = new Scope(scopeName);
            scope.setParent(this.currentScope);
            this.currentScope.children.add(scope);
            this.scopeStack.push(this.currentScope);
            this.currentScope = scope;
        }
    }

    //Pops the top scope from the stack and sets it as the currentScope
    //TODO: C* sets the name of the original currentScope, find out if there is a reason for this
    public void leaveScope(){
        if(!this.scopeStack.empty()){
            this.currentScope = this.scopeStack.pop();
        } else{
            this.currentScope = this.globalScope;
        }
    }

    //TODO: Find better name than searchScope
    //This method searches for a scope with the given name starting from the given searchScope
    //This method is recursive, and will most often be called with the globalScope as the searchScope
    //It then calls itself with the children of the searchScope as the new searchScope
    public Scope findScope(String scopeName, Scope searchScope){
        //Enters if you are searching for the global scope
        if (this.globalScope.getScopeName().equals(scopeName)){
            return this.globalScope;
        }

        Scope scope = null;
        //Iterates through the children of the scope you start the search from
        for(Scope childScope: searchScope.children){
            scope = findScope(scopeName, childScope);

            //Breaks the loop if the scope is found
            if(scope != null){
                break;
            }
        }

        return scope;
    }

    public Scope lookUpScope(String scopeName){
        return findScope(scopeName, this.globalScope);
    }

    public Attributes lookUpSymbol(String symbol){
        Scope scope = this.currentScope;

        //Iterates through the scopes starting from the currentScope moving up the scope hierarchy
        //Returns the symbol if it is found or returns null if the symbol is not found
        while(scope != null){
            if(!scope.getSymbols().isEmpty() && scope.getSymbols().containsKey(symbol)){
                return scope.getSymbols().get(symbol);
            } else if (!scope.getParams().isEmpty() && scope.getParams().containsKey(symbol)) {
                return scope.getParams().get(symbol);
            }

            scope = scope.getParent();
        }
        return null;
    }

    public void insertSymbol(String symbol, Attributes attributes){
        this.currentScope.addSymbol(symbol, attributes);
    }

    public void insertParams(String param, Attributes attributes){
        this.currentScope.addParams(param, attributes);
    }

}