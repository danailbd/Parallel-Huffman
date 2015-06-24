/**
 * Created by danailbd on 5/21/15.
 */

import java.util.HashMap;

/**
 * An immutable class representing a binary tree,
 * used for encoding the string data
 */
public class HuffmanTree  implements Comparable<HuffmanTree> {

    static class Node{

        private int weight;

        public int getWeight() {
            return weight;
        }

        private Node left, right;

        public final Node getRight() {
            return right;
        }

        public final Node getLeft() {
            return left;
        }

        public Node(int weight) {
            this.weight = weight;
            left = right = null;
        }

        public Node(int weight, Node left, Node right) {
            this.weight = weight;
            this.left = left;
            this.right = right;
        }
    }

    static class LeafNode extends Node {
        private char symbol;

        public LeafNode(int count, char symbol) {
            super(count);
            this.symbol = symbol;
        }
    }

    private Node root;

    public final Node getRoot() {return root;}

    public HuffmanTree(char symbol, int count) {
        root = new LeafNode(count, symbol);
    }

    /**
     * Merges trees
     * N.B. - for proper tree "left" should be single leaf tree !!!
     * @param left
     * @param right
     */
    public HuffmanTree(HuffmanTree left, HuffmanTree right) {
        Node leftRoot = left.root,
             rightRoot = right.root;
        int newCount = leftRoot.getWeight() + rightRoot.getWeight();

        root = new Node(newCount);
        if (leftRoot.right != null) {
            root.right = new Node(newCount - root.left.getWeight());
            root.left = leftRoot.left;
            root.right.right = leftRoot.right;
            root.right.left = rightRoot;
        } else {
            // Reuse the same objects
            // !!! An object must be immutable !!!
            root.left = leftRoot; // ???
        }
    }

    public int getWeight() {
        return root.weight;
    }

    /**
     * Makes a new tree out of the current and the new
     * @param other - tree to use for merge
     * @return - the new immutable tree
     */
    public HuffmanTree mergeTrees(HuffmanTree other) {
        return new HuffmanTree(this, other);
    }

    /**
     * Generates the table of the characters in the string
     * containing their bit representation
     * @return - table with codes
     */
    public HashMap<Character, Byte> generateTable() {
        // Set initial state
        HashMap<Character, Byte> charsCodeMap = new HashMap<>();
        generateTableRec(root, (byte) 0x0, charsCodeMap);

        return charsCodeMap;
    }

    // TODO -- CAN BE PARALLELLED

    /**
     * Recursive version
     * @param node - current node
     * @param curState - current binary state ( 0, 1, 10, ...),
     *                 depending on the traveled path
     * @param coded - map containing the encoded chars
     */
    private void generateTableRec(Node node, byte curState, HashMap<Character, Byte> coded) {
        // At the bottom of the tree should exist only
        // --- LeafNodes ---
        if (node instanceof LeafNode) {
            // Add the code for the character
            coded.put(((LeafNode) node).symbol, curState);
            return;
        }

        // if going left add 0 else 1
        generateTableRec(node.left, (byte) (curState << 1), coded);
        generateTableRec(node.right, (byte) ((curState << 1) + 0x1), coded);
    }

    @Override
    public int compareTo(HuffmanTree other) {
        if(getWeight() > other.getWeight()) {
            return -1;
        } else {
            return getWeight() == other.getWeight() ? 0 : 1;
        }
    }

    @Override
    public String toString() {
        StringBuilder treeBuffer = new StringBuilder();
        getTree(root.getLeft(), treeBuffer);
        treeBuffer.append('|');
        getTree(root.getRight(), treeBuffer);
        // remove trailing space
        treeBuffer.deleteCharAt(treeBuffer.length()-1);

        return treeBuffer.toString();
    }

    /**
     * Fills a given string buffer with the data from the tree.
     * Gets tree from left to right
     * @param root
     * @param treeString
     */
    private void getTree(Node root, StringBuilder treeString) {
        if (root instanceof  LeafNode) {
            // symbol-weight
            treeString.append(((LeafNode) root).symbol);
            treeString.append('-');
            treeString.append(root.getWeight());
            treeString.append(' ');
            return;
        }

        getTree(root.getLeft(), treeString);
        getTree(root.getRight(), treeString);
    }
}
