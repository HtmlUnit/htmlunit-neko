package org.htmlunit.benchmark.util;

/**
 * Ultra-fast pseudo random generator that is not synchronized!
 * Don't use anything from Random by inheritance, this will inherit
 * a volatile!
 *
 * @author rschwietzke
 *
 */
public class FastRandom
{
    private long seed;

    public FastRandom()
    {
        this.seed = System.currentTimeMillis();
    }

    public FastRandom(long seed)
    {
        this.seed = seed;
    }

    protected int next(int nbits)
    {
        // N.B. Not thread-safe!
        long x = this.seed;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        this.seed = x;

        x &= ((1L << nbits) -1);

        return (int) x;
    }

    /**
     * Borrowed from the JDK
     *
     * @param bound
     * @return
     */
    public int nextInt(int bound) {

        int r = next(31);
        int m = bound - 1;
        if ((bound & m) == 0)  // i.e., bound is a power of 2
            r = (int)((bound * (long)r) >> 31);
        else {
            for (int u = r;
                 u - (r = u % bound) + m < 0;
                 u = next(31))
                ;
        }
        return r;
    }

    /**
     * Borrowed from the JDK
     * @return
     */
    public int nextInt() {
        return next(32);
    }
}
