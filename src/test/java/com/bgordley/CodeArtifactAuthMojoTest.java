/*
 * Copyright (c) 2021 Bryan Gordley
 *
 * This software is released under the MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.bgordley;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import software.amazon.awssdk.services.codeartifact.CodeartifactClient;

@RunWith(JUnit4.class)
public class CodeArtifactAuthMojoTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void execute() {
    }

    @Test
    public void addEnvVar() throws MojoExecutionException {
        String testKey = "test.key";
        String testValue = "test-value";

        assertNull(System.getProperty(testKey));

        new CodeArtifactAuthMojo().addSystemProperty(testKey, testValue);

        assertEquals(testValue, System.getProperty(testKey));
    }

    @Test
    public void getAuthorizationToken() {
    }

    @Test
    public void buildAuthorizationTokenRequest() {
        CodeArtifactAuthMojo mojo = new CodeArtifactAuthMojo() {
            @Override
            protected CodeartifactClient getCodeArtifactClient() {
                CodeartifactClient client = super.getCodeArtifactClient();

                assertNotNull(client);

                return client;
            }
        };

        mojo.getCodeArtifactClient();
    }
}