package il.co.codeguru.corewars8086.war;

import java.util.Iterator;

/**
 * Allows iteration over all the ways to pick <i>groupSize</i> items out of a
 * total <i>numItems</i> items.
 * 
 * @author bs
 */
public class BinomialIterator implements Iterator<int[]> {
    //private int[] group;
    //private int size, numItems;
	private int[] counters;
	private int itemsNum;

    public BinomialIterator(int numItems, int groupSize) {
        assert numItems >= groupSize;
        itemsNum = numItems;
        counters = new int[groupSize];
        
        /*group = new int[groupSize];
        size = groupSize;
        this.numItems = numItems;
        for (int i = 1; i < groupSize; i++) {
            group[i] = i;
        }*/
    }	

    /**
     * Returns the next group in the sequence
     */
    public int[] next()
    {
    	int[] results = new int[counters.length];
    	results[0] = counters[0];
    	for (int i = 1; i < counters.length; i++)
    		results[i] = results[i - 1] + counters[i] + 1;
    	
    	if (!increaseCounter(0))
    		counters[0] = -1;
    	
    	return results;
        /*int i;
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
        return copy;*/
    }
    
    /**
     * increases the counter given
     * @param i the number of the counter in the order
     * @return did it work
     */
	private boolean increaseCounter(int i)
    {
		counters[getCounterNum(i)]++;
		int sum = 0;
		for (int j = 0; j < counters.length; j++)
			sum += counters[j] + 1;

		if (sum > itemsNum)
		{
			if (i < counters.length - 1)
			{
				counters[getCounterNum(i)] = 0;
				return increaseCounter(i+1);
			}
			else
			{
				return false;
			}
		}
		else
		{
			return true;
		}
    }

	public boolean hasNext() {
        return counters[0] != -1;
    }
    
    public void remove() {
    }
    
    public void reset()
    {
    	for (int i = 0; i < counters.length; i++)
        	counters[i] = 0;
    }
    
    /**
     * in order to make the wars more distributed, we are using a different counter system for each item
     * in the group than usual
     * @param i the counter index
     * @return the number in the actual order of the counters
     */
	private int getCounterNum(int i)
    {
    	if (i % 2 == 0)
    		return i / 2;
    	else
    		return counters.length - 1 - i / 2;
    }

    public long getNumberOfItems() {
        return factorial(itemsNum) / (factorial(counters.length)*factorial(itemsNum-counters.length));
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