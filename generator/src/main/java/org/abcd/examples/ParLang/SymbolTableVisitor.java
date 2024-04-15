package org.abcd.examples.ParLang;

import org.abcd.examples.ParLang.AstNodes.*;
import org.abcd.examples.ParLang.symbols.Attributes;
import org.abcd.examples.ParLang.symbols.SymbolTable;

//TODO: Find out if scope checking also includes checking for legal calls to other actors
public class SymbolTableVisitor implements NodeVisitor {
    SymbolTable symbolTable;

    public SymbolTableVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public void visitChildren(AstNode node){
        for(AstNode child : node.getChildren()){
            child.accept(this);
        }
    }

    //Declares a variable in the symbol table if it does not already exist
    private void declareNode(VarDclNode node){
        if(this.symbolTable.lookUpSymbol(node.getId()) == null){
            Attributes attributes = new Attributes(node.getType(), "dcl");
            this.symbolTable.insertSymbol(node.getId(), attributes);
        }
        //TODO: Find out what should be done, when trying to declare a symbol that already exists. Override?
    }

    @Override
    public void visit(VarDclNode node){
        declareNode(node);
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
        //Leaves the scope after visiting the children, as the variables in the select node are not available outside the select node
        this.symbolTable.leaveScope();
    }

    @Override
    //Adds a method to the symbol table if it does not already exist
    public void visit(MethodDclNode node){
        if(this.symbolTable.lookUpSymbol(node.getId()) == null){
            Attributes attributes = new Attributes(node.getReturnType(), "method");
            this.symbolTable.insertSymbol(node.getId(), attributes);

            this.symbolTable.addScope(node.getNodeHash());
            //Visits the children of the node to add the symbols to the symbol table
            this.visitChildren(node);
            //Leaves the scope after visiting the children, as the variables in the method node are not available outside the method node
            this.symbolTable.leaveScope();
        }
    }


    @Override
    //Adds the parameters of a method to the symbol table
    public void visit(ParametersNode node){
        String scopeName = this.symbolTable.getCurrentScope().getScopeName();

        //Iterates through the children of the node and adds them to the symbol table
        for(AstNode child: node.getChildren()){
            IdentifierNode paramNode = (IdentifierNode)child;
            Attributes attributes = new Attributes(paramNode.getType(), "param");
            attributes.setScope(scopeName);
            this.symbolTable.insertParams(paramNode.getName(), attributes);
        }
    }

    @Override
    public void visit(ActorDclNode node) {
        this.symbolTable.addScope(node.getNodeHash());
        //Visits the children of the node to add the symbols to the symbol table
        this.visitChildren(node);
        //Leaves the scope after visiting the children, as the variables in the Actor node are not available outside the iteration node
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(MainDclNode node) {
        this.symbolTable.addScope(node.getNodeHash());
        //Visits the children of the node to add the symbols to the symbol table
        this.visitChildren(node);
        //Leaves the scope after visiting the children, as the variables in the Main node are not available outside the iteration node
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(SpawnActorNode node) {
        this.symbolTable.addScope(node.getNodeHash());
        //Visits the children of the node to add the symbols to the symbol table
        this.visitChildren(node);
        //Leaves the scope after visiting the children, as the variables in the Spawn node are not available outside the iteration node
        this.symbolTable.leaveScope();
    }


    @Override
    public void visit(SpawnDclNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ExprNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(AccessNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ReturnStatementNode node) {
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
    public void visit(DclNode node) {
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
    public void visit(IdentifierNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ActorIdentifierNode node) {
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
    public void visit(ActorStateNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(KnowsNode node) {
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
    public void visit(ArithExprNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(IterationNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ArrayAccessNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(StateAccessNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(KnowsAccessNode node) {
        this.visitChildren(node);
    }
}