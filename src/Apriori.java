import java.util.*;

public class Apriori {
    /***
     * The TRANSACTIONS 2-dimensional array holds the full data set for the lab assignment
     */
    static final int[][] TRANSACTIONS = new int[][] {{1, 2, 3, 4, 5, 6}, {1, 3, 5, 6}, {2, 3, 5}, {1, 5, 6},
            {1, 3, 4, 6}, {2, 3, 5}, {2, 3, 5}, {3, 4, 5}, {4, 5}, {2}, {2, 3}, {2, 3, 4}, {3, 4, 5}};

    static final int[][] BOOK_TRANSACTIONS = new int[][] {{1, 2, 5}, {2, 4}, {2, 3}, {1, 2, 4}, {1, 3},
            {2, 3}, {1, 3}, {1, 2, 3, 5}, {1, 2, 3}};

    public static void main(String[] args) {
        // TASK: Select a reasonable support threshold via trial-and-error. Can either be percentage or absolute value.
        final int supportThreshold = 3;
        final int confidenceThreshold = 60;
        List<ItemSet> result = apriori(TRANSACTIONS, supportThreshold, confidenceThreshold);

        System.out.println("Final result of apriori: " + result.size());
        System.out.println();
    }


    public static List<ItemSet> apriori(int[][] transactions, int supportThreshold, int confidenceThreshold) {
        int k;
        // Itemsets mapped to their countSupport
        Hashtable<ItemSet, Integer> frequentItemSets = generateFrequentItemSetsLevel1(transactions, supportThreshold);
        List<ItemSet> finalItemSets = new ArrayList<>();

        for (k = 1; frequentItemSets.size() > 0; k++) {
            System.out.print( "Finding frequent itemsets of length " + (k + 1) + "…" );
            frequentItemSets = generateFrequentItemSets(supportThreshold, transactions, frequentItemSets);

            finalItemSets.addAll(frequentItemSets.keySet());
            System.out.println( " found " + frequentItemSets.size() );
        }

        // TASK: create association rules from the frequent itemsets
        List<String> confidenceRules = generateConfidenceRules(finalItemSets, confidenceThreshold);
        for(String rule : confidenceRules) {
            System.out.println(rule);
        }

        return finalItemSets;
    }

    private static Hashtable<ItemSet, Integer> generateFrequentItemSets(int supportThreshold, int[][] transactions,
                                                                        Hashtable<ItemSet, Integer> lowerLevelItemSets) {
        // TASK: first generate candidate itemsets from the lower level itemsets
        Hashtable<ItemSet, Integer> frequentItemSets = new Hashtable<>();

        List<ItemSet> frequentItemSetsList = new ArrayList<>();
        for(ItemSet setA : lowerLevelItemSets.keySet()) {
            for(ItemSet setB : lowerLevelItemSets.keySet()) {
                // we only join sets where the LAST element differs between the itemsets
                if(checkForIdenticalItems(setA, setB)) {
                    ItemSet joined = joinSets(setA, setB);
                    // if the subsets of joined are all frequent, then add joined to list of frequent itemsets
                    if(!hasInfrequentItemsets(joined, lowerLevelItemSets)) {
                        frequentItemSetsList.add(joined);
                    }
                }
            }
        }

        // TASK: now check the support for all candidates and add only those that have enough support to the set
        for(ItemSet item : frequentItemSetsList) {
            int support = countSupport(item.set, transactions);
            if(support >= supportThreshold) {
                frequentItemSets.put(item, support);
            }
        }

        return frequentItemSets;
    }

    private static boolean hasInfrequentItemsets(ItemSet joined, Hashtable<ItemSet, Integer> lowerLevelItemSets) {
        List<ItemSet> subsets = generateSubsets(joined);

        for(ItemSet set : subsets) {
            // System.out.println(set.toString());
            if(!lowerLevelItemSets.containsKey(set)) {
                return true;
            }
        }

        return false;
    }

    private static List<ItemSet> generateSubsets(ItemSet joined) {
        List<ItemSet> subsets = new ArrayList<>();
        List<Integer> items = new ArrayList<>();

        for(int item : joined.set) {
            items.add(item);
        }

        for(int i = 0; i < items.size(); i++) {
            List<Integer> clone = new ArrayList<>(items);
            clone.remove(i);

            int[] itemSet = new int[clone.size()];
            for(int j = 0; j < itemSet.length; j++) {
                itemSet[j] = clone.get(j);
            }

            subsets.add(new ItemSet(itemSet));
        }

        return subsets;
    }

    /**
     * Generates all non-empty subsets of a itemset of length itemset - 1
     * @param itemset the set to be split into subsets
     * @return a list of itemsets
     */
    private static List<ItemSet> generateAllSubsets(ItemSet itemset) {
        List<ItemSet> subsets = new ArrayList<>();

        // creating all subsets of a set
        int n = itemset.set.length;
        for(int i = 0; i < (1<<n); i++) {
            List<Integer> temp = new ArrayList<>();
            for(int j = 0; j < n; j++) {
                if((i & (1<<j)) > 0){
                    temp.add(itemset.set[j]);
                }
            }

            // don't add the empty set
            if(!temp.isEmpty() && temp.size() != n) {
                int[] newSet = new int[temp.size()];
                for(int k = 0; k < temp.size(); k++) {
                    newSet[k] = temp.get(k);
                }

                ItemSet subset = new ItemSet(newSet);
                subsets.add(subset);
            }
        }

        return subsets;
    }

    private static boolean checkForIdenticalItems(ItemSet setA, ItemSet setB) {
        for(int i = 0; i < setA.set.length - 1; i++) {
            if(setB.set[i] != setA.set[i]) {
                return false;
            }
        }
        return setA.set[setA.set.length - 1] < setB.set[setB.set.length - 1];
    }

    private static ItemSet joinSets(ItemSet first, ItemSet second) {
        // using a dynamic collection since end size is unknown to begin with
        List<Integer> newItems = new ArrayList<>();

        // add all items from first ItemSet
        for(int item : first.set) {
            newItems.add(item);
        }

        // only add items from second itemset if not already present (avoid duplicates)
        for(int item : second.set) {
            if(!newItems.contains(item)) {
                newItems.add(item);
            }
        }

        // sort items and add to int array
        Collections.sort(newItems);
        int[] items = new int[newItems.size()];
        for(int i = 0; i < newItems.size(); i++) {
            items[i] = newItems.get(i);
        }

        // return new itemset with joined items
        return new ItemSet(items);
    }

    private static Hashtable<ItemSet, Integer> generateFrequentItemSetsLevel1(int[][] transactions, int supportThreshold) {
        Hashtable<ItemSet, Integer> frequentItemSets = new Hashtable<>();

        // for every transaction
        for(int i = 0; i < transactions.length; i++) {
            // for every item in transaction
            for(int j = 0; j < transactions[i].length; j++) {
                // create int array with 1-items
                int[] items = new int[] {transactions[i][j]};
                int countSupport = countSupport(items, transactions);
                ItemSet itemSet = new ItemSet(items);

                if(!frequentItemSets.contains(itemSet) && countSupport >= supportThreshold) {
                    frequentItemSets.put(itemSet, countSupport);

                }
            }
        }
        return frequentItemSets;
    }

    /**
     * Calculates the count support for a given itemset
     * @param itemSet an itemsets set
     * @param transactions all transactions
     * @return the count support for the given itemset
     */
    private static int countSupport(int[] itemSet, int[][] transactions) {
        // assumes that items in ItemSets and transactions are both unique
        int supportCount =  0;
        // for every transaction
        for(int i = 0; i < transactions.length; i++) {
            int count = 0;
            // for every item in transaction
            for(int j = 0; j < transactions[i].length; j++) {
                // compare with every item in itemset
                for(int k = 0; k < itemSet.length; k++) {
                    int element = transactions[i][j];
                    int itemSetElement = itemSet[k];
                    if(element == itemSetElement) count++;
                }
            }

            if(count == itemSet.length) supportCount++;

        }
        return supportCount;
    }

    /**
     * Calculates confidence rules for the frequent itemsets
     * @param frequentItemSets list of frequent ItemSets
     * @param confidenceThreshold lower boundary for confidence level
     * @return a list of strings representing the derived confidence rules
     */
    public static List<String> generateConfidenceRules(List<ItemSet> frequentItemSets, int confidenceThreshold) {
        List<String> confidenceRules = new ArrayList<>();

        // for every frequent itemset
        for(ItemSet set : frequentItemSets) {
            StringBuilder string = new StringBuilder();
            string.append("\n*** Frequent ItemSet: " + set.toString() + "***");
            //System.out.println("\n*** Frequent ItemSet: " + set.toString() + "***");

            // get a list of subsets, excl. the empty set and the identical set
            List<ItemSet> subsets = generateAllSubsets(set);

            // for every subset
            for(ItemSet subset : subsets) {
                // for every other subset
                for(ItemSet otherSubset : subsets) {
                    // if they don't contain the same elements
                    if(!subset.containsSameElement(otherSubset)) {
                        // and if the two subsets together are the same length as original frequent itemset
                        if(subset.set.length + otherSubset.set.length == set.set.length) {
                            // calculate the confidence
                            int relativeSupport = countSupport(set.set, TRANSACTIONS) * 100 / TRANSACTIONS.length;
                            int confidence = (countSupport(set.set, TRANSACTIONS) * 100) / countSupport(subset.set, TRANSACTIONS);
                            // if it meets the confidence threshold then add string to list
                            double kulc = calculateKulczynski(subset, otherSubset, set);
                            if(confidence > confidenceThreshold) {
                                string.append("\n" + subset.toString() + " --> " + otherSubset.toString());
                                string.append(" = [support = " + relativeSupport + "%, confidence = " + confidence + "%, ");
                                string.append(" correlation = " + kulc + "]");
                                confidenceRules.add(string.toString());

                            }
                        }
                    }
                }
            }
        }

        return confidenceRules;
    }

    // calculate the kulczynski score
    private static double calculateKulczynski(ItemSet subset, ItemSet otherSubset, ItemSet set) {
        double confidenceBgivenA = countSupport(set.set, TRANSACTIONS) / countSupport(subset.set, TRANSACTIONS);
        double confidenceAgivenB = countSupport(set.set, TRANSACTIONS) / countSupport(otherSubset.set, TRANSACTIONS);

        return (confidenceAgivenB + confidenceBgivenA) / 2;
    }
}