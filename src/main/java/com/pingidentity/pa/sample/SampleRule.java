/**************************************************************************
 * Copyright (C) 2014 Ping Identity Corporation
 * All rights reserved.
 *
 * The contents of this file are subject to the Apache License,
 * Version 2.0, available at: http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS SOFTWARE IS PROVIDED ?AS IS?, WITHOUT ANY WARRANTIES, EXPRESS,
 * IMPLIED, STATUTORY OR ARISING BY CUSTOM OR TRADE USAGE, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * TITLE, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. THE USER
 * ASSUMES ALL RISK ASSOCIATED WITH ACCESS  AND USE OF THE SOFTWARE.
 **************************************************************************/
package com.pingidentity.pa.sample;

import com.pingidentity.pa.sdk.http.Exchange;
import com.pingidentity.pa.sdk.interceptor.Outcome;
import com.pingidentity.pa.sdk.policy.*;
import com.pingidentity.pa.sdk.policy.error.InternalServerErrorCallback;
import com.pingidentity.pa.sdk.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Rule(type = "SampleRule", label = "SampleRule" , expectedConfiguration = SampleRule.Configuration.class)
public class SampleRule extends RuleInterceptorBase<SampleRule.Configuration> {

    Logger log = LoggerFactory.getLogger(SampleRule.class);

    @Override
    public ErrorHandlingCallback getErrorHandlingCallback() {
        return new InternalServerErrorCallback();
    }

    @Override
    public List<ConfigurationField> getConfigurationFields() {
        return ConfigurationBuilder
                .from(Configuration.class)
                .toConfigurationFields();
    }

    @Override
    public void configure(Configuration pluginConfiguration) throws ValidationException {
        super.configure(pluginConfiguration);
        getConfiguration().getPattern();
    }

    @Override
    public Outcome handleRequest(Exchange exchange) throws RuntimeException, IOException, InterruptedException {
        log.debug("Begin Handling Request");

        String userAgent = exchange.getRequest().getHeader().getLastValue("User-Agent");

        if (userAgent != null && getConfiguration().getPattern().matcher(userAgent).matches()) {
            log.debug( "Found a valid user agent {} ", userAgent );
        } else {
            log.debug( "Found an invalid user agent {} ", userAgent );
            throw new AccessException("No UserAgent Found.");
        }

        log.debug("Done Handling Request");
        return Outcome.CONTINUE;
    }

    public static class Configuration extends SimplePluginConfiguration {

        @UIElement(label = "A Valid User Agent",
                type = ConfigurationType.TEXT,
                order=0,
                help = @Help(title = "This is a Regex for the allowable User Agents")
        )
        @NotNull(message="Please provide a Regex")
        @Regex(message="Not a valid Regex")
        public String validUserAgentRegex = null;

        Pattern pattern;

        public Pattern getPattern()  {
            if (pattern == null ) {
                pattern = Pattern.compile(validUserAgentRegex);
            }
            return pattern;
        }
    }

}
