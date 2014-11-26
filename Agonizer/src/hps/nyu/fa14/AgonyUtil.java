package hps.nyu.fa14;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgonyUtil {

    /**
     * Returns the agony of a graph as detailed in:
     * http://www.cs.rutgers.edu/~iftode/www11_socialhierarchy.pdf
     * 
     * @param graph An (N+1)x(N+1) adjacency matrix representation of a graph where nodes are 1-indexed.
     * Index 0 is unused in each dimension.  A value of true at i,j indicates a directed edge from ndoe i to node j
     * @return The agony as calculated under an optimal ranking.
     */
    public static int getAgony(Graph g){
        // Copy g so that it does not get modified
        g = g.clone();
        
        int[][] w = new int[g.nodes + 1][g.nodes + 1];
        for(int x = 1; x <= g.nodes; x++) {
            for(int y = 1; y <= g.nodes; y++) {
                if(g.edges[x][y]) {
                    w[x][y] = -1;
                }
            }
        }
        // we now calculate the optimal ranking for graph g
        // optimal ranking is one which minimized agony of the graph
        List<Integer> cycleNodes = new CycleFinder()
                .getNodesOfCycleWithNegativeEdges(g, w);
        while (cycleNodes != null && cycleNodes.size() > 0) {
            for(int m = 0; m < cycleNodes.size() - 1; m++) {
                // there is an edge from mth node to m+1th node
                int u = cycleNodes.get(m);
                int v = cycleNodes.get(m + 1);
                // and one more edge from the last node to the 0th node
                w[u][v] = w[u][v] * -1;
                // reverses the edge
                g.edges[v][u] = true;
                g.edges[u][v] = false;
            }
            cycleNodes = new CycleFinder()
                    .getNodesOfCycleWithNegativeEdges(g, w);
        }

        reverseAllPositiveEdges(g, w);

        // all edges in g1 labeled -1 form a DAG
        // rest of the edges form an eulerian subgraph
        // label all vertices as 0
        int[] labels = new int[g.nodes + 1];
        List<Integer> faultyEdge = null;
        while ((faultyEdge = getFaultyEdgeIfExists(w, labels)).size() > 0) {
            labels[faultyEdge.get(1)] = labels[faultyEdge.get(0)]
                    - w[faultyEdge.get(0)][faultyEdge.get(1)];
        }

        // calculate agony for this graph now.
        // this is the agony of the pair i and j
        return getAgony(w, labels);
    }
    
    public static int getAgony(int[][] graph, int[] labels) {
        int agony = 0;
        for(int u = 1; u < graph.length; u++) {
            for(int v = 1; v < graph.length; v++) {
                if(graph[u][v] > 0) {
                    agony += Math.max(labels[u] - labels[v] + 1, 0);
                }
            }
        }
        return agony;
    }
    
    private static void reverseAllPositiveEdges(Graph g, int[][] weights) {
        for(int i = 1; i <= g.nodes; i++) {
            for(int j = 1; j <= g.nodes; j++) {
                if(weights[i][j] == 1) {
                    g.edges[j][i] = true;
                    g.edges[j][i] = false;
                }
            }
        }
    }

    private static List<Integer> getFaultyEdgeIfExists(int[][] graph,
            int[] labels) {
        List<Integer> nodes = new ArrayList<Integer>();
        for(int i = 1; i < graph.length; i++) {
            for(int j = 1; j < graph.length; j++) {
                if(!(graph[i][j] != 0) && (labels[j] < labels[i] - graph[i][j])) {
                    nodes.add(i);
                    nodes.add(j);
                    return nodes;
                }
            }
        }
        return nodes;
    }
    
    static class CycleFinder {

        private boolean[] marked;
        private boolean[] onStack;
        private Graph graph;
        private int[][] weights;
        int[] edgeTo;
        List<Integer> cycleNodes = null;

        public CycleFinder() {
        }

        private void dfs(int v) {
            onStack[v] = true;
            marked[v] = true;
            for(int w = 1; w <= graph.nodes; w++) {
                if(!graph.edges[v][w]) {
                    continue; // Only proceed if adjacent
                }
                if(cycleNodes != null) {
                    return;
                } else if(!marked[w]) {
                    // Check that it is a negative weight edge
                    if(graph.edges[v][w] && weights[v][w] == -1) {
                        edgeTo[w] = v;
                    }
                    dfs(w);
                    // add this to the cycle
                } else if(onStack[w] && graph.edges[v][w]
                        && weights[v][w] == -1) {
                    // we know all the nodes in this cycle - they are the ones
                    // that have
                    // onstack set to true
                    cycleNodes = new ArrayList<Integer>();
                    for(int m = v; m != w && m != 0; m = edgeTo[m]) {
                        cycleNodes.add(m);
                    }
                    cycleNodes.add(w);
                    cycleNodes.add(v);
                    // Need to return the edges in the correct order
                    // (following the direction of edges)
                    Collections.reverse(cycleNodes);
                }
            }
            onStack[v] = false;
        }

        public List<Integer> getNodesOfCycleWithNegativeEdges(Graph g, int[][] w) {
            graph = g;
            weights = w;
            marked = new boolean[graph.nodes + 1];
            edgeTo = new int[graph.nodes + 1];
            onStack = new boolean[graph.nodes + 1];
            for(int v = 1; v <= graph.nodes; v++) {
                if(!marked[v]) {
                    dfs(v);
                    if(cycleNodes != null) {
                        return cycleNodes;
                    }
                }
            }
            return cycleNodes;
        }
    }
}
