//Based on SnifferSSLSocketFactory.java from The Grinder distribution.
// The Grinder distribution is available at http://grinder.sourceforge.net/

package mitm;

import iaik.asn1.structures.AlgorithmID;
import iaik.x509.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;


/**
 * MITMSSLSocketFactory is used to create SSL sockets.
 *
 * This is needed because the javax.net.ssl socket factory classes don't
 * allow creation of factories with custom parameters.
 *
 */
public final class MITMSSLSocketFactory implements MITMSocketFactory
{
    final ServerSocketFactory m_serverSocketFactory;
    final SocketFactory m_clientSocketFactory;
    final SSLContext m_sslContext;

    public KeyStore ks = null;

    /*
     *
     * We can't install our own TrustManagerFactory without messing
     * with the security properties file. Hence we create our own
     * SSLContext and initialise it. Passing null as the keystore
     * parameter to SSLContext.init() results in a empty keystore
     * being used, as does passing the key manager array obtain from
     * keyManagerFactory.getInstance().getKeyManagers(). To pick up
     * the "default" keystore system properties, we have to read them
     * explicitly. UGLY, but necessary so we understand the expected
     * properties.
     *
     */

    /**
     * This constructor will create an SSL server socket factory
     * that is initialized with a fixed CA certificate
     */
    public MITMSSLSocketFactory()
	throws IOException,GeneralSecurityException
    {
	m_sslContext = SSLContext.getInstance("SSL");

	final KeyManagerFactory keyManagerFactory =
	    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

	final String keyStoreFile = System.getProperty(JSSEConstants.KEYSTORE_PROPERTY);
	final char[] keyStorePassword = System.getProperty(JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, "").toCharArray();
	final String keyStoreType = System.getProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY, "jks");

	final KeyStore keyStore;
	
	if (keyStoreFile != null) {
	    keyStore = KeyStore.getInstance(keyStoreType);
	    keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);

	    this.ks = keyStore;
	} else {
	    keyStore = null;
	}

	keyManagerFactory.init(keyStore, keyStorePassword);

	m_sslContext.init(keyManagerFactory.getKeyManagers(),
			  new TrustManager[] { new TrustEveryone() },
			  null);

	m_clientSocketFactory = m_sslContext.getSocketFactory();
	m_serverSocketFactory = m_sslContext.getServerSocketFactory(); 
    }

    /**
     * This constructor will create an SSL server socket factory
     * that is initialized with a dynamically generated server certificate
     * that contains the specified Distinguished Name.
     */
    public MITMSSLSocketFactory(Principal serverDN, BigInteger serialNumber)
	throws IOException,GeneralSecurityException, Exception
    {
        // TODO(cs255): replace this with code to generate a new (forged) server certificate with a DN of serverDN
        //   and a serial number of serialNumber.

	// You may find it useful to work from the comment skeleton below.

	final String keyStoreFile = System.getProperty(JSSEConstants.KEYSTORE_PROPERTY);
	final char[] keyStorePassword = System.getProperty(JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, "").toCharArray();
	final String keyStoreType = System.getProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY, "jks");
	// The "alias" is the name of the key pair in our keystore. (default: "mykey")
	//String alias = System.getProperty(JSSEConstants.KEYSTORE_ALIAS_PROPERTY);
	String alias = "mykey";
	//final char[] keyStorePassword = System.getProperty (JSSEConstants.DEFAULT_PWD, "").toCharArray ();

	final KeyStore keyStore;
	
	if (keyStoreFile != null) {
	    keyStore = KeyStore.getInstance(keyStoreType);
	    keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);
	    
	    this.ks = keyStore;
	} else {
	    keyStore = null;
	}

	// Get our key pair and our own DN (not the remote server's DN) from the keystore.
	if (ks == null) {
	    System.err.println("Null keystore ks initialized");
	}


	PrivateKey privateKey = (PrivateKey) ks.getKey (alias, keyStorePassword);
	Certificate cert = keyStore.getCertificate (alias);
	iaik.x509.X509Certificate certificate = new iaik.x509.X509Certificate(cert.getEncoded());
	PublicKey publicKey = cert.getPublicKey();
	//Principal ourDN = certificate.getIssuerX500Principal();
	Principal ourDN = certificate.getIssuerDN();
	

	// Initialize a new certificate for the server

	iaik.x509.X509Certificate serverCertificate = new iaik.x509.X509Certificate ();
	serverCertificate.setIssuerDN (ourDN);
	serverCertificate.setSubjectDN (serverDN);
	serverCertificate.setSerialNumber (serialNumber);
	serverCertificate.setPublicKey (publicKey);

	// Set Certificate Date
	Calendar cal = Calendar.getInstance();
	cal.set (Calendar.WEEK_OF_YEAR, 1);
	cal.set (Calendar.DAY_OF_WEEK, 1);
	cal.set (Calendar.HOUR_OF_DAY, 1);
	cal.set (Calendar.MINUTE, 1);
	cal.set (Calendar.SECOND, 1);
	Date before = cal.getTime();
	

	serverCertificate.setValidNotBefore (before); // beginning of year
	cal.add (Calendar.YEAR, 1); // next year
	Date after = cal.getTime();


	serverCertificate.setValidNotAfter (after);

	// Sign certificate
	serverCertificate.sign(AlgorithmID.sha1WithRSAEncryption, privateKey);	

  char[] emptyPassword = {};

	// Initialize new KeyStore to be sent to server
	KeyStore serverKeyStore = KeyStore.getInstance (keyStoreType);
	serverKeyStore.load (null, emptyPassword);
	serverKeyStore.setKeyEntry (alias, privateKey, emptyPassword, new Certificate[] {serverCertificate});

	//System.err.println("emptyPassword: " + emptyPassword);
	// . . .
	
	final KeyManagerFactory keyManagerFactory =
	    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	//keyManagerFactory.init(serverKeyStore, emptyPassword);
	keyManagerFactory.init(serverKeyStore, emptyPassword);

	m_sslContext = SSLContext.getInstance("SSL");
	m_sslContext.init(keyManagerFactory.getKeyManagers(),
			  new TrustManager[] { new TrustEveryone() },
			  null);

	m_clientSocketFactory = m_sslContext.getSocketFactory();
	m_serverSocketFactory = m_sslContext.getServerSocketFactory(); 
    }

    public final ServerSocket createServerSocket(String localHost,
						 int localPort,
						 int timeout)
	throws IOException
    {
	final SSLServerSocket socket =
	    (SSLServerSocket)m_serverSocketFactory.createServerSocket(
		localPort, 50, InetAddress.getByName(localHost));

	socket.setSoTimeout(timeout);

	socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

	return socket;
    }

    public final Socket createClientSocket(String remoteHost, int remotePort)
	throws IOException
    {
	final SSLSocket socket =
	    (SSLSocket)m_clientSocketFactory.createSocket(remoteHost,
							  remotePort);

	socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
	
	socket.startHandshake();

	return socket;
    }

    /**
     * We're carrying out a MITM attack, we don't care whether the cert
     * chains are trusted or not ;-)
     *
     */
    private static class TrustEveryone implements javax.net.ssl.X509TrustManager
    {
	public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
				       String authenticationType) {
	}
	
	public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
				       String authenticationType) {
	}

	public java.security.cert.X509Certificate[] getAcceptedIssuers()
	{
	    return null;
	}
    }
}

