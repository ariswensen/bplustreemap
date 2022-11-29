import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * A B+ tree based {@link Map} implementation.
 * The map is sorted according to the {@link Comparator}
 * provided at map creation time.
 *
 * <p> This implementation provides log(n) time cost for the
 * {@code containsKey}, {@code get}, and {@code put} implementations.
 * This is the cost to traverse the height [asymptotically log(n)] of
 * the tree. This implementation is part of research being conducted
 * by Arianna Swensen at the University of Mississippi.
 *
 * <p><strong> Please note that this implementation is not synchronized. </strong>
 * No testing has been done at this time on concurrent structural modifications.
 *
 * <p> This map does <strong>not</strong> support the {@code Entry.setValue}
 * method. (Note that it is possible to change mappings using {@code put}.)
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author Arianna Swensen
 * @see Map
 * @see Comparator
 * @see TreeMap
 *
 */

public class BPlusTreeMap<K, V> implements Map<K, V> {

    private BPlusTree tree;
    private final Comparator<K> comparator;

    /**
     * Constructs an empty BPlusTreeMap for use, according to the given comparator. A
     * All keys inserted into the map must be <em>mutually
     * comparable</em> by the given comparator: {@code comparator.compare(k1,
     * k2)} must not throw a {@code ClassCastException} for any keys
     * {@code k1} and {@code k2} in the map.
     *
     * @param comparator a comparator of given type K
     */
    public BPlusTreeMap(Comparator<K> comparator) {
        this.comparator = comparator;
    }

    /**
     * Constructs an empty BPlusTreeMap for use, according to the given comparator. A
     * All keys inserted into the map must be <em>mutually
     * comparable</em> by the given comparator: {@code comparator.compare(k1,
     * k2)} must not throw a {@code ClassCastException} for any keys
     * {@code k1} and {@code k2} in the map.
     *
     * The order {m} of a given tree is the number of children each node may have.
     * {@code m - 1} gives the number of keys contained within a node.
     *
     * @param comparator a comparator of given type K
     * @param order the order of the B+ tree
     */
    public BPlusTreeMap(int order, Comparator<K> comparator) {
        this.tree = new BPlusTree(order);
        this.comparator = comparator;
    }


    //Query Operations

    /**
     * Returns the height of the B+ tree.
     * @return the height of the B+ tree
     */

    @Override
    public int size() {
        int size = 0;
        Node curr = tree.root;
        while(curr != null) {
            size++;
            if(curr.children.size() != 0) curr = curr.children.get(0);
            else curr = null;
        }
        return size;
    }

    /**
     * Returns {@code true} if this map is empty.
     * @return {@code true} if this map is empty.
     */
    @Override
    public boolean isEmpty() {
        return this.tree.root == null;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     *
     * @param key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified key
     * @throws ClassCastException if the specified key cannot be compared with the keys
     *         currently in the map
     * @throws NullPointerException if the specified key is null and the comparator does not permit
     *         null keys
     */
    @Override
    public boolean containsKey(Object key) {
        Node curr = searchForLeaf((K)key);
        return curr.keys.contains((K)key);
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {code key} compares
     * equal to {@code k} according to the mapping, then this method
     * will return {@code v}.
     * (There can be at most one such mapping.)
     *
     * @param key the key for which to retrieve the value
     * @return the value associated with the key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null and its
     *          comparator does not permit null keys
     */
    @Override
    public V get(Object key) {
        Node curr = searchForLeaf((K)key);
        return curr.values.get(curr.keys.indexOf((K)key));
    }

    /**
     * Returns the set of keys contained within the map as a HashSet.
     * @return the set of keys contained within the map as a HashSet
     */
    @Override
    public HashSet<K> keySet() {
        HashSet<K> keys = new HashSet<>();
        Node curr = tree.root;
        while(!curr.isLeaf) {
            curr = curr.children.get(0);
        }
        while(curr != null) {
            for (K key: curr.keys){
                keys.add(key);
            }
            curr = curr.next;
        }

        return keys;
    }

    /**
     * Returns the list of all values contained within the mapping as an ArrayList.
     * @return the list of all values contained within the mapping as an ArrayList.
     */
    @Override
    public ArrayList<V> values() {
        ArrayList<V> values = new ArrayList<>();
        Node curr = tree.root;
        while(!curr.isLeaf) {
            curr = curr.children.get(0);
        }
        while(curr != null) {
            for (V value: curr.values){
                values.add(value);
            }
            curr = curr.next;
        }

        return values;
    }

    /**
     * Returns the set of all {@code MapEntry} objects contained within the map as a HashSet.
     * @return the set of all {@code MapEntry} objects contained within the map as a HashSet.
     */
    @Override
    public HashSet<Entry<K, V>> entrySet() {
        HashSet<Entry<K, V>> entries = new HashSet<>();
        Node curr = tree.root;
        while(!curr.isLeaf) {
            curr = curr.children.get(0);
        }
        while(curr != null) {
            for (int i = 0; i < curr.keys.size(); i++){
                K key = curr.keys.get(i);
                V value = curr.values.get(i);
                entries.add(new MapEntry<>(key, value));
            }
            curr = curr.next;
        }

        return entries;
    }

    /**
     * Returns the {@code Node} where a key should be inserted or currently exists.
     * @param key the key that needs to be inserted or searched for within a leaf node
     * @return the {@code Node} where a key should be inserted or currently exists.
     */
    public Node searchForLeaf(K key) {
        Node curr = tree.root;
        int c1; int c2;
        while(!curr.isLeaf) {
            c1 = comparator.compare(key, curr.keys.get(0));
            if(c1 < 0) curr = curr.children.get(0);
            else if(comparator.compare(key, curr.keys.get(curr.keys.size() - 1)) >= 0) curr = curr.children.get(curr.children.size() - 1);
            else {
                for(int i = 0; i < curr.keys.size() - 1; i++){
                    c2 = comparator.compare(key, curr.keys.get(i + 1));
                    if(c1 == 0 && i == 0 || 0 < c1 && c2 < 0) {
                        curr = curr.children.get(i + 1);
                        break;
                    } else {
                        c1 = c2;
                    }
                }
            }
        }
        return curr;
    }

    //Insertion Operations

    /**
     * If the tree contains no values, inserts the K,V pair into the map
     * and sets that node as the root. If the key already exists, edits
     * the value to reflect the updated value. Otherwise, inserts the K,V
     * pair into the appropriate tree node using the {@link #insert insert}
     * operation.
     *
     * @param key
     * @param value
     * @return the value that is now associated with the key
     */

    @Override
    public V put(K key, V value) {
        if(isEmpty()) {
            Node root = new Node();
            root.keys.add(key);
            root.values.add(value);
            tree.setRoot(root);
        } else if (containsKey(key)) {
            Node curr = searchForLeaf(key);
            curr.values.set(curr.keys.indexOf(key), value);
        } else insert(key, value);

        return value;
    }

    /**
     * Inserts K, V pair into the tree in the appropriate leaf node.
     * Calls the {@link #split split} method if necessary to maintain
     * the order property of the B+ tree.
     *
     * @param key the key to be inserted
     * @param value the value to be mapped to the key
     */
    public void insert(K key, V value) {
        Node leaf = searchForLeaf(key);
        leaf.addKey(key);
        leaf.setValueForKey(key, value);
        if(leaf.keys.size() >= tree.order) split(leaf);
    }

    /**
     * Splits the node into left and right components to maintain
     * the order of the overall B+ tree. Will ensure that the parent
     * node being edited also meets this constraint. At worst runs
     * in linear time based on the height of the tree / logarithimic
     * time based on the number of K,V pairs.
     *
     * @param n the {@code Node} that must be split
     */
    public void split(Node n) {
        int h = (tree.order / 2);
        Node left = new Node(), right = new Node(), parent = new Node();

        left.keys.addAll(n.keys.subList(0, h));
        left.values.addAll(n.values.subList(0, h));
        left.setNext(right);
        left.setParent(parent);

        right.keys.addAll(n.keys.subList(h, n.keys.size()));
        right.values.addAll(n.values.subList(h, n.values.size()));
        right.setNext(n.next);
        right.setParent(parent);

        if(n.parent != null) {
            parent = n.parent;
            parent.setLeaf(false);
            int i = parent.children.indexOf(n);
            parent.children.set(i, left);
            parent.children.add(i + 1, right);
            parent.addKey(n.keys.get(h));
            if(parent.keys.size() >= tree.order) split(parent);
        } else {
            parent.addKey(n.keys.get(h));
            parent.setLeaf(false);
            parent.children.add(left);
            parent.children.add(right);
            tree.setRoot(parent);
        }

    }

    //Deletion Operations

    /**
     * Clears the map of all K,V pairs.
     */
    @Override
    public void clear() {
        tree = new BPlusTree();
    }

    //Supporting Classes

    /**
     * The BPlusTree class that supports the K,V mapping.
     */
    class BPlusTree {
        /**
         * The order {@code m} of the B+ tree, where m is the number
         * of children a node can have and {@code m-1} is the
         * number of K,V pairs it may store within a node.
         */
        int order;
        /**
         * The root {@link Node Node} of the tree.
         */
        Node root;

        /**
         * Constructs a new, empty B+ tree, using the default order
         * of 16 and the root is null.
         */
        public BPlusTree(){
            this.order = 16;
            this.root = null;
        }

        /**
         * Constructs a new, empty B+ tree, using the specified order
         * and the root is null.
         */
        public BPlusTree(int order) {
            this.order = order;
            this.root = null;
        }

        /**
         * Sets the root of the tree to a new value.
         * @param root the root {@link Node Node} of the B+ tree
         */
        public void setRoot(Node root) {
            this.root = root;
        }


    }

    /**
     * The Node class that stores keys and values as well as
     * tree information.
     */
    class Node {
        /**
         * The keys contained within the node.
         */
        ArrayList<K> keys;

        /**
         * The values contained within the node.
         */
        ArrayList<V> values;

        /**
         * The parent node which is above this node.
         */
        Node parent;

        /**
         * The right neighbor to the node, if it is a leaf node.
         */
        Node next;

        /**
         * The childern of this node, if it is not a leaf node.
         */
        ArrayList<Node> children;

        /**
         * Defines if the Node is a leaf or not.
         */
        boolean isLeaf;

        /**
         * Constructs a new, empty node. Intializes all of the lists
         * as empty, the additional nodes as {@code null}, and the value
         * of {@code isLeaf} as {@code true}.
         */
        public Node() {
            this.keys = new ArrayList<>();
            this.values = new ArrayList<>();
            this.parent = null;
            this.children = new ArrayList<>();
            this.next = null;
            this.isLeaf = true;
        }

        /**
         * Sets the parent as Node n.
         * @param n the parent node to be linked to this node
         */
        public void setParent(Node n) {
            this.parent = n;
        }

        /**
         * Sets the value of {@code isLeaf} to b.
         * @param b the boolean indicating whether this is or is not a leaf node
         */
        public void setLeaf(boolean b) {
            this.isLeaf = b;
        }

        /**
         * Sets the next node as Node n.
         * @param n the right neighbor node to be linked to this node
         */
        public void setNext(Node n) {
            this.next = n;
        }

        /**
         * Adds the key into the list of keys contained within this node,
         * maintaining the sorted property of the keys.
         * @param key the key to be inserted into this node
         */
        public void addKey(K key) {
            int n = -1;
            int i = 0;
            while(i < keys.size()){
                if(comparator.compare(keys.get(i), key) >= 0) {
                    n = i;
                    break;
                }
                i++;
            }
            if(n >= 0) {
                keys.add(n, key);
            } else {
                keys.add(key);
            }
        }

        /**
         * Sets the value for a key contained within this node.
         * @param key the key that the value is associated with
         * @param value the value to be added or updated in this node
         */
        public void setValueForKey(K key, V value) {
            if(keys.indexOf(key) < values.size()) {
                values.add(keys.indexOf(key), value);
            } else {
                values.add(value);
            }
        }
    }

    /**
     * The MapEntry class, which supports the {@link #entrySet() entrySet()} operation.
     *
     * @param <K> the type of keys inserted into the map
     * @param <V> the type of values inserted into the map
     */
    class MapEntry<K, V> implements Entry<K, V> {

        /**
         * The key for this entry.
         */
        K key;

        /**
         * The value mapped to the key {@code key}
         */
        V value;


        /**
         * Constructs a MapEntry with both a key and a value.
         *
         * @param key the key for this entry
         * @param value the value mapped to the key {@code key}
         */
        public MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key for this entry.
         * @return the key for this entry
         */
        @Override
        public K getKey() {
            return this.key;
        }

        /**
         * Returns the value for this entry.
         * @return the value for this entry
         */
        @Override
        public V getValue() {
            return this.value;
        }

        /**
         * Sets the value for this entry.
         * @param value the value to be set
         * @return the new value for this entry
         */
        @Override
        public V setValue(V value) {
            this.value = value;
            return this.value;
        }

        /**
         * Converts this entry to a string.
         * @return a string version of this K,V pair.
         */
        public String toString() {
            return "{" + key + ": " + value + " }";
        }
    }

    //Unsupported Operations

    /**
     * This operations is not supported in this version of the BPlusTreeMap.
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("This operation is not supported. Please query using a key.");
    }

    /**
     * This operations is not supported in this version of the BPlusTreeMap.
     * @throws UnsupportedOperationException
     */
    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("This operation is not currently supported. Please check back for updates.");
    }

    /**
     * This operations is not supported in this version of the BPlusTreeMap.
     * @throws UnsupportedOperationException
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("This operation is not currently supported. Please check back for updates.");
    }

}


