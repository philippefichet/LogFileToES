/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.logfiletoes.config;

import fr.logfiletoes.Config;
import static fr.logfiletoes.Main.inputSteamToString;
import fr.logfiletoes.TailerListenerUnit;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.io.input.Tailer;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author philippefichet
 */
public class Unit {
    private static final Logger LOG = Logger.getLogger(Unit.class.getName());
    
    private File logFile = null;
    private Pattern logPattern = null;
    private Pattern concatPreviousPattern = null;
    private Map<Integer, String> groupToField = new HashMap<Integer, String>();
    private ElasticSearch elasticSearch = new ElasticSearch();
    private SimpleDateFormat sdf = null;
    private Integer fieldToTimestamp = null;
    private String addFieldToTimestamp = null;
    private Tailer tailer = null;
    private CloseableHttpClient httpclient = null;
    private HttpClientContext context = null;
    
    public Pattern getPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = Pattern.compile(logPattern);
    }
    
    public Pattern getConcatPreviousPattern() {
        return concatPreviousPattern;
    }

    public void setConcatPreviousPattern(String concatPreviousPattern) {
        this.concatPreviousPattern = Pattern.compile(concatPreviousPattern);
    }

    public Map<Integer, String> getGroupToField() {
        return groupToField;
    }

    public void setGroupToField(Map<Integer, String> groupToField) {
        this.groupToField = groupToField;
    }

    public ElasticSearch getElasticSearch() {
        return elasticSearch;
    }

    public void setElasticSearch(ElasticSearch elasticSearch) {
        this.elasticSearch = elasticSearch;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public SimpleDateFormat getSdf() {
        return sdf;
    }

    public void setSdf(SimpleDateFormat sdf) {
        this.sdf = sdf;
    }

    public Integer getFieldToTimestamp() {
        return fieldToTimestamp;
    }

    public void setFieldToTimestamp(Integer fieldToTimestamp) {
        this.fieldToTimestamp = fieldToTimestamp;
    }

    public String getAddFieldToTimestamp() {
        return addFieldToTimestamp;
    }

    public void setAddFieldToTimestamp(String addFieldToTimestamp) {
        this.addFieldToTimestamp = addFieldToTimestamp;
    }
    
    /**
     * Initialise ElasticSearch Index ans taile file
     * @throws IOException 
     */
    public void start() throws IOException {
        httpclient = HttpClients.createDefault();
        context = HttpClientContext.create();
        HttpGet httpGet = new HttpGet(getElasticSearch().getUrl());
        if (getElasticSearch().getLogin() != null && getElasticSearch().getPassword() != null) {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getElasticSearch().getLogin(), getElasticSearch().getPassword());
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            context.setCredentialsProvider(credentialsProvider);
        }
        CloseableHttpResponse elasticSearchCheck = httpclient.execute(httpGet, context);
        LOG.log(Level.INFO, "Check index : \"{0}\"", elasticSearchCheck.getStatusLine().getStatusCode());
        LOG.log(Level.INFO, inputSteamToString(elasticSearchCheck.getEntity().getContent()));
        int statusCodeCheck = elasticSearchCheck.getStatusLine().getStatusCode();
        EntityUtils.consume(elasticSearchCheck.getEntity());
        elasticSearchCheck.close();
        
        if (statusCodeCheck == 404) {
            // Création de l'index
            HttpPut httpPut = new HttpPut(getElasticSearch().getUrl());
            CloseableHttpResponse executeCreateIndex = httpclient.execute(httpPut, context);
            LOG.log(Level.INFO, "Create index : \"{0}\"", executeCreateIndex.getStatusLine().getStatusCode());
            LOG.log(Level.INFO, inputSteamToString(executeCreateIndex.getEntity().getContent()));
            EntityUtils.consume(executeCreateIndex.getEntity());
            executeCreateIndex.close();
            
            CloseableHttpResponse elasticSearchCheckCreate = httpclient.execute(httpGet, context);
            LOG.log(Level.INFO, "Check create index : \"{0}\"", elasticSearchCheckCreate.getStatusLine().getStatusCode());
            LOG.log(Level.INFO, inputSteamToString(elasticSearchCheckCreate.getEntity().getContent()));
            int statusCodeCheckCreate = elasticSearchCheckCreate.getStatusLine().getStatusCode();
            EntityUtils.consume(elasticSearchCheckCreate.getEntity());
            elasticSearchCheckCreate.close();
            
            if (statusCodeCheckCreate != 200) {
                LOG.log(Level.SEVERE, "unable to create index \"{0}\"", getElasticSearch().getUrl());
                throw new IOException("unable to create index \"" + getElasticSearch().getUrl() + "\"");
            }
        } else if(elasticSearchCheck.getStatusLine().getStatusCode() != 200) {
            LOG.severe("unkown error elasticsearch");
            throw new IOException("unkown error elasticsearch");
        }
        LOG.log(Level.INFO, "Initialisation ElasticSearch r\u00e9ussi pour {0}", getElasticSearch().getUrl());

        tailer = Tailer.create(getLogFile(), new TailerListenerUnit(this, httpclient, context));
    }
    
    /**
     * 
     */
    public void stop() {
        if (tailer != null) {
            tailer.stop();
        }
        
        if (httpclient != null) {
            try {
                httpclient.close();
            } catch(IOException ioe) {
                LOG.log(Level.WARNING, "Close http client error", ioe);
            }
        }
    }
}
