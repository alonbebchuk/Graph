import javafx.util.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Measurements {
    public static void main(String[] args) {
        Graph empty = new Graph();
        Graph.Node[] nodes = new Graph.Node[(int) Math.pow(2, 21)];

        for (int j = 0; j < Math.pow(2, 21); j++) {
            nodes[j] = empty.new Node(j + 1, 1);
        }

        for (int i = 6; i <= 21; i++) {
            int n = (int) Math.pow(2, i);

            Graph graph = new Graph(Arrays.copyOf(nodes, n));

            for (Pair<Integer, Integer> pair : Measurements.generateRandomOrderedPairs(n)) {
                graph.addEdge(pair.getKey(), pair.getValue());
            }

            System.out.println(graph.getNeighborhoodWeight(graph.maxNeighborhoodWeight().getId()) - 1);
        }
    }

    public static Set<Pair<Integer, Integer>> generateRandomOrderedPairs(int n) {
        HashSet<Pair<Integer, Integer>> pairs = new HashSet<>();
        Random random = new Random();

        int i, j;
        while (pairs.size() < n) {
            i = 1 + random.nextInt(n - 1);
            j = i + 1 + random.nextInt(n - i);

            pairs.add(new Pair<>(i, j));
        }

        return pairs;
    }
}