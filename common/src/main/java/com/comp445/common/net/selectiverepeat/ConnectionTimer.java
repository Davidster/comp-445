package com.comp445.common.net.selectiverepeat;

import com.comp445.common.Utils;

import java.io.Closeable;
import java.util.concurrent.Future;

public class ConnectionTimer implements Closeable {

    private Future<Void> timeoutFuture;
    private int latestTimeout;

    public ConnectionTimer(int initialTimeout) {
        reset(initialTimeout);
    }

    public void reset() {
        reset(this.latestTimeout);
    }

    public void reset(int newTimeout) {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(true);
        }
        this.latestTimeout = newTimeout;
        timeoutFuture = Utils.asyncTimer(newTimeout);
    }

    public boolean isTimedOut() {
        return this.timeoutFuture.isDone();
    }

    public Future<Void> asFuture() {
        return this.timeoutFuture;
    }

    @Override
    public void close() {
        timeoutFuture.cancel(true);
    }
}
