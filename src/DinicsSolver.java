import java.util.*;

/**
 * @author w2082277/20232931 Robosriyas Stravakske Fernando
 */
public class DinicsSolver {

    static final long INF = Long.MAX_VALUE / 2;

    // network properties
    private final int N; // number of vertices
    private final int S;  // source
    private final int T; // target

    private boolean solved; // Indicates whether max flow is calculated
    private long maxFlow; // variable to hold the flow

    // Data structure that represents the network flow
    // each element is a list of edges that starts from the node
    private List<Edge>[] graph; //

    // Level graph for Dinic's algorithm
    private int[] level;

    // Reusable data structures for Dijkstra's algorithm
    private long[] maxFlowArr; // array to store max flow that can reach each node
    private Edge[] edgeTo; // array to keep track of which node was used to reach node on the best path so far
    private boolean[] processed; // array to keep track of which node has been fully explored so far

    // For tracking algorithm performance - using dynamic lists instead of fixed arrays
    private int bfsLevelCount = 0; // Keeps track of how many times BFS was run / how many times level graphs were formed
    private List<Long> flowPerLevel; //  keeps track of flow per level graph
    private List<Integer> pathsPerLevel; // keeps track of paths per level graph

    public DinicsSolver(int n, int s, int t) {
        this.N = n;
        this.S = s;
        this.T = t;
        initializeEmptyFlowGraph();

        // Initialize algorithm data structures
        this.level = new int[n];
        this.maxFlowArr = new long[n];
        this.edgeTo = new Edge[n];
        this.processed = new boolean[n];

        // Initialize tracking lists - these can grow as needed
        this.flowPerLevel = new ArrayList<>();
        this.pathsPerLevel = new ArrayList<>();
    }

    /**
     * Method holding the logic of Dinic's algo
     * Uses BFS to create level graphs and Dijkstra's algo for path finding
     */
    public void solve() {
        if (solved) return;

        System.out.println("Starting Dinic's algorithm with Dijkstra's for finding paths");

        while (bfs()) {
            bfsLevelCount++;
            System.out.println("BFS Level " + bfsLevelCount  + ": Building level graph...");

            long startTime = System.currentTimeMillis();
            int pathCount = 0;
            long levelFlow = 0;

            // Looping till an augmenting level path is found using the Dijkstra's
            for (long f = findPathDijkstra(); f != 0; f = findPathDijkstra()) {
                maxFlow += f;
                levelFlow += f;
                pathCount++;

                // Print progress periodically for large networks
                if (pathCount % 10000 == 0) {
                    System.out.println("Found " + pathCount + " paths so far in this level\n");
                }
            }

            // Storing for reporting
            flowPerLevel.add(levelFlow);
            pathsPerLevel.add(pathCount);

            System.out.println("BFS Level " + bfsLevelCount + ": Found " + pathCount + " paths adding " + levelFlow + " flow, total flow: " + maxFlow + " (" +
                    (System.currentTimeMillis() - startTime) / 1000.0 + " seconds)");

            // garbage collection after large level processing
            if (pathCount > 10000) {
                System.gc();
            }
        }

        System.out.println("\nDinic's algorithm completed");
        solved = true;
    }

    /**
     * Method to initialize a graph network
     */
    private void initializeEmptyFlowGraph() {
        graph = new List[N];
        for (int i = 0; i < N; i++) {
            graph[i] = new ArrayList<>();
        }
    }

    /**
     * Method to add an edge to the network
     *
     * @param from starting vertex of the forward edge
     * @param to ending vertex of the forward edge
     * @param capacity capacity of the edge
     */
    public void addEdge(int from, int to, long capacity) {
        if (capacity <= 0) // An edge cannot have a negative or 0 capacity in a valid network flow problem
            throw new IllegalArgumentException("Forward edge capacity <= 0");
        Edge e1 = new Edge(from, to, capacity); // Creating a forward edge
        Edge e2 = new Edge(to, from, 0); // Creating a residual edge
        e1.residual = e2;
        e2.residual = e1;
        graph[from].add(e1);
        graph[to].add(e2);
    }

    /**
     * Returns the residual graph after computing the maximum flow
     *
     * @return residual graph
     */
    public List<Edge>[] getGraph() {
        solve();
        return graph;
    }

    /**
     * Returns the maximum flow from the source to the sink
     *
     * @return max flow
     */
    public long getMaxFlow() {
        solve();
        return maxFlow;
    }

    /**
     * Implementation of BFS algorithm which is used to create level graphs
     *
     * @return boolean value to indicate if more flow can be pushed
     */
    private boolean bfs() {
        // Initialize all nodes to have no level (indicates that no node has been visited yet)
        Arrays.fill(level, -1);

        // Create a queue for BFS
        Queue<Integer> openList = new LinkedList<>();

        // Start from the source node
        openList.offer(S);
        level[S] = 0;

        boolean sinkReached = false;

        while (!openList.isEmpty()) {
            // Get the next node from the queue
            int node = openList.poll();

            // Early termination optimization
            if (sinkReached && level[node] > level[T]) continue;

            // If we've reached our target, we could stop here, but we continue to
            // build the complete level graph up to the sink's level
            if (node == T) {
                sinkReached = true;
            }

            // Explore all edges from this node
            for (Edge edge : graph[node]) {
                long cap = edge.remainingCapacity();

                // Only consider edges with remaining capacity and unvisited destinations
                if (cap > 0 && level[edge.to] == -1) {
                    level[edge.to] = level[node] + 1;
                    openList.offer(edge.to);
                }
            }
        }

        // Return whether we were able to reach the sink node
        return level[T] != -1;
    }

    /**
     * Dijkstra's algorithm adapted for finding augmenting paths with maximum capacity in a flow network
     *
     * @return a bottleneck value if augmenting paths do exist, returns 0 if no augmenting path is found
     */
    private long findPathDijkstra() {
        // Reset reusable data structures
        Arrays.fill(maxFlowArr, 0);
        Arrays.fill(processed, false);
        // No need to reset edgeTo as we only care about entries where maxFlowArr > 0

        maxFlowArr[S] = INF;

        // Priority queue ordered by flow capacity (descending)
        // Limiting initial capacity for memory efficiency
        PriorityQueue<Integer> pq = new PriorityQueue<>(
                Math.min(N, 1024),
                (a, b) -> Long.compare(maxFlowArr[b], maxFlowArr[a]));

        pq.add(S);

        while (!pq.isEmpty() && !processed[T]) {
            int node = pq.poll();

            // Skipping if we've processed this node already
            if (processed[node]) continue;
            processed[node] = true;

            // Stopping if we've reached the sink
            if (node == T) break;

            // Exploring all edges from this node that follow the level graph
            for (Edge edge : graph[node]) {
                // Only considering edges in the level graph with remaining capacity
                if (edge.remainingCapacity() > 0 && level[edge.to] == level[node] + 1) {
                    // Calculating the bottleneck flow to this edge's destination
                    long newFlow = Math.min(maxFlowArr[node], edge.remainingCapacity());

                    // If we found a better path to this node
                    if (newFlow > maxFlowArr[edge.to]) {
                        maxFlowArr[edge.to] = newFlow;
                        edgeTo[edge.to] = edge;
                        pq.add(edge.to);
                    }
                }
            }
        }

        // If we couldn't reach the sink, return 0
        if (maxFlowArr[T] == 0) return 0;

        // Augmenting flow along the path
        long bottleNeck = maxFlowArr[T];

        // Tracing back the path and augment each edge
        int node = T;
        while (node != S) {
            Edge edge = edgeTo[node];
            edge.augment(bottleNeck);
            node = edge.from;
        }

        return bottleNeck;
    }

    /**
     * Method to gain additional information explaining how the maximum flow was obtained.
     * @return a string with all the information
     */
    public String getProgressSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nAlgorithm Progress Summary:\n");
        for (int i = 0; i < bfsLevelCount; i++) {
            sb.append(String.format("Level %d: %d paths, %d flow\n",
                    i+1, pathsPerLevel.get(i), flowPerLevel.get(i)));
        }
        return sb.toString();
    }

}
