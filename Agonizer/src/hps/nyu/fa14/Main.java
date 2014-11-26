package hps.nyu.fa14;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class Main {
    static int numNodes;
    static int numGraphs;
    static int numPartitions;
    static List<Graph> graphs;
    static int[][] agonyMatrix;
    static Random rand = new Random();
    static final boolean onlyBestBreed = true;
    private static final int populationSize = 1000;
    static Organism bestOrganism;
    static int bestAgony = Integer.MAX_VALUE;

    public static void prettyPrintAgony() {
        for (int i = 0; i < numGraphs; i++) {
            for (int j = 0; j < numGraphs; j++) {
                System.out.print(agonyMatrix[i][j] + "\t");
            }
            System.out.print("\n");
        }
    }


    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // parse out params
        String paramsString = null;
        try {
            paramsString = reader.readLine();
        } catch (IOException e) {
            System.out.println("Cannot readline");
            return;
        }

        List<Integer> params = new ArrayList<Integer>(Collections2.transform(
                Arrays.asList(paramsString.split(" ")),
                new Function<String, Integer>() {
                    @Override
                    public Integer apply(String s) {
                        return Integer.parseInt(s);
                }
        }));

        numNodes = params.get(0);
        numGraphs = params.get(1);
        numPartitions = params.get(2);

        graphs = new ArrayList<Graph>(numGraphs);

        // Get graph specs
        String graphSpec;
        for (int i = 0; i < numGraphs; i++) {
            Graph newGraph = new Graph(numNodes);

            try {
                graphSpec = reader.readLine();
            } catch (IOException e) {
                System.out.println("Cannot read graph line");
                return;
            }

            String[] graphSpecPairs = graphSpec.split(" ");

            for (String pair : graphSpecPairs) {
                List<Integer> pairIndices = new ArrayList<Integer>(Collections2.transform(
                        Arrays.asList(pair.split(",")),
                        new Function<String, Integer>() {
                            @Override
                            public Integer apply(String s) {
                                return Integer.parseInt(s);
                        }
                }));

                newGraph.edges[pairIndices.get(0)][pairIndices.get(1)] = true;
            }
            graphs.add(newGraph);
            assert AgonyUtil.getAgony(newGraph) == 0;
        }

        agonyMatrix = new int[numGraphs][numGraphs];

        for (int i = 0; i < numGraphs; i++) {
            for (int j = i + 1; j < numGraphs; j++) {
                Graph unionGraph = graphs.get(i).union(graphs.get(j));
                int agony = AgonyUtil.getAgony(unionGraph);
                agonyMatrix[i][j] = agony;
                agonyMatrix[j][i] = agony;
            }
        }

        Organism.setParams(numPartitions, graphs, agonyMatrix, rand);


        List<Organism> population = generatePopulation();

        double averageAgony = 0;
        for (int i = 0; i < 100; i++) {
            generation(population);
            averageAgony += getBestAgony(population);
        }
        System.out.println("average agony: " + averageAgony / 15);
        System.out.println("Best agony: " + bestAgony);
    }

    private static List<Organism> generatePopulation() {
        List<Organism> organisms = new ArrayList<Organism>(populationSize);
        for (int i = 0; i <  populationSize; i++) {
            organisms.add(new Organism());
        }
        return  organisms;
    }

    private static void generation(List<Organism> population) {
        if (onlyBestBreed) {
            int bestSeen = Integer.MAX_VALUE;
            int secondBestSeen = Integer.MAX_VALUE;
            Organism best = null;
            Organism secondBest = null;
            for (Organism organism : population) {
                int agony = organism.getAgony();
                if (agony <= bestSeen) {
                    secondBest = best;
                    secondBestSeen = bestSeen;
                    best = organism;
                    bestSeen = agony;
                } else if (agony <= secondBestSeen) {
                    secondBest = organism;
                    secondBestSeen = agony;
                }
            }
            System.out.println("----");
            printDna(best);
            printDna(secondBest);
            System.out.println("----");
            for (int i = 0; i < populationSize; i++) {
                population.set(i, new Organism(best, secondBest));
            }
        } else {
            int maxAgony = 0;
            for (Organism organism : population) {
                if (organism.getAgony() > maxAgony) {
                    maxAgony = organism.getAgony();
                }
            }

            int totalSquaredDistance = 0;
            List<Integer> cummulativeDistance = new ArrayList<Integer>(populationSize);

            for (Organism organism : population) {
                
            }

        }
        for (Organism organism : population) {
            if (organism.getAgony() < bestAgony) {
                bestAgony = organism.getAgony();
                bestOrganism = organism;
                printDna(bestOrganism);
            }
        }
    }

    public static int getBestAgony(List<Organism> population) {
        int lowestAgony = Integer.MAX_VALUE;
        for (Organism organism : population) {
            int agony = organism.getAgony();
            if (agony < lowestAgony) {
                lowestAgony = organism.getAgony();
            }
        }
        return lowestAgony;
    }

    public static void printDna(Organism organism) {
        for (int i = 0; i < organism.dna.size(); i ++) {
            System.out.print(organism.dna.get(i) + " ");
        }
        System.out.print("\n");
    }

}
