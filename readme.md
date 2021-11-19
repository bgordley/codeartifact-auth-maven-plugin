# WIP - THIS MAVEN PLUGIN IS **NOT PRODUCTION READY!**
# Overview
Maven plugin designed to automate AWS CodeArtifact authentication for Java 1.8+ projects.

## AWS CLI
```sh
aws codeartifact get-authorization-token --domain your-domain --domain-owner your-domain-owner --query authorizationToken --output text
```