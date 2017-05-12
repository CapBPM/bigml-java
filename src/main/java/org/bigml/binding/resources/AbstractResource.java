package org.bigml.binding.resources;

import org.bigml.binding.AuthenticationException;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Entry point to create, retrieve, list, update, and delete sources, datasets,
 * models and predictions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers
 *
 *
 */
public abstract class AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(AbstractResource.class);

    public final static String SOURCE_PATH = "source";
    public final static String DATASET_PATH = "dataset";
    public final static String MODEL_PATH = "model";
    public final static String PREDICTION_PATH = "prediction";
    public final static String EVALUATION_PATH = "evaluation";
    public final static String ENSEMBLE_PATH = "ensemble";
    public final static String BATCH_PREDICTION_PATH = "batchprediction";
    public final static String CLUSTER_PATH = "cluster";
    public final static String CENTROID_PATH = "centroid";
    public final static String BATCH_CENTROID_PATH = "batchcentroid";
    public final static String ANOMALY_PATH = "anomaly";
    public final static String ANOMALYSCORE_PATH = "anomalyscore";
    public final static String BATCHANOMALYSCORE_PATH = "batchanomalyscore";
    public final static String PROJECT_PATH = "project";
    public final static String SAMPLE_PATH = "sample";
    public final static String CORRELATION_PATH = "correlation";
    public final static String STATISTICALTEST_PATH = "statisticaltest";
    public final static String LOGISTICREGRESSION_PATH = "logisticregression";
    public final static String SCRIPT_PATH = "script";
    public final static String EXECUTION_PATH = "execution";
    public final static String LIBRARY_PATH = "library";
    public final static String ASSOCIATION_PATH = "association";
    public final static String ASSOCIATIONSET_PATH = "associationset";
    public final static String TOPICMODEL_PATH = "topicmodel";
    public final static String TOPICDISTRIBUTION_PATH = "topicdistribution";
    public final static String BATCH_TOPICDISTRIBUTION_PATH = "batchtopicdistribution";
    public final static String CONFIGURATION_PATH = "configuration";

    // Base Resource regular expressions
    static String SOURCE_RE = "^" + SOURCE_PATH + "/[a-f,0-9]{24}$";
    static String DATASET_RE = "^(public/|)" + DATASET_PATH
            + "/[a-f,0-9]{24}$|^shared/" + DATASET_PATH
            + "/[a-zA-Z0-9]{26,27}$";
    static String MODEL_RE = "^(public/|)" + MODEL_PATH
            + "/[a-f,0-9]{24}$|^shared/" + MODEL_PATH + "/[a-zA-Z0-9]{26,27}$";
    static String PREDICTION_RE = "^" + PREDICTION_PATH + "/[a-f,0-9]{24}$";
    static String EVALUATION_RE = "^" + EVALUATION_PATH + "/[a-f,0-9]{24}$";
    static String ENSEMBLE_RE = "^" + ENSEMBLE_PATH + "/[a-f,0-9]{24}$";
    static String BATCH_PREDICTION_RE = "^" + BATCH_PREDICTION_PATH
            + "/[a-f,0-9]{24}$";
    static String CLUSTER_RE = "^(public/|)" + CLUSTER_PATH
            + "/[a-f,0-9]{24}$|^shared/" + CLUSTER_PATH
            + "/[a-zA-Z0-9]{26,27}$";
    static String CENTROID_RE = "^" + CENTROID_PATH + "/[a-f,0-9]{24}$";
    static String BATCH_CENTROID_RE = "^" + BATCH_CENTROID_PATH
            + "/[a-f,0-9]{24}$";
    static String ANOMALY_RE = "^" + ANOMALY_PATH
            + "/[a-f,0-9]{24}$";
    static String ANOMALYSCORE_RE = "^" + ANOMALYSCORE_PATH
            + "/[a-f,0-9]{24}$";
    static String BATCH_ANOMALYSCORE_RE = "^" + BATCHANOMALYSCORE_PATH
            + "/[a-f,0-9]{24}$";
    static String PROJECT_RE = "^" + PROJECT_PATH
            + "/[a-f,0-9]{24}$";
    static String SAMPLE_RE = "^" + SAMPLE_PATH
            + "/[a-f,0-9]{24}$";
    static String CORRELATION_RE = "^" + CORRELATION_PATH
            + "/[a-f,0-9]{24}$";
    static String STATISTICALTEST_RE = "^" + STATISTICALTEST_PATH
            + "/[a-f,0-9]{24}$";
    static String LOGISTICREGRESSION_RE = "^" + LOGISTICREGRESSION_PATH
            + "/[a-f,0-9]{24}$";
    static String SCRIPT_RE = "^" + SCRIPT_PATH + "/[a-f,0-9]{24}$";
    static String EXECUTION_RE = "^" + EXECUTION_PATH + "/[a-f,0-9]{24}$";
    static String LIBRARY_RE = "^" + LIBRARY_PATH + "/[a-f,0-9]{24}$";
    static String ASSOCIATION_RE = "^" + ASSOCIATION_PATH
            + "/[a-f,0-9]{24}$";
    static String ASSOCIATIONSET_RE = "^" + ASSOCIATIONSET_PATH
            + "/[a-f,0-9]{24}$";
    static String TOPICMODEL_RE = "^" + TOPICMODEL_PATH
            + "/[a-f,0-9]{24}$";
    static String TOPICDISTRIBUTION_RE = "^" + TOPICDISTRIBUTION_PATH
            + "/[a-f,0-9]{24}$";
    static String BATCH_TOPICDISTRIBUTION_RE = "^" + BATCH_TOPICDISTRIBUTION_PATH
            + "/[a-f,0-9]{24}$";
    static String CONFIGURATION_RE = "^" + CONFIGURATION_PATH
            + "/[a-f,0-9]{24}$";

    // HTTP Status Codes from https://bigml.com/api/status_codes
    public static int HTTP_OK = 200;
    public static int HTTP_CREATED = 201;
    public static int HTTP_ACCEPTED = 202;
    public static int HTTP_NO_CONTENT = 204;
    public static int HTTP_BAD_REQUEST = 400;
    public static int HTTP_UNAUTHORIZED = 401;
    public static int HTTP_PAYMENT_REQUIRED = 402;
    public static int HTTP_FORBIDDEN = 403;
    public static int HTTP_NOT_FOUND = 404;
    public static int HTTP_METHOD_NOT_ALLOWED = 405;
    public static int HTTP_LENGTH_REQUIRED = 411;
    public static int HTTP_REQUEST_ENTITY_TOO_LARGE = 413;
    public static int HTTP_UNSUPPORTED_MEDIA_TPE = 415;
    public static int HTTP_TOO_MANY_REQUESTS = 429;
    public static int HTTP_INTERNAL_SERVER_ERROR = 500;
    public static int HTTP_SERVICE_UNAVAILABLE = 500;

    // Resource status codes
    public static int WAITING = 0;
    public static int QUEUED = 1;
    public static int STARTED = 2;
    public static int IN_PROGRESS = 3;
    public static int SUMMARIZED = 4;
    public static int FINISHED = 5;
    public static int UPLOADING = 6;
    public static int FAULTY = -1;
    public static int UNKNOWN = -2;
    public static int RUNNABLE = -3;

    static HashMap<Integer, String> STATUSES = new HashMap<Integer, String>();
    static {
        STATUSES.put(WAITING, "WAITING");
        STATUSES.put(QUEUED, "QUEUED");
        STATUSES.put(STARTED, "STARTED");
        STATUSES.put(IN_PROGRESS, "IN_PROGRESS");
        STATUSES.put(SUMMARIZED, "SUMMARIZED");
        STATUSES.put(FINISHED, "FINISHED");
        STATUSES.put(UPLOADING, "UPLOADING");
        STATUSES.put(FAULTY, "FAULTY");
        STATUSES.put(UNKNOWN, "UNKNOWN");
        STATUSES.put(RUNNABLE, "RUNNABLE");
    }

    protected String bigmlUser;
    protected String bigmlApiKey;
    protected String bigmlDomain;
    protected String bigmlAuth;

    protected boolean devMode;

    // Base URL
    protected String BIGML_URL;

    protected String SOURCE_URL;
    protected String DATASET_URL;
    protected String MODEL_URL;
    protected String PREDICTION_URL;
    protected String EVALUATION_URL;
    protected String ENSEMBLE_URL;
    protected String BATCH_PREDICTION_URL;
    protected String CLUSTER_URL;
    protected String CENTROID_URL;
    protected String BATCH_CENTROID_URL;
    protected String ANOMALY_URL;
    protected String ANOMALYSCORE_URL;
    protected String BATCHANOMALYSCORE_URL;
    protected String PROJECT_URL;
    protected String SAMPLE_URL;
    protected String CORRELATION_URL;
    protected String STATISTICALTEST_URL;
    protected String LOGISTICREGRESSION_URL;
    protected String SCRIPT_URL;
    protected String EXECUTION_URL;
    protected String LIBRARY_URL;
    protected String ASSOCIATION_URL;
    protected String ASSOCIATIONSET_URL;
    protected String TOPICMODEL_URL;
    protected String TOPICDISTRIBUTION_URL;
    protected String BATCH_TOPICDISTRIBUTION_URL;
    protected String CONFIGURATION_URL;

    public final static String DOWNLOAD_DIR = "/download";

    public CacheManager cacheManager;

    protected void init(CacheManager cacheManager) {
        try {
            BIGML_URL = BigMLClient.getInstance(devMode).getBigMLUrl();
            SOURCE_URL = BIGML_URL + SOURCE_PATH;
            DATASET_URL = BIGML_URL + DATASET_PATH;
            MODEL_URL = BIGML_URL + MODEL_PATH;
            PREDICTION_URL = BIGML_URL + PREDICTION_PATH;
            EVALUATION_URL = BIGML_URL + EVALUATION_PATH;
            ENSEMBLE_URL = BIGML_URL + ENSEMBLE_PATH;
            BATCH_PREDICTION_URL = BIGML_URL + BATCH_PREDICTION_PATH;
            CLUSTER_URL = BIGML_URL + CLUSTER_PATH;
            CENTROID_URL = BIGML_URL + CENTROID_PATH;
            BATCH_CENTROID_URL = BIGML_URL + BATCH_CENTROID_PATH;
            ANOMALY_URL = BIGML_URL + ANOMALY_PATH;
            ANOMALYSCORE_URL = BIGML_URL + ANOMALYSCORE_PATH;
            BATCHANOMALYSCORE_URL = BIGML_URL + BATCHANOMALYSCORE_PATH;
            PROJECT_URL = BIGML_URL + PROJECT_PATH;
            SAMPLE_URL = BIGML_URL + SAMPLE_PATH;
            CORRELATION_URL = BIGML_URL + CORRELATION_PATH;
            STATISTICALTEST_URL = BIGML_URL + STATISTICALTEST_PATH;
            LOGISTICREGRESSION_URL = BIGML_URL + LOGISTICREGRESSION_PATH;
            SCRIPT_URL = BIGML_URL + SCRIPT_PATH;
            EXECUTION_URL = BIGML_URL + EXECUTION_PATH;
            LIBRARY_URL = BIGML_URL + LIBRARY_PATH;
            ASSOCIATION_URL = BIGML_URL + ASSOCIATION_PATH;
            ASSOCIATIONSET_URL = BIGML_URL + ASSOCIATIONSET_PATH;
            TOPICMODEL_URL = BIGML_URL + TOPICMODEL_PATH;
            TOPICDISTRIBUTION_URL = BIGML_URL + TOPICDISTRIBUTION_PATH;
            BATCH_TOPICDISTRIBUTION_URL = BIGML_URL + BATCH_TOPICDISTRIBUTION_PATH;
            CONFIGURATION_URL = BIGML_URL + CONFIGURATION_PATH;

            this.cacheManager = cacheManager;
        } catch (AuthenticationException ae) {

        }
    }

    /**
     * Check if the current resource is an instance of this resource
     *
     * @param resource the resource to be checked
     * @return true if it's an instance
     */
    public abstract boolean isInstance(JSONObject resource);

        /**
         * Create a new resource.
         */
    public JSONObject createResource(final String urlString, final String json) {
        int code = HTTP_INTERNAL_SERVER_ERROR;
        String resourceId = null;
        JSONObject resource = null;
        String location = urlString;

        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("code", code);
        status.put("message", "The resource couldn't be created");
        error.put("status", status);

        try {
            HttpURLConnection connection = Utils.processPOST(urlString + bigmlAuth, json);

            code = connection.getResponseCode();
            if (code == HTTP_CREATED) {
                location = connection.getHeaderField(location);
                resource = (JSONObject) JSONValue.parse(Utils
                        .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                resourceId = (String) resource.get("resource");
                error = new JSONObject();
            } else {
                if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
                        || code == HTTP_PAYMENT_REQUIRED
                        || code == HTTP_NOT_FOUND) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }
        } catch (Throwable e) {
            logger.error("Error creating resource", e);
        }

        // Cache the resource if the resource if ready
        if( cacheManager != null && resource != null && isResourceReady(resource)) {
            cacheManager.put(resourceId, null, resource);
        }

        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("resource", resourceId);
        result.put("location", location);
        result.put("object", resource);
        result.put("error", error);
        return result;
    }

    /**
     * Retrieve a resource.
     */
    public JSONObject getResource(final String urlString) {
        return getResource(urlString, null, null, null);
    }

    /**
     * Retrieve a resource.
     */
    public JSONObject getResource(final String urlString,
            final String queryString) {
        return getResource(urlString, queryString, null, null);
    }

    /**
     * Retrieve a resource.
     */
    public JSONObject getResource(final String urlString,
            final String queryString, final String apiUser, final String apiKey) {
        int code = HTTP_INTERNAL_SERVER_ERROR;
        JSONObject resource = null;
        String resourceId = null;
        String location = urlString;

        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("code", code);
        status.put("message", "The resource couldn't be retrieved");
        error.put("status", status);

        // Check the cache first
        if( cacheManager != null ) {
            resourceId = urlString.substring(BIGML_URL.length(), urlString.length());
            if( cacheManager.exists(resourceId, queryString) ) {
                resource = cacheManager.get(resourceId, queryString);

                JSONObject result = new JSONObject();
                result.put("code", HTTP_OK);
                result.put("resource", resourceId);
                result.put("location", location);
                result.put("object", resource);
                result.put("error", new JSONObject());
                return result;
            }
        }

        try {
            String query = queryString != null ? queryString : "";
            String auth = apiUser != null && apiKey != null ? "?username="
                    + apiUser + ";api_key=" + apiKey + ";" : bigmlAuth;

            HttpURLConnection connection = Utils.processGET(urlString + auth + query);

            code = connection.getResponseCode();

            if (code == HTTP_OK) {
                resource = (JSONObject) JSONValue.parse(Utils
                        .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                resourceId = (String) resource.get("resource");
                error = new JSONObject();
            } else {
                if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
                        || code == HTTP_NOT_FOUND) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }

        } catch (Throwable e) {
            logger.error("Error getting resource", e);
        }

        // Cache the resource if the resource if ready
        if( cacheManager != null && resource != null && isResourceReady(resource)) {
            cacheManager.put(resourceId, queryString, resource);
        }

        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("resource", resourceId);
        result.put("location", location);
        result.put("object", resource);
        result.put("error", error);
        return result;
    }

    /**
     * List resources.
     */
    public JSONObject listResources(final String urlString,
            final String queryString) {
        int code = HTTP_INTERNAL_SERVER_ERROR;
        JSONObject meta = null;
        JSONArray resources = null;

        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("code", code);
        status.put("message", "The resource couldn't be listed");
        error.put("status", status);

        try {
            String query = queryString != null ? queryString : "";

            HttpURLConnection connection = Utils.processGET(urlString + bigmlAuth + query);

            code = connection.getResponseCode();

            if (code == HTTP_OK) {
                JSONObject resource = (JSONObject) JSONValue.parse(Utils
                        .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                meta = (JSONObject) resource.get("meta");
                resources = (JSONArray) resource.get("objects");
                error = new JSONObject();
            } else {
                if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
                        || code == HTTP_NOT_FOUND) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }
        } catch (Throwable e) {
            logger.error("Error listing resources ", e);
        }

        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("meta", meta);
        result.put("objects", resources);
        result.put("error", error);
        return result;
    }

    /**
     * Update a resource.
     */
    public JSONObject updateResource(final String urlString, final String json) {
        int code = HTTP_INTERNAL_SERVER_ERROR;
        JSONObject resource = null;
        String resourceId = null;
        String location = urlString;
        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("code", code);
        status.put("message", "The resource couldn't be updated");
        error.put("status", status);

        try {
            HttpURLConnection connection = Utils.processPUT(urlString + bigmlAuth, json);

            code = connection.getResponseCode();
            if (code == HTTP_ACCEPTED) {
                resource = (JSONObject) JSONValue.parse(Utils
                        .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                resourceId = (String) resource.get("resource");
                error = new JSONObject();
            } else {
                if (code == HTTP_UNAUTHORIZED || code == HTTP_PAYMENT_REQUIRED
                        || code == HTTP_METHOD_NOT_ALLOWED) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("Error updating resource", e);
        }

        // Cache the resource if the resource is ready
        if( cacheManager != null && resource != null
                && code == HTTP_ACCEPTED && isResourceReady(resource)) {
            cacheManager.put(resourceId, null, resource);
        }

        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("resource", resourceId);
        result.put("location", location);
        result.put("object", resource);
        result.put("error", error);
        return result;
    }

    /**
     * Delete a resource.
     */
    public JSONObject deleteResource(final String urlString) {
        int code = HTTP_INTERNAL_SERVER_ERROR;

        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("code", code);
        status.put("message", "The resource couldn't be deleted");
        error.put("status", status);

        try {
            HttpURLConnection connection = Utils.processDELETE(urlString + bigmlAuth);

            code = connection.getResponseCode();

            if (code == HTTP_NO_CONTENT) {
                error = new JSONObject();
            } else {
                if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
                        || code == HTTP_NOT_FOUND) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }
        } catch (Throwable e) {
            logger.error("Error deleting resource: ", urlString);
        }

        // Delete the resource from the cache
        if( cacheManager != null && code == HTTP_NO_CONTENT) {
            String resourceId = urlString.substring(BIGML_URL.length(), urlString.length());
            cacheManager.evict(resourceId, null);
        }

        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("error", error);
        return result;
    }

    /**
     * Return a dictionary of fields
     *
     */
    public JSONObject getFields(final String resourceId) {
        if (resourceId == null
                || resourceId.length() == 0
                || !(resourceId.matches(SOURCE_RE)
                        || resourceId.matches(DATASET_RE)
                        || resourceId.matches(MODEL_RE) || resourceId
                            .matches(PREDICTION_RE))) {
            logger.info("Wrong resource id");
            return null;
        }

        JSONObject resource = get(BIGML_URL + resourceId);

        JSONObject obj = (JSONObject) resource.get("object");
        if (obj == null) {
            obj = (JSONObject) resource.get("error");
        }

        if ((Integer) resource.get("code") == HTTP_OK) {
            if (resourceId.matches(MODEL_RE)) {
                JSONObject model = (JSONObject) resource.get("model");
                return (JSONObject) model.get("fields");
            } else {
                return (JSONObject) obj.get("fields");
            }
        }
        return null;
    }

    /**
     * Maps status code to string.
     *
     */
    public String status(final String resourceId) {
        if (resourceId == null
                || resourceId.length() == 0
                || !(resourceId.matches(SOURCE_RE)
                        || resourceId.matches(DATASET_RE)
                        || resourceId.matches(MODEL_RE) || resourceId
                            .matches(PREDICTION_RE))) {
            logger.info("Wrong resource id");
            return null;
        }
        JSONObject resource = get(BIGML_URL + resourceId);
        JSONObject obj = (JSONObject) resource.get("object");
        if (obj == null) {
            obj = (JSONObject) resource.get("error");
        }
        JSONObject status = (JSONObject) obj.get("status");
        Long code = (Long) status.get("code");
        if (STATUSES.get(code.intValue()) != null) {
            return STATUSES.get(code.intValue());
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Check whether a resource' status is FINISHED.
     *
     * @param resource
     *            a resource
     *
     */
    public boolean isResourceReady(final JSONObject resource) {
        if (resource == null) {
            return false;
        }
        JSONObject obj = (JSONObject) resource.get("object");
        if (obj == null) {
            obj = (JSONObject) resource.get("error");
        }

        if( obj == null ) {
            if( resource.containsKey("status") ) {
                JSONObject status = (JSONObject) resource.get("status");
                Number statusCode = (Number) status.get("code");
                return (statusCode != null && statusCode.intValue() == FINISHED);
            }

            return false;
        } else {
            JSONObject status = (JSONObject) obj.get("status");
            Number code = (Number) resource.get("code");
            Number statusCode = (Number) status.get("code");
            return (code != null && code.intValue() == HTTP_OK
                    && statusCode != null && statusCode.intValue() == FINISHED);
        }
    }

    // ################################################################
    // #
    // # Abstract methods
    // #
    // ################################################################

    /**
     * Retrieve a resource.
     *
     */
    abstract JSONObject get(final String resourceId);

    /**
     * Retrieve a resource.
     *
     */
    abstract JSONObject get(final JSONObject resource);

    /**
     * Check whether a resource' status is FINISHED.
     *
     */
    abstract boolean isReady(final String resourceId);

    /**
     * Check whether a resource' status is FINISHED.
     *
     */
    abstract boolean isReady(final JSONObject resource);

    /**
     * List all your resource.
     *
     */
    abstract public JSONObject list(final String queryString);

    /**
     * Update a resource.
     *
     */
    abstract public JSONObject update(final String resourceId, final String json);

    /**
     * Update a resource.
     *
     */
    abstract public JSONObject update(final JSONObject resource,
            final JSONObject json);

    /**
     * Delete a resource.
     *
     */
    abstract public JSONObject delete(final String resourceId);

    /**
     * Delete a resource.
     *
     */
    abstract public JSONObject delete(final JSONObject resource);

    // ################################################################
    // #
    // # Protected methods
    // #
    // ################################################################

    /**
     * Builds args dictionary for the create call from a `dataset` or a list of
     * `datasets`
     */
    @Deprecated
    protected JSONObject createFromDatasets(final String[] datasets,
            String args, Integer waitTime, Integer retries, String key) {

        return createFromDatasets(datasets, (JSONObject) JSONValue.parse(args),
                waitTime, retries, key);
    }

    /**
     * Builds args dictionary for the create call from a `dataset` or a list of
     * `datasets`
     */
    protected JSONObject createFromDatasets(final String[] datasets,
            JSONObject args, Integer waitTime, Integer retries, String key) {

        JSONObject createArgs = new JSONObject();
        if (args != null) {
            createArgs = args;
        }

        List<String> datasetsIds = new ArrayList<String>();

        for (String datasetId : datasets) {
            // Checking valid datasetId
            if (datasetId == null || datasetId.length() == 0
                    || !(datasetId.matches(DATASET_RE))) {
                logger.info("Wrong dataset id");
                return null;
            }

            // Checking status
            try {
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                                    .datasetIsReady(datasetId)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }
                datasetsIds.add(datasetId);
            } catch (Throwable e) {
                logger.error("Error creating object");
                return null;
            }

        }

        if (datasetsIds.size() == 1) {
            key = (key == null || key.equals("") ? "dataset" : key);
            createArgs.put(key, datasetsIds.get(0));
        } else {
            key = (key == null || key.equals("") ? "datasets" : key);
            createArgs.put(key, datasetsIds);
        }

        return createArgs;
    }

    /**
     * Retrieves a remote file.
     *
     * Uses HTTP GET to download a file object with a BigML `url`.
     *
     */
    protected JSONObject download(final String url, final String fileName) {
        int code = HTTP_INTERNAL_SERVER_ERROR;

        JSONObject error = new JSONObject();
        String csv = "";
        try {
            HttpURLConnection connection = Utils.processGET(url + bigmlAuth);

            code = connection.getResponseCode();

            csv = Utils.inputStreamAsString(connection.getInputStream(), "UTF-8");

            if (code == HTTP_OK) {
                if (fileName != null) {
                    File file = new File(fileName);
                    if (!file.exists()) {

                    }
                    BufferedWriter output = new BufferedWriter(new FileWriter(
                            file));
                    output.write(csv);
                    output.close();
                }
            } else {
                if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
                        || code == HTTP_NOT_FOUND) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                    logger.info("Error downloading:" + code);
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }

        } catch (Throwable e) {
            logger.error("Error downloading batch prediction", e);
        }

        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("error", error);
        result.put("csv", csv);
        return result;

    }

    /**
     * Retrieves a remote async file.
     *
     * Uses HTTP GET to download a file object with a BigML `url` asynchronous.
     *
     */
    protected JSONObject downloadAsync(final String url, final String fileName) {
        return downloadAsync(url, fileName, 10L, 10, 0);
    }

    /**
     * Retrieves a remote async file.
     *
     * Uses HTTP GET to download a file object with a BigML `url` asynchronous.
     *
     * @param waitTime time between retries in seconds
     *
     *
     */
    protected JSONObject downloadAsync(final String url, final String fileName,
                                       Long waitTime, Integer retries, Integer counter) {
        int code = HTTP_INTERNAL_SERVER_ERROR;

        if( waitTime == null ) {
            waitTime = 10L;
        }
        if( retries == null ){
            retries = 10;
        }
        if( counter == null ) {
            counter = 0;
        }

        JSONObject error = new JSONObject();
        String csv = "";
        try {
            HttpURLConnection connection = Utils.processGET(url + bigmlAuth);

            code = connection.getResponseCode();

            csv = Utils.inputStreamAsString(connection.getInputStream(), "UTF-8");

            if (code == HTTP_OK) {

                try {
                    JSONObject downloadStatus = (JSONObject) JSONValue.parse(csv);

                    if( downloadStatus != null ) {
                        if( counter < retries ) {
                            Number downloadCode = (Number) Utils.getJSONObject(downloadStatus, "status.code");
                            if( downloadCode.intValue() != FINISHED ) {
                                try {
                                    Thread.sleep(1000 * Utils.getExponentialWait(waitTime, counter));
                                } catch (InterruptedException e) {
                                }

                                counter += 1;
                                return downloadAsync(url, fileName, waitTime, retries, counter);
                            } else {
                                return downloadAsync(url, fileName, waitTime, retries, retries + 1);
                            }
                        } else {
                            logger.error("The maximum number of retries " +
                                    " for the download has been " +
                                    " exceeded. You can retry your " +
                                    " command again in" +
                                    " a while.");
                            return null;
                        }
                    }
                } catch(Exception e) {
                    // This exception will be thrown when we try to parse a JSON
                    //  response when we finally receive the file content
                    if( counter < retries ) {
                        return downloadAsync(url, fileName, waitTime, retries, retries + 1);
                    }
                }


                if (fileName != null) {
                    File file = new File(fileName);
                    if (!file.exists()) {

                    }
                    BufferedWriter output = new BufferedWriter(new FileWriter(
                            file));
                    output.write(csv);
                    output.close();
                }
            } else {
                if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
                        || code == HTTP_NOT_FOUND) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                    logger.info("Error downloading:" + code);
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }

        } catch (Throwable e) {
            logger.error("Error downloading batch prediction", e);
        }

        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("error", error);
        result.put("csv", csv);
        return result;

    }
}
