# Guardian Java

[![Build][travis-ci-badge]][travis-ci-url]
[![MIT][mit-badge]][mit-url]
[![Maven][maven-badge]][maven-url]
[![JCenter][jcenter-badge]][jcenter-url]
[![codecov][codecov-badge]][codecov-url]

Java library for Auth0's [Guardian](https://auth0.com/multifactor-authentication) platform.

## Download

Get Guardian Java via Maven:

```xml
<dependency>
  <groupId>com.auth0</groupId>
  <artifactId>guardian</artifactId>
  <version>0.3.0</version>
</dependency>
```

or Gradle:

```gradle
compile 'com.auth0:guardian:0.3.0'
```

## Usage

Create an instance of `Guardian` using you Guardian URL:

```java
Guardian guardian = new Guardian("https://<tenant>.guardian.auth0.com");
```

Obtain an enrollment ticket from API2:

```java
String enrollmentTicket = "Ag1qX7vZVBvyTKhFwrkzaCH2M8vn5b6c";
```

### Enrollment

#### TOTP

Use the ticket and `EnrollmentType.TOTP()` to request an TOTP enrollment.
For TOTP you must ask for the TOTP URI to show to the user in the QR code.

```java
Transaction enrollmentTransaction;
try {
    enrollmentTransaction = guardian
            .requestEnroll(enrollmentTicket, EnrollmentType.TOTP());

    // Only for TOTP: use the TOTP URI to create a QR and scan with an app
    String totpURI = enrollmentTransaction.totpURI("Username", "Issuer");
    System.out.println(totpURI);

} catch (IOException e) {
    // connection issue, might be internet (or invalid certificates for example)
} catch (GuardianException e) {
    if (e.isAlreadyEnrolled()) {
        // the user was already enrolled
    } else if (e.isInvalidToken()) {
        // the ticket is not valid anymore, or was already used
    } else {
        // some other guardian error, check the message
    }
}
```

#### SMS

For SMS use `EnrollmentType.SMS()` and the phone number instead:

```java
Transaction enrollmentTransaction;
try {
    enrollmentTransaction = guardian
            .requestEnroll(enrollmentTicket, EnrollmentType.SMS("+549XXXXXXXX58"));

} catch (IOException e) {
    // connection issue, might be internet (or invalid certificates for example)
} catch (GuardianException e) {
    if (e.isAlreadyEnrolled()) {
        // the user was already enrolled
    } else if (e.isInvalidToken()) {
        // the ticket is not valid anymore, or was already used
    } else {
        // some other guardian error, check the message
    }
}
```

### Transaction storage

`Transaction` implements `java.io.Serializable` interface so you can save and restore it easily.

> The transaction contains sensitive information like the transaction token and the recovery code. Keep in mind this
> when considering possible storage options.

### Confirm enrollment

Restore the enrollment transaction from wherever you saved it, and use it together with the OTP that the user inputs to
confirm the enrollment, whether it's TOTP or SMS.

If the OTP was valid, the enrollment is confirmed and you get an object that contains the recovery code.

```java
// get the OTP from SMS or TOTP app
String code = "123456";

try {
    Enrollment enrollment = guardian.confirmEnroll(enrollmentTransaction, code);

    // Get the recovery code and show to the user
    String recoveryCode = enrollment.getRecoveryCode();
    System.out.println(recoveryCode);

} catch (IOException e) {
    // connection issue, might be internet (or invalid certificates for example)
} catch (GuardianException e) {
    if (e.isInvalidToken()) {
        // the transaction is not valid anymore
    } else if (e.isInvalidOTP()) {
        // the OTP is not valid
    } else {
        // some other guardian error, check the message
    }
}
```

## Documentation

For more information about [auth0](http://auth0.com) check our [documentation page](http://docs.auth0.com/).

## What is Auth0?

Auth0 helps you to:

* Add authentication with [multiple authentication sources](https://docs.auth0.com/identityproviders), either social like **Google, Facebook, Microsoft Account, LinkedIn, GitHub, Twitter, Box, Salesforce, amont others**, or enterprise identity systems like **Windows Azure AD, Google Apps, Active Directory, ADFS or any SAML Identity Provider**.
* Add authentication through more traditional **[username/password databases](https://docs.auth0.com/mysql-connection-tutorial)**.
* Add support for **[linking different user accounts](https://docs.auth0.com/link-accounts)** with the same user.
* Support for generating signed [Json Web Tokens](https://docs.auth0.com/jwt) to call your APIs and **flow the user identity** securely.
* Analytics of how, when and where users are logging in.
* Pull data from other sources and add it to the user profile, through [JavaScript rules](https://docs.auth0.com/rules).

## Create a free Auth0 Account

1. Go to [Auth0](https://auth0.com) and click Sign Up.
2. Use Google, GitHub or Microsoft Account to login.

## Issue Reporting

If you have found a bug or if you have a feature request, please report them at this repository issues section. Please do not report security vulnerabilities on the public GitHub issue tracker. The [Responsible Disclosure Program](https://auth0.com/whitehat) details the procedure for disclosing security issues.

## Author

[Auth0](https://auth0.com)

## License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for more info.


<!-- Vars -->

[travis-ci-badge]: https://travis-ci.org/auth0/Guardian.java.svg?branch=master
[travis-ci-url]: https://travis-ci.org/auth0/Guardian.java
[mit-badge]: http://img.shields.io/:license-mit-blue.svg?style=flat
[mit-url]: https://raw.githubusercontent.com/auth0/Guardian.java/master/LICENSE
[maven-badge]: https://img.shields.io/maven-central/v/com.auth0/guardian.svg
[maven-url]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.auth0%22%20AND%20a%3A%22guardian%22
[jcenter-badge]: https://api.bintray.com/packages/auth0/java/guardian/images/download.svg
[jcenter-url]: https://bintray.com/auth0/java/guardian/_latestVersion
[codecov-badge]: https://codecov.io/gh/auth0/Guardian.java/branch/master/graph/badge.svg
[codecov-url]: https://codecov.io/gh/auth0/Guardian.java
