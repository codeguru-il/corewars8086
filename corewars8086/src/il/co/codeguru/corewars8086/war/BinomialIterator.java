package il.co.codeguru.corewars8086.war;

import java.util.Iterator;

/**
 * Allows iteration over all the ways to pick <i>groupSize</i> items out of a
 * total <i>numItems</i> items.
 * 
 * @author bs
 */
public class BinomialIterator implements Iterator<int[]> {
    private int[] group;
    private int size, numItems;

    public BinomialIterator(int numItems, int groupSize) {
        assert numItems >= groupSize;
        group = new int[groupSize];
        size = groupSize;
        this.numItems = numItems;
        for (int i = 1; i < groupSize; i++) {
            group[i] = i;
        }
    }	

    public boolean hasNext() {
        return group != null;
    }

    /**
     * Returns the next group in the sequence
     */
    public int[] next() {
        int i;
        int[] copy = new int[size];
        System.arraycopy(group, 0, copy, 0, size);
        for (i = size -1; i >= 0; i--) {
            if ((group[i] < numItems-1)  && (numItems - 1 - (group[i]+1) >= size - i-1)) {
                group[i] ++;
                for (int j = i+1; j < size; j++) {
                    group[j] = group[j-1] +1;
                }
                break;
            }
        }
        if (i == -1) {
            group = null;
        }
        return copy;
    }

    public void remove() {
    }

    public long getNumberOfItems() {
        return factorial(numItems) / (factorial(size)*factorial(numItems-size));
    }

    /**
     * calculate n factorial. Only good for 0 <= n <= 20
     * 
     * @author Roedy Green email
     */
    static long factorial (int n) {
        if ( ! ( 0 <= n && n <= 20 ) ) {
            throw new IllegalArgumentException( "factorial can only handle 0 <= n <= 20" );
        }
        return factorials[n];
    }

    private static long [] factorials = {
        1L /* 0 */,
        1L /* 1 */,
        2L /* 2 */,
        6L /* 3 */,
        24L /* 4 */,
        120L /* 5 */,
        720L /* 6 */ ,
        5040L /* 7 */ ,
        40320L /* 8 */ ,
        362880L /* 9 */ ,
        3628800L /* 10 */ ,
        39916800L /* 11 */ ,
        479001600L /* 12 */ ,
        6227020800L /* 13 */ ,
        87178291200L /* 14 */ ,
        1307674368000L /* 15 */ ,
        20922789888000L /* 16 */ ,
        355687428096000L /* 17 */ ,
        6402373705728000L /* 18 */ ,
        121645100408832000L /* 19 */ ,
        2432902008176640000L /* 20 */
    };
}