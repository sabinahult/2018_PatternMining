/***
 * The ItemSet class is used to store information concerning a single transaction.
 * Should not need any changes.
 *
 */
public class ItemSet {

    /***
     * The PRIMES array is internally in the ItemSet-class' hashCode method
     */
    private static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 23, 27, 31, 37};
    final int[] set;

    /***
     * Creates a new instance of the ItemSet class.
     * @param set Transaction content
     */
    public ItemSet(int[] set) {
        this.set = set;
    }

    @Override
    /**
     * hashCode functioned used internally in Hashtable
     */
    public int hashCode() {
        int code = 0;
        for (int i = 0; i < set.length; i++) {
            code += set[i] * PRIMES[i];
        }
        return code;
    }

    public boolean containsSameElement(ItemSet other) {
        for(int thisElement : this.set) {
            for(int otherElement : other.set) {
                if(thisElement == otherElement) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    /**
     * Used to determine whether two ItemSet objects are equal
     */
    public boolean equals(Object o) {
        if (!(o instanceof ItemSet)) {
            return false;
        }
        ItemSet other = (ItemSet) o;
        if (other.set.length != this.set.length) {
            return false;
        }
        for (int i = 0; i < set.length; i++) {
            if (set[i] != other.set[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("ItemSet[");
        for(int item : set) {
            string.append(" " + item).append(" ");
        }
        string.append("]");
        return string.toString();
    }
}