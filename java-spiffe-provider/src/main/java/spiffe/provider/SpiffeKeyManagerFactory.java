package spiffe.provider;

import lombok.val;
import spiffe.exception.SocketEndpointAddressException;
import spiffe.exception.X509SourceException;
import spiffe.svid.x509svid.X509SvidSource;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import java.security.KeyStore;

/**
 * A <code>SpiffeKeyManagerFactory</code></cod> is an implementation of a {@link KeyManagerFactorySpi}
 * to create a {@link KeyManager} that backed by the Workload API.
 * <p>
 * The JSSE API will call engineGetKeyManagers() to get an instance of a KeyManager. This KeyManager
 * instance is injected with a {@link spiffe.workloadapi.X509Source} to obtain the latest X.509 SVIDs.
 *
 * @see SpiffeSslContextFactory
 * @see X509SvidSource
 * @see X509SourceManager
 * @see SpiffeSslContextFactory
 */
public final class SpiffeKeyManagerFactory extends KeyManagerFactorySpi {

    /**
     * Default method for creating the KeyManager, uses a X509Source instance
     * that is handled by the Singleton {@link X509SourceManager}
     *
     * @throws SpiffeProviderException in case there is an error setting up the X509 source
     */
    @Override
    protected KeyManager[] engineGetKeyManagers() {
        SpiffeKeyManager spiffeKeyManager;
        try {
            spiffeKeyManager = new SpiffeKeyManager(X509SourceManager.getX509Source());
        } catch (X509SourceException e) {
            throw new SpiffeProviderException("The X509 source could not be created", e);
        } catch (SocketEndpointAddressException e) {
            throw new SpiffeProviderException("The Workload API Socket endpoint address configured is not valid", e);
        }
        return new KeyManager[]{spiffeKeyManager};
    }

    /**
     * This method creates a KeyManager and initializes with the given X.509 SVID source.
     *
     * @param x509SvidSource an instance of a {@link X509SvidSource}
     * @return an array with an instance of a {@link KeyManager}
     */
    public KeyManager[] engineGetKeyManagers(X509SvidSource x509SvidSource) {
        val spiffeKeyManager = new SpiffeKeyManager(x509SvidSource);
        return new KeyManager[]{spiffeKeyManager};
    }

    @Override
    protected void engineInit(KeyStore keyStore, char[] chars) {
        //no implementation needed
    }

    @Override
    protected void engineInit(ManagerFactoryParameters managerFactoryParameters) {
        //no implementation needed
    }

}