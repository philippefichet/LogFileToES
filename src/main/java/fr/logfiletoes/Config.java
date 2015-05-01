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
    
    private static final Logger LOG = Logger.getLogger(Config.class.getName());
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
        LOG.fine(unitsJSON.length() + " unit(s)");
        for (int i = 0; i < unitsJSON.length(); i++) {
            JSONObject unitFormJson = unitsJSON.getJSONObject(i);
            Unit unit = createUnit(unitFormJson);
            if (unit != null) {
                this.units.add(unit);
            } else {
                LOG.warning("unit skip : " + unitFormJson.toString());
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

        if (unitFormJson.has("file")) {
            String filepathUnit = unitFormJson.getString("file");
            File file = new File(filepathUnit);
            if(file.exists()) {
                unit.setLogFile(file);
            } else {
                LOG.warning("file error");
                return null;
            }
        } else {
            LOG.warning("file is required");
            return null;
        }

        if(unitFormJson.has("pattern")) {
            String pattern = unitFormJson.getString("pattern");
            try {
            unit.setLogPattern(pattern);
            } catch(PatternSyntaxException exception) {
                LOG.log(Level.WARNING, "pattern error", exception);
                return null;
            }
        } else {
            LOG.warning("Pattern of unit config is required");
            return null;
        }
        
        if(unitFormJson.has("elasticsearch")) {
            JSONObject elasticSearchJson = unitFormJson.getJSONObject("elasticsearch");
            ElasticSearch createElasticSearch = createElasticSearch(elasticSearchJson);
            if (createElasticSearch == null) {
                LOG.log(Level.WARNING, "elasticsearch error");
            } else {
                unit.setElasticSearch(createElasticSearch);
            }
        } else {
            LOG.warning("elasticsearch is required");
            return null;
        }
        
        if(unitFormJson.has("fields")) {
            JSONObject mapping = unitFormJson.getJSONObject("fields");
            unit.setGroupToField(extractMappingForUnit(mapping));
        }

        if(unitFormJson.has("concatPreviousLog")) {
            String concatPreviousLog = unitFormJson.getString("concatPreviousLog");
            try {
                unit.setConcatPreviousPattern(concatPreviousLog);
            } catch(PatternSyntaxException exception) {
                LOG.log(Level.WARNING, "concatPreviousLog error", exception);
            }
        } else {
            LOG.info("concatPreviousLog can be used to no matching is concat previous log line");
        }
        
        if(unitFormJson.has("timestamp")) {
            JSONObject timestampConfig = unitFormJson.getJSONObject("timestamp");
            Integer field = null;
            String format = null;
            String addField = null;
            if(timestampConfig.has("field")) {
                field = timestampConfig.getInt("field");
            } else {
                LOG.warning("Position of field is required for timestamp");
            }
            if(timestampConfig.has("format")) {
                format = timestampConfig.getString("format");
            } else {
                LOG.warning("Format (from SimpleDateFormat) is required for date to timestamp");
            }
            if(timestampConfig.has("addField")) {
                addField = timestampConfig.getString("addField");
            } else {
                LOG.warning("addField is required for add timestamp in new field in elasticsearch format");
            }
            
            if (field != null && format != null && addField != null) {
                unit.setFieldToTimestamp(field);
                unit.setSdf(new SimpleDateFormat(format));
                unit.setAddFieldToTimestamp(addField);
            }
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
            LOG.warning("elasticsearch url is required");
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

    /**
     * Extract mapping for regex group from JSON
     * @param mapping JSON to extract mappgin for regex group
     * @return Mappgin for regex group
     */
    private Map<Integer, String> extractMappingForUnit(JSONObject mapping) {
        Map<Integer, String> mappingGroupToField = new HashMap<>();
        Iterator<String> keys = mapping.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    Integer groupPosition = Integer.parseInt(key);
                    mappingGroupToField.put(groupPosition, mapping.getString(key));
                } catch(NumberFormatException exception) {
                    LOG.warning(key + " is not an Integer");
                }
            }
        return mappingGroupToField;
    }
}
