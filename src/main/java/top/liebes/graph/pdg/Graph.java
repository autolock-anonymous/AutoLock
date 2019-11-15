package top.liebes.graph.pdg;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.slf4j.LoggerFactory;
import top.liebes.env.Env;

import java.util.*;

public class Graph {
    private static Logger logger = (Logger) LoggerFactory.getLogger(Graph.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }

    public String name;

    List<Node> nodes;

    public Graph(String name) {
        this.nodes = new ArrayList<>();
        this.name = name;
    }

    public void addNode(Node node){
        this.nodes.add(node);
    }

    public Node findNode(IVariableBinding binding){
        if(binding == null){
            logger.warn("binding is null");
            return null;
        }
        for(int i = 0; i < nodes.size(); i ++){
            if(binding == nodes.get(i).getVar()){
                return nodes.get(i);
            }
        }
        Node node = new Node(binding);
        this.addNode(node);
        return node;
    }

    public void print(){
        System.out.println(String.format("========= GRAPH %s ========", this.name));
        for(Node node : nodes){
            printSingleNode(node);
        }
    }

    public void printSingleNode(Node node){
        System.out.println(String.format("-------- NODE %s --------", node.var.getName()));
        Set<Node> set = new HashSet<>();
        set.add(node);
        Queue<Node> queue = new LinkedList<>();
        queue.add(node);
        while(!queue.isEmpty()){
            Node tNode = queue.poll();
            for(Node cNode : tNode.toNodes){
                if(!set.contains(cNode)){
                    queue.add(cNode);
                    set.add(cNode);
                    System.out.print(cNode.var.getName() + "; ");
                }
            }
        }
        System.out.println();
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
