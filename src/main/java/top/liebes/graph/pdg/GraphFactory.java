package top.liebes.graph.pdg;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class GraphFactory {

    public void test(){
        int a = 1, d = 10;
        int b = 2;
        int c = 2;
        a = b;
        if(c == 2){
            a = c;
        }
        else{
            a = d;
        }
    }

    public static Graph createGraphFromMethod(MethodDeclaration methodDeclaration){
        Graph graph = new Graph(methodDeclaration.getName().toString());
        methodDeclaration.accept(new ASTVisitor() {
            @Override
            public boolean visit(IfStatement node) {
                List<IVariableBinding> expVarList = new ArrayList<>();
                node.getExpression().accept(new ASTVisitor() {
                    @Override
                    public boolean visit(SimpleName node) {
                        if(node.resolveBinding() != null && node.resolveBinding() instanceof IVariableBinding) {
                            expVarList.add((IVariableBinding) node.resolveBinding());
                        }
                        return super.visit(node);
                    }
                });
                ASTVisitor visitor = new ASTVisitor() {
                    @Override
                    public boolean visit(SimpleName node) {
                        if(node.resolveBinding() != null && node.resolveBinding() instanceof IVariableBinding) {
                            Node fromNode = graph.findNode((IVariableBinding) node.resolveBinding());
                            for(int i = 0; i < expVarList.size(); i ++){
                                Node toNode = graph.findNode(expVarList.get(i));
                                fromNode.addEdge(toNode);
                            }
                        }
                        return super.visit(node);
                    }
                };
                node.getThenStatement().accept(visitor);
                if(node.getElseStatement() != null){
                    node.getElseStatement().accept(visitor);
                }
                return super.visit(node);
            }

            @Override
            public boolean visit(WhileStatement node) {
                List<IVariableBinding> expVarList = new ArrayList<>();
                node.getExpression().accept(new ASTVisitor() {
                    @Override
                    public boolean visit(SimpleName node) {
                        if(node.resolveBinding() != null && node.resolveBinding() instanceof IVariableBinding){
                            expVarList.add((IVariableBinding)node.resolveBinding());
                        }
                        return super.visit(node);
                    }
                });
                node.getBody().accept(new ASTVisitor() {
                    @Override
                    public boolean visit(SimpleName node) {
                        if(node.resolveBinding() != null && node.resolveBinding() instanceof IVariableBinding){
                            Node fromNode = graph.findNode((IVariableBinding) node.resolveBinding());
                            for(int i = 0; i < expVarList.size(); i ++){
                                Node toNode = graph.findNode(expVarList.get(i));
                                fromNode.addEdge(toNode);
                            }
                        }
                        return super.visit(node);
                    }
                });
                return super.visit(node);
            }

            @Override
            public boolean visit(Assignment node){
                List<IVariableBinding> rightHandSideList = new ArrayList<>();
                node.getRightHandSide().accept(new ASTVisitor() {
                    @Override
                    public boolean visit(SimpleName node) {
                        if(node.resolveBinding() != null && node.resolveBinding() instanceof IVariableBinding){
                            rightHandSideList.add((IVariableBinding)node.resolveBinding());
                        }
                        return super.visit(node);
                    }
                });
                node.getLeftHandSide().accept(new ASTVisitor() {
                    @Override
                    public boolean visit(SimpleName node) {
                        if(node.resolveBinding() != null && node.resolveBinding() instanceof IVariableBinding){
                            Node fromNode = graph.findNode((IVariableBinding) node.resolveBinding());
                            for(int i = 0; i < rightHandSideList.size(); i ++){
                                Node toNode = graph.findNode(rightHandSideList.get(i));
                                fromNode.addEdge(toNode);
                            }
                        }
                        return super.visit(node);
                    }
                });

                return super.visit(node);
            }

        });

        return graph;
    }
}
