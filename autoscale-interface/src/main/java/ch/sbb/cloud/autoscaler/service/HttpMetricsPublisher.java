package ch.sbb.cloud.autoscaler.service;

import ch.sbb.cloud.autoscaler.model.Metrics;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by micic on 04.09.16.
 */
public class HttpMetricsPublisher {

    private final MetricsEventSender eventSender;
    private final AtomicLong numberOfConnections = new AtomicLong();

    private final AtomicLong avgServerSideResponseTime = new AtomicLong();
    private final AtomicLong totalRequestsForAvgServerSideResponseTime = new AtomicLong();

    public HttpMetricsPublisher(String projectName, String serviceName, RabbitTemplate rabbitTemplate) {
        this.eventSender = new MetricsEventSender(projectName, serviceName, rabbitTemplate);
    }

    public void registerServiceEntry(HttpServletRequest request) {
        numberOfConnections.incrementAndGet();

        long startTime = System.currentTimeMillis();
        request.setAttribute("metricsStartTime", startTime);
    }

    public void regsterServiceExit(HttpServletRequest request){
        if (numberOfConnections.longValue() > 0) {
            numberOfConnections.decrementAndGet();
        }

        long startTime = (Long) request.getAttribute("metricsStartTime");
        updateAvgServerSideResponseTime((System.currentTimeMillis() - startTime));
    }

    private void updateAvgServerSideResponseTime(long newAvgValue) {
        long totalRequests = totalRequestsForAvgServerSideResponseTime.longValue();
        long currentAvgValue = avgServerSideResponseTime.longValue();
        avgServerSideResponseTime.set(
                (totalRequests * currentAvgValue + newAvgValue) / (totalRequests + 1)
        );
    }

    public void publishNumberOfConnections() {
        this.eventSender.sendMetricsEvent(Metrics.NUMBER_OF_HTTP_CONNECTIONS, numberOfConnections.longValue());
    }

    public void publishAvgServerSideResponseTimeAndClearMetrics() {
        this.eventSender.sendMetricsEvent(Metrics.SERVER_SIDE_RESPONSETIME, avgServerSideResponseTime.longValue());

        avgServerSideResponseTime.set(0);
        totalRequestsForAvgServerSideResponseTime.set(0);
    }
}
