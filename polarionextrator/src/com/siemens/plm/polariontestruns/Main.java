package com.siemens.plm.polariontestruns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.polarion.alm.ws.client.WebServiceFactory;
import com.polarion.alm.ws.client.projects.ProjectWebService;
import com.polarion.alm.ws.client.session.SessionWebService;
import com.polarion.alm.ws.client.testmanagement.TestManagementWebService;
import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.testmanagement.TestRecord;
import com.polarion.alm.ws.client.types.testmanagement.TestRun;
import com.polarion.alm.ws.client.types.tracker.Custom;
import com.polarion.alm.ws.client.types.tracker.EnumOptionId;
import com.polarion.alm.ws.client.types.tracker.WorkItem;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

// @TODO: Add proper toString for EnumOptionID CustomFields
public class Main {
    private static final int TESTRUNID_NOT_PROVIDED = 1;

    private static String confFile;
    private static SessionWebService sessionService;
    private static ProjectWebService projectService;
    private static TestManagementWebService testManagementService;
    private static TrackerWebService trackerService;
    private static WebServiceFactory factory;
    private static ObjectMapper objectMapper;


    private static String testRunID;
    private static String statuses;
    // Config should come from a properties file
    // Get the Test Run Template
    // Get The Module and the Query
    // Create a Final Query from the 2
    // Get all Work Items matching the Query
    private static Properties prop;


    public static void main(String args[]) throws IOException, ServiceException {
        assureUserInputIsValid(args);
        setUpPolarionWebServices();
        createPolarionSession();
        downloadTestRun();

    }

    private static void downloadTestRun() throws IOException {
        System.out.println("TestRunID to be Looked For: " + testRunID);
        System.out.println("Filtering for Status: " + statuses);
        String prefix = testRunID.replace("-", "_").split("_")[0];
        System.out.println(prefix);
        String projectId = prop.getProperty("testruns." + prefix + ".project_id", "Testautomation_Team");
        Path outputFile = Paths.get(prop.getProperty("testruns.working_folder", "common\\data\\temp")
                ,testRunID + ".json");

        System.out.println("Using Polarion Project: " + projectId);
        TestRun testRun = testManagementService.getTestRunById(projectId, testRunID);
        System.out.println("Test Run URI: " + testRun.getUri());
        System.out.println("Test Run Query: " + testRun.getQuery());

        objectMapper = new ObjectMapper();
        ObjectNode jsonRoot = objectMapper.createObjectNode();
        ArrayNode fields = objectMapper.createArrayNode();
        ArrayNode tests = objectMapper.createArrayNode();
        jsonRoot.set("fields", fields);
        jsonRoot.set("tests", tests);

        Custom[] customFields = testRun.getCustomFields();
        if(customFields != null) {
            for (Custom customField : customFields) {
                ObjectNode aField = objectMapper.createObjectNode();
                aField.put(customField.getKey(), customField.getValue().toString());

                fields.add(aField);
            }
        }

        if(testRun.getIsTemplate()) {
            System.out.println("This is a Test Run Template. Need to QUery the Moduel because of a Polarion BUG");
            WorkItem[] workItems = trackerService.getModuleWorkItems(
                    testRun.getDocument().getUri(), null, true, new String[] {"id", "project.id"});

            if(workItems != null) {
                System.out.println(workItems.length + "  Work Items Found. Filtering it further with Query");
                String query = testRun.getQuery() != null ? testRun.getQuery() : "true";

                for(WorkItem wi : workItems) {
                    // @TODO: Optimize the querying of Work Items in Document --> Work Item by Query
                    WorkItem[] filtered = trackerService.queryWorkItems(
                                String.format("id:\"%s\" AND project.id:\"%s\" AND %s",
                                wi.getId(), wi.getProject().getId(), query), "title", new String[] {"title"});

                    if(filtered == null) {
                        // Second filter did not pass
                        continue;
                    }

                    WorkItem goodOne = trackerService.getWorkItemByUri(wi.getUri());
                    tests.add(createJsonReprForWi(goodOne));
                }

                System.out.println(tests.size() + "  Work Items after the Filtering");
            } else {
                System.out.println("No Test Records Found");
            }

        } else {
            System.out.println("Normal Test Run. Looking UP Test Results");
            TestRecord[] records = testRun.getRecords();

            if (records != null) {
                System.out.println(records.length + "  Test Records Found");
                for (TestRecord record : records) {
                    String recordStatus = record.getResult() == null ? "waiting" : record.getResult().getId();
                    if (!statuses.contains(recordStatus)) {
                        continue;
                    }

                    WorkItem testCase = trackerService.getWorkItemByUri(record.getTestCaseURI());
                    tests.add(createJsonReprForWi(testCase));
                }
            } else {
                System.out.println("No Test Records Found");
            }
        }

        Files.createDirectories(outputFile.getParent());
        Files.deleteIfExists(outputFile);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), jsonRoot);

        System.out.println("Test Run Successfully extracted To: " + outputFile.toString());

    }


    private static ObjectNode createJsonReprForWi(WorkItem testCase) {
        ObjectNode aTest = objectMapper.createObjectNode();
        aTest.put("title", testCase.getTitle());

        Custom[] wiCustomFields = testCase.getCustomFields();
        if (wiCustomFields != null) {
            for (Custom wiCustomField : wiCustomFields) {
                if (wiCustomField.getValue() instanceof EnumOptionId) {
                    aTest.put(wiCustomField.getKey(), ((EnumOptionId) wiCustomField.getValue()).getId());
                } else {
                    aTest.put(wiCustomField.getKey(), wiCustomField.getValue().toString());
                }
            }
        }

        return aTest;
    }

    private static void assureUserInputIsValid(String[] args) throws IOException {
		/*
		 * if(args.length < 1) { System.err.
		 * println("ERROR: Please provide the ID of the Polarion Test Run to be exported"
		 * ); System.exit(TESTRUNID_NOT_PROVIDED); }
		 */
		/*
		 * testRunID = args[0]; statuses = args.length > 1 ? args[1] : "waiting";
		 * confFile = args.length > 2 ? args[2] : "settings.properties"; prop = new
		 * Properties(); prop.load(new FileInputStream(confFile));
		 */
        testRunID = "drivepilot_std";
        statuses = "waiting";
        confFile = "C:\\JsonExtrator\\RanorexSolution\\polarionextrator\\settings.properties";
        prop = new Properties();
        prop.load(new FileInputStream(confFile));
    }

    private static void setUpPolarionWebServices() throws ServiceException, MalformedURLException {
        factory = new WebServiceFactory(prop.getProperty("polarion_server_address") + "/ws/services/");
        sessionService = factory.getSessionService();
        projectService = factory.getProjectService();
        trackerService = factory.getTrackerService();
        testManagementService = factory.getTestManagementService();
    }


    private static void createPolarionSession() throws IOException {
        String serverURL = prop.getProperty("polarion_server_address");
        System.out.println("Getting Credentials for: " + serverURL);

        String defaultLookupFolder = new File(confFile).getParentFile().getAbsolutePath() + "/data/secrets";
        Path usernameFile = Paths.get(
                prop.getProperty("username_file_path", defaultLookupFolder + "/polarion.usr"));
        Path passwordFile = Paths.get(
                prop.getProperty("password_file_path", defaultLookupFolder + "/polarion.key"));

        System.out.println("Loading User From: " + usernameFile.toString());
        System.out.println("Authenticating");

        String username = Files.readAllLines(usernameFile).get(0);
        String password = Files.readAllLines(passwordFile).get(0);
        sessionService.logIn(username, password);
        System.out.println("Authentication Successful");
    }
}

