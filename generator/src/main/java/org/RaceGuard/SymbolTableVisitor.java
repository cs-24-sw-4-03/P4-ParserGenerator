package org.RaceGuard;

import org.RaceGuard.AstNodes.*;
import org.RaceGuard.symbols.Attributes;
import org.RaceGuard.symbols.SymbolTable;
import org.RaceGuard.Exceptions.DuplicateScopeException;
import org.RaceGuard.Exceptions.DuplicateSymbolException;
import org.RaceGuard.Exceptions.ScopeNotFoundException;
import org.RaceGuard.Exceptions.SymbolNotFoundException;
import org.RaceGuard.Exceptions.ReservedActorNameException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SymbolTableVisitor implements NodeVisitor {
    SymbolTable symbolTable;
    private List<RuntimeException> exceptions = new ArrayList<>();

    public List<RuntimeException> getExceptions() {return this.exceptions;}

    public SymbolTableVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public void visitChildren(AstNode node){
        for(AstNode child : node.getChildren()){
            child.accept(this);
        }
    }

    @Override
    public void visit(ScriptDclNode node) {
        if(this.symbolTable.addScope(node.getId())){
            this.symbolTable.declaredScripts.add(node.getId());
            //Visits the children of the node to add the symbols to the symbol table
            this.visitChildren(node);
            //Leaves the scope after visiting the children, as the variables in the Script node are not available outside the Script node
            this.symbolTable.leaveScope();
        }else{
            this.exceptions.add(new DuplicateScopeException("Script: " + node.getId() + " already exists" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
        }
    }

    //Declares a variable in the symbol table if it does not already exist
    @Override
    public void visit(VarDclNode node){
        this.visitChildren(node);

        if(node.getParent() instanceof StateNode){
            if(this.symbolTable.lookUpStateSymbol(node.getId()) == null){
                Attributes attributes = new Attributes(node.getType());
                this.symbolTable.insertStateSymbol(node.getId(), attributes);
            }else{
                this.exceptions.add(new DuplicateSymbolException("State symbol: " + node.getId() + " already exists in Actor: " + this.symbolTable.findActorParent(node) + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
            }
        }else{
            if(this.symbolTable.lookUpSymbolCurrentScope(node.getId()) == null){
                Attributes attributes = new Attributes(node.getType());
                this.symbolTable.insertSymbol(node.getId(), attributes);
            }else{
                this.exceptions.add(new DuplicateSymbolException("Symbol " + node.getId() + " already exists in current scope" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
            }
        }
    }

    //Creates a new scope as a while iteration node is a new scope and leaves it after visiting the children
    @Override
    public void visit(WhileNode node) {
        this.symbolTable.addScope(node.getNodeHash());
        //Visits the children of the node to add the symbols to the symbol table
        this.visitChildren(node);
        //Leaves the scope after visiting the children, as the variables in the iteration node are not available outside the iteration node
        this.symbolTable.leaveScope();
    }

    //Creates a new scope as a for iteration node is a new scope and leaves it after visiting the children
    @Override
    public void visit(ForNode node) {
        this.symbolTable.addScope(node.getNodeHash());
        //Visits the children of the node to add the symbols to the symbol table
        this.visitChildren(node);
        //Leaves the scope after visiting the children, as the variables in the iteration node are not available outside the iteration node
        this.symbolTable.leaveScope();
    }


    @Override
    //Creates a new scope as a select node is a new scope and leaves it after visiting the children
    public void visit(SelectionNode node){
        this.symbolTable.addScope(node.getNodeHash());
        //Visits the children of the node to add the symbols to the symbol table
        this.visitChildren(node);
        //Leaves the scope after visiting the children, as the variables in the select node are not available outside the selection node
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(MethodDclNode node){
        //Checks if the method is a local or on method, and inserts it into the correct list
        if(Objects.equals(node.getMethodType(), RaceGuardE.LOCAL.getValue())){
            Attributes attributes = new Attributes(node.getType());
            this.symbolTable.insertLocalMethod(node.getId(), attributes);
        }else if(Objects.equals(node.getMethodType(), RaceGuardE.ON.getValue())){
            Attributes attributes = new Attributes(node.getType());
            this.symbolTable.insertOnMethod(node.getId(), attributes);
        }
        //Creates a new scope, as long as there is not already a method in the actor that is named the same
        if(this.symbolTable.addScope(node.getId() + this.symbolTable.findActorParent(node))){
            //Visits the children of the node to add the symbols to the symbol table
            this.visitChildren(node);
            //Leaves the scope after visiting the children, as the variables in the method node are not available outside the method node
            this.symbolTable.leaveScope();
        } else {
            this.exceptions.add(new DuplicateScopeException("Duplicate Method scope: " + node.getId() + " in Actor: " + this.symbolTable.findActorParent(node) + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
        }
    }


    @Override
    //Adds the parameters of a method to the symbol table
    public void visit(ParametersNode node){
        String scopeName = this.symbolTable.getCurrentScope().getScopeName();

        //Iterates through the children of the node and adds them to the symbol table
        for(AstNode child: node.getChildren()){
            IdentifierNode paramNode = (IdentifierNode)child;
            Attributes attributes = new Attributes(paramNode.getType());
            attributes.setScope(scopeName);
            if(!this.symbolTable.insertParams(paramNode.getName(), attributes)){
                this.exceptions.add(new DuplicateSymbolException("Param: " + paramNode.getName() + " already exists in current scope" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
            }
        }
    }

    @Override
    public void visit(ActorDclNode node) {
        if(node.getId().equals(RaceGuardE.REAPER.getValue())){
            this.exceptions.add(new ReservedActorNameException("The actor name \""+ RaceGuardE.REAPER.getValue()+"\" is reserved. Pick another name for the actor."));
        } else if (node.getId().equals(RaceGuardE.MAIN.getValue())) {
            this.exceptions.add(new ReservedActorNameException("The actor name \""+ RaceGuardE.MAIN.getValue()+"\" is reserved. Pick another name for the actor."));
        }
        if(this.symbolTable.addScope(node.getId())){
            //Visits the children of the node to add the symbols to the symbol table
            this.visitChildren(node);
            //Leaves the scope after visiting the children, as the variables in the Actor node are not available outside the Actor node
            this.symbolTable.leaveScope();
        }else{
            this.exceptions.add(new DuplicateScopeException("Actor: " + node.getId() + " already exists" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
        }
    }

    @Override
    public void visit(MainDclNode node) {
        this.symbolTable.addScope(node.getNodeHash());
        //Visits the children of the node to add the symbols to the symbol table
        this.visitChildren(node);
        //Leaves the scope after visiting the children, as the variables in the Main node are not available outside the Main node
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(SpawnDclNode node) {
        this.symbolTable.addScope(node.getNodeHash());
        //Visits the children of the node to add the symbols to the symbol table
        this.visitChildren(node);
        //Leaves the scope after visiting the children, as the variables in the Spawn node are not available outside the Spawn node
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(IdentifierNode node) {
        if(!(node.getParent() instanceof VarDclNode)){
            //Checks whether a symbol is a State, Knows or normal symbol and searches the appropriate list
            if(node.getParent().getParent() instanceof StateNode){
                if(this.symbolTable.lookUpStateSymbol(node.getName()) == null){
                    this.exceptions.add(new SymbolNotFoundException("State symbol: "  + node.getName() + " not found in Actor: " + this.symbolTable.findActorParent(node) + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
                }
                //Ensures that we do not search for IdentifierNodes for method calls
            }else if (!(node.getParent() instanceof MethodCallNode)){
                if(this.symbolTable.lookUpSymbol(node.getName()) == null){
                    this.exceptions.add(new SymbolNotFoundException("Symbol: "  + node.getName() + " not found" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
                }
            }
        }
    }

    @Override
    public void visit(StateAccessNode node) {
        if(this.symbolTable.lookUpStateSymbol(node.getAccessIdentifier()) == null){
            this.exceptions.add(new SymbolNotFoundException("State symbol: "  + node.getAccessIdentifier() + " not found in Actor: " + this.symbolTable.findActorParent(node) + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
        }
    }

    @Override
    public void visit(ArrayAccessNode node) {
        if (node.getAccessIdentifier().contains("State.")){
            if (this.symbolTable.lookUpStateSymbol(node.getAccessIdentifier().replace("State.", "")) == null){
                this.exceptions.add(new SymbolNotFoundException("State symbol: "  + node.getAccessIdentifier() + " not found in Actor: " + this.symbolTable.findActorParent(node) + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
            }
        }
        else {
            if (this.symbolTable.lookUpSymbol(node.getAccessIdentifier()) == null) {
                this.exceptions.add(new SymbolNotFoundException("Array symbol: " + node.getAccessIdentifier() + " not found" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(KnowsAccessNode node) {
        if(this.symbolTable.lookUpKnowsSymbol(node.getAccessIdentifier()) == null){
            this.exceptions.add(new SymbolNotFoundException("Knows symbol: "  + node.getAccessIdentifier() + " not found in Actor: " + this.symbolTable.findActorParent(node) + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
        }

        this.visitChildren(node);
    }

    @Override
    public void visit(KnowsNode node) {
        //Adds every child to the Knows symbol list, given there are no duplicates
        for(AstNode child: node.getChildren()){
            IdentifierNode idChildNode = (IdentifierNode)child;
            if (this.symbolTable.lookUpKnowsSymbol(idChildNode.getName()) == null) {
                Attributes attributes = new Attributes(idChildNode.getType());
                this.symbolTable.insertKnowsSymbol(idChildNode.getName(), attributes);
            }else{
                this.exceptions.add(new DuplicateScopeException("Duplicate Knows symbol: " + idChildNode.getName() + " found in Actor: " + this.symbolTable.findActorParent(node) + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
            }
        }
    }

    @Override
    public void visit(ScriptMethodNode node) {
        //Checks if the method is a local or on method and adds it to the appropriate list
        if(Objects.equals(node.getMethodType(), RaceGuardE.LOCAL.getValue())){
            Attributes attributes = new Attributes(node.getType());
            this.symbolTable.insertLocalMethod(node.getId(), attributes);
        }else if(Objects.equals(node.getMethodType(), RaceGuardE.ON.getValue())){
            Attributes attributes = new Attributes(node.getType());
            this.symbolTable.insertOnMethod(node.getId(), attributes);
        }
        //Creates a scope as long as there is not another method with the same name
        if(this.symbolTable.addScope(node.getId() + this.symbolTable.findActorParent(node))){
            //Visits the children of the node to add the symbols to the symbol table
            this.visitChildren(node);
            //Leaves the scope after visiting the children, as the variables in the method node are not available outside the method node
            this.symbolTable.leaveScope();
        } else {
            this.exceptions.add(new DuplicateScopeException("Duplicate Method scope: " + node.getId() + " in Script: " + this.symbolTable.findActorParent(node) + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
        }
    }

    @Override
    public void visit(FollowsNode node) {
        for(AstNode script : node.getChildren()){
            IdentifierNode scriptNode = (IdentifierNode)script;
            if(this.symbolTable.enterScope(scriptNode.getName())){
                this.symbolTable.addActorsFollowingScript(this.symbolTable.findActorParent(node));
                this.symbolTable.leaveScope();
            }else {
                exceptions.add(new ScopeNotFoundException("Script: " + scriptNode.getName() + " not found" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
            }
        }
    }

    @Override
    public void visit(SpawnActorNode node) {
        this.symbolTable.addScope(node.getNodeHash());
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(ExpNode node) {
        this.visitChildren(node);
    }


    @Override
    public void visit(ReturnStatementNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(SendMsgNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(BoolAndExpNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(MethodCallNode node){
        this.visitChildren(node);
    }

    @Override
    public void visit(LocalMethodBodyNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ArgumentsNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(InitNode node){
        this.visitChildren(node);
    }

    @Override
    public void visit(BodyNode node){
        this.visitChildren(node);
    }

    @Override
    public void visit(AssignNode node){
        this.visitChildren(node);
    }

    @Override
    public void visit(InitializationNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ListNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(StateNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(IntegerNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(DoubleNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(StringNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(BoolExpNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(SelfNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(BoolCompareNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(KillNode node) {}

    @Override
    public void visit(ArithExpNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(NegatedBoolNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(BoolNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(CompareExpNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(IterationNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(PrintCallNode node) {
        this.visitChildren(node);
    }
}
