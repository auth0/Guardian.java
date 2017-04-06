# Change Log

## [0.2.0](https://github.com/auth0/Guardian.java/tree/0.2.0) (2017-04-06)
[Full Changelog](https://github.com/auth0/Guardian.java/compare/0.1.0...0.2.0)

**Added**
- Add transaction not found error to GuardianException [\#5](https://github.com/auth0/Guardian.java/pull/5) ([nikolaseu](https://github.com/nikolaseu))

**Changed**
- Remove HTTP logging interceptor [\#6](https://github.com/auth0/Guardian.java/pull/6) ([nikolaseu](https://github.com/nikolaseu))

## [0.1.0](https://github.com/auth0/Guardian.java/tree/0.1.0) (2017-03-24)
[Full Changelog](https://github.com/auth0/Guardian.java/compare/0.0.1...0.1.0)

**Changed**
- Exposed `protected` Transaction data [\#3](https://github.com/auth0/Guardian.java/pull/3) ([mirichan](https://github.com/mirichan))

## [0.0.1](https://github.com/auth0/Guardian.java/tree/0.0.1) (2017-03-10)

First release of Guardian for Java

### Install

Get Guardian Java via Maven:

```xml
<dependency>
  <groupId>com.auth0</groupId>
  <artifactId>guardian</artifactId>
  <version>0.0.1</version>
</dependency>
```

or Gradle:

```gradle
compile 'com.auth0:guardian:0.0.1'
```

### Usage

Create an instance of `Guardian` using you Guardian URL:

```java
Guardian guardian = new Guardian("https://<tenant>.guardian.auth0.com");
```

Obtain an enrollment ticket from API2:

```java
String enrollmentTicket = "Ag1qX7vZVBvyTKhFwrkzaCH2M8vn5b6c";
```

#### Enrollment

Use the ticket and `EnrollmentType.TOTP()` to request an TOTP enrollment, or `EnrollmentType.SMS()` and the phone number
for an SMS enrollment.

For TOTP you must ask for the TOTP URI to show to the user in the QR code.

```java
Transaction enrollmentTransaction;
try {
    enrollmentTransaction = guardian
            .requestEnroll(enrollmentTicket, EnrollmentType.TOTP());
            // or .requestEnroll(enrollmentTicket, EnrollmentType.SMS("+549XXXXXXXX58"));

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

#### Transaction storage

`Transaction` implements `java.io.Serializable` interface so you can save and restore it easily.

> The transaction contains sensitive information like the transaction token and the recovery code. Keep in mind this
> when considering possible storage options.

#### Confirm enrollment

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
