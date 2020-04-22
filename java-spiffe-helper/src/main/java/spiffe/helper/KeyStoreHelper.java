package spiffe.helper;

import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import spiffe.result.Error;
import spiffe.result.Result;
import spiffe.workloadapi.Watcher;
import spiffe.workloadapi.WorkloadApiClient;
import spiffe.workloadapi.WorkloadApiClient.ClientOptions;
import spiffe.workloadapi.X509Context;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

/**
 * A <code>KeyStoreHelper</code> represents a helper for storing X095-SVIDs and Bundles,
 * that are automatically rotated via the Worklaod API, in a Java KeyStore in a file in disk.
 */
@Log
public class KeyStoreHelper {

    private final spiffe.helper.KeyStore keyStore;

    private final char[] privateKeyPassword;
    private final String privateKeyAlias;

    private final String spiffeSocketPath;

    /**
     * Create an instance of a KeyStoreHelper for fetching X509-SVIDs and Bundles
     * from a Workload API and store them in a Java binary KeyStore in disk.
     * <p>
     * It blocks until the initial update has been received from the Workload API.
     *
     * @param keyStoreFilePath   path to File storing the KeyStore.
     * @param keyStoreType       the type of keystore. Only JKS and PKCS12 are supported. If it's not provided, PKCS12 is used
     *  See the KeyStore section in the <a href=
     *  "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore">
     *  Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     *  for information about standard keystore types.
     * @param keyStorePassword   the password to generate the keystore integrity check
     * @param privateKeyPassword the password to protect the key
     * @param privateKeyAlias    the alias name
     * @param spiffeSocketPath   optional spiffeSocketPath, if absent uses SPIFFE_ENDPOINT_SOCKET env variable
     * @throws RuntimeException if this first update cannot be fetched.
     * @throws RuntimeException if the KeyStore cannot be setup.
     */
    @Builder
    public KeyStoreHelper(
            @NonNull final Path keyStoreFilePath,
            @NonNull final KeyStoreType keyStoreType,
            @NonNull final char[] keyStorePassword,
            @NonNull final char[] privateKeyPassword,
            @NonNull final String privateKeyAlias,
            @NonNull String spiffeSocketPath) {


        this.privateKeyPassword = privateKeyPassword.clone();
        this.privateKeyAlias = privateKeyAlias;
        this.spiffeSocketPath = spiffeSocketPath;

        this.keyStore =
                spiffe.helper.KeyStore
                        .builder()
                        .keyStoreFilePath(keyStoreFilePath)
                        .keyStoreType(keyStoreType)
                        .keyStorePassword(keyStorePassword)
                        .build();

        setupX509ContextFetcher();
    }

    @SneakyThrows
    private void setupX509ContextFetcher() {
        Result<WorkloadApiClient, String> workloadApiClient;

        if (StringUtils.isNotBlank(spiffeSocketPath)) {
            ClientOptions clientOptions = ClientOptions.builder().spiffeSocketPath(spiffeSocketPath).build();
            workloadApiClient = WorkloadApiClient.newClient(clientOptions);
        } else {
            workloadApiClient = WorkloadApiClient.newClient();
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        setX509ContextWatcher(workloadApiClient.getValue(), countDownLatch);
        countDownLatch.await();
    }

    private void setX509ContextWatcher(WorkloadApiClient workloadApiClient, CountDownLatch countDownLatch) {
        workloadApiClient.watchX509Context(new Watcher<X509Context>() {
            @Override
            public void OnUpdate(X509Context update) {
                log.log(Level.INFO, "Received X509Context update");
                storeX509ContextUpdate(update);
                countDownLatch.countDown();
            }

            @Override
            public void OnError(Error<X509Context, String> error) {
                throw new RuntimeException(error.getError());
            }
        });
    }

    private void storeX509ContextUpdate(final X509Context update) {
        val privateKeyEntry = PrivateKeyEntry.builder()
                .alias(privateKeyAlias)
                .password(privateKeyPassword)
                .privateKey(update.getDefaultSvid().getPrivateKey())
                .certificateChain(update.getDefaultSvid().getChainArray())
                .build();

        val storeKeyResult = keyStore.storePrivateKey(privateKeyEntry);
        if (storeKeyResult.isError()) {
            throw new RuntimeException(storeKeyResult.getError());
        }

        log.log(Level.INFO, "Stored X509Context update");

        // TODO: Store all the Bundles
        throw new NotImplementedException("Bundle Storing is not implemented");
    }
}