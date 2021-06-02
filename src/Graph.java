/*
You must NOT change the signatures of classes/methods in this skeleton file.
You are required to implement the methods of this skeleton file according to the requirements.
You are allowed to add classes, methods, and members as required.
 */


import java.util.Iterator;
import java.util.Random;

/**
 * This class represents a graph that efficiently maintains the heaviest neighborhood over edge addition and
 * vertex deletion.
 */
public class Graph {
    private static int P = (int) Math.pow(10, 9) + 9;

    //BASE CLASSES------------------------------------------------------------------------------------------------------

    /**
     * This class represents a node in the graph.
     */
    public class Node {
        private int id;
        private int weight;
        private EdgeDLL edges;
        private Neighborhood neighborhood;

        /**
         * Creates a new node object, given its id and its weight.
         *
         * @param id     - the id of the node.
         * @param weight - the weight of the node.
         */
        public Node(int id, int weight) {
            this.id = id;
            this.weight = weight;
            this.edges = new EdgeDLL();
            this.neighborhood = new Neighborhood(this);
        }

        /**
         * Returns the id of the node.
         *
         * @return the id of the node.
         */
        public int getId() {
            return this.id;
        }

        /**
         * Returns the weight of the node.
         *
         * @return the weight of the node.
         */
        public int getWeight() {
            return this.weight;
        }
    }

    private class Edge {
        private int neighborNodeId;
        private DLLNode<Edge> oppositeEdge;

        private Edge(int neighborNodeId) {
            this.neighborNodeId = neighborNodeId;
        }
    }

    private class Neighborhood {
        private int nodeId;
        private int weight;
        private int heapIndex;

        private Neighborhood(Node node) {
            this.nodeId = node.getId();
            this.weight = node.getWeight();
        }
    }

    //DOUBLY LINKED LIST------------------------------------------------------------------------------------------------
    private class DLLNode<T> {
        private DLLNode prev;
        private T item;
        private DLLNode next;

        private DLLNode() {
            this.prev = this;
            this.item = null;
            this.next = this;
        }

        private DLLNode(T item) {
            this.prev = this;
            this.item = item;
            this.next = this;
        }
    }

    private class DLL<T> {
        protected DLLNode<T> sentinel;
        protected int length;

        private DLL() {
            this.sentinel = new DLLNode<>();
            this.length = 0;
        }

        protected DLLNode<T> insertFirst(T item) {
            DLLNode<T> dllNode = new DLLNode<>(item);

            dllNode.prev = this.sentinel;
            dllNode.next = this.sentinel.next;
            this.sentinel.next = dllNode;
            dllNode.next.prev = dllNode;

            this.length++;

            return dllNode;
        }

        protected void delete(DLLNode<T> dllNode) {
            dllNode.prev.next = dllNode.next;
            dllNode.next.prev = dllNode.prev;

            this.length--;
        }
    }

    private class NodeDLL extends DLL<Node> {
        private DLLNode<Node> getDLLNode(int nodeId) {
            DLLNode<Node> dllNode = this.sentinel;

            while ((dllNode = dllNode.next) != this.sentinel) {
                if (dllNode.item.getId() == nodeId) {
                    return dllNode;
                }
            }

            return null;
        }

        private Node getNode(int nodeId) {
            DLLNode<Node> dllNode = this.getDLLNode(nodeId);

            return dllNode == null ? null : dllNode.item;
        }

        private void delete(int nodeId) {
            DLLNode<Node> dllNode = this.getDLLNode(nodeId);

            if (dllNode != null) {
                this.delete(dllNode);
            }
        }
    }

    private class EdgeDLL extends DLL<Edge> implements Iterable<Edge> {
        private class EdgeDLLIterator implements Iterator<Edge> {
            DLLNode<Edge> currDLLNode;

            private EdgeDLLIterator() {
                this.currDLLNode = EdgeDLL.this.sentinel.next;
            }

            @Override
            public boolean hasNext() {
                return this.currDLLNode != EdgeDLL.this.sentinel;
            }

            @Override
            public Edge next() {
                DLLNode<Edge> dllNode = currDLLNode;
                this.currDLLNode = this.currDLLNode.next;
                return dllNode.item;
            }
        }

        @Override
        public Iterator<Edge> iterator() {
            return new EdgeDLLIterator();
        }
    }

    //HASH TABLE--------------------------------------------------------------------------------------------------------
    private class NodeHashTable {
        private NodeDLL[] hashTable;

        private int m;
        private int a;
        private int b;

        private NodeHashTable(int size) {
            this.hashTable = new NodeDLL[size];

            Random rand = new Random();
            this.m = this.hashTable.length;
            this.a = rand.nextInt(Graph.P - 1) + 1;
            this.b = rand.nextInt(Graph.P);
        }

        private int getHashValue(int i) {
            return Math.floorMod(Math.floorMod((a * i + b), Graph.P), m);
        }

        private void insert(Node node) {
            int hashValue = this.getHashValue(node.getId());

            if (this.hashTable[hashValue] == null) {
                this.hashTable[hashValue] = new NodeDLL();
            }

            this.hashTable[hashValue].insertFirst(node);
        }

        private Node get(int nodeId) {
            int hashValue = this.getHashValue(nodeId);

            return this.hashTable[hashValue] == null ? null : this.hashTable[hashValue].getNode(nodeId);
        }

        private void delete(int nodeId) {
            int hashValue = this.getHashValue(nodeId);

            if (this.hashTable[hashValue] != null) {
                this.hashTable[hashValue].delete(nodeId);

                if (this.hashTable[hashValue].length == 0) {
                    this.hashTable[hashValue] = null;
                }
            }
        }
    }

    //HEAP--------------------------------------------------------------------------------------------------------------
    private class MaxNeighborhoodHeap {
        private Neighborhood[] neighborhoods;
        private int size;

        private MaxNeighborhoodHeap(Neighborhood[] neighborhoods) {
            this.neighborhoods = neighborhoods;
            this.size = this.neighborhoods.length;

            for (int i = 0; i < this.size; i++) {
                this.neighborhoods[i].heapIndex = i;
            }

            for (int i = (this.size / 2) - 1; i >= 0; i--) {
                heapifyDown(i);
            }
        }

        private void swap(int i, int j) {
            Neighborhood tmp = this.neighborhoods[i];

            this.neighborhoods[i] = this.neighborhoods[j];
            this.neighborhoods[i].heapIndex = i;

            this.neighborhoods[j] = tmp;
            this.neighborhoods[j].heapIndex = j;
        }

        private void heapifyDown(int i) {
            int max = i, leftChild = 2 * i + 1, rightChild = 2 * i + 2;

            if (leftChild < this.size && this.neighborhoods[leftChild].weight > this.neighborhoods[max].weight) {
                max = leftChild;
            }

            if (rightChild < this.size && this.neighborhoods[rightChild].weight > this.neighborhoods[max].weight) {
                max = rightChild;
            }

            if (max != i) {
                this.swap(i, max);
                this.heapifyDown(max);
            }
        }

        private void heapifyUp(int i) {
            while (i > 0 && this.neighborhoods[i].weight > this.neighborhoods[i / 2].weight) {
                this.swap(i, i / 2);
                i = i / 2;
            }
        }

        private void changeKey(int i, int delta) {
            this.neighborhoods[i].weight += delta;

            if (delta > 0) {
                this.heapifyUp(i);
            } else if (delta < 0) {
                this.heapifyDown(i);
            }
        }

        private void delete(int i) {
            this.swap(i, this.size - 1);
            this.neighborhoods[this.size - 1] = null;
            this.size--;
        }

        // @pre - this.size > 0
        private int getMaxNeighborhoodNodeId() {
            return this.neighborhoods[0].nodeId;
        }
    }

    //GRAPH-------------------------------------------------------------------------------------------------------------
    private int numNodes;
    private int numEdges;

    private NodeHashTable nodes;
    private MaxNeighborhoodHeap maxNeighborhoodHeap;

    public Graph() {
    }

    /**
     * Initializes the graph on a given set of nodes. The created graph is empty, i.e. it has no edges.
     * You may assume that the ids of distinct nodes are distinct.
     *
     * @param nodes - an array of node objects
     */
    public Graph(Node[] nodes) {
        this.numNodes = nodes.length;
        this.numEdges = 0;

        this.nodes = new NodeHashTable(this.numNodes);
        Neighborhood[] neighborhoods = new Neighborhood[this.numNodes];

        for (int i = 0; i < nodes.length; i++) {
            this.nodes.insert(nodes[i]);
            neighborhoods[i] = nodes[i].neighborhood;
        }

        this.maxNeighborhoodHeap = new MaxNeighborhoodHeap(neighborhoods);
    }

    /**
     * This method returns the node in the graph with the maximum neighborhood weight.
     * Note: nodes that have been removed from the graph using deleteNode are no longer in the graph.
     *
     * @return a Node object representing the correct node. If there is no node in the graph, returns 'null'.
     */
    public Node maxNeighborhoodWeight() {
        return this.numNodes == 0 ? null : this.nodes.get(this.maxNeighborhoodHeap.getMaxNeighborhoodNodeId());
    }

    /**
     * given a node id of a node in the graph, this method returns the neighborhood weight of that node.
     *
     * @param node_id - an id of a node.
     * @return the neighborhood weight of the node of id 'node_id' if such a node exists in the graph.
     * Otherwise, the function returns -1.
     */
    public int getNeighborhoodWeight(int node_id) {
        Node node = this.nodes.get(node_id);

        return node == null ? null : node.neighborhood.weight;
    }

    public void changeNeighborWeight(Node node, int delta) {
        this.maxNeighborhoodHeap.changeKey(node.neighborhood.heapIndex, delta);
    }

    /**
     * This function adds an edge between the two nodes whose ids are specified.
     * If one of these nodes is not in the graph, the function does nothing.
     * The two nodes must be distinct; otherwise, the function does nothing.
     * You may assume that if the two nodes are in the graph, there exists no edge between them prior to the call.
     *
     * @param node1_id - the id of the first node.
     * @param node2_id - the id of the second node.
     * @return returns 'true' if the function added an edge, otherwise returns 'false'.
     */
    public boolean addEdge(int node1_id, int node2_id) {
        Node node1, node2;

        if (
                node1_id == node2_id ||
                        (node1 = this.nodes.get(node1_id)) == null ||
                        (node2 = this.nodes.get(node2_id)) == null
        ) {
            return false;
        } else {
            Edge edge1to2 = new Edge(node2_id);
            Edge edge2to1 = new Edge(node1_id);

            edge2to1.oppositeEdge = node1.edges.insertFirst(edge1to2);
            this.changeNeighborWeight(node1, node2.weight);

            edge1to2.oppositeEdge = node2.edges.insertFirst(edge2to1);
            this.changeNeighborWeight(node2, node1.weight);

            this.numEdges++;

            return true;
        }
    }

    /**
     * Given the id of a node in the graph, deletes the node of that id from the graph, if it exists.
     *
     * @param node_id - the id of the node to delete.
     * @return returns 'true' if the function deleted a node, otherwise returns 'false'
     */
    public boolean deleteNode(int node_id) {
        Node node = this.nodes.get(node_id), neighbor;

        if (node == null) {
            return false;
        } else {
            for (Edge edge : node.edges) {
                neighbor = this.nodes.get(edge.neighborNodeId);
                neighbor.edges.delete(edge.oppositeEdge);
                this.changeNeighborWeight(neighbor, -node.getWeight());

                this.numEdges--;
            }

            return true;
        }
    }

    public int getNumNodes() {
        return this.numNodes;
    }

    public int getNumEdges() {
        return this.numEdges;
    }
}