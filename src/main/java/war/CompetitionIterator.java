package war;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.Iterator;

public class CompetitionIterator implements Iterator<int[]> {
	private final RandomDataGenerator rnd;
	private final int numItems;
	private final int groupSize;

    public CompetitionIterator(int numItems, int groupSize) {
        assert numItems >= groupSize;
        this.numItems = numItems;
        this.groupSize = groupSize;
        rnd = new RandomDataGenerator();
    }

    /**
     * Returns the next random group combination.
     */
    public int[] next() {
		return rnd.nextPermutation(numItems, Math.min(groupSize, numItems));
    }

	public boolean hasNext() {
        // This iterator can produce random combinations indefinitely.
        return true;
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public long getNumberOfCombos() {
		return CombinatoricsUtils.binomialCoefficient(numItems, groupSize);
    }
}