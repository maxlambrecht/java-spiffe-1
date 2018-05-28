package spiffe.api.svid;

import spiffe.api.svid.util.ExponentialBackOff;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static spiffe.api.svid.Workload.*;

/**
 * Provides functionality to interact with a Workload API
 *
 */
public final class WorkloadAPIClient {

    private SpiffeWorkloadStub spiffeWorkloadStub;

    /**
     * Constructor
     * @param spiffeEndpointAddress
     */
    public WorkloadAPIClient(String spiffeEndpointAddress) {
        spiffeWorkloadStub = new SpiffeWorkloadStub(spiffeEndpointAddress);
    }

    /**
     * Default constructor
     * The WorkloadAPI Address will be resolved by an Environment Variable
     *
     */
    public WorkloadAPIClient() {
        spiffeWorkloadStub = new SpiffeWorkloadStub(EMPTY);
    }

    /**
     * Fetch the SVIDs from the Workload API
     * Use a Exponential Backoff to handle the errors and retries
     *
     * @return List of X509SVID or Empty List if none have been fetched
     */
    public List<X509SVID> fetchX509SVIDs() {
        return ExponentialBackOff.execute(this::callWorkloadApi);
    }

    private List<X509SVID> callWorkloadApi() {
        Iterator<X509SVIDResponse> response = spiffeWorkloadStub.fetchX509SVIDs(newRequest());
        if (response.hasNext()) {
            return response.next().getSvidsList();
        } else {
            return Collections.emptyList();
        }
    }

    private X509SVIDRequest newRequest() {
        return X509SVIDRequest.newBuilder().build();
    }
}
