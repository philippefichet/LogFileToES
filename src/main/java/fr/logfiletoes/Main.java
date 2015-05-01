/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.logfiletoes;

import fr.logfiletoes.config.Unit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 *
 * @author philippefichet
 */
public class Main {
    
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private Main() {
    }
    
    /**
     * Convert InputStream to String
     * @param is InputStream read to convert to String
     * @return String from InputStream
     * @throws IOException 
     */
    public static String inputSteamToString(InputStream is) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while(is.available() > 0) {
            is.read(buffer);
            sb.append(new String(buffer));
        }
        return sb.toString();
    }
    
    public static void main(String[] args) throws IOException {
        String configFile = getConfigFilePath();
        
        LOG.log(Level.INFO, "Load config file \"{0}\"", configFile);
        Config config = new Config(configFile);
        LOG.info("Config file OK");
        
        for (Unit unit : config.getUnits()) {
            startUnit(unit);
        }
        
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Start unit tailer and ElasticSearch check
     * @param unit Unit to start
     * @return Tailer if start success, null if failed
     * @throws IOException 
     */
    public static Tailer startUnit(Unit unit) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpGet httpGet = new HttpGet(unit.getElasticSearch().getUrl());
        if (unit.getElasticSearch().getLogin() != null && unit.getElasticSearch().getPassword() != null) {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(unit.getElasticSearch().getLogin(), unit.getElasticSearch().getPassword());
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            context.setCredentialsProvider(credentialsProvider);
        }
        CloseableHttpResponse elasticSearchCheck = httpclient.execute(httpGet, context);
        LOG.log(Level.INFO, "Check index : \"{0}\"", elasticSearchCheck.getStatusLine().getStatusCode());
        LOG.log(Level.INFO, inputSteamToString(elasticSearchCheck.getEntity().getContent()));
        if (elasticSearchCheck.getStatusLine().getStatusCode() == 404) {
            // Création de l'index
            HttpPut httpPut = new HttpPut(unit.getElasticSearch().getUrl());
            httpclient.execute(httpPut, context);
            CloseableHttpResponse elasticSearchCheckCreate = httpclient.execute(httpGet, context);
            LOG.log(Level.INFO, "create index : \"{0}\"", elasticSearchCheck.getStatusLine().getStatusCode());
            if (elasticSearchCheckCreate.getStatusLine().getStatusCode() != 200) {
                LOG.log(Level.SEVERE, "unable to create index \"{0}\"", unit.getElasticSearch().getUrl());
                return null;
            }
        } else if(elasticSearchCheck.getStatusLine().getStatusCode() != 200) {
            LOG.severe("unkown error elasticsearch");
            return null;
        }
        LOG.log(Level.INFO, "Initialisation ElasticSearch r\u00e9ussi pour {0}", unit.getElasticSearch().getUrl());

        return Tailer.create(unit.getLogFile(), new TailerListenerUnit(unit, httpclient, context));
    }
    
    /**
     * Get config file and check if existe
     * @return 
     */
    public static String getConfigFilePath() {
        String configFile = System.getProperty("fr.logfiletoes.config.file");
        if (configFile == null) {
            LOG.severe("-Dfr.logfiletoes.config.file is required for config file");
            System.exit(1);
        } else {
            File config = new File(configFile);
            if (!config.exists()) {
                LOG.severe("\"" + config.getAbsolutePath() + "\" not found");
                System.exit(2);
            }
            if (!config.isFile()) {
                LOG.severe("\"" + config.getAbsolutePath() + "\" is not file");
                System.exit(3);
            }
        }
        return configFile;
    }
}
