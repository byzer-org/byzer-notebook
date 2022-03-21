package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.NotebookLauncherTestBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

public class BaseControllerTest extends NotebookLauncherTestBase {
    protected static final String DEFAULT_CONTENT_TYPE = "application/json";
    protected static final String DEFAULT_AUTH_HEADER = "Authorization";
    protected static final String DEFAULT_AUTH_TOKEN = "Basic YWRtaW46YWRtaW4=";
    private static final String MOCK_DATA_DIR = "src/test/resources/mock_data";

    protected static String loadResource(String resource) {
        try (InputStream inputStream = FileUtils.openInputStream(new File(resource))) {
            return IOUtils.toString(Objects.requireNonNull(inputStream), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getResponseContent(String instanceName) {
        String jsonPath = MOCK_DATA_DIR + "/" + getCollectionName() + "/" + instanceName + "_resp.json";
        return loadResource(jsonPath);
    }

    protected String getRequestContent(String instanceName) {
        String jsonPath = MOCK_DATA_DIR + "/" + getCollectionName() + "/" + instanceName + "_req.json";
        return loadResource(jsonPath);
    }

    protected InputStream getInputStream(String instanceName) throws IOException {
        String path = System.getProperty("NOTEBOOK_HOME") + "/" +
                MOCK_DATA_DIR + "/" + getCollectionName() + "/" + instanceName;
        return FileUtils.openInputStream(new File(path));
    }

    protected String getCollectionName() {
        throw new UnsupportedOperationException();
    }

}
