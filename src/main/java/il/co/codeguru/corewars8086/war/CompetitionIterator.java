package il.co.codeguru.corewars8086.war;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.Iterator;

public class CompetitionIterator implements Iterator<int[]> {
	private final RandomDataGenerator rnd;
	private int[] counters;
	private int numItems;
	private final int groupSize;

    public CompetitionIterator(int numItems, int groupSize) {
        assert numItems >= groupSize;
        this.numItems = numItems;
        this.groupSize = groupSize;
        counters = new int[groupSize];
        rnd = new RandomDataGenerator();
    }

    /**
     * Returns the next group in the sequence
     */
    public int[] next()
    {
		return rnd.nextPermutation(numItems, Math.min(groupSize, numItems));
    }

	public boolean hasNext() {
        return counters[0] != -1;
    }
    
    public void remove() {
    }

    public long getNumberOfItems() {
		return CombinatoricsUtils.binomialCoefficient(numItems, counters.length);
    }
}