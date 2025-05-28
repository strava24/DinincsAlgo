import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * @author w2082277/20232931 Robosriyas Stravakske Fernando
 */
public class Main {
    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();

            String filePath = "benchmarks/bridge_10.txt";
            System.out.println("Reading file: " + filePath);

            // Creating a buffered reader
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            // Reading the number of nodes
            int n = Integer.parseInt(reader.readLine().trim());
            System.out.println("Network has " + n + " nodes");

            // Source is node 0, target is node n-1
            int s = 0;
            int t = n - 1;

            // Creating the Dinic's solver
            DinicsSolver solver = new DinicsSolver(n, s, t);

            // Reading edges
            String line;
            int edgeCount = 0;
            System.out.println("Reading edges...");

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 3) {
                    int from = Integer.parseInt(parts[0]);
                    int to = Integer.parseInt(parts[1]);
                    long capacity = Long.parseLong(parts[2]);
                    solver.addEdge(from, to, capacity);
                    edgeCount++;

                    // Printing progress for large files
                    if (edgeCount % 1000000 == 0) {
                        System.out.println("Processed " + edgeCount + " edges so far...");
                    }
                }
            }
            reader.close(); // closing reader

            System.out.println("Finished reading " + edgeCount + " edges");
            System.out.println("Network construction time: " +
                    (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");

            // Optimize edge ordering for better cache locality
            System.out.println("Optimizing graph representation...");

            // Solving and print the maximum flow
            System.out.println("Computing maximum flow...");
            long solveStartTime = System.currentTimeMillis();

            long maxFlow = solver.getMaxFlow();

            long solveTime = System.currentTimeMillis() - solveStartTime;
            System.out.println("Maximum flow: " + maxFlow);
            System.out.println("Computation time: " + solveTime / 1000.0 + " seconds");

            // Printing algorithm progress summary for the report
            System.out.println(solver.getProgressSummary());

            // For large networks, only printing a summary of the flow
            int flowEdges = 0;
            for (int i = 0; i < n; i++) {
                for (Edge e : solver.getGraph()[i]) {
                    if (!e.isResidual() && e.flow > 0) {
                        flowEdges++;
                    }
                }
            }
            System.out.println("\nFlow summary: " + flowEdges + " edges have positive flow");

            // Capacity and conservation verification for the report
            System.out.println("\nFlow verification:");
            verifyFlow(solver.getGraph(), n, s, t);

            System.out.println("Total execution time: " +
                    (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numbers: " + e.getMessage());
        }
    }


    /**
     * Method to verify if the flow obeys the capacity constraint and conservation condition
     *
     * @param graph adjacency list of the network
     * @param n number of nodes
     * @param s source
     * @param t target/sink
     */
    private static void verifyFlow(List<Edge>[] graph, int n, int s, int t) {
        boolean capacityConstraints = true;
        boolean flowConservation = true;

        // Check capacity constraints
        for (int i = 0; i < n; i++) {
            for (Edge e : graph[i]) {
                // All of the non residual edges should obey the capacity constraints
                if (!e.isResidual() && (e.flow < 0 || e.flow > e.CAPACITY)) {
                    capacityConstraints = false;
                    System.out.println("Capacity violation: Edge (" + e.from + "->" + e.to + ") has flow " + e.flow + ", capacity " + e.CAPACITY);
                    break;
                }
            }
            if (!capacityConstraints) break;
        }

        // Check flow conservation - after running the loop each element (node) contains (total incoming flow) – (total outgoing flow)
        long[] netFlow = new long[n];
        for (int i = 0; i < n; i++) {
            for (Edge e : graph[i]) {
                if (!e.isResidual()) {
                    netFlow[e.from] -= e.flow;
                    netFlow[e.to] += e.flow;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            // according to flow conservation (total incoming flow) – (total outgoing flow) should be always 0 (non-source/sink)
            if (i != s && i != t && netFlow[i] != 0) {
                flowConservation = false;
                System.out.println("Conservation violation: Node " + i + " has net flow " + netFlow[i]);
                break;
            }
        }

        System.out.println("- All edges respect capacity constraints: " +
                (capacityConstraints ? "Yes" : "No"));
        System.out.println("- Flow conservation at all non-source/sink nodes: " +
                (flowConservation ? "Yes" : "No"));
        System.out.println("- Total flow out of source: " + Math.abs(netFlow[s]));
        System.out.println("- Total flow into sink: " + netFlow[t]);
    }
}