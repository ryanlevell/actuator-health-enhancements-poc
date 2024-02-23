package org.levell.demo.app;

import oracle.ucp.jdbc.JDBCConnectionPoolStatistics;
import oracle.ucp.jdbc.PoolDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConnectionPoolHealthIndicator implements HealthIndicator {

    final PoolDataSource dataSource;

    public ConnectionPoolHealthIndicator(PoolDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {

        JDBCConnectionPoolStatistics stats = dataSource.getStatistics();
        Map<String, Object> details = Map.of(
                "available", stats.getAvailableConnectionsCount(),
                "borrowed", stats.getBorrowedConnectionsCount(),
                "abandoned", stats.getAbandonedConnectionsCount(),
                "total", stats.getTotalConnectionsCount(),
                "averageConnectionWaitTime", stats.getAverageConnectionWaitTime(),
                "peakConnectionWaitTime", stats.getPeakConnectionWaitTime(),
                "pendingRequests", stats.getPendingRequestsCount(),
                "remainingPoolCapacity", stats.getRemainingPoolCapacityCount());

        if (stats.getAvailableConnectionsCount() <= 0) {
            return Health.down().withDetails(details).build();
        }

        return Health.up().withDetails(details).build();
    }
}