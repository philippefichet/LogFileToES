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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
        
/**
 *
 * @author glopinous
 */
public class ElasticSearchTest {
    
    private static final Logger LOG = Logger.getLogger(ElasticSearchTest.class.getName());
    
    private String stacktrace = "Configured system properties:\n" +
"	awt.toolkit = sun.awt.X11.XToolkit\n" +
"	file.encoding = UTF-8\n" +
"	file.encoding.pkg = sun.io\n" +
"	file.separator = /\n" +
"	java.awt.graphicsenv = sun.awt.X11GraphicsEnvironment\n" +
"	java.awt.printerjob = sun.print.PSPrinterJob\n" +
"	java.class.path = /xxx/wildfly-8.2.0.Final/jboss-modules.jar\n" +
"	java.class.version = 52.0\n" +
"	java.endorsed.dirs = /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/endorsed\n" +
"	java.ext.dirs = /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext:/usr/java/packages/lib/ext\n" +
"	java.home = /usr/lib/jvm/java-8-openjdk-amd64/jre\n" +
"	java.io.tmpdir = /tmp\n" +
"	java.library.path = /usr/java/packages/lib/amd64:/usr/lib/x86_64-linux-gnu/jni:/lib/x86_64-linux-gnu:/usr/lib/x86_64-linux-gnu:/usr/lib/jni:/lib:/usr/lib\n" +
"	java.runtime.name = OpenJDK Runtime Environment\n" +
"	java.runtime.version = 1.8.0_40-internal-b09\n" +
"	java.specification.name = Java Platform API Specification\n" +
"	java.specification.vendor = Oracle Corporation\n" +
"	java.specification.version = 1.8\n" +
"	java.util.logging.manager = org.jboss.logmanager.LogManager\n" +
"	java.vendor = Oracle Corporation\n" +
"	java.vendor.url = http://java.oracle.com/\n" +
"	java.vendor.url.bug = http://bugreport.sun.com/bugreport/\n" +
"	java.version = 1.8.0_40-internal\n" +
"	java.vm.info = mixed mode\n" +
"	java.vm.name = OpenJDK 64-Bit Server VM\n" +
"	java.vm.specification.name = Java Virtual Machine Specification\n" +
"	java.vm.specification.vendor = Oracle Corporation\n" +
"	java.vm.specification.version = 1.8\n" +
"	java.vm.vendor = Oracle Corporation\n" +
"	java.vm.version = 25.40-b13\n" +
"	javax.management.builder.initial = org.jboss.as.jmx.PluggableMBeanServerBuilder\n" +
"	javax.xml.datatype.DatatypeFactory = __redirected.__DatatypeFactory\n" +
"	javax.xml.parsers.DocumentBuilderFactory = __redirected.__DocumentBuilderFactory\n" +
"	javax.xml.parsers.SAXParserFactory = __redirected.__SAXParserFactory\n" +
"	javax.xml.stream.XMLEventFactory = __redirected.__XMLEventFactory\n" +
"	javax.xml.stream.XMLInputFactory = __redirected.__XMLInputFactory\n" +
"	javax.xml.stream.XMLOutputFactory = __redirected.__XMLOutputFactory\n" +
"	javax.xml.transform.TransformerFactory = __redirected.__TransformerFactory\n" +
"	javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema = __redirected.__SchemaFactory\n" +
"	javax.xml.xpath.XPathFactory:http://java.sun.com/jaxp/xpath/dom = __redirected.__XPathFactory\n" +
"	jboss.bundles.dir = /xxx/wildfly-8.2.0.Final/bundles\n" +
"	jboss.home.dir = /xxx/wildfly-8.2.0.Final\n" +
"	jboss.host.name = xxx\n" +
"	jboss.modules.dir = /xxx/wildfly-8.2.0.Final/modules\n" +
"	jboss.node.name = xxx\n" +
"	jboss.qualified.host.name = xxx\n" +
"	jboss.server.base.dir = /xxx/wildfly-8.2.0.Final/standalone\n" +
"	jboss.server.config.dir = /xxx/wildfly-8.2.0.Final/standalone/configuration\n" +
"	jboss.server.data.dir = /xxx/wildfly-8.2.0.Final/standalone/data\n" +
"	jboss.server.deploy.dir = /xxx/wildfly-8.2.0.Final/standalone/data/content\n" +
"	jboss.server.log.dir = /xxx/wildfly-8.2.0.Final/standalone/log\n" +
"	jboss.server.name = xxx\n" +
"	jboss.server.persist.config = true\n" +
"	jboss.server.temp.dir = /xxx/wildfly-8.2.0.Final/standalone/tmp\n" +
"	line.separator = \n" +
"\n" +
"	logging.configuration = file:/xxx/wildfly-8.2.0.Final/standalone/configuration/logging.properties\n" +
"	module.path = /xxx/wildfly-8.2.0.Final/modules\n" +
"	org.jboss.boot.log.file = /xxx/wildfly-8.2.0.Final/standalone/log/server.log\n" +
"	org.jboss.resolver.warning = true\n" +
"	org.xml.sax.driver = __redirected.__XMLReaderFactory\n" +
"	os.arch = amd64\n" +
"	os.name = Linux\n" +
"	os.version = 3.16.0-33-generic\n" +
"	path.separator = :\n" +
"	sun.arch.data.model = 64\n" +
"	sun.boot.class.path = /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/resources.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/sunrsasign.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jsse.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/charsets.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jfr.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/classes\n" +
"	sun.boot.library.path = /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/amd64\n" +
"	sun.cpu.endian = little\n" +
"	sun.cpu.isalist = \n" +
"	sun.io.unicode.encoding = UnicodeLittle\n" +
"	sun.java.command = /xxx/wildfly-8.2.0.Final/jboss-modules.jar -mp /xxx/wildfly-8.2.0.Final/modules org.jboss.as.standalone -server-config standalone.xml\n" +
"	sun.java.launcher = SUN_STANDARD\n" +
"	sun.jnu.encoding = UTF-8\n" +
"	sun.management.compiler = HotSpot 64-Bit Tiered Compilers\n" +
"	sun.os.patch.level = unknown\n" +
"	user.country = US\n" +
"	user.dir = /xxx/\n" +
"	user.home = /xxx/\n" +
"	user.language = en\n" +
"	user.name = xxx\n" +
"	user.timezone = Europe/Paris";
    
    @Test
    public void wildfly() throws IOException, InterruptedException {
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
        
        // Wait store log
        Thread.sleep(3000);

        // Search log
        SearchResponse response = client.prepareSearch("logdetest")
            .setSearchType(SearchType.DEFAULT)
            .setQuery(QueryBuilders.matchQuery("message", "Configured system properties"))
            .setSize(1000)
            .addSort("@timestamp", SortOrder.ASC)
            .execute()
            .actionGet();
        if(LOG.isLoggable(Level.FINEST)) {
            for (SearchHit hit : response.getHits().getHits()) {
                LOG.finest("-----------------");
                hit.getSource().forEach((key, value) -> {
                    LOG.log(Level.FINEST, "{0} = {1}", new Object[]{key, value});
                });
            }
        }
        
        // Get information need to test
        String expected = response.getHits().getHits()[0].getSource().get("message").toString();
        assertEquals(stacktrace, expected);

        // wait request
        Thread.sleep(10000);
        
        // Close tailer
        units.get(0).stop();
        
        // Close ElasticSearch
        node.close();
        
        // Clean data directory
        FileUtils.forceDelete(data);
    }
    
    @Test
    public void fail2ban() throws IOException, InterruptedException {
        File data = Files.createTempDirectory("it_es_data-").toFile();
        Settings settings = ImmutableSettings.settingsBuilder()
            .put("path.data", data.toString())
            .put("cluster.name", "IT-0002")
            .build();
        Node node = NodeBuilder.nodeBuilder().local(true).settings(settings).build();
        Client client = node.client();
        node.start();
        Config config = new Config("./src/test/resources/fail2ban.json");
        List<Unit> units = config.getUnits();
        assertEquals(1, units.size());
        units.get(0).start();
        
        // Wait store log
        Thread.sleep(3000);

        // Search log
        SearchResponse response = client.prepareSearch("system")
            .setSearchType(SearchType.DEFAULT)
            .setQuery(QueryBuilders.matchQuery("message", "58.218.204.248"))
            .setSize(1000)
            .addSort("@timestamp", SortOrder.ASC)
            .execute()
            .actionGet();
        if(LOG.isLoggable(Level.FINEST)) {
            for (SearchHit hit : response.getHits().getHits()) {
                LOG.finest("-----------------");
                hit.getSource().forEach((key, value) -> {
                    LOG.log(Level.FINEST, "{0} = {1}", new Object[]{key, value});
                });
            }
        }
        
        // Get information need to test
        assertEquals(6, response.getHits().getHits().length);
        assertEquals("Found 58.218.204.248", response.getHits().getHits()[0].getSource().get("message").toString());
        assertEquals("Ban 58.218.204.248", response.getHits().getHits()[5].getSource().get("message").toString());

        // wait request
        Thread.sleep(10000);
        
        // Close tailer
        units.get(0).stop();
        
        // Close ElasticSearch
        node.close();
        
        // Clean data directory
        FileUtils.forceDelete(data);
    }
}
