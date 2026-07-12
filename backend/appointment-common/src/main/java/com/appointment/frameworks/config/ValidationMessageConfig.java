package com.appointment.frameworks.config;

import jakarta.validation.MessageInterpolator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class ValidationMessageConfig {

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean(Environment environment) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setMessageInterpolator(
                new PropertyPlaceholderMessageInterpolator(new ParameterMessageInterpolator(), environment)
        );
        return factoryBean;
    }

    private static class PropertyPlaceholderMessageInterpolator implements MessageInterpolator {

        private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

        private final MessageInterpolator delegate;
        private final Environment environment;

        PropertyPlaceholderMessageInterpolator(MessageInterpolator delegate, Environment environment) {
            this.delegate = delegate;
            this.environment = environment;
        }

        @Override
        public String interpolate(String messageTemplate, Context context) {
            return delegate.interpolate(resolve(messageTemplate), context);
        }

        @Override
        public String interpolate(String messageTemplate, Context context, Locale locale) {
            return delegate.interpolate(resolve(messageTemplate), context, locale);
        }

        private String resolve(String messageTemplate) {
            Matcher matcher = PLACEHOLDER.matcher(messageTemplate);
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String value = environment.getProperty(matcher.group(1));
                matcher.appendReplacement(result, Matcher.quoteReplacement(value != null ? value : matcher.group(0)));
            }
            matcher.appendTail(result);
            return result.toString();
        }
    }
}
