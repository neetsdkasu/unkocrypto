package neetsdkasu.fenc;

import mt19937ar.MersenneTwister;

final class FencRandom extends java.util.Random {

    final MersenneTwister mt;

    FencRandom(final String password) {
        final long[] key = FencRandom.makeKey(password);
        this.mt = new MersenneTwister(key);
    }

    @Override
    public int nextInt() {
        return (int)mt.genrand_int31();
    }

    static long toLong(final char ch) {
        return 0xFFFFL & (long)ch;
    }

    static long[] makeKey(final String password) {
        final char[] chars = password.toCharArray();
        final long[] key = new long[chars.length];
        for (int i = 0; i < chars.length; i++) {
            int p = chars.length - 1 - i;
            key[i] = (FencRandom.toLong(chars[i]) << 16) | FencRandom.toLong(chars[p]);
        }
        return key;
    }
}

