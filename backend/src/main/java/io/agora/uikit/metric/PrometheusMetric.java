package io.agora.uikit.metric;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import lombok.Data;

@Data
@Component
public class PrometheusMetric {
    @Autowired
    private CollectorRegistry collectorRegistry;

    private Counter httpRequestsCounter;
    private Counter taskCounter;
    private Histogram httpRequestsDurationSecondsHistogram;
    private Histogram rtmRequestsDurationSecondsHistogram;

    @PostConstruct
    private void init() {
        httpRequestsCounter = Counter.build()
                .name("http_requests_total")
                .help("http requests total")
                .labelNames("api", "code", "message")
                .register(collectorRegistry);

        taskCounter = Counter.build()
                .name("task_total")
                .help("task")
                .labelNames("name", "status")
                .register(collectorRegistry);

        httpRequestsDurationSecondsHistogram = Histogram.build()
                .name("http_requests_duration_seconds")
                .help("http requests duration seconds")
                .labelNames("api", "status")
                .register(collectorRegistry);

        rtmRequestsDurationSecondsHistogram = Histogram.build()
                .name("rtm_requests_duration_seconds")
                .help("rtm requests duration seconds")
                .labelNames("api")
                .register(collectorRegistry);
    }
}