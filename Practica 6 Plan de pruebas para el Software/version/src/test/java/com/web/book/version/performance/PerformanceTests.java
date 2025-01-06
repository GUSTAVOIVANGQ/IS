package com.web.book.version.performance;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.reporters.ResultCollector;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PerformanceTests {

    private static final String BASE_URL = "http://localhost:8083";
    private static final int NUM_THREADS = 100;
    private static final int RAMP_UP_PERIOD = 10;
    private static final int NUM_LOOPS = 10;
    private static final long MAX_RESPONSE_TIME = 2000; // 2 segundos

    @BeforeAll
    public static void setupJMeter() {
        try {
            // Crear directorio temporal para JMeter
            File jmeterHome = new File("target/jmeter");
            if (!jmeterHome.exists()) {
                jmeterHome.mkdirs();
            }

            // Copiar archivo de propiedades desde resources
            File propsFile = new File(jmeterHome, "jmeter.properties");
            if (!propsFile.exists()) {
                try (InputStream is = PerformanceTests.class.getResourceAsStream("/jmeter/jmeter.properties")) {
                    if (is == null) {
                        throw new RuntimeException("No se puede encontrar jmeter.properties en el classpath");
                    }
                    Files.copy(is, propsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Configurar JMeter
            System.setProperty("jmeter.home", jmeterHome.getAbsolutePath());
            JMeterUtils.setJMeterHome(jmeterHome.getAbsolutePath());
            JMeterUtils.loadJMeterProperties(propsFile.getAbsolutePath());
            JMeterUtils.initLocale();
            
            // Configurar log
            JMeterUtils.initLogging();
        } catch (Exception e) {
            throw new RuntimeException("Error al configurar JMeter: " + e.getMessage(), e);
        }
    }

    @Test
    public void testHomePagePerformance() throws Exception {
        // Crear el plan de prueba
        TestPlan testPlan = new TestPlan("Test Plan");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, ArgumentsPanel.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Crear grupo de hilos
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Thread Group");
        threadGroup.setNumThreads(NUM_THREADS);
        threadGroup.setRampUp(RAMP_UP_PERIOD);
        
        LoopController loopController = new LoopController();
        loopController.setLoops(NUM_LOOPS);
        loopController.setFirst(true);
        threadGroup.setSamplerController(loopController);

        // Crear sampler HTTP
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8083);
        httpSampler.setPath("/");
        httpSampler.setMethod("GET");
        httpSampler.setName("Home Page Request");
        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        // Crear árbol de prueba
        HashTree testPlanTree = new HashTree();
        testPlanTree.add(testPlan);
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(httpSampler);

        // Modificar esta parte para usar nuestro custom listener
        SummariserListener listener = new SummariserListener();
        ResultCollector resultCollector = new ResultCollector(listener);
        testPlanTree.add(testPlanTree.getArray()[0], resultCollector);

        // Guardar el plan de prueba
        //SaveService.saveTree(testPlanTree, System.out);

        // Ejecutar el plan de prueba
        Instant startTime = Instant.now();
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        jmeter.configure(testPlanTree);
        jmeter.run();
        Duration duration = Duration.between(startTime, Instant.now());

        // Usar el listener personalizado para obtener estadísticas
        long averageResponseTime = listener.getTotal() / listener.getNum();
        assertTrue(averageResponseTime < MAX_RESPONSE_TIME, 
            "Tiempo de respuesta promedio (" + averageResponseTime + 
            "ms) excede el máximo permitido (" + MAX_RESPONSE_TIME + "ms)");
        
        System.out.println("Test completado en: " + duration.toSeconds() + " segundos");
        System.out.println("Tiempo de respuesta promedio: " + averageResponseTime + "ms");
        System.out.println("Total de solicitudes: " + listener.getNum());
        System.out.println("Errores: " + listener.getErrorCount());
    }

    @Test
    public void testSearchEndpointPerformance() throws Exception {
        // Crear el plan de prueba
        TestPlan testPlan = new TestPlan("Search Test Plan");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, ArgumentsPanel.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Crear grupo de hilos con menos carga que la página principal
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Search Thread Group");
        threadGroup.setNumThreads(50); // Menos usuarios concurrentes para búsqueda
        threadGroup.setRampUp(5);
        
        LoopController loopController = new LoopController();
        loopController.setLoops(5);
        loopController.setFirst(true);
        threadGroup.setSamplerController(loopController);

        // Configurar sampler HTTP para búsqueda
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8083);
        httpSampler.setPath("/api/books/search");
        httpSampler.setMethod("GET");
        httpSampler.addArgument("query", "harry potter");
        httpSampler.setName("Search Request");
        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        // Crear árbol de prueba
        HashTree testPlanTree = new HashTree();
        testPlanTree.add(testPlan);
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(httpSampler);

        // Modificar esta parte para usar nuestro custom listener
        SummariserListener listener = new SummariserListener();
        ResultCollector resultCollector = new ResultCollector(listener);
        testPlanTree.add(testPlanTree.getArray()[0], resultCollector);

        // Ejecutar prueba
        Instant startTime = Instant.now();
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        jmeter.configure(testPlanTree);
        jmeter.run();
        Duration duration = Duration.between(startTime, Instant.now());

        // Usar el listener personalizado para obtener estadísticas
        long averageResponseTime = listener.getTotal() / listener.getNum();
        assertTrue(averageResponseTime < MAX_RESPONSE_TIME, 
            "Tiempo de respuesta promedio (" + averageResponseTime + 
            "ms) excede el máximo permitido (" + MAX_RESPONSE_TIME + "ms)");

        System.out.println("Test de búsqueda completado en: " + duration.toSeconds() + " segundos");
        System.out.println("Tiempo de respuesta promedio: " + averageResponseTime + "ms");
        System.out.println("Total de búsquedas: " + listener.getNum());
        System.out.println("Errores en búsquedas: " + listener.getErrorCount());
    }

    @Test
    public void testUserProfilePerformance() throws Exception {
        // Crear el plan de prueba
        TestPlan testPlan = new TestPlan("User Profile Test Plan");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, ArgumentsPanel.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Crear grupo de hilos para perfil de usuario
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Profile Thread Group");
        threadGroup.setNumThreads(25); // Menos usuarios para endpoint autenticado
        threadGroup.setRampUp(5);
        
        LoopController loopController = new LoopController();
        loopController.setLoops(3);
        loopController.setFirst(true);
        threadGroup.setSamplerController(loopController);

        // Configurar sampler HTTP para perfil
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8083);
        httpSampler.setPath("/api/users/profile");
        httpSampler.setMethod("GET");
        // Headers para autenticación
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("Authorization", "Bearer test-token"));
        headerManager.add(new Header("Content-Type", "application/json"));
        httpSampler.setHeaderManager(headerManager);
        httpSampler.setName("Profile Request");
        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        // Crear árbol de prueba
        HashTree testPlanTree = new HashTree();
        testPlanTree.add(testPlan);
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(httpSampler);
        threadGroupHashTree.add(headerManager);

        // Modificar esta parte para usar nuestro custom listener
        SummariserListener listener = new SummariserListener();
        ResultCollector resultCollector = new ResultCollector(listener);
        testPlanTree.add(testPlanTree.getArray()[0], resultCollector);

        // Ejecutar prueba
        Instant startTime = Instant.now();
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        jmeter.configure(testPlanTree);
        jmeter.run();
        Duration duration = Duration.between(startTime, Instant.now());

        // Usar el listener personalizado para obtener estadísticas
        long averageResponseTime = listener.getTotal() / listener.getNum();
        assertTrue(averageResponseTime < MAX_RESPONSE_TIME, 
            "Tiempo de respuesta promedio (" + averageResponseTime + 
            "ms) excede el máximo permitido (" + MAX_RESPONSE_TIME + "ms)");
 
        System.out.println("Test de perfil completado en: " + duration.toSeconds() + " segundos");
        System.out.println("Tiempo de respuesta promedio: " + averageResponseTime + "ms");
        System.out.println("Total de solicitudes de perfil: " + listener.getNum());
        System.out.println("Errores en solicitudes de perfil: " + listener.getErrorCount());
    }
}
