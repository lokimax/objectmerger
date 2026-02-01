# Release Guide

## Prerequisites

Before releasing to Maven Central, ensure your local environment is configured.

### 1. GPG Key
You need a GPG key to sign artifacts.
1.  **Generate Key**: `gpg --gen-key`
2.  **List Keys**: `gpg --list-keys`
3.  **Distribute Public Key**: `gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>`

### 2. Maven Settings (`~/.m2/settings.xml`)
Configure your Sonatype (Central Portal) credentials and GPG passphrase.

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_TOKEN_USERNAME</username>
      <password>YOUR_TOKEN_PASSWORD</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>gpg</id>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

> **Note**: Generate the Token (User/Pass) in the Sonatype Central Portal under "Account" -> "Generate User Token".

## Publishing

To publish a release:

1.  **Checkout develop**: `git checkout develop`
2.  **Deploy**:
    ```bash
    mvn clean deploy -P release
    ```

This command will:
- Build the project.
- Generate Source and Javadoc JARs.
- Sign all artifacts with GPG.
- Upload them to the Central Publishing Portal.
- Automatically publish them (if `autoPublish` is set to true in `pom.xml`).

## Verification
Login to [central.sonatype.com](https://central.sonatype.com) to check the status of your deployment.
