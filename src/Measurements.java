import javafx.util.Pair;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

public class Measurements {
    public static void main(String[] args) {
        int numExperiments = 5;
        int[] results = new int[16];

        for (int experiment = 0; experiment < numExperiments; experiment++) {
            for (int i = 6; i <= 21; i++) {
                int n = (int) Math.pow(2, i);

                Graph.Node[] nodes = new Graph.Node[n];

                for (int j = 0; j < n; j++) {
                    nodes[j] = new Graph.Node(j + 1, 1);
                }

                Graph graph = new Graph(nodes);

                for (Pair<Integer, Integer> pair : Measurements.generateRandomOrderedPairs(n)) {
                    graph.addEdge(pair.getKey(), pair.getValue());
                }

                results[i - 6] += graph.getNeighborhoodWeight(graph.maxNeighborhoodWeight().getId()) - 1;
            }
        }

        IntStream.of(results).forEach(x -> System.out.println(x / numExperiments));
    }

    public static Set<Pair<Integer, Integer>> generateRandomOrderedPairs(int n) {
        HashSet<Pair<Integer, Integer>> pairs = new HashSet<>();
        Random random = new Random();

        int i, j;
        while (pairs.size() < n) {
            i = random.nextInt(n);
            j = random.nextInt(n);

            if (i != j && !pairs.contains(new Pair<>(i, j)) && !pairs.contains(new Pair<>(j, i))) {
                pairs.add(new Pair<>(i, j));
            }
        }

        return pairs;
    }
}