import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class HuffmanTreeTest {
    private ArrayList<HuffmanTree> testTrees;


    /*
     * Test word: camtcommtc
     */
    @Before
    public void setUp() throws Exception {
        testTrees = new ArrayList<>();

        HuffmanTree c_es = new HuffmanTree('c', 3);
        HuffmanTree m_es = new HuffmanTree('m', 3);
        HuffmanTree t_es = new HuffmanTree('t', 2);
        HuffmanTree a_es = new HuffmanTree('a', 1);
        HuffmanTree o_es = new HuffmanTree('o', 1);

        testTrees.add(c_es);
        testTrees.add(m_es);
        testTrees.add(t_es);
        testTrees.add(a_es);
        testTrees.add(o_es);
    }

    @Test
    public void testMergeTrees() throws Exception {

        HuffmanTree ao_es = testTrees.get(3).mergeTrees(testTrees.get(4));
        HuffmanTree tao_es = testTrees.get(2).mergeTrees(ao_es);
        HuffmanTree taom_es = testTrees.get(1).mergeTrees(tao_es);
        HuffmanTree taomc_es = testTrees.get(0).mergeTrees(taom_es);

        assertEquals("Single item tree size", 2, testTrees.get(2).getWeight());

         // TODO FIX TESTS !

        assertEquals(HuffmanTree.LeafNode.class, ao_es.getRoot().getLeft().getClass());
        assertEquals(HuffmanTree.LeafNode.class, ao_es.getRoot().getRight().getClass());
        assertEquals("Tree size", 2, ao_es.getWeight());
        assertEquals("Tree cargo", "a-1 |o-1", ao_es.toString());

        assertEquals("Tree size", 4, tao_es.getWeight());
        assertEquals("Tree cargo", "t-2 |a-1 o-1", tao_es.toString());

        assertEquals("Tree size", 6, taom_es.getWeight());
        assertEquals("Tree cargo", "m-3 |t-2 a-1 o-1", taom_es.toString());

        assertEquals("Tree size", 10, taomc_es.getWeight());
        assertEquals("Tree cargo", "c-3 |m-3 t-2 a-1 o-1", taomc_es.toString());
    }

    @Test
    public void testGenerateTable() throws Exception {

        HuffmanTree ao_es = testTrees.get(3).mergeTrees(testTrees.get(4));
        HuffmanTree tao_es = testTrees.get(2).mergeTrees(ao_es);
        HuffmanTree cm_es = testTrees.get(0).mergeTrees(testTrees.get(1));
        HuffmanTree taocm_es = cm_es.mergeTrees(tao_es);

        HashMap<Character, Byte> map = taocm_es.generateTable();


        Byte expectedCode = (byte) 0x0; // 0
        assertEquals(expectedCode, map.get('c'));
        expectedCode = (byte) 0x2; // 10
        assertEquals(expectedCode, map.get('m'));
        expectedCode = (byte) 0x0; // 110
        assertEquals(expectedCode, map.get('t'));
        expectedCode = (byte) 0x0; // 1110
        assertEquals(expectedCode, map.get('a'));
        expectedCode = (byte) 0x0; // 1111
        assertEquals(expectedCode, map.get('o'));



    }

    @Test
    public void testCompare() throws Exception {

        assertEquals(-1, testTrees.get(0).compareTo(testTrees.get(2)));
        assertEquals(0, testTrees.get(0).compareTo(testTrees.get(1)));

    }
}