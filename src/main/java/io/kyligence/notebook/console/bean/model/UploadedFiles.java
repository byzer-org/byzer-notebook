package io.kyligence.notebook.console.bean.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UploadedFiles {

    private List<FileRecord> files;

    private Double totalSize;
    
    @Data
    @NoArgsConstructor
    public static class FileRecord{
        private String fileName;

        private Double fileSize;

        public static FileRecord valueOf(String fileName, Double fileSize) {
            FileRecord record = new FileRecord();
            record.setFileName(fileName);
            record.setFileSize(fileSize);
            return record;
        }
    }
    
    
}
