package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.bean.dto.IdNameTypeDTO;
import io.kyligence.notebook.console.tools.ImportResponseDTO;
import io.kyligence.notebook.console.util.JacksonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FileControllerTest extends NotebookLauncherBaseTest {

    private static final String EXPORT_CONTENT_TYPE = "application/xml;charset=UTF-8";
    private static final String TEST_IMPORT_NOTEBOOK = "test_import_notebook.bznb";

    @Override
    public String getCollectionName() {
        return "files";
    }

    @Test
    public void testImportNotebook() throws Exception {
        String content = importNotebook(TEST_IMPORT_NOTEBOOK).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ImportResponseDTO result = JacksonUtils.readJson(content, ImportResponseDTO.class);
        Assert.assertNotNull(result);
        List<IdNameTypeDTO> listDTO = result.getData();
        Assert.assertEquals(1, listDTO.size());
    }

    @Test
    public void testExportNotebook() throws Exception {
        this.mvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/api/file/export/%s?type=notebook", defaultMockNotebookId))
                        .header(DEFAULT_AUTH_HEADER, DEFAULT_AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(EXPORT_CONTENT_TYPE));
    }

    @Test
    public void testExportByzerScript() throws Exception {
        this.mvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/api/file/export/%s?type=notebook&output=byzer", defaultMockNotebookId))
                        .header(DEFAULT_AUTH_HEADER, DEFAULT_AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(EXPORT_CONTENT_TYPE));
    }
}
