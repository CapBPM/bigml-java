package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class ClustersStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ClustersStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    private String sharedHash;
    private String sharedKey;

    private String downloadedFile;

    @Given("^I create a cluster with options \"(.*)\"$")
    public void I_create_a_cluster_with_options(String options) throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = (JSONObject) JSONValue.parse(options);

        args.put("tags", Arrays.asList("unitTest"));
        args.put("k", 8);

        JSONObject resource = BigMLClient.getInstance().createCluster(
                datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.cluster = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a cluster$")
    public void I_create_a_cluster() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("k", 8);

        JSONObject resource = BigMLClient.getInstance().createCluster(
                datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.cluster = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a local cluster$")
    public void I_create_a_local_cluster() throws Exception {
        context.localCluster = new LocalCluster(context.cluster);
    }

    @Given("^I wait until the cluster status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_cluster_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.cluster.get("status"))
                .get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_cluster((String) context.cluster.get("resource"));
            code = (Long) ((JSONObject) context.cluster.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the cluster is ready less than (\\d+) secs$")
    public void I_wait_until_thecluster_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_cluster_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I wait until the cluster is ready less than (\\d+) secs and I return it$")
    public JSONObject I_wait_until_the_cluster_is_ready_less_than_secs_and_return(
            int secs) throws AuthenticationException {
        I_wait_until_cluster_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
        return context.cluster;
    }

    @Given("^I get the cluster \"(.*)\"")
    public void I_get_the_cluster(String clusterId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getCluster(clusterId);
        Integer code = (Integer) resource.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
        context.cluster = (JSONObject) resource.get("object");
    }

    @When("^I create a centroid for \"(.*)\"$")
    public void I_create_a_centroid_for(String inputData)
            throws AuthenticationException {
        String clusterId = (String) context.cluster.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createCentroid(
                clusterId, (JSONObject) JSONValue.parse(inputData),
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.centroid = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create a local centroid for \"(.*)\"$")
    public void I_create_a_local_centroid_for(String inputData)
            throws AuthenticationException {
        context.localCentroid = context.localCluster.calculateCentroid((JSONObject) JSONValue.parse(inputData),
                Boolean.TRUE);
    }

    @Given("^I check the centroid is ok$")
    public void I_check_the_centroid_is_ok() throws AuthenticationException {
        int secs = 60;

        Long code = (Long) ((JSONObject) context.centroid.get("status"))
                .get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != AbstractResource.FINISHED
                && code.intValue() != AbstractResource.FAULTY) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_cluster((String) context.cluster.get("resource"));
            code = (Long) ((JSONObject) context.cluster.get("status"))
                    .get("code");
        }
        assertEquals(AbstractResource.FINISHED, code.intValue());
    }

    @Then("the centroid is \"([^\"]*)\" with distance (.*)$")
    public void the_centroid_is_with_distance(String result, Double distance) throws AuthenticationException {

        assertEquals(result, context.centroid.get("centroid_name"));

        BigDecimal centroidDist = new BigDecimal(((Number) context.centroid.get("distance")).doubleValue());
        centroidDist = centroidDist.setScale(5, RoundingMode.HALF_EVEN);

        BigDecimal expectedDistance = new BigDecimal(distance);
        expectedDistance = expectedDistance.setScale(5, RoundingMode.HALF_EVEN);

        String confidenceValue = String.format("%.5g", centroidDist);
        assertTrue(confidenceValue.equals(String.format("%.5g", expectedDistance)));
    }

    @Then("the local centroid is \"([^\"]*)\" with distance (.*)$")
    public void the_local_centroid_is_with_distance(String result, Double distance) throws AuthenticationException {
        assertEquals(result, context.localCentroid.get("centroid_name"));

        BigDecimal centroidDist = new BigDecimal(((Number) context.centroid.get("distance")).doubleValue());
        centroidDist = centroidDist.setScale(5, RoundingMode.HALF_EVEN);

        BigDecimal expectedDistance = new BigDecimal(distance);
        expectedDistance = expectedDistance.setScale(5, RoundingMode.HALF_EVEN);

        String confidenceValue = String.format("%.5g", centroidDist);
        assertTrue(confidenceValue.equals(String.format("%.5g", expectedDistance)));
    }

    @Then("the centroid is \"([^\"]*)\"$")
    public void the_centroid_is(String result) throws AuthenticationException {
        assertEquals(result, context.centroid.get("centroid_name"));
    }

    @When("^I create a batch centroid for the dataset$")
    public void I_create_a_batch_centroid_for_the_dataset() throws Throwable {
        String clusterId = (String) context.cluster.get("resource");
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createBatchCentroid(
                clusterId, datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.batchCentroid = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I get the batch centroid \"(.*)\"")
    public void I_get_the_batch_centroid(String batchCentroidId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getBatchCentroid(
                batchCentroidId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.batchCentroid = (JSONObject) resource.get("object");
    }

    @Given("^I wait until the batch centroid is ready less than (\\d+) secs$")
    public void I_wait_until_the_batch_centroid_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_batch_centroid_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I wait until the batch centroid status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_batch_centroid_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.batchCentroid.get("status"))
                .get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_batch_centroid((String) context.batchCentroid
                    .get("resource"));
            code = (Long) ((JSONObject) context.batchCentroid.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @When("^I download the created centroid file to \"([^\"]*)\"$")
    public void I_download_the_created_centroid_file_to(String fileTo)
            throws Throwable {
        downloadedFile = fileTo;

        BigMLClient.getInstance().downloadBatchCentroid(context.batchCentroid,
                fileTo);
    }

    @Then("^the batch centroid file is like \"([^\"]*)\"$")
    public void the_batch_centroid_file_is_like(String checkFile)
            throws Throwable {
        FileInputStream downloadFis = new FileInputStream(new File(
                downloadedFile));
        FileInputStream checkFis = new FileInputStream(new File(checkFile));

        String localCvs = Utils.inputStreamAsString(downloadFis, "UTF-8");
        String checkCvs = Utils.inputStreamAsString(checkFis, "UTF-8");

        if (!localCvs.equals(checkCvs)) {
            throw new Exception();
        }
    }

    // @Then("^the batch prediction file \"([^\"]*)\" is like \"([^\"]*)\"$")
    // public void the_batch_prediction_file_is_like(String downloadedFile,
    // String checkFile)
    // throws Throwable {
    //
    // FileInputStream downloadFis = new FileInputStream(new
    // File(downloadedFile));
    // FileInputStream checkFis = new FileInputStream(new File(checkFile));
    //
    // String localCvs = Utils.inputStreamAsString(downloadFis);
    // String checkCvs = Utils.inputStreamAsString(checkFis);
    //
    // if (!localCvs.equals(checkCvs)) {
    // throw new Exception();
    // }
    //
    // }
}