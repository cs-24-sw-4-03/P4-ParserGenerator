package org.RaceGuard.AstNodes;

public abstract class DclNode extends AstNode {
    private String id;

    public DclNode(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
