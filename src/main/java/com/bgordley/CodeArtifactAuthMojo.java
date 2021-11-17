/**
 * Copyright (c) 2021 Bryan Gordley
 *
 * This software is released under the MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.bgordley;

import io.vavr.control.Try;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.codeartifact.CodeartifactClient;
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest;
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenResponse;

@Slf4j
@Mojo(name = "codeartifact-auth", defaultPhase = LifecyclePhase.COMPILE)
public class CodeArtifactAuthMojo extends AbstractMojo {

    private static final String CODEARTIFACT_ENV_VAR =
        "CODEARTIFACT_AUTH_TOKEN";

    @Parameter(property = "scope", required = true)
    String domain;

    @Parameter(property = "scope", required = true)
    String domainOwner;

    @Parameter(property = "scope")
    Long durationSeconds;

    public void execute() throws MojoExecutionException, MojoFailureException {
        CodeartifactClient codeartifactClient = CodeartifactClient
            .builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

        Objects.requireNonNull(domain);
        Objects.requireNonNull(domainOwner);
        GetAuthorizationTokenRequest request = GetAuthorizationTokenRequest
            .builder()
            .domain(domain)
            .domainOwner(domainOwner)
            .durationSeconds(
                Optional.ofNullable(durationSeconds).orElse(43200L)
            )
            .build();

        GetAuthorizationTokenResponse response = codeartifactClient.getAuthorizationToken(
            request
        );

        codeartifactClient.close();

        Try
            .of(EnvironmentUtils::getProcEnvironment)
            .andThenTry(
                env ->
                    EnvironmentUtils.addVariableToEnvironment(
                        env,
                        String.format(
                            "%s=%s",
                            CODEARTIFACT_ENV_VAR,
                            response.authorizationToken()
                        )
                    )
            )
            .onFailure(
                ex ->
                    log.error(
                        "Failed to create environment variable '{}'",
                        CODEARTIFACT_ENV_VAR
                    )
            );
    }
}
