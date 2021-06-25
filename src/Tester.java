import javafx.util.Pair;
import sun.text.normalizer.UnicodeSetIterator;

import java.util.*;
import java.util.function.BiFunction;

public class Tester {

    public Graph G;
    public int N;
    public Set<Integer> nodesIds;
    public Map<Integer, Integer> nodesWeights;
    public Map<Integer, Set<Integer>> edges;
    public int numNodes;
    public int numEdges;
    public Map<Integer, Long> neighborhoodWeights;
    public Random rnd = new Random();

    public Tester(int N)
    {
        this.N = N;
        Graph.Node[] nodes = new Graph.Node[N];
        nodesIds = new HashSet<>();
        nodesWeights = new HashMap<>();
        neighborhoodWeights = new HashMap<>();
        this.edges = new HashMap<>();
        for(int i=0;i<N;i++)
        {
            int id;
            do { id = rnd.nextInt(100000); } while(nodesIds.contains(id));
            int weight = rnd.nextInt(100);
            nodesIds.add(id);
            nodesWeights.put(id, weight);
            neighborhoodWeights.put(id, (long)weight);
            this.edges.put(id, new HashSet<>());
            nodes[i] = new Graph.Node(id, weight);
        }
        this.G = new Graph(nodes);
        this.numNodes = N;
        this.numEdges = 0;
    }

    public boolean addEdge(int v1, int v2)
    {
        if(edges.get(v1).contains(v2))
        {
           return false;
        }
        G.addEdge(v1, v2);
        edges.get(v1).add(v2);
        edges.get(v2).add(v1);
        numEdges++;
        neighborhoodWeights.replace(v1, neighborhoodWeights.get(v1) + nodesWeights.get(v2));
        neighborhoodWeights.replace(v2, neighborhoodWeights.get(v2) + nodesWeights.get(v1));
        return true;
    }

    public boolean deleteNode(int v)
    {
        if(!nodesIds.contains(v))
        {
            System.out.println("WTF");
            return false;
        }
        G.deleteNode(v);
        neighborhoodWeights.replaceAll((u, Sw) -> edges.get(v).contains(u) ? Sw - nodesWeights.get(v) : Sw);
        for(int u : this.edges.get(v))
        {
            this.edges.get(u).remove(v);
        }
        numEdges -= this.edges.get(v).size();
        this.edges.remove(v);
        nodesIds.remove(v);
        nodesWeights.remove(v);
        numNodes = nodesIds.size();
        return true;
    }

    public void randomEdges(int amount)
    {
        Integer[] idsArr = nodesIds.toArray(new Integer[numNodes]);
        for(int i=0;i<amount;i++)
        {
            int v1, v2;

            do {
                v1 = idsArr[rnd.nextInt(numNodes)];
                v2 = idsArr[rnd.nextInt(numNodes)];
            } while(v1 == v2 || edges.get(v1).contains(v2));
            this.addEdge(v1, v2);
        }
        verifyAll();
    }

    public void randomDeletions(int amount)
    {
        HashSet<Integer> deletionIndicesSet = new HashSet<>();
        int[] deletionIndices = new int[amount];
        while(deletionIndicesSet.size() < amount)
        {
            int index;
            do {
                index = rnd.nextInt(numNodes);
            } while(deletionIndicesSet.contains(index));
            deletionIndicesSet.add(index);
            deletionIndices[deletionIndicesSet.size()-1] = index;
        }
        Integer[] statNodesIds = nodesIds.toArray(new Integer[numNodes]);
        for(int i=0 ; i<amount ; i++) {
            int counter = 0;
            for (int v : statNodesIds) {
                if (counter == deletionIndices[i]) {
                    this.deleteNode(v);
                    break;
                }
                counter++;
            }
        }
        verifyAll();
    }

    public void randomOperations(int amount) // not so random, but kinda...
    {
        randomEdges(amount/6);
        randomDeletions(amount/3 - amount/6);
        randomEdges(amount/2 - amount/3);
        randomDeletions(2*amount/3 - amount/2);
        randomEdges(5*amount/6 - 2*amount/3);
        randomDeletions(amount - 5*amount/6);
        verifyAll();
    }

    public void deleteOneByOne()
    {
        this.randomDeletions(numNodes);
        if(numNodes != 0)
        {
            System.out.println("ERR - One by one deletion");
            System.out.println("numNodes = " + numNodes);
            int a = 1/0;
        }
    }

    public void addAllEdgesOneByOne()
    {
        randomEdges(numNodes*(numNodes-1)/2-numEdges);
        if(numEdges != numNodes*(numNodes-1)/2)
        {
            System.out.println("ERR - One by one Edges' insertion");
            int a = 1/0;
        }
    }

    public boolean verifyAll()
    {
        return verifyAmounts() && verifyNeighborhoodWeight() && verifyMaximum();
    }

    public boolean verifyAmounts()
    {
        if(numNodes != G.getNumNodes())
        {
            System.out.println("ERR - nodes amount");
            int a = 1/0;
            return false;
        }
        if (numEdges != G.getNumEdges()){
            System.out.println("ERR - edges amount");
            int a = 1/0;
            return false;
        }
        return true;
    }

    public boolean verifyNeighborhoodWeight()
    {
        for(int v : this.nodesIds)
        {
            if(G.getNeighborhoodWeight(v) != this.neighborhoodWeights.get(v))
            {
                System.out.println("ERR - neighborhood weights");
                int a = 1/0;
                return false;
            }
        }
        return true;
    }

    public boolean verifyMaximum()
    {
        Integer maxNeighborhoodWeightNode = null;
        for(Integer v : nodesIds)
        {
            if(maxNeighborhoodWeightNode == null || neighborhoodWeights.get(v) > neighborhoodWeights.get(maxNeighborhoodWeightNode))
            {
                maxNeighborhoodWeightNode = v;
            }
        }
        if((maxNeighborhoodWeightNode == null && G.maxNeighborhoodWeight() != null) ||
                (maxNeighborhoodWeightNode != null && this.neighborhoodWeights.get(maxNeighborhoodWeightNode) != G.getNeighborhoodWeight(G.maxNeighborhoodWeight().getId())))
        {
            System.out.println("ERR - Maximum");
            System.out.println("Should be: " + maxNeighborhoodWeightNode + ": " + this.neighborhoodWeights.get(maxNeighborhoodWeightNode));
            System.out.println("But is:    " + G.maxNeighborhoodWeight().getId() + ": " + G.getNeighborhoodWeight(G.maxNeighborhoodWeight().getId()));
            int a = 1/0;
            return false;
        }
        return true;
    }

    public static void main(String[] args)
    {
        Tester T;

        // small:
        for(int i=1 ; i<=10 ; i++) {
            System.out.println("\nTest #" + i + ": (small inputs)");
            T = new Tester(50);
            T.randomEdges(200);
            System.out.println("Random edges' insertion succeeded!");
            T.randomDeletions(15);
            System.out.println("Random nodes' deletion succeeded!");
            T.randomOperations(30);
            System.out.println("Random modifications' operation succeeded!");
            T.addAllEdgesOneByOne();
            System.out.println("One by one addition of edges succeeded!");
            T.deleteOneByOne();
            System.out.println("One by one deletion of nodes succeeded!");
        }

        // average:
        for(int i = 11 ; i<=15 ; i++) {
            System.out.println("\nTest #" + i + ": (avg inputs)");
            T = new Tester(1000);
            T.randomEdges(2000);
            System.out.println("Random edges' insertion succeeded!");
            T.randomDeletions(500);
            System.out.println("Random nodes' deletion succeeded!");
            T.randomOperations(500);
            System.out.println("Random modifications' operation succeeded!");
        }

        // big:
        for(int i = 16 ; i<= 17 ; i++) {
            System.out.println("\nTest #" + i + ": (big inputs)");
            T = new Tester(10000);
            T.randomEdges(50000);
            System.out.println("Random edges' insertion succeeded!");
            T.randomDeletions(2000);
            System.out.println("Random nodes' deletion succeeded!");
            T.randomOperations(8000);
            System.out.println("Random modifications' operation succeeded!");
        }
    }
}