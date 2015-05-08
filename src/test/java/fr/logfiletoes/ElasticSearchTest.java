/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.logfiletoes;

import fr.logfiletoes.config.Unit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;
import static org.junit.Assert.*;
        
/**
 *
 * @author glopinous
 */
public class ElasticSearchTest {
    @Test
    public void es() throws IOException {
        File data = Files.createTempDirectory("it_es_data-").toFile();
        Settings settings = ImmutableSettings.settingsBuilder()
            .put("path.data", data.toString())
            .put("cluster.name", "IT-0001")
            .build();
        Node node = NodeBuilder.nodeBuilder().local(true).settings(settings).build();
        Client client = node.client();
        node.start();
        Config config = new Config("./src/test/resources/config-wildfly-one.json");
        List<Unit> units = config.getUnits();
        assertEquals(1, units.size());
        units.get(0).start();
        // TODO test
        
        // Close tailer
        units.get(0).stop();
        
        // Close ElasticSearch
        node.close();
        
        // Clean data directory
        FileUtils.forceDelete(data);
    }
}
