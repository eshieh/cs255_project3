Sam Keller (samath)
Evan Shieh (eshieh)


DESIGN CHOICES:
We used BCrypt to dynamically create a password file & associated salt. The choice of BCrypt was influenced
by our desire to store salted passwords. To interface with the BCrypt library, we create a new Java utility
called PasswordUtils.java. 

To generate our Keystore, we use the keytool utility with password foobar and alias mykey. We intentionally
do not use this password in the keystore to be sent to the server so as to minimize the chances that our local
keystore password is compromised. In order to ensure that our certificate is recognized by the browser, we use
a deterministic hashing algorithm (sha1WithRSAEncryption) that matches the same algorithm used in keytool (RSA).



TO RUN :
Run the java commands as in the handout.

TO CREATE A NEW ADMIN PASSWORD:
Run :
  java mitm/PasswordUtils [new password]


SHORT ANSWERS

1.
