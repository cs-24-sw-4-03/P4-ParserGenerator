package org.RaceGuard;

import org.RaceGuard.AstNodes.*;
import org.RaceGuard.symbols.Attributes;
import org.RaceGuard.symbols.SymbolTable;
import org.RaceGuard.Exceptions.LocalMethodCallException;
import org.RaceGuard.Exceptions.MissingOnMethodException;
import org.RaceGuard.Exceptions.OnMethodCallException;
import org.RaceGuard.Exceptions.SymbolNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodVisitor implements NodeVisitor {
    SymbolTable symbolTable;
    private List<RuntimeException> exceptions = new ArrayList<>();

    public List<RuntimeException> getExceptions() {return this.exceptions;}

    public MethodVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public void visit(MethodCallNode node) {
        //First we find all the methods that the actor is allowed to call
        HashMap<String, Attributes> legalLocalMethods = this.symbolTable.getDeclaredLocalMethods();
        //Then we check if the called method is part of that list
        if (!legalLocalMethods.containsKey(node.getMethodName())) {
            exceptions.add(new LocalMethodCallException("Local method id " + node.getMethodName() + " not found" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
        }
    }

    @Override
    public void visit(SendMsgNode node) {
        try{
            //First we check if it is either a Knows, State, Self or a normal variable
            if(node.getReceiver().contains(".")){
                //If it contains . then it is a Knows or State access. We therefore check which
                String receiver = node.getReceiver().split("\\.")[1];
                String accessModifier = node.getReceiver().split("\\.")[0];
                if(accessModifier.equals(RaceGuardE.STATE.getValue())){
                    //We then enter the scope based on the correct symbol list
                    this.symbolTable.enterScope(this.symbolTable.lookUpStateSymbol(receiver).getVariableType());
                } else if (accessModifier.equals(RaceGuardE.KNOWS.getValue())) {
                    this.symbolTable.enterScope(this.symbolTable.lookUpKnowsSymbol(receiver).getVariableType());
                }

            }else if (node.getReceiver().equals(RaceGuardE.SELF.getValue())){
                //We check if the Actor itself has the method we call
                this.symbolTable.enterScope(this.symbolTable.findActorParent(node));
            } else{
                //Otherwise we look up the symbol in the normal symbol list
                String type = this.symbolTable.lookUpSymbol(node.getReceiver()).getVariableType().split("\\[")[0];
                this.symbolTable.enterScope(type);
            }

            //Then we find the list of messages it can receive
            HashMap<String, Attributes> legalOnMethods = this.symbolTable.getDeclaredOnMethods();
            //We then check if the message is part of the list of allowed messages
            if (!legalOnMethods.containsKey(node.getMsgName())) {
                exceptions.add(new OnMethodCallException("On method id " + node.getMsgName() + " not found in actor: " + symbolTable.getCurrentScope().getScopeName() + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
            }

            //We then leave the scope, such that we do not mess with our scope stack
            this.symbolTable.leaveScope();

            this.visitChildren(node);
        } catch (NullPointerException e){
            exceptions.add(new SymbolNotFoundException("Symbol: " + node.getReceiver() + " not found" + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
        }

    }


    @Override
    public void visit(FollowsNode node) {
        for(int x = 0; x < node.getChildren().size(); x++){
            //A FollowsNode can only IdentifierNode children
            IdentifierNode script = (IdentifierNode) node.getChildren().get(x);
            //We get the list of on methods from the Actor we are currently within
            HashMap<String, Attributes> legalOnMethodsActor = this.symbolTable.getDeclaredOnMethods();
            HashMap<String, Attributes> legalLocalMethodsActor = this.symbolTable.getDeclaredLocalMethods();

            //We then enter the scope of the Script the Actor follows
            this.symbolTable.enterScope(script.getName());
            //We find its on methods
            HashMap<String, Attributes> legalOnMethodsScript = this.symbolTable.getDeclaredOnMethods();
            HashMap<String, Attributes> legalLocalMethodsScript = this.symbolTable.getDeclaredLocalMethods();
            //Then we leave the scope, such that we do not mess with the scope stack
            this.symbolTable.leaveScope();

            //We then check if every entry in the Scripts list also is in the Actors list
            for (String onMethod : legalOnMethodsScript.keySet()) {
                if (!legalOnMethodsActor.containsKey(onMethod)) {
                    exceptions.add(new MissingOnMethodException("Actor: " + this.symbolTable.findActorParent(node) +  " does not have on method: " + onMethod + " from Script: " + script.getName() + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
                }
            }
            for(String localMethod : legalLocalMethodsScript.keySet()){
                if (!legalLocalMethodsActor.containsKey(localMethod)) {
                    exceptions.add(new MissingOnMethodException("Actor: " + this.symbolTable.findActorParent(node) +  " does not have local method: " + localMethod + " from Script: " + script.getName() + ". Line: " + node.getLineNumber() + " Column: " + node.getColumnNumber()));
                }
            }
        }

        this.visitChildren(node);
    }

    @Override
    public void visit(BoolCompareNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(KillNode node) {}

    @Override
    public void visitChildren(AstNode node){
        for(AstNode child : node.getChildren()){
            child.accept(this);
        }
    }

    @Override
    public void visit(ScriptDclNode node) {
        this.symbolTable.enterScope(node.getId());
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(ScriptMethodNode node) {
        this.symbolTable.enterScope(node.getId() + this.symbolTable.findActorParent(node));
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(InitNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(BodyNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(SelectionNode node) {
        this.symbolTable.enterScope(node.getNodeHash());
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(SpawnActorNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ActorDclNode node) {
        this.symbolTable.enterScope(node.getId());
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(StateNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ParametersNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(ReturnStatementNode node) {
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
    public void visit(VarDclNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(IdentifierNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(AssignNode node) {
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
    public void visit(KnowsNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(MethodDclNode node) {
        //The name of the actor the method is declared in is used to differentiate between methods with the same name in different actors
        this.symbolTable.enterScope(node.getId() + this.symbolTable.findActorParent(node));
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(MainDclNode node) {
        this.symbolTable.enterScope(node.getNodeHash());
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(SpawnDclNode node) {
        this.symbolTable.enterScope(node.getNodeHash());
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(ExpNode node) {
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
    public void visit(WhileNode node) {
        this.symbolTable.enterScope(node.getNodeHash());
        this.visitChildren(node);
        this.symbolTable.leaveScope();
    }

    @Override
    public void visit(ForNode node) {
        this.symbolTable.enterScope(node.getNodeHash());
        this.visitChildren(node);
        this.symbolTable.leaveScope();
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

    @Override
    public void visit(PrintCallNode node) {
        this.visitChildren(node);
    }

    @Override
    public void visit(BoolAndExpNode node) {
        this.visitChildren(node);
    }

}
