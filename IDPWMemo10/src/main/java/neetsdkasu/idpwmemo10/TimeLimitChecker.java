package neetsdkasu.idpwmemo10;

class TimeLimitChecker {
    // 30 minute. (== 1800 seconds == 1800000 milli-seconds)
    // static final long TIME_LIMIT = 30L * 60L * 1000L;

    // FOR TEST a minute. (== 60 seconds == 60000 milli-seconds)
    static final long TIME_LIMIT = 1L * 60L * 1000L;

    long timeLimit = 0L;

    boolean hasSuperLimit = false;
    long superLimit = 0L;

    TimeLimitChecker() {
        this.clear();
    }

    long clear() {
        this.timeLimit = System.currentTimeMillis() + TIME_LIMIT;
        return this.timeLimit;
    }

    boolean isOver() {
        long time = System.currentTimeMillis();
        if (this.hasSuperLimit) {
            return this.superLimit < time;
        }
        return this.timeLimit < time;
    }

    void setSuperLimit(long newSuperLimit) {
        this.superLimit = newSuperLimit;
        this.hasSuperLimit = true;
    }
}
