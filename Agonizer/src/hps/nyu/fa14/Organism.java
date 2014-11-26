package hps.nyu.fa14;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ling on 25/11/14.
 */
public class Organism {
    public List<Integer> dna;
    public static int dnaSize;
    public static int maxDna;
    public static int[][] agonyMatrix;
    public static List<Graph> graphs;
    public static Random rand;
    private final double mutationRate = 0.3;
    private final boolean doMatching = true;
    private int agony = -1;


    public static void setParams(int maxDna, List<Graph> graphs, int[][] agonyMatrix, Random rand) {
        dnaSize = agonyMatrix.length;
        Organism.maxDna = maxDna;
        Organism.agonyMatrix = agonyMatrix;
        Organism.rand = rand;
        Organism.graphs = graphs;
    }

    // Default constructor generates random organisms
    public Organism() {
        dna = new ArrayList<Integer>(dnaSize);

        for (int i = 0; i < dnaSize; i++) {
           dna.add(rand.nextInt(maxDna));
        }
    }

    // offspring of parents
    public Organism(Organism a, Organism b) {
        if (doMatching) {
            a = a.clone();
            a.match(b);
        }

        dna = new ArrayList<Integer>(dnaSize);

        for (int i = 0; i < dnaSize; i++) {
            int dnaVal;
            // mutation
            if (rand.nextFloat() < mutationRate) {
                dnaVal = rand.nextInt(maxDna);
            } else {
                if (rand.nextFloat() < 0.5) {
                    dnaVal = a.dna.get(i);
                } else {
                    dnaVal = b.dna.get(i);
                }
            }
            dna.add(dnaVal);
        }
    }

    public Organism(List<Integer> dna) {
        this.dna = new ArrayList<Integer>(dna);
    }

    public Organism clone() {
        return new Organism(dna);
    }

    public void match(Organism other) {
        boolean[] matched = new boolean[maxDna];
        int[][] counters = new int[maxDna][maxDna];
        int[] map = new int[maxDna];
        int[] localCounters = new int[maxDna];

        for (int i = 0; i < dnaSize; i++) {
            localCounters[dna.get(i)]++;
            counters[dna.get(i)][other.dna.get(i)]++;
        }

        for (int i = 0; i < maxDna; i++) {
            int biggestSeen = -1;
            int biggestCount = 0;

            for (int j = 0; j < maxDna; j++) {
                if (localCounters[j] > biggestCount) {
                    biggestCount = localCounters[j];
                    biggestSeen = j;
                }
            }

            // if no more pairings are to be made
            if (biggestSeen == -1) {
                break;
            }

            // zero it out to indicate it's used
            localCounters[biggestSeen] = 0;

            int biggestPartner = -1;
            int biggestPartnerCount = 0;
            for (int j = 0; j < maxDna; j++) {
                if (matched[j]) {
                    continue;
                }
                if (counters[biggestSeen][j] > biggestPartnerCount) {
                    biggestPartner = j;
                    biggestPartnerCount = counters[biggestSeen][j];
                }
            }

            if (biggestPartner == -1) {
                for (int j = 0; j < maxDna; j++) {
                    if (!matched[j]) {
                        biggestPartner = j;
                        break;
                    }
                }
            }

            map[biggestSeen] = biggestPartner;
            matched[biggestPartner] = true;
        }

        // now convert
        for (int i = 0; i < dnaSize; i++) {
            int currentGene = dna.get(i);
            dna.set(i, map[currentGene]);
        }
    }

    public int getAgony() {
        if (agony == -1) {
            List<List<Integer>> clusters = new ArrayList<List<Integer>>(maxDna);
            for (int i = 0; i < maxDna; i++) {
                clusters.add(new ArrayList<Integer>());
            }

            for (int i = 0; i < dnaSize; i++) {
                int cluster = dna.get(i);
                clusters.get(cluster).add(i);
            }
            agony = 0;
            for (int i = 0; i < maxDna; i++) {
                int maxAgony = 0;
                List<Integer> clusterNodes = clusters.get(i);

                // calc the max agony in ths cluster
                for (int j = 0; j < clusterNodes.size(); j++) {
                    for (int k = j + 1; k < clusterNodes.size(); k++) {
                        int agony = agonyMatrix[j][k];
                        if (agony > maxAgony) {
                            maxAgony = agony;
                        }
                    }
                }
                agony += maxAgony;
            }
        }
        return agony;
    }
}
