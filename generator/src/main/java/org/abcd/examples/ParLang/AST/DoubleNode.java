package org.abcd.examples.ParLang.AST;

import org.abcd.examples.ParLang.NodeVisitor;

public class DoubleNode extends LiteralNode<Double>{
    public DoubleNode(double value){
        super(value);
        System.out.println("double node created");
        };

    @Override
    public AstNode accept(NodeVisitor visitor) {
        return  visitor.visit(this);
    }
}
