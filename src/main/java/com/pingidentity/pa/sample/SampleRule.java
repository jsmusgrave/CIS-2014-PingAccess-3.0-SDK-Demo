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

/**
 *This is not a valid Rule yet.
 **/
public class SampleRule extends RuleInterceptorBase {
    @Override
    public ErrorHandlingCallback getErrorHandlingCallback() {
        return null;
    }

    @Override
    public List<ConfigurationField> getConfigurationFields() {
        return null;
    }
}
