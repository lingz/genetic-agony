package hps.nyu.fa14;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.sun.xml.internal.bind.api.impl.NameConverter;

public class Main {
    static int numNodes;
    static int numGraphs;
    static int numPartitions;
    static List<Graph> graphs;
    static int[][] agonyMatrix;
    static Random rand = new Random();
    static final boolean onlyBestBreed = true;
    private static final int populationSize = 1000;
    private static final int numGenerations = 1000;
    static Organism bestOrganism;
    static int bestAgony = Integer.MAX_VALUE;
    static String outfile;

    public static void prettyPrintAgony() {
        for (int i = 0; i < numGraphs; i++) {
            for (int j = 0; j < numGraphs; j++) {
                System.out.print(agonyMatrix[i][j] + "\t");
            }
            System.out.print("\n");
        }
    }


    public static void main(String[] args) {
        outfile = args[0];

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

        for (int i = 0; i < 10; i++) {
            System.out.println("Cycle done");
            List<Organism> population = generatePopulation();

            for (int j = 0; j < numGenerations; j++) {
                generation(population);
            }
        }
    }

    private static List<Organism> generatePopulation() {
        List<Organism> organisms = new ArrayList<Organism>(populationSize);
        for (int i = 0; i <  populationSize; i++) {
            organisms.add(new Organism());
        }
        return  organisms;
    }

    private static void printBest() {
        for (int i = 0; i < numGraphs; i++) {
            System.out.println(bestOrganism.dna.get(i) + 1);
        }
    }

    private static void writeBest() {
        try {
            File file = new File(outfile + ".tmp");

            file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < numGraphs; i++) {
                bw.write(new Integer(bestOrganism.dna.get(i) + 1).toString());
                bw.newLine();
            }
            bw.close();
            CopyOption[] options = new CopyOption[]{
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            };
            Files.move(Paths.get(outfile + ".tmp"), Paths.get(outfile), options);
        } catch (IOException e) {
            System.out.println("IO Error");
        }

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

            List<Integer> cumulativeDistance = new ArrayList<Integer>(populationSize);

            for (int i = 0; i < populationSize; i++) {
                Organism organism = population.get(i);
                int distance = maxAgony - organism.getAgony();
                int transformedDistance = 1 + distance * distance;
                if (i != 0) {
                    transformedDistance += cumulativeDistance.get(i - 1);
                }
                cumulativeDistance.add(transformedDistance);
            }

            List<Organism> newPopulation = new ArrayList<Organism>(populationSize);

            for (int i = 0; i < populationSize; i++) {
                Organism parentA = population.get(weightedRandom(cumulativeDistance));
                Organism parentB = population.get(weightedRandom(cumulativeDistance));
                newPopulation.add(new Organism(parentA, parentB));
            }

            population = newPopulation;
        }
        for (Organism organism : population) {
            if (organism.getAgony() < bestAgony) {
                bestAgony = organism.getAgony();
                bestOrganism = organism;
                System.out.println("New Best: " + organism.getAgony());
                writeBest();
            }
        }
    }

    private static int weightedRandom(List<Integer> cumulativeWeights) {
        int totalWeight = cumulativeWeights.get(cumulativeWeights.size() - 1);
        int randPick = 1 + rand.nextInt(totalWeight);
        for (int i = 0; i < cumulativeWeights.size(); i++) {
            if (cumulativeWeights.get(i) >= randPick) {
                return i;
            }
        }
        return -1;
    }

    public static Organism getBest(List<Organism> population) {
        int lowestAgony = Integer.MAX_VALUE;
        Organism best = null;
        for (Organism organism : population) {
            int agony = organism.getAgony();
            if (agony < lowestAgony) {
                lowestAgony = organism.getAgony();
                best = organism;
            }
        }
        return best;
    }

    public static void printDna(Organism organism) {
        for (int i = 0; i < organism.dna.size(); i ++) {
            System.out.print(organism.dna.get(i) + " ");
        }
        System.out.print("\n");
    }

}
