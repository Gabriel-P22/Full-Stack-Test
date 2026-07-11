package com.appointment.frameworks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "appointment.kafka")
public class AppointmentKafkaProperties {

    private String topic = "appointment-events-topic";
    private Dlt dlt = new Dlt();
    private Retry retry = new Retry();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Dlt getDlt() {
        return dlt;
    }

    public void setDlt(Dlt dlt) {
        this.dlt = dlt;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public static class Dlt {
        private String suffix = "-dlt";
        private String deserializationSuffix = "-deserialization-dlt";

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public String getDeserializationSuffix() {
            return deserializationSuffix;
        }

        public void setDeserializationSuffix(String deserializationSuffix) {
            this.deserializationSuffix = deserializationSuffix;
        }
    }

    public static class Retry {
        private long backoffIntervalMs = 1_000L;
        private long maxAttempts = 2L;

        public long getBackoffIntervalMs() {
            return backoffIntervalMs;
        }

        public void setBackoffIntervalMs(long backoffIntervalMs) {
            this.backoffIntervalMs = backoffIntervalMs;
        }

        public long getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(long maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
    }
}
