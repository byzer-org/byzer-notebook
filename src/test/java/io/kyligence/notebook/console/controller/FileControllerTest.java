package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.IdNameTypeDTO;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FileControllerTest extends BaseControllerTest {

    private static final String EXPORT_CONTENT_TYPE = "application/xml;charset=UTF-8";
    private static final String TEST_IMPORT_NOTEBOOK = "test_import_notebook.bznb";

    @Data
    private static class ImportResponseDTO {
        private String code;
        private List<IdNameTypeDTO> data;
        private String msg;
    }

    @Override
    public String getCollectionName() {
        return "files";
    }

    @Before
    public void prepareNotebook() throws Exception {
        importNotebook();
    }

    private ResultActions importNotebook() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", TEST_IMPORT_NOTEBOOK, "application/json", getInputStream(TEST_IMPORT_NOTEBOOK));
        return this.mvc.perform(
                MockMvcRequestBuilders.multipart("/api/file/import")
                        .file(file)
                        .contentType(DEFAULT_CONTENT_TYPE)
                        .header(DEFAULT_AUTH_HEADER, DEFAULT_AUTH_TOKEN)
        );
    }

    @Test
    public void testImportNotebook() throws Exception {
        String content = importNotebook().andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ImportResponseDTO result = JacksonUtils.readJson(content, ImportResponseDTO.class);
        Assert.assertNotNull(result);
        List<IdNameTypeDTO> listDTO = result.getData();
        Assert.assertEquals(1, listDTO.size());
    }

    @Test
    public void testExportNotebook() throws Exception {
        this.mvc.perform(
                MockMvcRequestBuilders
                        .get("/api/file/export/27?type=notebook")
                        .header(DEFAULT_AUTH_HEADER, DEFAULT_AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(EXPORT_CONTENT_TYPE));
    }

    @Test
    public void testExportByzerScript() throws Exception {
        this.mvc.perform(
                MockMvcRequestBuilders
                        .get("/api/file/export/27?type=notebook&output=byzer")
                        .header(DEFAULT_AUTH_HEADER, DEFAULT_AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(EXPORT_CONTENT_TYPE));
    }
}
