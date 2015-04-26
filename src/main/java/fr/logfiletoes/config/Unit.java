/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.logfiletoes.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author philippefichet
 */
public class Unit {
    private File logFile = null;
    private Pattern logPattern = null;
    private Pattern concatPreviousPattern = null;
    private Map<Integer, String> groupToField = new HashMap<Integer, String>();
    private ElasticSearch elasticSearch = new ElasticSearch();
    private SimpleDateFormat sdf = null;
    private Integer fieldToTimestamp = null;
    private String addFieldToTimestamp = null;
    
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
    
    
}
