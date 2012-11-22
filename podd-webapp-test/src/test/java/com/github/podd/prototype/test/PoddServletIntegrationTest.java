package com.github.podd.prototype.test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import junit.framework.Assert;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.util.Series;
import org.semanticweb.owlapi.model.IRI;

/**
 * This integration test class validates the PODD-prototype web service operations.
 * 
 * @author kutila
 * 
 */
public class PoddServletIntegrationTest extends AbstractPoddIntegrationTest
{
    //HACK - copying these temporarily (this is the integration test code inside the web-app project
    public static final String KEY_FILE_REF_TYPE = "file_reference_type";
    public static final String KEY_OBJECT_URI = "object_uri";
    public static final String KEY_ARTIFACT_URI = "artifact_uri";
    public static final String KEY_FILE_DESCRIPTION = "file_description";
    public static final String KEY_FILE_NAME = "file_name";
    public static final String KEY_FILE_PATH = "file_path";
    public static final String KEY_FILE_SERVER_ALIAS = "file_server_alias";
    
    
    /** set of cookies containing session information */
    private Series<Cookie> cookies = null;
    
    @Override
    protected void login(final String username, final String password)
    {
        final Request request = new Request(Method.POST, this.BASE_URL + "/login");
        final Form form = new Form();
        form.add("username", username);
        form.add("password", password);
        request.setEntity(form.getWebRepresentation(CharacterSet.UTF_8));
        
        final Response response = this.getClient().handle(request);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), response.getStatus().getCode());
        
        // -- session cookie for subsequent requests
        this.cookies = new Series<Cookie>(Cookie.class);
        final Series<CookieSetting> cookieSettingSeries = response.getCookieSettings();
        for(final CookieSetting cookieSetting : cookieSettingSeries)
        {
            this.cookies.add(cookieSetting);
        }
    }
    
    @Override
    protected void logout()
    {
        final Request logoutRequest = new Request(Method.GET, this.BASE_URL + "/logout");
        logoutRequest.setCookies(this.cookies);
        this.getClient().handle(logoutRequest);
        // Note: not asserting response code which depends on whether there was an active session
    }
    
    @Override
    protected void resetWebService()
    {
        final Request resetRequest = new Request(Method.POST, this.BASE_URL + "/podd/reset");
        resetRequest.setCookies(this.cookies);
        this.getClient().handle(resetRequest);
        // Note: not asserting response code which depends on whether there was an active session
    }
    
    /**
     * Helper method that logs in to the web service and adds an artifact.
     * 
     * @param path
     *            Location of the artifact file
     * @param mediaType
     * @param expectedStatusCode
     * @return A string representation of the added artifact's URI
     * @throws Exception
     */
    protected String loginAndAddArtifact(final String path, final MediaType mediaType, final int expectedStatusCode)
        throws Exception
    {
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        
        final Request addRequest = new Request(Method.POST, this.BASE_URL + "/podd/artifact/new");
        addRequest.setCookies(this.cookies);
        final FileRepresentation artifactToAdd = new FileRepresentation(path, mediaType);
        addRequest.setEntity(artifactToAdd);
        final Response addResponse = this.getClient().handle(addRequest);
        Assert.assertEquals(expectedStatusCode, addResponse.getStatus().getCode());
        return addResponse.getEntityAsText().replace("://", "/");
    }
    
    /**
     * Test that the home page exists to ensure the PODD web-app is deployed.
     * 
     * @throws Exception
     */
    @Test
    public void testHomePage() throws Exception
    {
        final Request request = new Request(Method.GET, this.BASE_URL + "/index.html");
        final Response response = this.getClient().handle(request);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), response.getStatus().getCode());
        Assert.assertTrue(response.getEntityAsText().contains("PODD Prototype Web Service"));
    }
    
    /**
     * Tests the ability to login and logout of the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testLogin() throws Exception
    {
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        
        // -- logout without sending cookie: should fail with Status 401 (UNAUTHORIZED)
        final Request incorrectLogoutRequest = new Request(Method.GET, this.BASE_URL + "/logout");
        final Response incorrectLogoutResponse = this.getClient().handle(incorrectLogoutRequest);
        Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED.getCode(), incorrectLogoutResponse.getStatus().getCode());
        
        // -- logout: should succeed
        final Request logoutRequest = new Request(Method.GET, this.BASE_URL + "/logout");
        logoutRequest.setCookies(this.cookies);
        final Response logoutResponse = this.getClient().handle(logoutRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), logoutResponse.getStatus().getCode());
        
        // -- try to logout again: should fail with Status 401 (UNAUTHORIZED)
        final Request secondLogoutRequest = new Request(Method.GET, this.BASE_URL + "/logout");
        secondLogoutRequest.setCookies(this.cookies);
        final Response secondLogoutResponse = this.getClient().handle(secondLogoutRequest);
        Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED.getCode(), secondLogoutResponse.getStatus().getCode());
    }
    
    /**
     * Tests that a new artifact can be added through the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testAddArtifact() throws Exception
    {
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1.rdf").getFile();
        final String artifactUri =
                this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        Assert.assertNotNull(artifactUri);
    }
    
    /**
     * Tests that it is possible to retrieve an artifact from the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testGetArtifact() throws Exception
    {
        // -- login and add an artifact
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1.rdf").getFile();
        final String artifactUri =
                this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        
        // -- GET the "base" artifact from the web service and verify results
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseResponse.getStatus().getCode());
        final String getResult = getBaseResponse.getEntityAsText();
        Assert.assertEquals(29, this.getStatementCount(getResult));
        
        // -- GET the "inferred" artifact from the web service and verify results
        final Request getInferredRequest =
                new Request(Method.GET, this.BASE_URL + "/podd/artifact/inferred/" + artifactUri);
        getInferredRequest.setCookies(this.cookies);
        final Response getInferredResponse = this.getClient().handle(getInferredRequest);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getInferredResponse.getStatus().getCode());
        final String getInferredResult = getInferredResponse.getEntityAsText();
        Assert.assertEquals(396, this.getStatementCount(getInferredResult));
    }
    
    /**
     * Tests that it is possible to delete an artifact through the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteArtifact() throws Exception
    {
        // -- login and add an artifact
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1.rdf").getFile();
        final String artifactUri =
                this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        
        // -- DELETE the artifact using the web service and verify results
        final Request deleteRequest = new Request(Method.DELETE, this.BASE_URL + "/podd/artifact/" + artifactUri);
        deleteRequest.setCookies(this.cookies);
        final Response deleteResponse = this.getClient().handle(deleteRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), deleteResponse.getStatus().getCode());
        Assert.assertTrue(deleteResponse.getEntityAsText().contains(artifactUri.substring(6)));
        
        // -- try to retrieve the artifact from the web service and verify that it no longer exists
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        
        Assert.assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), getBaseResponse.getStatus().getCode());
    }
    
    /**
     * Tests that resetting the web service results in the repository contents being wiped.
     * 
     * @throws Exception
     */
    @Test
    public void testReset() throws Exception
    {
        // -- login and add an artifact
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1.rdf").getFile();
        final String artifactUri =
                this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        
        // -- send RESET request
        final Request resetRequest = new Request(Method.POST, this.BASE_URL + "/podd/reset");
        resetRequest.setCookies(this.cookies);
        final Response resetResponse = this.getClient().handle(resetRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), resetResponse.getStatus().getCode());
        
        // -- try to retrieve the artifact from the web service and verify that it no longer exists
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        
        Assert.assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), getBaseResponse.getStatus().getCode());
    }
    
    /**
     * Tests that an artifact can be edited through the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testEditArtifactWithMerge() throws Exception
    {
        final long noOfStatements = this.genericTestEditArtifact("merge");
        Assert.assertEquals(33, noOfStatements);
    }
    
    /**
     * Tests that an artifact can be edited through the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testEditArtifactWithReplace() throws Exception
    {
        final long noOfStatements = this.genericTestEditArtifact("replace");
        Assert.assertEquals(32, noOfStatements);
    }
    
    public long genericTestEditArtifact(final String editType) throws Exception
    {
        // -- login and add an artifact
        final String path = this.getClass().getResource("/test/artifacts/editableProject-1.rdf").getFile();
        final String artifactUri =
                this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        
        // -- generate and send an edit request
        final String fragmentPath = this.getClass().getResource("/test/artifacts/fragment.rdf").getFile();
        final FileRepresentation artifactToAdd = new FileRepresentation(fragmentPath, MediaType.APPLICATION_RDF_XML);
        final Request editRequest =
                new Request(Method.POST, this.BASE_URL + "/podd/artifact/edit/" + editType + "/" + artifactUri);
        editRequest.setCookies(this.cookies);
        editRequest.setEntity(artifactToAdd);
        final Response editResponse = this.getClient().handle(editRequest);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), editResponse.getStatus().getCode());
        
        // -- retrieve edited artifact and verify the modifications are present
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseResponse.getStatus().getCode());
        final String modifiedRDFString = getBaseResponse.getEntityAsText();
        
        Assert.assertTrue(modifiedRDFString.contains("John.Doe@csiro.au"));
        final long noOfStatements = this.getStatementCount(modifiedRDFString);
        return noOfStatements;
    }
    
    /**
     * Tests adding a file reference without providing sufficient information to generate a file
     * reference
     * 
     * @throws Exception
     */
    @Test
    public void testAttachReferenceInsufficientInformation4FileReference() throws Exception
    {
        // -- login and add an artifact
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1-internal-object.rdf").getFile();
        final String artifactUri =
                this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        
        // -- retrieve the artifact and check that file ref. does not exist
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseResponse.getStatus().getCode());
        final String originalRdfString = getBaseResponse.getEntityAsText();
        Assert.assertEquals(33, this.getStatementCount(originalRdfString));
        
        // -- generate and send an attach request
        final Form form = new Form();
        form.add(KEY_FILE_REF_TYPE, "HTTP");
        form.add(KEY_ARTIFACT_URI, artifactUri);
        // object URI is missing here
        form.add(KEY_FILE_SERVER_ALIAS, "w3");
        form.add(KEY_FILE_PATH, "Protocols/rfc2616");
        form.add(KEY_FILE_NAME, "rfc2616.html");
        form.add(KEY_FILE_DESCRIPTION, "http RFC");
        
        final Request editRequest = new Request(Method.POST, this.BASE_URL + "/attachref");
        editRequest.setCookies(this.cookies);
        editRequest.setEntity(form.getWebRepresentation(CharacterSet.UTF_8));
        final Response editResponse = this.getClient().handle(editRequest);
        
        Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), editResponse.getStatus().getCode());
    }
    
    /**
     * Tests adding a file reference where the referred file's existence is not verifiable.
     * 
     * @throws Exception
     */
    @Test
    public void testAttachReferenceNonExistentFileReferred() throws Exception
    {
        // -- login and add an artifact
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1-internal-object.rdf").getFile();
        final String artifactUri =
                this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        
        // -- retrieve the artifact and check that file ref. does not exist
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseResponse.getStatus().getCode());
        final String originalRdfString = getBaseResponse.getEntityAsText();
        Assert.assertEquals(33, this.getStatementCount(originalRdfString));
        
        // -- generate and send an attach request
        final Form form = new Form();
        form.add(KEY_FILE_REF_TYPE, "HTTP");
        form.add(KEY_ARTIFACT_URI, artifactUri);
        form.add(KEY_OBJECT_URI, "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0");
        form.add(KEY_FILE_SERVER_ALIAS, "w3");
        form.add(KEY_FILE_PATH, "Protocols/rfc2616thisiswrong");
        form.add(KEY_FILE_NAME, "rfc2616.html");
        form.add(KEY_FILE_DESCRIPTION, "http RFC");
        
        final Request editRequest = new Request(Method.POST, this.BASE_URL + "/attachref");
        editRequest.setCookies(this.cookies);
        editRequest.setEntity(form.getWebRepresentation(CharacterSet.UTF_8));
        final Response editResponse = this.getClient().handle(editRequest);
        
        Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), editResponse.getStatus().getCode());
    }
    
    /**
     * Tests that a file reference to an HTTP resource can be attached to an object via the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testAttachReference_HTTP() throws Exception
    {
        // -- login and add an artifact
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1-internal-object.rdf").getFile();
        final String artifactUri =
                this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        
        // -- retrieve the artifact and check that file ref. does not exist
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseResponse.getStatus().getCode());
        final String originalRdfString = getBaseResponse.getEntityAsText();
        Assert.assertFalse(originalRdfString.contains("rfc2616.html")); // artifact is not aware of
                                                                        // this file
        Assert.assertEquals(33, this.getStatementCount(originalRdfString));
        
        // -- generate and send an attach request
        final Form form = new Form();
        form.add(KEY_FILE_REF_TYPE, "HTTP");
        form.add(KEY_ARTIFACT_URI, artifactUri);
        form.add(KEY_OBJECT_URI, "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0");
        form.add(KEY_FILE_SERVER_ALIAS, "w3");
        form.add(KEY_FILE_PATH, "Protocols/rfc2616");
        form.add(KEY_FILE_NAME, "rfc2616.html");
        form.add(KEY_FILE_DESCRIPTION, "http RFC");
        
        final Request editRequest = new Request(Method.POST, this.BASE_URL + "/attachref");
        editRequest.setCookies(this.cookies);
        editRequest.setEntity(form.getWebRepresentation(CharacterSet.UTF_8));
        final Response editResponse = this.getClient().handle(editRequest);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), editResponse.getStatus().getCode());
        Assert.assertNotNull(editResponse.getEntityAsText());
        
        // -- retrieve edited artifact and verify the attached file reference is present
        final Request getBaseAfterAttachRequest =
                new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseAfterAttachRequest.setCookies(this.cookies);
        final Response getBaseAfterAttachResponse = this.getClient().handle(getBaseAfterAttachRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseAfterAttachResponse.getStatus().getCode());
        final String modifiedRdfString = getBaseAfterAttachResponse.getEntityAsText();
        
        Assert.assertTrue(modifiedRdfString.contains("rfc2616.html"));
        Assert.assertEquals(41, this.getStatementCount(modifiedRdfString));
    }

    /**
     * Tests that a file reference to an SSH resource can be attached to an object via the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testAttachReference_SSH() throws Exception
    {
        // -- start the test SSH Service here since other tests don't need it
        SSHService sshd = new SSHService(); 
        
        try
        {
            sshd.startTestSSHServer(9856);
            
            // -- login and add an artifact
            final String path = this.getClass().getResource("/test/artifacts/basicProject-1-internal-object.rdf").getFile();
            final String artifactUri =
                    this.loginAndAddArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
            
            // -- retrieve the artifact and check that file ref. does not exist
            final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
            getBaseRequest.setCookies(this.cookies);
            final Response getBaseResponse = this.getClient().handle(getBaseRequest);
            Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseResponse.getStatus().getCode());
            final String originalRdfString = getBaseResponse.getEntityAsText();
            Assert.assertFalse(originalRdfString.contains("basicProject-1.rdf"));
            Assert.assertEquals(33, this.getStatementCount(originalRdfString));
            
            // -- generate and send an attach request
            final Form form = new Form();
            form.add(KEY_FILE_REF_TYPE, "SSH");
            form.add(KEY_ARTIFACT_URI, artifactUri);
            form.add(KEY_OBJECT_URI, "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0");
            form.add(KEY_FILE_SERVER_ALIAS, "localssh");
            form.add(KEY_FILE_PATH, "src/test/resources/test/artifacts");
            form.add(KEY_FILE_NAME, "basicProject-1.rdf");
            form.add(KEY_FILE_DESCRIPTION, "Refers to one of the test artifacts, to be accessed through an ssh server");
            
            final Request editRequest = new Request(Method.POST, this.BASE_URL + "/attachref");
            editRequest.setCookies(this.cookies);
            editRequest.setEntity(form.getWebRepresentation(CharacterSet.UTF_8));
            final Response editResponse = this.getClient().handle(editRequest);
            
            Assert.assertEquals(Status.SUCCESS_OK.getCode(), editResponse.getStatus().getCode());
            Assert.assertNotNull(editResponse.getEntityAsText());
            
            // -- retrieve edited artifact and verify the attached file reference is present
            final Request getBaseAfterAttachRequest =
                    new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
            getBaseAfterAttachRequest.setCookies(this.cookies);
            final Response getBaseAfterAttachResponse = this.getClient().handle(getBaseAfterAttachRequest);
            Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseAfterAttachResponse.getStatus().getCode());
            final String modifiedRdfString = getBaseAfterAttachResponse.getEntityAsText();
            
            Assert.assertTrue(modifiedRdfString.contains("basicProject-1.rdf"));
            Assert.assertEquals(41, this.getStatementCount(modifiedRdfString));
        }
        finally
        {
            sshd.stopTestSSHServer();
        }
    }
    
    
    protected long getStatementCount(final String rdf) throws Exception
    {
        final URI baseContext = IRI.create("urn:get-attached-base:").toOpenRDFURI();
        this.getTestRepositoryConnection().add(new ByteArrayInputStream(rdf.getBytes(StandardCharsets.UTF_8)), "",
                RDFFormat.RDFXML, baseContext);
        final long statementCount = this.getTestRepositoryConnection().size(baseContext);
        this.getTestRepositoryConnection().rollback();
        return statementCount;
    }
    
}
