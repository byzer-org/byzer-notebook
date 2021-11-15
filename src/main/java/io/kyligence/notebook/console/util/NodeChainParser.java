package io.kyligence.notebook.console.util;

import com.google.common.collect.Sets;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import io.kyligence.notebook.console.bean.entity.NodeInfo;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;

import java.util.*;
import java.util.stream.Collectors;

public class NodeChainParser {
    public static class Node {

        private final MutableGraph<Integer> fullGraph;

        private final NodeInfo info;

        private final Set<Node> previousNode;

        private final Set<Node> nextNode;

        private final Set<String> input;

        private final Set<String> output;

        private void addPrevNode(Node node) {
            this.previousNode.add(node);
            node.nextNode.add(this);
        }

        private void addNextNode(Node node) {
            node.previousNode.add(this);
            this.nextNode.add(node);
        }

        public Set<Node> getPreviousNode() {
            return this.previousNode;
        }

        public Set<Node> getNextNode() {
            return this.nextNode;
        }

        public NodeInfo getInfo() {
            return this.info;
        }

        public Integer getId() {
            return this.info.getId();
        }

        public List<String> getSortedNodeIdList(Set<Node> nodeSet) {
            return nodeSet.stream().map(Node::getId).sorted(Comparator.naturalOrder()).
                    map(EntityUtils::toStr).collect(Collectors.toList());
        }

        public void link(Node otherNode) {
            this.output.forEach(value -> {
                if (otherNode.input.contains(value)) {
                    this.addNextNode(otherNode);
                    this.fullGraph.putEdge(this.getId(), otherNode.getId());
                }
            });
            this.input.forEach(value -> {
                if (otherNode.output.contains(value)) {
                    this.addPrevNode(otherNode);
                    this.fullGraph.putEdge(otherNode.getId(), this.getId());
                }
            });
        }

        public Node(NodeInfo nodeInfo, MutableGraph<Integer> graph) {
            this.info = nodeInfo;
            this.previousNode = Sets.newLinkedHashSet();
            this.nextNode = Sets.newLinkedHashSet();
            this.input = new HashSet<>(Objects.requireNonNull(JacksonUtils.readJsonArray(this.info.getInput(), String.class)));
            this.output = new HashSet<>(Objects.requireNonNull(JacksonUtils.readJsonArray(this.info.getOutput(), String.class)));
            this.fullGraph = graph;
            this.fullGraph.addNode(this.info.getId());
        }
    }

    private final ArrayList<Node> nodeList;

    private final MutableGraph<Integer> graph;

    public NodeChainParser(List<NodeInfo> nodeInfos) {
        graph = GraphBuilder.directed().nodeOrder(ElementOrder.<Integer>natural()).allowsSelfLoops(false).build();
        nodeList = new ArrayList<>();
        for (NodeInfo nodeInfo : nodeInfos) {
            this.addNode(new Node(nodeInfo, graph));
        }
    }

    public void checkCycle(){
        if (Graphs.hasCycle(graph)) {
            throw new ByzerException(ErrorCodeEnum.WORKFLOW_HAS_CYCLE);
        }
    }

    public void addNode(Node node) {
        nodeList.forEach(node::link);
        nodeList.add(node);
    }

    public Set<Node> visitAll() {
        Set<Node> viewed = Sets.newLinkedHashSet();
        nodeList.forEach(node -> this.visit(node, viewed));
        return viewed;
    }

    public void visit(Node currentNode, Set<Node> viewedNodes) {
        if (!viewedNodes.contains(currentNode)) {
            if (this.needFallback(currentNode, viewedNodes)) {
                this.fallback(currentNode, viewedNodes);
            } else {
                viewedNodes.add(currentNode);
                for (Node nextNode : currentNode.nextNode) {
                    this.visit(nextNode, viewedNodes);
                }
            }
        }

    }

    public void fallback(Node currentNode, Set<Node> viewedNodes) {
        for (Node prevNode : currentNode.previousNode) {
            if (!viewedNodes.contains(prevNode)) {
                this.visit(prevNode, viewedNodes);
            }
        }
    }

    public boolean needFallback(Node currentNode, Set<Node> viewedNodes) {
        for (Node node : currentNode.previousNode) {
            if (!viewedNodes.contains(node)) {
                return true;
            }
        }
        return false;
    }
}
