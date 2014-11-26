package hps.nyu.fa14;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Represents a directed graph as a implicitly numbered set of nodes and edges
 * from node i to j that are either present or not
 **/
public class Graph {

    /**
     * Nodes are 1-indexed
     */
    public final int nodes;

    /**
     * Nodes are 1-indexed, so this matrix is 1 larger than it needs to be in each dimension
     */
    public final boolean[][] edges;

    public Graph(int nodeCount) {
        nodes = nodeCount;
        edges = new boolean[nodes + 1][nodes + 1];
    }

    /**
     * Returns the number of directed edges currently in the graph
     * @return
     */
    public int numEdges(){
        int eCount = 0;
        for(int i = 1; i <= nodes; i++){
            for(int j = 1; j <= nodes; j++){
                if(edges[i][j]){
                    eCount++;
                }
            }
        }
        return eCount;
    }
    
    /**
     * Returns true if the graph is weakly connected meaning the undirected
     * version is connected
     * 
     * @return
     */
    public boolean isConnected() {
        // Construct the undirected version of the graph
        Graph c = this.clone();
        for(int i = 1; i <= c.nodes; i++){
            for(int j = 1; j <= c.nodes; j++){
                if(c.edges[i][j] || c.edges[j][i]){
                    c.edges[i][j] = c.edges[j][i] = true;
                }
            }
        }
        // Keep a set of the reachable nodes, start at 1 and walk the connections
        Set<Integer> connectedNodes = new HashSet<Integer>();
        Queue<Integer> addedNodes = new LinkedList<Integer>();
        connectedNodes.add(1);
        addedNodes.add(1);
        while(addedNodes.size() > 0){
            int nodeToExplore = addedNodes.remove();
            // Find all of the nodes connected to this one
            for(int i = 1; i <= c.nodes; i++){
                if(i == nodeToExplore){
                    continue; // don't consider self edges
                }
                if(c.edges[nodeToExplore][i] || c.edges[i][nodeToExplore]){
                    // Add this node if it isn't already in the connected set
                    boolean added = connectedNodes.add(i);
                    if(added){
                        // If we added it to the connected set,
                        // make sure we explore from here to find further connections
                        addedNodes.add(i);
                    }
                }
            }
        }
        // return if all nodes are connected
        return connectedNodes.size() == c.nodes;
    }

    /**
     * Returns true if the graph does not contain any cycles
     * @return
     */
    public boolean isAcyclic() {
        return !(new CycleFinder().hasCycle());
    }
    
    /**
     * Deep copy a graph
     */
    public Graph clone() {
        Graph g = new Graph(nodes);
        for(int i = 1; i <= nodes; i++){
            for(int j = 1; j <= nodes; j++){
                g.edges[i][j] = edges[i][j];
            }
        }
        return g;
    }
    
    /**
     * Returns a new graph with the same nodeset and the union of all of the edges from both graphs
     * @param other
     * @return
     */
    public Graph union(Graph other){
        if(this.nodes != other.nodes){
            throw new IndexOutOfBoundsException("Graph must have same node set");
        }
        Graph g = new Graph(this.nodes);
        for(int i = 1; i <= this.nodes; i++){
            for(int j = 1; j <= this.nodes; j++){
                g.edges[i][j] = this.edges[i][j] || other.edges[i][j];
            }
        }
        return g;
    }

    /**
     * Write the representation of a graph as a set of edges
     * @param bw
     */
    public void write(BufferedWriter bw) throws IOException {
        for(int i = 1; i <= nodes; i++){
            for(int j = 1; j <= nodes; j++){
                if(edges[i][j]){
                    // There is an edge from i to j
                    bw.write(String.format("%d,%d ", i, j));
                }
            }
        }
        bw.newLine();
    }
    
    public static Graph fromString(int nodeCount, String edgeList){
        Graph g = new Graph(nodeCount);
        String[] edges = edgeList.trim().split("\\s");
        for(String e : edges){
            String[] endPoints = e.trim().split(",");
            int from = Integer.parseInt(endPoints[0]);
            int to = Integer.parseInt(endPoints[1]);
            g.edges[from][to] = true;
        }
        return g;
    }
    
    private static final Random RAND = new Random();
    
    /**
     * Generates a weakly connected directed acyclic graph with random edges
     * @param nodeCount
     * @return
     */
    public static Graph randomDAG(int nodeCount){
        Graph g = new Graph(nodeCount);

        // Randomly add nodes - 1 edges as long as it doesn't create a cycle
        int eCount = 0;
        while(eCount < nodeCount - 1 || !g.isConnected()){
            int i = RAND.nextInt(nodeCount) + 1;
            int j = RAND.nextInt(nodeCount) + 1;
            // Don't generate self loops or add edges that already exist
            if(i != j && !g.edges[i][j]){
                g.edges[i][j] = true;
                if(g.isAcyclic()){
                    // Graph is still acyclic, commit edge to the graph
                    eCount++;
                } else {
                    // take out this edge that caused a cycle
                    g.edges[i][j] = false;
                }
            }
        }
        return g;
    }
    
    /**
     * Generates an arbitrary directed graph with n-1 edges
     * (possibly connected)
     * @param nodeCount
     * @return
     */
    public static Graph random(int nodeCount){
        Graph g = new Graph(nodeCount);
        
        int eCount = 0;
        while(eCount < nodeCount - 1){
            int i = RAND.nextInt(nodeCount) + 1;
            int j = RAND.nextInt(nodeCount) + 1;
            // Don't generate self loops or add edges that already exist
            if(i != j && !g.edges[i][j]){
                g.edges[i][j] = true;
                eCount++;
            }
        }
        return g;
    }
    
    // based on implementation from
    // http://algs4.cs.princeton.edu/42directed/DirectedCycle.java.html
    private class CycleFinder {
        
        private final boolean[] marked = new boolean[nodes + 1];
        private final boolean[] onStack  = new boolean[nodes + 1];;
        private boolean cycle = false;
        
        public CycleFinder(){
            for(int v = 1; v <= nodes; v++){
                if(!marked[v]){
                    dfs(v);
                }
            }
        }
        
        private void dfs(int v){
            onStack[v] = true;
            marked[v] = true;
            for(int w = 1; w <= nodes; w++){
                if(!edges[v][w]){
                    continue;  // Only proceed if adjacent
                }
                if(cycle){
                    return; // short circuit
                } else if(!marked[w]){
                    dfs(w);
                } else if(onStack[w]){
                    cycle = true;
                }
            }
            onStack[v] = false;
        }
        
        public boolean hasCycle(){
            return cycle;
        }
    }
}
