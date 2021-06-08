/*
id: 314023516
name: Alon Bebchuk
username: alonbebchuk

id: 328634373
name: Aryeh Gorun
username: aryehgorun
 */

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
    public static class Node {
        private int id;
        private int weight;
        private EdgeDLL edges;
        private Neighborhood neighborhood;

        /**
         * Creates a new node object, given its id and its weight. O(1).
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
         * Returns the id of the node. O(1).
         *
         * @return the id of the node.
         */
        public int getId() {
            return this.id;
        }

        /**
         * Returns the weight of the node. O(1).
         *
         * @return the weight of the node.
         */
        public int getWeight() {
            return this.weight;
        }
    }

    /**
     * This class represents an edge emanating from a node in the graph.
     */
    private class Edge {
        private int neighborNodeId;
        private DLLNode<Edge> reciprocalEdge;

        /**
         * Creates a new edge object, given id of node to which it connects. O(1).
         *
         * @param neighborNodeId - id of node to which it connects.
         */
        private Edge(int neighborNodeId) {
            this.neighborNodeId = neighborNodeId;
        }
    }

    /**
     * This class represents a neighborhood of a node in the graph.
     */
    private static class Neighborhood {
        private int weight;
        private int nodeId;
        private int heapIndex;

        /**
         * Creates a new neighborhood object, given its node. O(1).
         *
         * @param node - node of neighborhood.
         */
        private Neighborhood(Node node) {
            this.weight = node.getWeight();
            this.nodeId = node.getId();
        }
    }

    //DOUBLY LINKED LISTS-----------------------------------------------------------------------------------------------

    /**
     * This class represents a node in a doubly linked list.
     */
    private static class DLLNode<T> {
        private DLLNode<T> prev;
        private T info;
        private DLLNode<T> next;

        /**
         * Creates a new dll node object, given its info. O(1).
         *
         * @param info - info of dll node.
         */
        private DLLNode(T info) {
            this.prev = this;
            this.info = info;
            this.next = this;
        }
    }

    /**
     * This class represents a doubly linked list.
     */
    private static class DLL<T> {
        protected DLLNode<T> sentinel;
        protected int length;

        /**
         * Creates a new dll object. O(1).
         */
        private DLL() {
            this.sentinel = new DLLNode<>(null);
            this.length = 0;
        }

        /**
         * Adds a new dll node containing info to start of dll and returns added dll node. O(1).
         *
         * @param info - info of added dll node.
         * @return added dll node.
         */
        protected DLLNode<T> insert(T info) {
            DLLNode<T> dllNode = new DLLNode<>(info);

            dllNode.prev = this.sentinel;
            dllNode.next = this.sentinel.next;

            this.sentinel.next = dllNode;
            dllNode.next.prev = dllNode;

            // increment length by 1
            this.length++;

            return dllNode;
        }

        /**
         * Deletes dll node, given reference. O(1).
         *
         * @param dllNode - dll node reference.
         */
        protected void delete(DLLNode<T> dllNode) {
            dllNode.prev.next = dllNode.next;
            dllNode.next.prev = dllNode.prev;

            // decrement length by 1
            this.length--;
        }
    }

    /**
     * This class represents a doubly linked list of node objects.
     */
    private class NodeDLL extends DLL<Node> {
        /**
         * Returns dll node containing info node with given id, or null if no such dll node exists. O(n).
         *
         * @param nodeId - id of info node.
         * @return dll node containing info node with given id, or null.
         */
        private DLLNode<Node> getDLLNode(int nodeId) {
            DLLNode<Node> dllNode = this.sentinel;

            while ((dllNode = dllNode.next) != this.sentinel) {
                if (dllNode.info.getId() == nodeId) {
                    return dllNode;
                }
            }

            return null;
        }

        /**
         * Returns info node contained in dll node with given id, or null if no such node exists. O(n).
         *
         * @param nodeId - id of info node.
         * @return info node contained in dll node with given id, or null.
         */
        private Node getNode(int nodeId) {
            DLLNode<Node> dllNode = this.getDLLNode(nodeId);

            return dllNode == null ? null : dllNode.info;
        }

        /**
         * Delete dll node containing info node with given id, if exists. O(n).
         *
         * @param nodeId - id of info node.
         */
        private void delete(int nodeId) {
            DLLNode<Node> dllNode = this.getDLLNode(nodeId);

            if (dllNode != null) {
                this.delete(dllNode);
            }
        }
    }

    /**
     * This class represents a doubly linked list of edge objects.
     */
    private static class EdgeDLL extends DLL<Edge> implements Iterable<Edge> {
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
                return dllNode.info;
            }
        }

        /**
         * Returns iterator of edge dll. O(1).
         *
         * @return iterator of edge dll.
         */
        @Override
        public Iterator<Edge> iterator() {
            return new EdgeDLLIterator();
        }
    }

    //NODE HASH TABLE---------------------------------------------------------------------------------------------------

    /**
     * This class represents a hash table of node objects.
     */
    private class NodeHashTable {
        private NodeDLL[] hashTable;

        private int m;
        private int a;
        private int b;

        /**
         * Creates a new hash table object with random modular hash function, given its size. O(1).
         */
        private NodeHashTable(int size) {
            this.hashTable = new NodeDLL[size];

            this.m = this.hashTable.length;

            Random rand = new Random();
            this.a = 1 + rand.nextInt(Graph.P - 1);
            this.b = rand.nextInt(Graph.P);
        }

        /**
         * Returns hash value of given integer. O(1).
         *
         * @param i - integer.
         * @return hash value of integer.
         */
        private int getHashValue(int i) {
            return Math.floorMod(Math.floorMod((a * i + b), Graph.P), m);
        }

        /**
         * Inserts node. O(1).
         *
         * @param node - node.
         */
        private void insert(Node node) {
            int hashValue = this.getHashValue(node.getId());

            if (this.hashTable[hashValue] == null) {
                this.hashTable[hashValue] = new NodeDLL();
            }

            this.hashTable[hashValue].insert(node);
        }

        /**
         * Returns node with given id, or null if no such node existed. O(1) - Expected.
         *
         * @param nodeId - id of node.
         * @return node with given id, or null.
         */
        private Node get(int nodeId) {
            int hashValue = this.getHashValue(nodeId);

            return this.hashTable[hashValue] == null ? null : this.hashTable[hashValue].getNode(nodeId);
        }

        /**
         * Deletes node with given id, if exists. O(1) - Expected.
         *
         * @param nodeId - id of node.
         */
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

    //NEIGHBORHOOD MAX HEAP---------------------------------------------------------------------------------------------

    /**
     * This class represents a max binary heap of neighborhood objects. Heap order determined by neighborhood weight.
     */
    private class MaxNeighborhoodHeap {
        /**
         * Functions in this class assume that heap is not empty (size>0) and indexes given are legal (less than size).
         */

        private Neighborhood[] heap;
        private int size;

        /**
         * Creates a new max binary heap of neighborhood objects, given array of neighborhood objects. O(n).
         *
         * @param neighborhoods - array of neighborhood objects.
         */
        private MaxNeighborhoodHeap(Neighborhood[] neighborhoods) {
            this.heap = neighborhoods;
            this.size = this.heap.length;

            // convert array to max binary heap using heapify down from the bottom up
            for (int i = (this.size / 2) - 1; i >= 0; i--) {
                heapifyDown(i);
            }
        }

        /**
         * Swaps neighborhood objects, given their indexes. O(1).
         *
         * @param i - index of first neighborhood.
         * @param j - index of second neighborhood.
         */
        private void swap(int i, int j) {
            Neighborhood tmp = this.heap[i];

            this.heap[i] = this.heap[j];
            this.heap[i].heapIndex = i;

            this.heap[j] = tmp;
            this.heap[j].heapIndex = j;
        }

        /**
         * Heapify down neighborhood object, given its index. O(log n).
         *
         * @param i - index of neighborhood.
         */
        private void heapifyDown(int i) {
            int max = i, leftChild = 2 * i + 1, rightChild = 2 * i + 2;

            if (leftChild < this.size && this.heap[leftChild].weight > this.heap[max].weight) {
                max = leftChild;
            }

            if (rightChild < this.size && this.heap[rightChild].weight > this.heap[max].weight) {
                max = rightChild;
            }

            if (max != i) {
                this.swap(i, max);
                this.heapifyDown(max);
            }
        }

        /**
         * Heapify up neighborhood object, given its index. O(log n).
         *
         * @param i - index of neighborhood.
         */
        private void heapifyUp(int i) {
            while (i > 0 && this.heap[i].weight > this.heap[i / 2].weight) {
                this.swap(i, i / 2);
                i = i / 2;
            }
        }

        /**
         * Changes weight of neighborhood object by delta and heapifies, given its index and change in weight. O(log n).
         *
         * @param i     - index of neighborhood.
         * @param delta - change in weight.
         */
        private void changeKey(int i, int delta) {
            this.heap[i].weight += delta;

            if (delta > 0) {
                this.heapifyUp(i);
            } else if (delta < 0) {
                this.heapifyDown(i);
            }
        }

        /**
         * Deletes neighborhood object, given its index. O(log n).
         *
         * @param i - index of neighborhood.
         */
        private void delete(int i) {
            int deletedW = this.heap[i].weight, replacedW = this.heap[this.size - 1].weight;

            // swap with last
            this.swap(i, this.size - 1);

            // heapify as needed
            if (replacedW > deletedW) {
                this.heapifyUp(i);
            } else if (replacedW < deletedW) {
                this.heapifyDown(i);
            }

            // delete neighborhood from array
            this.heap[this.size - 1] = null;

            // decrement size by 1
            this.size--;
        }

        /**
         * Returns node id of max neighborhood weight. O(1).
         *
         * @return node id of max neighborhood weight.
         */
        private int getMaxNeighborhoodNodeId() {
            return this.heap[0].nodeId;
        }
    }

    //GRAPH-------------------------------------------------------------------------------------------------------------
    /**
     * Functions in this class assume that user will not cause insertion of duplicated.
     */

    private int numNodes;
    private int numEdges;

    private NodeHashTable nodes;
    private MaxNeighborhoodHeap maxNeighborhoodHeap;

    /**
     * Initializes the graph on a given set of nodes,
     * by creating a hash table of nodes and a max binary heap of their neighborhoods.
     * The created graph is empty, i.e. it has no edges. O(n).
     *
     * @param nodes - an array of node objects
     */
    public Graph(Node[] nodes) {
        this.numNodes = nodes.length;
        this.numEdges = 0;

        this.nodes = new NodeHashTable(this.numNodes);

        Neighborhood[] neighborhoods = new Neighborhood[this.numNodes];

        for (int i = 0; i < nodes.length; i++) {
            // insert node into hash table
            this.nodes.insert(nodes[i]);

            // insert node neighbor into array and update its index
            neighborhoods[i] = nodes[i].neighborhood;
            neighborhoods[i].heapIndex = i;
        }

        this.maxNeighborhoodHeap = new MaxNeighborhoodHeap(neighborhoods);
    }

    /**
     * This method returns the node in the graph with the maximum neighborhood weight, or null if graph is empty.
     * O(1) - Expected.
     *
     * @return a Node object representing the correct node. If there is no node in the graph, returns 'null'.
     */
    public Node maxNeighborhoodWeight() {
        return this.numNodes == 0 ? null : this.nodes.get(this.maxNeighborhoodHeap.getMaxNeighborhoodNodeId());
    }

    /**
     * Given a node id of a node in the graph, this method returns the neighborhood weight of that node,
     * or null if no such node exists. O(1) - Expected.
     *
     * @param node_id - an id of a node.
     * @return the neighborhood weight of the node of id 'node_id' if such a node exists in the graph.
     * Otherwise, the function returns -1.
     */
    public int getNeighborhoodWeight(int node_id) {
        Node node = this.nodes.get(node_id);

        return node == null ? -1 : node.neighborhood.weight;
    }

    /**
     * Given a node of a node in the graph, this method changes the neighborhood weight of that node by delta.
     * O(log n).
     *
     * @param node - node in graph.
     */
    private void changeNeighborhoodWeight(Node node, int delta) {
        this.maxNeighborhoodHeap.changeKey(node.neighborhood.heapIndex, delta);
    }

    /**
     * This function adds an edge between the two nodes whose ids are specified.
     * If one of these nodes is not in the graph, the function does nothing.
     * The two nodes must be distinct; otherwise, the function does nothing.
     * Returns true if edge added, otherwise returns false. O(log n).
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

            // update reference to dll node of reciprocal edge
            edge1to2.reciprocalEdge = node2.edges.insert(edge2to1);
            edge2to1.reciprocalEdge = node1.edges.insert(edge1to2);

            // update neighborhood weight
            this.changeNeighborhoodWeight(node1, node2.weight);
            this.changeNeighborhoodWeight(node2, node1.weight);

            // increment numEdges by 1
            this.numEdges++;

            return true;
        }
    }

    /**
     * Given the id of a node in the graph, deletes the node of that id from the graph, if it exists.
     * Returns true if node deleted, otherwise returns false. O((d+1) log n).
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

                // delete reciprocal edges from all neighbors
                neighbor.edges.delete(edge.reciprocalEdge);

                // update neighbors weight
                this.changeNeighborhoodWeight(neighbor, -node.getWeight());

                // decrease numEdges by 1
                this.numEdges--;
            }

            // delete node neighborhood from max heap
            this.maxNeighborhoodHeap.delete(node.neighborhood.heapIndex);

            // delete node from hash table
            this.nodes.delete(node_id);

            // decrease numNodes by 1
            this.numNodes--;

            return true;
        }
    }

    /**
     * Return number of nodes in graph. O(1).
     *
     * @return number of nodes in graph
     */
    public int getNumNodes() {
        return this.numNodes;
    }

    /**
     * Return number of edges in graph. O(1).
     *
     * @return number of edges in graph
     */
    public int getNumEdges() {
        return this.numEdges;
    }
}