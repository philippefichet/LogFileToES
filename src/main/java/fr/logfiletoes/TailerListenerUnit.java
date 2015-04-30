/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.logfiletoes;

import fr.logfiletoes.config.Unit;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

/**
 *
 * @author philippefichet
 */
public class TailerListenerUnit extends TailerListenerAdapter {
    private StringBuilder sb = null;
    private JSONObject json = null;
    private Unit unit = null;
    private final CloseableHttpClient httpclient;
    private static Logger LOG = Logger.getLogger(TailerListenerUnit.class.getName());
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    private HttpClientContext context;

    public TailerListenerUnit(Unit unit, CloseableHttpClient httpclient, HttpClientContext context) {
        this.unit = unit;
        this.httpclient = httpclient;
        this.context = context;
    }

    @Override
    public void handle(Exception ex) {
        super.handle(ex);
        LOG.log(Level.SEVERE, "Error handle", ex);
    }

    @Override
    public void fileRotated() {
        super.fileRotated();
        LOG.info(unit.getLogFile().getAbsolutePath() + " rotated");
    }

    @Override
    public void fileNotFound() {
        super.fileNotFound();
        LOG.warning(unit.getLogFile().getAbsolutePath() + " not found");
    }

    @Override
    public void init(Tailer tailer) {
        super.init(tailer);
        LOG.info("init : " + tailer.toString());
    }
    
    public void handle(String line) {
        LOG.log(Level.FINEST, "handle log: " + line);
        Matcher matcher = unit.getPattern().matcher(line);
        if (unit.getConcatPreviousPattern() != null && unit.getConcatPreviousPattern().matcher(line).find() == false) {
            if (sb != null) {
                sb.append("\n").append(line);
            }
        } else {
            if (sb != null) {
                json.put("message", sb.toString());
                String id = null;
                
                // Sauvegarde de la sb
                HttpPost elasticSearchPost = new HttpPost(unit.getElasticSearch().getUrl() + "/" + unit.getElasticSearch().getType());
                try {
                    elasticSearchPost.setEntity(new StringEntity(json.toString()));
                    CloseableHttpResponse execute = httpclient.execute(elasticSearchPost, context);
                    if (execute.getStatusLine().getStatusCode() < 200 || execute.getStatusLine().getStatusCode() >= 300) {
                        LOG.log(Level.SEVERE, "Add log to ElasticSearch failed : " + execute.getStatusLine().getStatusCode());
                        LOG.log(Level.SEVERE, inputSteamToString(execute.getEntity().getContent()));
                    } else {
                        LOG.log(Level.FINE, "Add log to ElasticSearch successful.");
                        LOG.log(Level.FINER, json.toString());
                    }
                    execute.close();
                } catch (UnsupportedEncodingException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            sb = new StringBuilder(line);
            json = new JSONObject();
            while(matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String field = unit.getGroupToField().get(i);
                    if (field != null) {
                        json.put(field, matcher.group(i));
                        // Surcharge du message
                        if ("message".equals(field)) {
                            sb = new StringBuilder(line);
                        }
                    }
                }
                
                if (unit.getAddFieldToTimestamp() != null && unit.getSdf() != null && unit.getFieldToTimestamp() != null) {
                    String timestampFieldGroup = unit.getGroupToField().get(unit.getFieldToTimestamp());
                    if (timestampFieldGroup != null) {
                        String date = json.getString(timestampFieldGroup);
                        try {
                            Date timestamp = unit.getSdf().parse(date);
                            json.put(unit.getAddFieldToTimestamp(), dateFormat.format(timestamp));
                        } catch(ParseException exception) {
                            LOG.warning("Unable to parse date \"" + date + "\" with pattern \"" + unit.getSdf().toPattern() + "\"");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Transform InpoutStream to String
     * @param is
     * @return
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
}
