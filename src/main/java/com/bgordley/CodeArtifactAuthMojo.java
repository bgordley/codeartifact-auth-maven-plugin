/*
 * Copyright (c) 2021 Bryan Gordley
 *
 * This software is released under the MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.bgordley;

import io.vavr.control.Try;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.codeartifact.CodeartifactClient;
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest;

@Mojo(name = "codeartifact-auth", defaultPhase = LifecyclePhase.COMPILE)
public class CodeArtifactAuthMojo extends AbstractMojo {

    private static final String CODEARTIFACT_ENV_VAR = "CODEARTIFACT_AUTH_TOKEN";

    @Parameter(property = "scope", required = true)
    String domain;

    @Parameter(property = "scope", required = true)
    String domainOwner;

    @Parameter(property = "scope")
    Long durationSeconds;

    public void execute() throws MojoExecutionException {
        GetAuthorizationTokenRequest request = getAuthorizationTokenRequest(
            domain, domainOwner, durationSeconds);
        String token = getAuthorizationToken(getCodeArtifactClient(), request);

        addEnvVar(CODEARTIFACT_ENV_VAR, token);
    }

    protected void addEnvVar(String varName, String varValue) throws MojoExecutionException {
        Try
            .of(EnvironmentUtils::getProcEnvironment)
            .andThenTry(envVarMap -> EnvironmentUtils.addVariableToEnvironment(
                envVarMap, String.format("%s=%s", varName, varValue)))
            .onFailure(ex -> logError("Failed to create environment variable '%s'", varName))
            .onSuccess(envVarMap -> logInfo(
                "Successfully added environment variable '%s'.", varName))
            .getOrElseThrow(ex -> new MojoExecutionException(
                String.format("Failed to create environment variable '%s'.", varName)
            ));
    }

    protected String getAuthorizationToken(CodeartifactClient codeartifactClient,
        GetAuthorizationTokenRequest request)
        throws MojoExecutionException {
        return Try
            .withResources(() -> codeartifactClient)
            .of(client -> client.getAuthorizationToken(request).authorizationToken())
            .onSuccess(token -> logInfo(
                "Successfully retrieved AWS authorization token for domain '%s' and domainOwner '%s'.",
                request.domain(), request.domainOwner()))
            .getOrElseThrow(ex -> new MojoExecutionException(
                "Failed to retrieve AWS CodeArtifact authorization token.", ex));
    }

    protected GetAuthorizationTokenRequest getAuthorizationTokenRequest(String domain,
        String domainOwner, Long durationSeconds) {
        return GetAuthorizationTokenRequest.builder()
            .domain(Objects.requireNonNull(domain, "Missing required param 'domain'."))
            .domainOwner(
                Objects.requireNonNull(domainOwner, "Missing required param 'domainOwner'."))
            .durationSeconds(Optional.ofNullable(durationSeconds).orElse(43200L))
            .build();
    }

    protected CodeartifactClient getCodeArtifactClient() {
        return CodeartifactClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    private void logError(String message, Object... params) {
        log(getLog()::error, message, params);
    }

    private void logInfo(String message, Object... params) {
        log(getLog()::error, message, params);
    }

    private void log(Consumer<CharSequence> logConsumer, String message, Object... params) {
        logConsumer.accept(String.format(message, params));
    }
}
