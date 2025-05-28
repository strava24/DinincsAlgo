/**
 * @author w2082277/20232931 Robosriyas Stravakske Fernando
 */
public class Edge {

    public int from; // Indicates the starting node of the edge
    public int to; // Indicated the ending node of edge

    public Edge residual; // Indicates the residual edge

    public long flow; // current flow through the edge
    public final long CAPACITY; // maximum flow allowed through the edge

    public Edge(int from, int to, long capacity) {
        this.from = from;
        this.to = to;
        this.CAPACITY = capacity;
    }

    /**
     * Method to classify residual edges
     * @return true if the edge is residual
     */
    public boolean isResidual() {
        return CAPACITY == 0;
    }

    /**
     * Method to return the remaining capacity
     * @return the remaining capacity
     */
    public long remainingCapacity() {
        return CAPACITY - flow;
    }

    /**
     * Method to add flow to the exising flow, also modifies the residual edge
     * @param bottleNeck max cut flow of the augmenting path
     */
    public void augment(long bottleNeck) {
        flow += bottleNeck;
        residual.flow -= bottleNeck;
    }

    public String toString(int s, int t) {
        String u = (from == s) ? "s" : ((from == t) ? "t" : String.valueOf(from));
        String v = (to == s) ? "s" : ((to == t) ? "t" : String.valueOf(to));
        return String.format(
                "Edge %s -> %s | flow = %3d | capacity = %3d | is residual: %s",
                u, v, flow, CAPACITY, isResidual());
    }
}