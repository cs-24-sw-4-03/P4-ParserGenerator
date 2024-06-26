package org.RaceGuard;

import org.RaceGuard.AstNodes.*;

public interface NodeVisitor {
    void visitChildren(AstNode node);

    void visit(ScriptDclNode node);
    void visit(ScriptMethodNode node);
    void visit(SendMsgNode node);
    void visit(InitNode node);
    void visit(BodyNode node);
    void visit(SelectionNode node);

    void visit(SpawnActorNode node);
    void visit(ActorDclNode node);
    void visit(StateNode node);
    void visit(FollowsNode node);


    void visit(ParametersNode node);
    void visit(ReturnStatementNode node);
    void visit(MethodCallNode node);
    void visit(LocalMethodBodyNode node);
    void visit(ArgumentsNode node);

    void visit(VarDclNode node);

    void visit(IdentifierNode node);
    void visit(AssignNode node);
    void visit(InitializationNode node);

    void visit(ListNode node);

    void visit(KnowsNode node);
    void visit(MethodDclNode node);
    void visit(MainDclNode node);
    void visit(SpawnDclNode node);

    void visit(ExpNode node);
    void visit(IntegerNode node);
    void visit(DoubleNode node);
    void visit(StringNode node);
    void visit(ArithExpNode node);
    void visit(NegatedBoolNode node);
    void visit(BoolNode node);
    void visit(CompareExpNode node);

    void visit(IterationNode node);
    void visit(WhileNode node);
    void visit(ForNode node);


    void visit(ArrayAccessNode node);
    void visit(StateAccessNode node);
    void visit(KnowsAccessNode node);

    void visit(PrintCallNode node);
    void visit(BoolAndExpNode node);
    void visit(BoolExpNode node);
    void visit(SelfNode node);

    void visit(BoolCompareNode node);

    void visit(KillNode node);
}
