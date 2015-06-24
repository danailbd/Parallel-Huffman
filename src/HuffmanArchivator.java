import java.util.*;

/**
 * Created by danailbd on 5/21/15.
 */
public class HuffmanArchivator {

    String dataForCompression;
    private HashMap<Character, Byte> encodingTable;

    public HuffmanArchivator(String data) {
        dataForCompression = data;
    }

    private HashMap<Character, Integer> countOccurrences() {
        HashMap<Character, Integer> countMap = new HashMap<>();
        int mostOccurs = 0;
        for (int i = 0 ; i < dataForCompression.length() ; i++) {
            Integer value = countMap.get(dataForCompression.charAt(i));
            if(value == null) {
                countMap.put(dataForCompression.charAt(i), 0);
            } else {
                countMap.put(dataForCompression.charAt(i), value+1);
            }
        }

        return countMap;
    }


    // TODO SHOULD BE PARALLEl -- Use :
    /*
      threadCount = x;
       divide data : ->
     */

    /* TODO --
     * Basic encode logic:
     * 1) keep the number of occupied bits int the first 4 bytes (1 int)
     * 2) after that save the encoded data
     * 3) if number of bits is > 2^31-8 (int) repeat from 1)
     */

    /**
     * - Reads the word for encoding char by char and
     * parses it to code (byte), using the created code table.
     * - Reads the code (byte), from leftmost bit ( shifts left ) and
     * concat it with the bits, left from previous iterations
     * in the current byte for save:
     * * Ex. for save bitCount=3 (101 0000) && new code (0000 0010) -> (10110 000)
     * -- Shifts to the 1st meaning bit from the code
     * ** Ex. code (0000 0101) -> (1010 0000)
     * -- Uses mask (1000 0000) to read the first symbol's bit and removes it
     * ** Ex. symbol's code (1101 0000) -> (1010 0000)
     * -- Adds to current byte at proper place
     * ** (5 - 1010 0000 | 0000 0100) -> (6 - 1010 0100)
     * -- On byte filled, saves it and refreshes the counters
     * @param codeMap
     * @return
     */
    // TODO - check logic again
    private ArrayList<Byte> encodeData(HashMap<Character, Byte> codeMap) {

        ArrayList<Byte> encodedData = new ArrayList<>();

        final long MAX_BATCH_BITS = ((long)1<<32)-8; // last whole byte
        long batchBitsCounter = 0; // keeps info for current encoded batch
        int tempBitCounter = 0; // current byte's bits (for save)
        byte tempByte = 0x0; // current byte for save

        // Leave place for batch bits counter
        int i;
        for (i = 0; i<4 ;i++) {
            encodedData.add(tempByte);
        }

        for (i = 0; i < dataForCompression.length(); i++) {
            char charForEncoding = dataForCompression.charAt(i);
            byte codeForChar = codeMap.get(charForEncoding);

            // go to first meaning bit
            while (codeForChar != 0 || (0x80 & codeForChar) != 0) {
                codeForChar <<= 1;
            }

            boolean noMoreBits = false;
            // Read and compress the code
            while(!noMoreBits) {
                // read next coded symbol's bit
                byte readBit = (byte)(codeForChar & 0x80);

                // place the read bit at the right place
                // Ex. 1000 0000 -> 0001 0000
                readBit >>= tempBitCounter;

                // add the new bit
                tempByte = (byte) (tempByte | readBit);
                tempBitCounter++;
                batchBitsCounter++;

                // remove the read bit
                codeForChar <<= 1;

                // check if current symbol code has finished
                noMoreBits = (codeForChar == 0);

                // Check if whole byte is filled ... or its the last read byte
                if(tempBitCounter == 7) {
                    encodedData.add(tempByte);
                    tempByte = 0x0;
                    tempBitCounter = 0;
                }

                // End of batch
                if (batchBitsCounter == MAX_BATCH_BITS) {
                    saveBatchBitsCount(encodedData, batchBitsCounter);
                    batchBitsCounter = 0;
                }
            }
        }
        // save last (unfinished batch)
        saveBatchBitsCount(encodedData, batchBitsCounter);

        return encodedData;
    }

    /**
     * Saves the number of used bits for the last batch
     * @param encodedData
     * @param batchBitsNumber
     */
    private void saveBatchBitsCount(ArrayList<Byte> encodedData,
                                    long batchBitsNumber) {
        int numberOFBatches = (int)(batchBitsNumber / 8),
            bytesNumber = encodedData.size();

        if (batchBitsNumber % 8 != 0) {
            // if there is one more byte;
            numberOFBatches++;
        }

        byte byteReadMask = (byte) -1; // equals 1111 1111
        // Saves number's bytes from right to left
        for (int j = 0; j<4 ;j++) {
            // Fill the empty bytes at the beginning of the batch
            encodedData.add(bytesNumber - numberOFBatches - j,
                     (byte)(batchBitsNumber & byteReadMask));
            batchBitsNumber >>= 8; // remove used bits
        }
    }


    // TODO -- SHOULD BE PARALLEL
    /**
     *
     * @param countMap - represent the number of occurrences
     * @return Char-To-Code Table
     */
    private HashMap<Character, Byte> generateCharactersEncoding(HashMap<Character, Integer> countMap) {
        List<HuffmanTree> binaryTreesList = new ArrayList<>();
        int normalizationValue = 0;

        Iterator it = countMap.entrySet().iterator();
        /* Create list of trees for encoding */
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Integer count = (Integer) entry.getValue();
            HuffmanTree newTree = new HuffmanTree((Character) entry.getKey(),
                    count);
            binaryTreesList.add(newTree);


            if (count > normalizationValue) {
                normalizationValue = count;
            }
        }

        // Merge all trees
        HuffmanTree mergedTree = buildTree(binaryTreesList, normalizationValue);

        return mergedTree.generateTable();
    }


    // TODO - create parallel
    private HuffmanTree buildTree(List<HuffmanTree> binaryTreesList, int normalizationValue) {
        HuffmanTree mergedTree,
                    first, second;

        /*
         * Ensure ordered list
         */
        Collections.sort(binaryTreesList);

        /*
         * For each class at index i is true -> 1/2^i <= classAt[i] < 1/2^(i-1)
         * classes can be merged separately
         */
        List<ArrayList<HuffmanTree>> classes = createClasses(binaryTreesList);
        /*
         * Keep upperbound elements
         */
        HuffmanTree[] light = new HuffmanTree[classes.size()];
        HuffmanTree[][] classesL = new HuffmanTree[classes.size()][];

        int i = classes.size();
        int bound = 1 << i;  // 2^i
        while (i >= 1) {
            if (light[i] != null) {
                HuffmanTree t = light[i].mergeTrees(classes.get(i).get(0));
                classes.get(i).remove(0);
                if (t.getWeight() > 1/(bound << 1) || classes.get(i).size() == 0) {
                    if (nextClass[i] != i-1) {
                        nextClass[i-1] = nextClass[i];
                        nextClass[i] = i-1;
                    }
                }
                if (t.getWeight() > 1/(bound<<1)) {
                    classes.add(i-1, merge(classes.get(i-1), t));
                } else {
                    classes.add(i, merge(classes.get(i), t));
                }
            }
            // TODO - parallel
            for (int j = 0; j <= classes.get(i).size()/2; j++) {
                classesM[i][j] = classes.get(i).get(2*j-1).mergeTrees(classes.get(i).get(2*j));
            }
            classes.add(i-1, merge(classes.get(i-1), classesM[i]));
            if (classes.size() % 2 == 0) {
                // light[nextclass[i]] := last(Wi);
                light[nextClassp[i]] = classes.get(i).get(classes.size());
            }
        }

        return null;
//        while (binaryTreesList.size() > 1) {
//            first = binaryTreesList.pollLast();
//            second = binaryTreesList.pollLast();
//
//            mergedTree = second.mergeTrees(first);
//            binaryTreesList.add(mergedTree);
//        }
//        // There should be only one element left
//        mergedTree = binaryTreesList.pollLast();
//        return mergedTree;
    }

    private List<ArrayList<HuffmanTree>> createClasses(List<HuffmanTree> binaryTreesList) {
        return null;
    }

    // TODO -- IMPLEMENT
    public ArrayList<Byte> compress() {
        HashMap<Character, Integer> countMap = countOccurrences();
        encodingTable = generateCharactersEncoding(countMap);

        return encodeData(encodingTable);
    }

    public String decode(ArrayList<Byte> compressedData) {
        ArrayList<Character> decodedData = new ArrayList<>();

        HashMap<Byte, Character> codeToCharTable = flipCodeTable(encodingTable);

        // The byte and bits count used for the read data
        byte currentByte = 0x0;
        int bitsCounter = 0;
        for (byte loadedByte : compressedData) {
            // TODO -- think what to do with last byte's zeros (0)
            // TODO rethink logic of decoding
            int loadedByteBits = 0;
            while (loadedByteBits < 8) {
                // get the last bit
                currentByte = (byte) (currentByte | (loadedByte & 0x80));

                // remove the read bit
                loadedByte <<= 1;
                loadedByteBits++;

                // free bit for next read
                currentByte <<= 1;
                bitsCounter++;

                // TODO -- how to know its end of symbol ... ?
                if (bitsCounter == 8) {

                }
            }
        }
        return null;
    }

    private HashMap<Byte, Character> flipCodeTable(HashMap<Character, Byte> encodingTable) {
        HashMap<Byte, Character> flippedTable = new HashMap<>();

        for (Map.Entry<Character, Byte> entry : encodingTable.entrySet()) {
            Character value = entry.getKey();
            Byte key = entry.getValue();

            flippedTable.put(key, value);
        }

        return flippedTable;
    }
}
