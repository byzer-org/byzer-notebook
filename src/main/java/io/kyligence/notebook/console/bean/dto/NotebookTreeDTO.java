package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.ExecFileInfo;
import io.kyligence.notebook.console.bean.entity.NotebookCommit;
import io.kyligence.notebook.console.bean.entity.NotebookFolder;
import io.kyligence.notebook.console.bean.entity.WorkflowCommit;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class NotebookTreeDTO {

    @JsonProperty("id")
    public String id;

    @JsonProperty("folder_id")
    public String folderId;

    @JsonProperty("name")
    public String name;

    @JsonProperty("type")
    public String type;

    @JsonProperty("commit_id")
    public String commitId;

    @JsonProperty("is_demo")
    public Boolean isDemo;

    @JsonProperty("list")
    public List<NotebookTreeDTO> list;


    public static NotebookTreeDTO valueOfDemoFiles(List<NotebookCommit> notebookDemos, List<WorkflowCommit> workflowDemos){
        NotebookTreeDTO demoFolder = new NotebookTreeDTO();
        demoFolder.setFolderId("0");
        demoFolder.setName("OnlineDemos_Latest");
        demoFolder.setIsDemo(true);
        List<NotebookTreeDTO> demoList = notebookDemos.stream().map(
                demoNb -> {
                    NotebookTreeDTO entity = new NotebookTreeDTO();
                    entity.setId(demoNb.getNotebookId().toString());
                    entity.setCommitId(demoNb.getCommitId());
                    entity.setIsDemo(true);
                    entity.setName(demoNb.getName());
                    entity.setType("notebook");
                    return entity;
                }
        ).collect(Collectors.toList());

        demoList.addAll(workflowDemos.stream().map(
                demoWf -> {
                    NotebookTreeDTO entity = new NotebookTreeDTO();
                    entity.setId(demoWf.getWorkflowId().toString());
                    entity.setCommitId(demoWf.getCommitId());
                    entity.setIsDemo(true);
                    entity.setName(demoWf.getName());
                    entity.setType("workflow");
                    return entity;
                }
        ).collect(Collectors.toList()));
        demoFolder.setList(demoList);
        return demoFolder;
    }

    public static NotebookTreeDTO valueOf(List<ExecFileInfo> execFiles, List<NotebookFolder> folders) {
        NotebookTreeDTO notebookTree = buildFolderTree(folders);
        if (notebookTree == null) {
            // set all execFiles in root dir
            notebookTree = new NotebookTreeDTO();
            notebookTree.list = new ArrayList<>();
            if (execFiles == null) {
                return notebookTree;
            }

            for (ExecFileInfo execfile : execFiles) {
                NotebookTreeDTO child = new NotebookTreeDTO();
                child.id = EntityUtils.toStr(execfile.getId());
                child.name = execfile.getName();
                child.type = execfile.getType();
                notebookTree.list.add(child);
            }
            return notebookTree;
        }

        Set<Integer> allFolderIds = folders.stream()
                .map(NotebookFolder::getId)
                .collect(Collectors.toSet());


        Map<Integer, List<ExecFileInfo>> mapFolderId2ExecFile = new HashMap<>();

        if (execFiles != null) {
            for (ExecFileInfo execFile : execFiles) {
                Integer folderId = execFile.getFolderId();
                if (folderId == null || !allFolderIds.contains(folderId)) {
                    folderId = -1;
                }
                List<ExecFileInfo> execFileTestInfos = mapFolderId2ExecFile.get(folderId);
                if (execFileTestInfos == null) {
                    execFileTestInfos = new ArrayList<>();
                }
                execFileTestInfos.add(execFile);
                mapFolderId2ExecFile.put(folderId, execFileTestInfos);
            }
        }

        populateExecFilesById(notebookTree,mapFolderId2ExecFile, -1);
        populateExecFiles(notebookTree, mapFolderId2ExecFile);

        return notebookTree;
    }

    public NotebookTreeDTO subTree(String folderId) {
        if (folderId.equals(this.folderId)) {
            return this;
        }

        if (this.list != null) {
            for (NotebookTreeDTO subTree : this.list) {
                NotebookTreeDTO result = subTree.subTree(folderId);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private static void populateExecFilesById(NotebookTreeDTO notebookTree, Map<Integer, List<ExecFileInfo>> mapFolderId2ExecFile, Integer folderId) {
        List<ExecFileInfo> execFileTestInfos = mapFolderId2ExecFile.get(folderId);
        if (execFileTestInfos != null) {
            for (ExecFileInfo execFile : execFileTestInfos) {
                NotebookTreeDTO childNotebook = new NotebookTreeDTO();
                childNotebook.id = EntityUtils.toStr(execFile.getId());
                childNotebook.name = execFile.getName();
                childNotebook.type = execFile.getType();
                notebookTree.list.add(childNotebook);
            }
        }
    }

    private static void populateExecFiles(NotebookTreeDTO notebookTree, Map<Integer, List<ExecFileInfo>> mapFolderId2ExecFile) {
        // folder node
        if (notebookTree.id == null && notebookTree.name != null) {
            Integer folderId = Integer.valueOf(notebookTree.getFolderId());
            populateExecFilesById(notebookTree, mapFolderId2ExecFile, folderId);
        }

        // child node
        if (notebookTree.list != null) {
            for (NotebookTreeDTO childTree : notebookTree.list) {
                populateExecFiles(childTree, mapFolderId2ExecFile);
            }
        }
    }

    private static NotebookTreeDTO buildFolderTree(List<NotebookFolder> folders) {
        if (folders == null || folders.size() == 0) {
            return null;
        }

        List<NotebookFolder> rootFolders = new ArrayList<>();
        Map<String, List<NotebookFolder>> mapParent2Folders = new HashMap<>();

        folders.forEach(folder -> {
            String absolutePath = folder.getAbsolutePath();
            String[] folderParts = absolutePath.split("[.]");
            String parent;
            int depth = folderParts.length;
            if (depth == 1) {
                rootFolders.add(folder);
                return;
            }

            parent = absolutePath.substring(0, absolutePath.lastIndexOf('.'));
            List<NotebookFolder> foldersInParent = mapParent2Folders.get(parent);
            if (foldersInParent == null) {
                foldersInParent = new ArrayList<>();
            }
            foldersInParent.add(folder);
            mapParent2Folders.put(parent, foldersInParent);

        });

        // build folder by depth
        NotebookTreeDTO root = new NotebookTreeDTO();
        root.list = new ArrayList<>();
        for (NotebookFolder rootFolder : rootFolders) {
            NotebookTreeDTO rootTree = buildRootFolderTree(rootFolder, mapParent2Folders);
            root.list.add(rootTree);
        }

        return root;

    }

    private static NotebookTreeDTO buildRootFolderTree(NotebookFolder root, Map<String, List<NotebookFolder>> mapParent2Folders) {
        NotebookTreeDTO tree = new NotebookTreeDTO();
        tree.folderId = EntityUtils.toStr(root.getId());
        tree.name = root.getName();
        tree.list = new ArrayList<>();
        List<NotebookFolder> children = mapParent2Folders.get(root.getAbsolutePath());
        if (children == null) {
            return tree;
        }

        children.forEach(child -> {
            NotebookTreeDTO childTree = buildRootFolderTree(child, mapParent2Folders);
            tree.list.add(childTree);
        });
        return tree;
    }

}
