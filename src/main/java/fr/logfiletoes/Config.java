/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.logfiletoes;

import fr.logfiletoes.config.ElasticSearch;
import fr.logfiletoes.config.Unit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author philippefichet
 */
public class Config {
    
    private static Logger logger = Logger.getLogger(Config.class.getName());
    private final List<Unit> units = new ArrayList<>();
    
    /**
     * Construct config from json file path
     * @param filepath json config file path
     * @throws IOException
     */
    public Config(String filepath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filepath)));
        JSONTokener jsont = new JSONTokener(json);
        JSONObject config = (JSONObject)jsont.nextValue();
        JSONArray unitsJSON = config.getJSONArray("unit");
        logger.fine(unitsJSON.length() + " unit(s)");
        for (int i = 0; i < unitsJSON.length(); i++) {
            JSONObject unitFormJson = unitsJSON.getJSONObject(i);
            Unit unit = createUnit(unitFormJson);
            if (unit != null) {
                this.units.add(unit);
            } else {
                logger.warning("unit skip : " + unitFormJson.toString());
            }
        }
    }

    /**
     * 
     * @param unitFormJson
     * @return 
     */
    protected Unit createUnit(JSONObject unitFormJson) {
        Unit unit = new Unit();

        try {
            String filepathUnit = unitFormJson.getString("file");
            File file = new File(filepathUnit);
            if(file.exists()) {
                unit.setLogFile(file);
            } else {
                logger.warning("file error");
                return null;
            }
        } catch(JSONException exception) {
            logger.warning("file is required");
            return null;
        }

        try {
            String pattern = unitFormJson.getString("pattern");
            unit.setLogPattern(pattern);
        } catch(JSONException exception) {
            logger.warning("Pattern of unit config is required");
            return null;
        } catch(PatternSyntaxException exception) {
            logger.log(Level.WARNING, "pattern error", exception);
            return null;
        }

        try {
            JSONObject elasticSearchJson = unitFormJson.getJSONObject("elasticsearch");
            ElasticSearch createElasticSearch = createElasticSearch(elasticSearchJson);
            if (createElasticSearch == null) {
                logger.log(Level.WARNING, "elasticsearch error");
            } else {
                unit.setElasticSearch(createElasticSearch);
            }
        } catch(JSONException exception) {
            logger.warning("elasticsearch is required");
            return null;
        }
        
        try {
            JSONObject mapping = unitFormJson.getJSONObject("fields");
            Map<Integer, String> mappingGroupToField = new HashMap<>();
            Iterator<String> keys = mapping.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    Integer groupPosition = Integer.parseInt(key);
                    mappingGroupToField.put(groupPosition, mapping.getString(key));
                } catch(NumberFormatException exception) {
                    logger.warning(key + " is not an Integer");
                }
            }
            unit.setGroupToField(mappingGroupToField);
        } catch(JSONException exception) {
        }

        try {
            String concatPreviousLog = unitFormJson.getString("concatPreviousLog");
            unit.setConcatPreviousPattern(concatPreviousLog);
        } catch(JSONException exception) {
            logger.warning("concatPreviousLog can be used to no matching is concat previous log line");
        } catch(PatternSyntaxException exception) {
            logger.log(Level.WARNING, "concatPreviousLog error", exception);
        }

        try {
            JSONObject timestampConfig = unitFormJson.getJSONObject("timestamp");
            Integer field = null;
            String format = null;
            String addField = null;
            try {
                field = timestampConfig.getInt("field");
            } catch(JSONException exception) {
                logger.warning("Position of field is required for timestamp");
            }
            try {
                format = timestampConfig.getString("format");
            } catch(JSONException exception) {
                logger.warning("Format (from SimpleDateFormat) is required for date to timestamp");
            }
            try {
                addField = timestampConfig.getString("addField");
            } catch(JSONException exception) {
                logger.warning("addField is required for add timestamp in new field in elasticsearch format");
            }
            
            if (field != null && format != null && addField != null) {
                unit.setFieldToTimestamp(field);
                unit.setSdf(new SimpleDateFormat(format));
                unit.setAddFieldToTimestamp(addField);
            }
        } catch(JSONException exception) {
            // Optionnal parameter
        }

        return unit;
    }
    
    /**
     * Get unit list of logfile/ElasticSearch
     * @return list of logfile/ElasticSearch
     */
    public List<Unit> getUnits() {
        return units;
    }

    /**
     * Create an ElasticSearch config object from JSONObject
     * @param elasticSearchJson Config object from JSONObject
     * @return null if required parameter is missed
     */
    private ElasticSearch createElasticSearch(JSONObject elasticSearchJson) {
        ElasticSearch elasticSearch = new ElasticSearch();
        try {
            String url = elasticSearchJson.getString("url");
            elasticSearch.setUrl(url);
        } catch(JSONException exception) {
            logger.warning("elasticsearch url is required");
            return null;
        }
        
        try {
            String type = elasticSearchJson.getString("type");
            elasticSearch.setType(type);
        // Optionnal
        } catch(JSONException exception) {
        }
        
        try {
            String login = elasticSearchJson.getString("login");
            elasticSearch.setLogin(login);
        // Optionnal
        } catch(JSONException exception) {
        }
        
        try {
            String password = elasticSearchJson.getString("password");
            elasticSearch.setPassword(password);
        // Optionnal
        } catch(JSONException exception) {
        }
        
        return elasticSearch;
    }
}
