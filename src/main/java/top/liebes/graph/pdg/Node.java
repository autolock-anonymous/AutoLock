package top.liebes.graph.pdg;

import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.*;

public class Node {
    IVariableBinding var;
    Set<Node> toNodes;
    Set<Node> fromNodes;

    public Node(IVariableBinding var) {
        this.var = var;
        this.toNodes = new HashSet<>();
        this.fromNodes = new HashSet<>();
    }

    public IVariableBinding getVar() {
        return var;
    }

    public void setVar(IVariableBinding var) {
        this.var = var;
    }

    public void addEdge(Node toNode){
        if(toNode == this){
            return;
        }
        this.toNodes.add(toNode);
        toNode.fromNodes.add(this);
    }

    public boolean isConnectedTo(Node toNode){
        if(toNode == this){
            return true;
        }
        Set<Node> set = new HashSet<>();
        set.add(this);
        Queue<Node> queue = new LinkedList<>();
        queue.add(this);
        while(!queue.isEmpty()){
            Node tNode = queue.poll();
            for(Node node : tNode.toNodes){
                if(node == toNode){
                    return true;
                }
                if(!set.contains(node)){
                    queue.add(node);
                    set.add(node);
                }
            }
        }
        return false;
    }

    public Set<Node> getAllRelatedNodes(){
        Set<Node> set = new HashSet<>();
        set.add(this);
        Queue<Node> queue = new LinkedList<>();
        queue.add(this);
        while(!queue.isEmpty()){
            Node tNode = queue.poll();
            for(Node node : tNode.toNodes){
                if(!set.contains(node)){
                    queue.add(node);
                    set.add(node);
                }
            }
        }
        return set;
    }
}
