package com.web.book.version.performance;

import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;

public class SummariserListener extends Summariser {
    private long total = 0;
    private long num = 0;
    private long errors = 0;

    public SummariserListener() {
        super();
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        super.sampleOccurred(e);
        total += e.getResult().getTime();
        num++;
        if (!e.getResult().isSuccessful()) {
            errors++;
        }
    }

    public long getTotal() {
        return total;
    }

    public long getNum() {
        return num;
    }

    public long getErrorCount() {
        return errors;
    }
}