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
Run :
  java mitm/MITMProxyServer -keyStore keystore.jks -keyStorePassword foobar -outputFile log.txt -pwdFile pwdFile

TO CREATE A NEW ADMIN PASSWORD:
Run :
  java mitm/PasswordUtils [new password] [filename]


SHORT ANSWERS

1.) Assuming that the attacker can intercept or redirect messages across the network, our attacker will have
the capability to replay any such queries to the admin server as well as control all legitimate admin clients
on the network by intercepting their messages (and either replacing them with a set of commands at the will
of the attacker, or preventing them from reaching the server). Furthermore, under our current scheme the password
of the admin server is passed in plain sight over the network to the admin server, and thus the attacker can
even set up a "legitimate" admin client simply with eavesdropping knowledge of that password.

2.) For threat model a.), our adversary is attempting to gain access to our admin server with knowledge of the
password file pwdFile. Since we store a salted version of our password in pwdFile and then verify that the
entered plaintext password hashes to the same password hash contained in pwdFile, we postulate that our 
system is secure under CPA since BCrypt is a one-way salted hash. For threat model b.), the attacker has the
ability to read/write to the password file in between server invocations. Our implementation as it stands does
not protect against this kind of attack, since now the attacker can mount an attack by simply choosing a 
plaintext password, using the same salt as before and running BCrypt to hash the chosen password and then 
storing it in the file. Under this threat model, the server must either retain "state" in order to prevent against
such replay-style attacks, or use a non-deterministic password verification model. The simpler option would be to
implement our server so that it retains state, and thus avoiding the latter option which would require a MAC-based
key setup session with a secret key and verifier key (and thus rid the user of the freedom of choosing his/her
own password). Instead, by retaining state we can simply use a nonce-based counter mode, in which hashing is done
both locally as well as during verification on the server. Since we presume the attacker is not in control of the 
nonce (which can be chosen at random and then shared between client and server, or can be initialized in conjunction
between the server and client and then incremented with each login), the attacker cannot predict the hash by simply
changing a file and thus the new system is secure under the threat model b.).

3.) Our current MITM attack relies on the user to trust the certificate authority, and thus any browser security
model that is more secure must not be as susceptible to social engineering. As it stands, the browser simply 
presents the user with an error message saying "This connection is Untrusted", with the indirect option to view
the certificate after clicking through multiple layers. An initial first step towards preventing the user from
being manipulated would be to present the information more clearly: i.e., the details of the certificate authority
immediately on the first page. Furthermore, the exception cannot be permanently stored - currently, if an 
exception is confirmed then all future calls will be unprompted. Highlighting any such website calls clearly (such
as by using a flag that indicates that browsing is potentially insecure while a user is issued a certificate from
a suspicious CA) will make the user more likely to distrust the certificate authority (as valid alternatives exist).
An equally valid option would be to alert the user with a flag while the browser settings are set to visit an SSL
proxy (which may have been modified without the user's knowledge, such as through a malicious third-party program).
