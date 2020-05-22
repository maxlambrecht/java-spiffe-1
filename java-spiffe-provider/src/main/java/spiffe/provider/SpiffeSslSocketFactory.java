package spiffe.provider;

import lombok.val;
import spiffe.provider.SpiffeSslContextFactory.SslContextOptions;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * A <code>SpiffeSslSocketFactory</code> is an implementation of SSLSocketFactory
 * that provides methods to create {@link javax.net.ssl.SSLSocket}
 * backed by a SPIFFE SSLContext {@link SpiffeSslContextFactory}.
 */
public class SpiffeSslSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegate;

    public SpiffeSslSocketFactory(SslContextOptions contextOptions) throws KeyManagementException, NoSuchAlgorithmException {
        val sslContext = SpiffeSslContextFactory.getSslContext(contextOptions);
        delegate = sslContext.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
        return delegate.createSocket(socket, s, i, b);
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException {
        return delegate.createSocket(s, i);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {
        return delegate.createSocket(s, i, inetAddress, i1);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return delegate.createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        return delegate.createSocket(inetAddress, i, inetAddress1, i1);
    }

    @Override
    public Socket createSocket() throws IOException {
        return delegate.createSocket();
    }
}