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

    @Parameter(property = "domain", required = true)
    String domain;

    @Parameter(property = "domainOwner", required = true)
    String domainOwner;

    @Parameter(property = "durationSeconds")
    Long durationSeconds;

    public void execute() throws MojoExecutionException {
        GetAuthorizationTokenRequest request = getAuthorizationTokenRequest(
            domain, domainOwner, durationSeconds);
        String token = getAuthorizationToken(getCodeArtifactClient(), request);

        addSystemProperty(getPropertyKey(), token);
    }

    protected void addSystemProperty(String key, String value) throws MojoExecutionException {
        Try.of(() -> System.setProperty(key, value))
            .onSuccess(envVarMap -> getLog().info(
                String.format("Successfully set system property '%s'.", key)))
            .getOrElseThrow(ex -> new MojoExecutionException(
                String.format("Failed to set system property '%s'", key)
            ));
    }

    protected String getAuthorizationToken(CodeartifactClient codeartifactClient,
        GetAuthorizationTokenRequest request)
        throws MojoExecutionException {
        return Try
            .withResources(() -> codeartifactClient)
            .of(client -> client.getAuthorizationToken(request).authorizationToken())
            .onSuccess(token -> getLog().info(String.format(
                "Successfully retrieved AWS authorization token for domain '%s' and domainOwner '%s'.",
                request.domain(), request.domainOwner())))
            .getOrElseThrow(ex -> new MojoExecutionException(
                String.format(
                    "Failed to retrieved AWS authorization token for domain '%s' and domainOwner '%s'.",
                    request.domain(), request.domainOwner()), ex));
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

    protected String getPropertyKey() {
        return "codeartifact.auth.token";
    }

    protected CodeartifactClient getCodeArtifactClient() {
        return CodeartifactClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
