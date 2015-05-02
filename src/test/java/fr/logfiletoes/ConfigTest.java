package fr.logfiletoes;


import fr.logfiletoes.Config;
import fr.logfiletoes.config.Unit;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author philippefichet
 */
public class ConfigTest {
    @Test
    public void loadFile() throws IOException {
        Config config = new Config("./src/test/resources/config-wildfly.json");
    }
    
    @Test
    public void unit() throws IOException {
        Config config = new Config("./src/test/resources/config-wildfly.json");
        assertEquals(2, config.getUnits().size());
    }
    
    @Test
    public void unitCheck() throws IOException {
        Config config = new Config("./src/test/resources/config-wildfly.json");
        assertEquals(2, config.getUnits().size());
        Unit unit = config.getUnits().get(0);
        System.out.println("unit = " + unit.getPattern().pattern());
        assertEquals("([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}) ([^ ]+) +\\[([^]]+)\\] \\(([^)]+)\\) (.+)", unit.getPattern().pattern());
        assertEquals("."+File.separator+"src"+File.separator+"test"+File.separator+"resources"+File.separator+"wildfly.log", unit.getLogFile().getPath());
        assertEquals("http://localhost:9200/logdetest", unit.getElasticSearch().getUrl());
        assertEquals("wildfly", unit.getElasticSearch().getType());
        assertNull(unit.getElasticSearch().getLogin());
        assertNull(unit.getElasticSearch().getPassword());
        assertEquals(5, unit.getGroupToField().size());
        assertEquals("date", unit.getGroupToField().get(1));
        assertEquals("level", unit.getGroupToField().get(2));
        assertEquals("logger", unit.getGroupToField().get(3));
        assertEquals("service", unit.getGroupToField().get(4));
        assertEquals("message", unit.getGroupToField().get(5));

        unit = config.getUnits().get(1);
        assertEquals("([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}) ([^ ]+) +\\[([^]]+)\\] \\(([^)]+)\\) (.+)", unit.getPattern().pattern());
        assertEquals("."+File.separator+"src"+File.separator+"test"+File.separator+"resources"+File.separator+"wildfly.log", unit.getLogFile().getPath());
        assertEquals("https://localhost/logdetest", unit.getElasticSearch().getUrl());
        assertEquals("wildfly", unit.getElasticSearch().getType());
        assertEquals("logindetest", unit.getElasticSearch().getLogin());
        assertEquals("passworddetest", unit.getElasticSearch().getPassword());

    
    }
    
    @Test
    public void patternError() throws IOException {
        Config config = new Config("./src/test/resources/config-error-pattern.json");
        assertEquals(0, config.getUnits().size());
    }
}
