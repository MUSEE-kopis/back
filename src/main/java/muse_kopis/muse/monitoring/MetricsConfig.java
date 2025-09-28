package muse_kopis.muse.monitoring;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public ProcessMemoryMetrics processMemoryMetrics(MeterRegistry registry) {
        return new ProcessMemoryMetrics();
    }
}
