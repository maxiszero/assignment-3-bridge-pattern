package bridge;

/** Shared input contract keeps allocation and integer arithmetic bounded. */
final class Dimensions {
    private static final int MAX_SIZE = 1000;

    private Dimensions() { }

    static int requireValid(int value, String name) {
        if (value < 1 || value > MAX_SIZE) {
            throw new IllegalArgumentException(name + " must be between 1 and " + MAX_SIZE);
        }
        return value;
    }
}
