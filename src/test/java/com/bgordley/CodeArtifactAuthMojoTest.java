/*
 * Copyright (c) 2021 Bryan Gordley
 *
 * This software is released under the MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.bgordley;

import static org.junit.Assert.assertNotNull;

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
    public void addEnvVar() {
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