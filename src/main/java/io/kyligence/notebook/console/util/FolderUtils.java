package io.kyligence.notebook.console.util;

public class FolderUtils {

    public static String getParentFolder(String folder) {
        if (folder == null || folder.length() == 0) {
            return null;
        }

        if (folder.indexOf('.') == -1) {
            return null;
        }

        return folder.substring(0, folder.lastIndexOf('.'));
    }

    public static String getRelativePath(String folder, String baseDir) {
        if (folder == null || folder.length() == 0) {
            return null;
        }
        if (baseDir == null) {
            return folder;
        }
        if (folder.startsWith(baseDir)) {
            return folder.substring(baseDir.length() + 1);
        }
        return null;
    }

    public static int getFolderDepth(String folder) {
        if (folder == null || folder.length() == 0) {
            return 0;
        }

        String[] folderParts = folder.split("[.]");
        return folderParts.length;
    }

    public static String getFolderName(String path) {
        int depth = getFolderDepth(path);

        if (depth == 0) {
            return null;
        }

        if (depth == 1) {
            return path;
        }

        if (depth > 1) {
            return path.substring(path.lastIndexOf('.') + 1);
        }
        return null;
    }
}
