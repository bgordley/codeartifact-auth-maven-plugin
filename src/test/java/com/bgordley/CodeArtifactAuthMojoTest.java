/*
 * Copyright (c) 2021 Bryan Gordley
 *
 * This software is released under the MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.bgordley;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.Optional;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.codeartifact.CodeartifactClient;
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest;
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenResponse;

@RunWith(MockitoJUnitRunner.class)
public class CodeArtifactAuthMojoTest {

    private final String testKey = "test.key";
    private final String testValue = "test-value";
    private final String testDomain = "test-domain";
    private final String testDomainOwner = "test-domain-owner";
    private final Long testDurationSeconds = 60L;

    @Mock
    private CodeartifactClient mockCodeArtifactClient;

    @Spy
    private CodeArtifactAuthMojo spyCodeArtifactAuthMojo;

    @Before
    public void setUp() {
        spyCodeArtifactAuthMojo.domain = this.testDomain;
        spyCodeArtifactAuthMojo.domainOwner = this.testDomainOwner;
        spyCodeArtifactAuthMojo.durationSeconds = this.testDurationSeconds;
    }

    @After
    public void tearDown() {
        reset(mockCodeArtifactClient, spyCodeArtifactAuthMojo);

        System.clearProperty(this.testKey);
    }

    @Test
    public void execute() throws MojoExecutionException {
        assertNull(System.getProperty(this.testKey));

        when(this.spyCodeArtifactAuthMojo.getCodeArtifactClient())
            .thenReturn(this.mockCodeArtifactClient);

        when(this.spyCodeArtifactAuthMojo.getPropertyKey())
            .thenReturn(this.testKey);

        when(this.mockCodeArtifactClient.getAuthorizationToken(
            any(GetAuthorizationTokenRequest.class)))
            .thenReturn(GetAuthorizationTokenResponse.builder()
                .authorizationToken(this.testValue)
                .build());

        this.spyCodeArtifactAuthMojo.execute();

        assertEquals(this.testValue, System.getProperty(this.testKey));
    }

    @Test
    public void getAuthorizationTokenRequest() {
        GetAuthorizationTokenRequest request = this.spyCodeArtifactAuthMojo
            .getAuthorizationTokenRequest(
                this.testDomain, this.testDomainOwner, this.testDurationSeconds);

        assertNotNull(request);
        assertEquals(this.testDomain, request.domain());
        assertEquals(this.testDomainOwner, request.domainOwner());
        assertEquals(this.testDurationSeconds, request.durationSeconds());
    }

    @Test
    public void getAuthorizationToken() throws MojoExecutionException {
        when(this.mockCodeArtifactClient.getAuthorizationToken(
            any(GetAuthorizationTokenRequest.class)))
            .thenReturn(GetAuthorizationTokenResponse.builder()
                .authorizationToken(this.testValue)
                .build());

        GetAuthorizationTokenRequest request = GetAuthorizationTokenRequest.builder()
            .domain(this.testDomain)
            .domainOwner(this.testDomainOwner)
            .durationSeconds(this.testDurationSeconds)
            .build();

        String token = this.spyCodeArtifactAuthMojo.getAuthorizationToken(
            mockCodeArtifactClient, request);

        assertEquals(this.testValue, token);
    }

    @Test
    public void getCodeArtifactClient() {
        CodeartifactClient client = this.spyCodeArtifactAuthMojo.getCodeArtifactClient();

        assertNotNull(client);
    }

    @Test
    public void getPropertyKey() {
        String propertyKey = this.spyCodeArtifactAuthMojo.getPropertyKey();

        assertEquals("codeartifact.auth.token", propertyKey);
    }

    @Test
    public void addEnvVar() throws MojoExecutionException {
        assertNull(System.getProperty(this.testKey));

        this.spyCodeArtifactAuthMojo.addSystemProperty(this.testKey, this.testValue);

        assertEquals(this.testValue, System.getProperty(this.testKey));
    }
}