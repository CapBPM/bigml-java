package org.bigml.binding.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete
 * batch centroids.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/batchcentroids
 *
 *
 */
public class BatchCentroid extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchCentroid.class);

    public final static String DOWNLOAD_DIR = "/download";

    /**
     * Constructor
     *
     */
    public BatchCentroid() {
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public BatchCentroid(final String apiUser, final String apiKey,
            final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public BatchCentroid(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(cacheManager);
    }

    /**
     * Check if the current resource is a BatchCentroid
     *
     * @param resource the resource to be checked
     * @return true if it's a BatchCentroid
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(BATCH_CENTROID_RE);
    }

    /**
     * Creates a new batchcentroid.
     *
     * POST /andromeda/batchcentroid?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars for the cluster.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset.
     * @param args
     *            set of parameters for the new batchcentroid. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for centroid before to start to create the
     *            batchcentroid. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final String clusterId, final String datasetId,
            String args, Integer waitTime, Integer retries) {
        return create(clusterId, datasetId, (JSONObject) JSONValue.parse(args),
                waitTime, retries);
    }

    /**
     * Creates a new batchcentroid.
     *
     * POST /andromeda/batchcentroid?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars for the cluster.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset.
     * @param args
     *            set of parameters for the new batchcentroid. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for centroid before to start to create the
     *            batchcentroid. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String clusterId, final String datasetId,
            JSONObject args, Integer waitTime, Integer retries) {
        if (clusterId == null || clusterId.length() == 0
                || !(clusterId.matches(CLUSTER_RE))) {
            logger.info("Wrong cluster id");
            return null;
        }

        if (datasetId == null || datasetId.length() == 0
                || !(datasetId.matches(DATASET_RE))) {
            logger.info("Wrong dataset id");
            return null;
        }

        try {
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;
            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .clusterIsReady(clusterId)) {
                    Thread.sleep(waitTime);
                    count++;
                }
            }

            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .datasetIsReady(datasetId)) {
                    Thread.sleep(waitTime);
                    count++;
                }
            }

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            requestObject.put("cluster", clusterId);
            requestObject.put("dataset", datasetId);

            return createResource(BATCH_CENTROID_URL,
                    requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating batchcentroid");
            return null;
        }
    }

    /**
     * Retrieves a batchcentroid.
     *
     * A batchcentroid is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the batchcentroid values and state info available at the time it
     * is called.
     *
     * GET /andromeda/batchcentroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param batchcentroidId
     *            a unique identifier in the form batchcentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String batchcentroidId) {
        if (batchcentroidId == null || batchcentroidId.length() == 0
                || !batchcentroidId.matches(BATCH_CENTROID_RE)) {
            logger.info("Wrong batchcentroid id");
            return null;
        }

        return getResource(BIGML_URL + batchcentroidId);
    }

    /**
     * Retrieves the batch centroid file.
     *
     * Downloads predictions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchCentroidId
     *            a unique identifier in the form batchCentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchCentroid(final String batchCentroidId,
            final String filename) {

        if (batchCentroidId == null || batchCentroidId.length() == 0
                || !batchCentroidId.matches(BATCH_CENTROID_RE)) {
            logger.info("Wrong batch centroid id");
            return null;
        }

        String url = BIGML_URL + batchCentroidId + DOWNLOAD_DIR;
        return download(url, filename);
    }

    /**
     * Retrieves the batch centroid file.
     *
     * Downloads predictions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchCentroid
     *            a batch centroid JSONObject.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchCentroid(final JSONObject batchCentroid,
            final String filename) {

        String resourceId = (String) batchCentroid.get("resource");

        if (resourceId != null) {
            String url = BIGML_URL + resourceId + DOWNLOAD_DIR;
            return download(url, filename);
        }
        return null;
    }

    /**
     * Retrieves a batchcentroid.
     *
     * A batchcentroid is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the batchcentroid values and state info available at the time it
     * is called.
     *
     * GET /andromeda/batchcentroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param batchcentroid
     *            a batchcentroid JSONObject.
     *
     */
    @Override
    public JSONObject get(final JSONObject batchcentroid) {
        String batchcentroidId = (String) batchcentroid.get("resource");
        return get(batchcentroidId);
    }

    /**
     * Check whether a batchcentroid's status is FINISHED.
     *
     * @param batchcentroidId
     *            a unique identifier in the form batchcentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String batchcentroidId) {
        return isResourceReady(get(batchcentroidId));
    }

    /**
     * Check whether a batchcentroid's status is FINISHED.
     *
     * @param batchcentroid
     *            a batchcentroid JSONObject.
     *
     */
    @Override
    public boolean isReady(final JSONObject batchcentroid) {
        String resourceId = (String) batchcentroid.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your batchcentroids.
     *
     * GET /andromeda/batchcentroid?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(BATCH_CENTROID_URL, queryString);
    }

    /**
     * Updates a batchcentroid.
     *
     * PUT /andromeda/batchcentroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchcentroidId
     *            a unique identifier in the form batchcentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the batchcentroid. Optional
     *
     */
    @Override
    public JSONObject update(final String batchcentroidId, final String changes) {
        if (batchcentroidId == null || batchcentroidId.length() == 0
                || !batchcentroidId.matches(BATCH_CENTROID_RE)) {
            logger.info("Wrong batchcentroid id");
            return null;
        }
        return updateResource(BIGML_URL + batchcentroidId, changes);
    }

    /**
     * Updates a batchcentroid.
     *
     * PUT /andromeda/batchcentroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchcentroid
     *            an batchcentroid JSONObject
     * @param changes
     *            set of parameters to update the batchcentroid. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject batchcentroid,
            final JSONObject changes) {
        String resourceId = (String) batchcentroid.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a batchcentroid.
     *
     * DELETE /andromeda/batchcentroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchcentroidId
     *            a unique identifier in the form batchcentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String batchcentroidId) {
        if (batchcentroidId == null || batchcentroidId.length() == 0
                || !batchcentroidId.matches(BATCH_CENTROID_RE)) {
            logger.info("Wrong batchcentroid id");
            return null;
        }
        return deleteResource(BIGML_URL + batchcentroidId);
    }

    /**
     * Deletes a batchcentroid.
     *
     * DELETE /andromeda/batchcentroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchcentroid
     *            an batchcentroid JSONObject.
     *
     */
    @Override
    public JSONObject delete(final JSONObject batchcentroid) {
        String resourceId = (String) batchcentroid.get("resource");
        return delete(resourceId);
    }

}
