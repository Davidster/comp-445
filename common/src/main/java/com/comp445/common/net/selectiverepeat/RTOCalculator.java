package com.comp445.common.net.selectiverepeat;

import static com.comp445.common.Utils.SR_CLOCK_GRANULARITY;

public class RTOCalculator {

    private static final float INITIAL_VALUE = 1000f;
    private static final float ALPHA = 1 / 8f;
    private static final float BETA = 1 / 4f;
    private static final float K = 4f;

    private float latestRto;
    private float latestRtt;
    private float latestSrtt;
    private float latestRttvar;

    public RTOCalculator() {
        latestRto = INITIAL_VALUE;
        latestRtt = -1;
    }

    public int getLatestRto() {
        return Math.round(this.latestRto);
    }

    public void update(float latestRtt) {
        if (latestRtt <= 0) {
            throw new IllegalArgumentException("New RTT value must be above 0ms");
        }

        if (this.latestRtt == -1) {
            this.latestRtt = latestRtt;
            this.latestSrtt =  this.latestRtt;
            this.latestRttvar =  this.latestRtt / 2f;
        } else {
            this.latestRtt = latestRtt;
            this.latestRttvar = (1 - BETA) * this.latestRttvar + BETA * Math.abs(this.latestSrtt - this.latestRtt);
            this.latestSrtt = (1 - ALPHA) * this.latestSrtt + ALPHA * this.latestRtt;
        }

        this.latestRto = this.latestSrtt  + Math.max(SR_CLOCK_GRANULARITY, K * this.latestRttvar);
    }

    public void onTimeout() {
        this.latestRto *= 2;
    }
}
