package org.RaceGuard.AstNodes;

import org.RaceGuard.NodeVisitor;

public class UnaryExpNode extends ExpNode {
    private boolean isNegated = false;

    public  UnaryExpNode(){
    }

    public void setIsNegated(boolean isNegated){
        this.isNegated = isNegated;
    }

    public boolean isNegative(){
        return this.isNegated;
    }
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
