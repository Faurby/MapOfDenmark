package bfst21.address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Ternary search trie from the algs4 library by Robert Sedgewick and Kevin Wayne.
 * <p>
 * Modified to fit the mapping program. Has methods to find address and address suggestions.
 * Implements Serializable so it can be saved in an .obj file.
 */
public class TST implements Serializable {

    private static final long serialVersionUID = -3195198871698251378L;

    private int n;              // size
    private Node root;   // root of TST

    private static class Node implements Serializable {
        private static final long serialVersionUID = 1097052710816157996L;

        private byte c;                 // character
        private Node left, mid, right;  // left, middle, and right subtries
        private List<OsmAddress> val;   // value associated with string
    }

    /**
     * Initializes an empty string symbol table.
     */
    public TST() {
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     *
     * @return the number of key-value pairs in this symbol table
     */
    public int size() {
        return n;
    }

    /**
     * Does this symbol table contain the given key?
     *
     * @param key the key
     * @return {@code true} if this symbol table contains {@code key} and
     * {@code false} otherwise
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public boolean contains(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to contains() is null");
        }
        return get(key) != null;
    }

    /**
     * Returns the value associated with the given key.
     *
     * @param key the key
     * @return the value associated with the given key if the key is in the symbol table
     * and {@code null} if the key is not in the symbol table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public List<OsmAddress> get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with null argument");
        }
        if (key.length() == 0) throw new IllegalArgumentException("key must have length >= 1");
        Node x = get(root, key, 0);
        if (x == null) return null;
        return x.val;
    }

    // return subtrie corresponding to given key
    private Node get(Node x, String key, int d) {
        if (x == null) return null;
        if (key.length() == 0) throw new IllegalArgumentException("key must have length >= 1");
        byte c = Alphabet.getByteValue(key.charAt(d));
        if (c < x.c) return get(x.left, key, d);
        else if (c > x.c) return get(x.right, key, d);
        else if (d < key.length() - 1) return get(x.mid, key, d + 1);
        else return x;
    }

    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is {@code null}, this effectively deletes the key from the symbol table.
     *
     * @param key the key
     * @param val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void put(String key, List<OsmAddress> val) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with null key");
        }
        if (!contains(key)) n++;
        else if (val == null) n--;       // delete existing key
        root = put(root, key, val, 0);
    }

    private Node put(Node x, String key, List<OsmAddress> val, int d) {
        byte c = Alphabet.getByteValue(key.charAt(d));
        if (x == null) {
            x = new Node();
            x.c = c;
        }
        if (c < x.c) x.left = put(x.left, key, val, d);
        else if (c > x.c) x.right = put(x.right, key, val, d);
        else if (d < key.length() - 1) x.mid = put(x.mid, key, val, d + 1);
        else x.val = val;
        return x;
    }

    /**
     * Returns all of the keys in the set that start with {@code prefix}.
     *
     * @param prefix the prefix
     * @return all of the keys in the set that start with {@code prefix},
     * as an iterable
     * @throws IllegalArgumentException if {@code prefix} is {@code null}
     */
    public Iterable<String> keysWithPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("calls keysWithPrefix() with null argument");
        }
        Queue<String> queue = new Queue<>();
        Node x = get(root, prefix, 0);
        if (x == null) {
            return queue;
        }
        if (x.val != null) {
            queue.enqueue(prefix);
        }
        collect(x.mid, new StringBuilder(prefix), queue);
        return queue;
    }

    // all keys in subtrie rooted at x with given prefix
    private void collect(Node x, StringBuilder prefix, Queue<String> queue) {
        if (x == null) {
            return;
        }
        collect(x.left, prefix, queue);
        if (x.val != null) {
            queue.enqueue(prefix.toString() + Alphabet.getCharValue(x.c));
        }
        collect(x.mid, prefix.append(Alphabet.getCharValue(x.c)), queue);
        prefix.deleteCharAt(prefix.length() - 1);
        collect(x.right, prefix, queue);
    }

    /**
     * Find a matching OsmAddress with the given address input.
     * <p>
     * If the TST cannot find any results from the input, use longest substring that does.
     * Gradually checks shorter substring of original input until a matching OsmAddress is found.
     */
    public OsmAddress findAddress(String originalInput) {

        originalInput = originalInput.toLowerCase();
        String addressInput = originalInput.replace(" ", "");

        Iterator<String> it = keysWithPrefix(addressInput).iterator();

        //If the string doesn't return matches, find a substring that does
        if (!it.hasNext()) {
            addressInput = findLongestSubstringWithMatches(addressInput);
            it = keysWithPrefix(addressInput).iterator();
        }

        List<String> addressKeys = new ArrayList<>();
        while (it.hasNext()) {
            addressKeys.add(it.next());
        }
        if (addressKeys.size() > 0) {

            for (String key : addressKeys) {
                for (OsmAddress osmAddress : get(key)) {
                    String addr = osmAddress.toString().toLowerCase();

                    if (addr.contains(originalInput)) {
                        return osmAddress;
                    }
                }
            }
            int endIndex = originalInput.length();
            while (endIndex >= 1) {
                originalInput = originalInput.substring(0, endIndex);

                for (String key : addressKeys) {
                    for (OsmAddress osmAddress : get(key)) {
                        String addr = osmAddress.toString().toLowerCase();

                        if (addr.contains(originalInput)) {
                            return osmAddress;
                        }
                    }
                }
                endIndex--;
            }
        }
        return null;
    }

    /**
     * Update list of address suggestions from the given input.
     * <p>
     * If the TST cannot find any results from the input, use longest substring that does.
     * Only gives full address suggestions if a full street name has been typed.
     */
    public void updateAddressSuggestions(String originalInput, List<String> suggestions) {
        suggestions.clear();
        List<OsmAddress> osmSuggestions = new ArrayList<>();

        originalInput = originalInput.toLowerCase();
        String addressInput = originalInput.replace(" ", "");

        Iterator<String> it = keysWithPrefix(addressInput).iterator();

        //If the string doesn't return matches, find a substring that does
        if (!it.hasNext()) {
            addressInput = findLongestSubstringWithMatches(addressInput);
            it = keysWithPrefix(addressInput).iterator();
        }

        //Has a valid street name been typed? Either street names or house numbers are then suggested
        String streetName = null;
        if (it.hasNext()) {
            String streetInfo = keysWithPrefix(addressInput).iterator().next();
            streetName = streetInfo.substring(0, streetInfo.length() - 4).trim();
        }

        //Has a full street named been typed? Start giving suggestions with full address.
        if (streetName != null && addressInput.contains(streetName)) {
            if (it.hasNext()) {

                List<String> allSuggestions = new ArrayList<>();
                List<OsmAddress> allOsmSuggestions = new ArrayList<>();

                for (OsmAddress osmAddress : get(it.next())) {
                    String address = osmAddress.toString();

                    allSuggestions.add(address);
                    allOsmSuggestions.add(osmAddress);

                    //Give suggestions for addresses that contains original input
                    if (address.toLowerCase().contains(originalInput)) {

                        osmSuggestions.add(osmAddress);
                        suggestions.add(address);
                    }
                }
                //If no suggestions were found, add all suggestions instead.
                //Happens if someone types an ambiguous address but still with a full street name.
                if (suggestions.size() == 0) {
                    suggestions.addAll(allSuggestions);
                    osmSuggestions.addAll(allOsmSuggestions);
                }
            }
        } else {
            //Returns a variation of suggestions if a full street hasn't been typed.
            while (it.hasNext()) {
                osmSuggestions.add(get(it.next()).get(0));
            }
            for (OsmAddress osmAddress : osmSuggestions) {
                String address = osmAddress.omitHouseNumberToString();

                suggestions.add(address);
            }
        }
    }

    /**
     * Find longest substring that matches address input.
     * <p>
     * Gradually creates a shorter substring of the given address input
     * until a possible match has been found. Otherwise return address input.
     */
    private String findLongestSubstringWithMatches(String addressInput) {

        int endIndex = addressInput.length();
        while (endIndex >= 1) {
            String subStringInput = addressInput.substring(0, endIndex);
            Iterator<String> it = keysWithPrefix(subStringInput).iterator();

            if (it.hasNext()) {
                return subStringInput;
            } else {
                endIndex--;
            }
        }
        return addressInput;
    }
}