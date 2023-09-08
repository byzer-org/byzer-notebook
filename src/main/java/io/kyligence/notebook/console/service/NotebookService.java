package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.CellInfoDTO;
import io.kyligence.notebook.console.bean.dto.CodeSuggestDTO;
import io.kyligence.notebook.console.bean.dto.ExecFileDTO;
import io.kyligence.notebook.console.bean.dto.NotebookDTO;
import io.kyligence.notebook.console.bean.dto.req.CodeSuggestionReq;
import io.kyligence.notebook.console.bean.entity.*;
import io.kyligence.notebook.console.controller.NotebookHelper;
import io.kyligence.notebook.console.dao.*;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ByzerIgnoreException;
import io.kyligence.notebook.console.exception.EngineAccessException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.scalalib.hint.HintManager;
import io.kyligence.notebook.console.support.CriteriaQueryBuilder;
import io.kyligence.notebook.console.util.EntityUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Query;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotebookService implements FileInterface {

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private NotebookCommitRepository notebookCommitRepository;

    @Autowired
    private CellInfoRepository cellInfoRepository;

    @Autowired
    private CellCommitRepository cellCommitRepository;

    @Autowired
    private SharedFileRepository sharedFileRepository;

    @Autowired
    private CriteriaQueryBuilder criteriaQueryBuilder;

    @Autowired
    private NotebookHelper notebookHelper;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private EngineService engineService;

    private static final NotebookConfig config = NotebookConfig.getInstance();

    @Transactional
    public NotebookInfo create(String user, String name, Integer folderId) {
        if (isNotebookExist(user, name, folderId)) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_NOTEBOOK_NAME);
        }

        long currentTimeStamp = System.currentTimeMillis();
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setName(name);
        notebookInfo.setUser(user);
        notebookInfo.setCreateTime(new Timestamp(currentTimeStamp));
        notebookInfo.setUpdateTime(new Timestamp(currentTimeStamp));
        notebookInfo.setFolderId(folderId);
        notebookInfo = notebookRepository.save(notebookInfo);

        // create initial cell
        CellInfo cellInfo = new CellInfo();
        cellInfo.setNotebookId(notebookInfo.getId());
        cellInfo.setCreateTime(new Timestamp(currentTimeStamp));
        cellInfo.setUpdateTime(new Timestamp(currentTimeStamp));
        save(cellInfo);

        // update notebook cell list
        notebookInfo.setCellList("[ " + cellInfo.getId() + " ]");
        return notebookRepository.save(notebookInfo);
    }


    @Transactional
    public NotebookCommit commit(String user, Integer notebookId) {
        NotebookInfo notebookInfo = this.findById(notebookId);
        checkExecFileAvailable(user, notebookInfo, null);

        String commitId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        NotebookCommit notebookCommit = new NotebookCommit();
        notebookCommit.setCommitId(commitId);
        notebookCommit.setNotebookId(notebookId);
        notebookCommit.setName(notebookInfo.getName());
        notebookCommit.setCellList(notebookInfo.getCellList());
        notebookCommit.setCreateTime(new Timestamp(timestamp));

        notebookCommit = notebookCommitRepository.save(notebookCommit);

        List<CellInfo> cells = this.getCellInfos(notebookId);

        cells.forEach(
                cellInfo -> {
                    CellCommit cellCommit = new CellCommit();
                    cellCommit.setCommitId(commitId);
                    cellCommit.setContent(cellInfo.getContent());
                    cellCommit.setLastJobId(cellInfo.getLastJobId());
                    cellCommit.setNotebookId(notebookId);
                    cellCommit.setCellId(cellInfo.getId());
                    cellCommit.setCreateTime(new Timestamp(timestamp));
                    cellCommitRepository.save(cellCommit);
                }
        );

        return notebookCommit;
    }

    public List<NotebookCommit> listCommits(String user, Integer notebookId) {
        NotebookInfo notebookInfo = this.findById(notebookId);
        checkExecFileAvailable(user, notebookInfo, null);
        return notebookCommitRepository.listCommit(notebookId);
    }

    public void deleteCell(CellInfo cellInfo) {
        cellInfoRepository.delete(cellInfo.getId(), cellInfo.getNotebookId());
    }

    public NotebookInfo getDefault(String user) {
        List<NotebookInfo> defaultNotebooks = notebookRepository.findByType(user, "default");
        return defaultNotebooks.isEmpty() ? null : defaultNotebooks.get(0);
    }

    public NotebookCommit getDefaultDemo() {
        List<SharedFileInfo> demos = sharedFileRepository.findByOwner("admin").stream().filter(
                sharedFileInfo -> sharedFileInfo.getEntityType().equalsIgnoreCase("notebook")
        ).collect(Collectors.toList());
        if (demos.isEmpty()) return null;

        return findCommit(demos.get(0).getEntityId(), demos.get(0).getCommitId());
    }

    public CellInfo save(CellInfo cellInfo) {
        return cellInfoRepository.save(cellInfo);
    }

    public List<CellInfo> getCellInfos(Integer notebookId) {
        return cellInfoRepository.findByNotebook(notebookId);
    }

    public List<CellInfo> getCellInfos(String cellIds) {
        List<Integer> idList = Arrays.stream(cellIds.replace("[", "").replace("]", "").split(","))
                .map(String::trim).map(Integer::valueOf).collect(Collectors.toList());
        List<CellInfo> cellList = new ArrayList<>();
        for (Integer id : idList) {
            Optional<CellInfo> cellInfo = cellInfoRepository.findById(id);
            if (!cellInfo.isPresent()) {
                log.error("get cell(id:${}) info error, not found!", id);
            }
            cellList.add(cellInfo.get());
        }
        return cellList;
    }

    public List<CellCommit> getCommittedCellInfos(Integer notebookId, String commitId) {
        return cellCommitRepository.findByCommit(notebookId, commitId);
    }

    public CellInfo getCellInfo(Integer cellId) {
        return cellInfoRepository.findById(cellId).orElse(null);
    }


    public boolean isNotebookExist(String user, String name, Integer folderId) {
        NotebookInfo notebookInfo = find(user, name, folderId);
        // mysql search ignore case
        return notebookInfo != null && notebookInfo.getName().equals(name);
    }

    @Transactional
    public void updateById(ExecFileInfo execFileInfo) {
        Query query = criteriaQueryBuilder.updateNotNullByField((NotebookInfo) execFileInfo, "id");
        query.executeUpdate();
    }

    public void updateCellContent(CellInfo cellInfo) {
        cellInfoRepository.updateCellContent(cellInfo.getId(), cellInfo.getNotebookId(), cellInfo.getContent());
    }

    public void updateCellJobId(CellInfo cellInfo) {
        cellInfoRepository.updateCellJobId(cellInfo.getId(), cellInfo.getLastJobId());
    }


    public NotebookInfo findById(Integer id) {
        return notebookRepository.findById(id).orElse(null);
    }

    public NotebookInfo find(String user, String name, Integer folderId) {
        Map<String, String> filters = new HashMap<>();
        filters.put("user", user);
        filters.put("name", name);
        filters.put("folderId", EntityUtils.toStr(folderId));
        Query query = criteriaQueryBuilder.getAll(NotebookInfo.class, true, null, 1, 0, null, null, filters, null);
        List<NotebookInfo> notebooks = query.getResultList();
        if (notebooks == null || notebooks.size() == 0) {
            return null;
        }
        return notebooks.get(0);
    }

    public NotebookInfo save(NotebookInfo notebookInfo) {
        return notebookRepository.save(notebookInfo);
    }

    @Transactional
    public void delete(Integer id) {
        if (schedulerService.entityUsedInSchedule("notebook", id)) {
            throw new ByzerException("Notebook used in schedule");
        }

        if (isDemo(id)) {
            throw new ByzerException("Notebook has been shared with other users");
        }

        // 1. delete notebook info
        notebookRepository.deleteById(id);
        notebookCommitRepository.deleteByNotebook(id);
        // 2. delete notebook cells
        cellInfoRepository.deleteByNotebook(id);
        cellCommitRepository.deleteByNotebook(id);
    }

    public List<NotebookInfo> find(String user) {
        return notebookRepository.find(user);
    }

    public List<NotebookInfo> findAll() {
        return notebookRepository.findAll();
    }

    private boolean isDemo(Integer notebookId) {
        return !sharedFileRepository.findByEntity("admin", notebookId, "notebook").isEmpty();
    }

    private boolean isDemo(Integer notebookId, String commitId) {
        return !sharedFileRepository.findByCommit("admin", notebookId, "notebook", commitId).isEmpty();
    }

    @Override
    public void checkExecFileAvailable(String user, ExecFileInfo execFileInfo, String commitId) {
        if (execFileInfo == null) {
            throw new ByzerException(ErrorCodeEnum.NOTEBOOK_NOT_EXIST);
        }
        // user can access demo commits
        if (Objects.nonNull(commitId) && !commitId.isEmpty() && isDemo(execFileInfo.getId(), commitId)) return;

        if (!user.equalsIgnoreCase(execFileInfo.getUser()) && !user.equalsIgnoreCase("admin")) {
            throw new ByzerException(ErrorCodeEnum.NOTEBOOK_NOT_AVAILABLE);
        }
    }

    @Override
    public boolean isExecFileExist(String user, String name, Integer folderId) {
        return isNotebookExist(user, name, folderId);
    }

    @Override
    public ExecFileDTO analyzeFile(MultipartFile file) throws IOException {
        return JacksonUtils.readJson(file.getBytes(), NotebookDTO.class);
    }

    @Override
    public ExecFileInfo importExecFile(ExecFileDTO execFileDTO, Integer folderId) {
        return notebookHelper.importNotebook((NotebookDTO) execFileDTO, folderId);
    }

    @Override
    public ExecFileDTO getFile(Integer id, String user) {
        return getNotebook(id, user);
    }

    public NotebookDTO getNotebook(Integer notebookId, String user) {
        NotebookInfo notebookInfo = this.findById(notebookId);
        checkExecFileAvailable(user, notebookInfo, null);

        List<Integer> cellIds = null;
        List<CellInfo> cellInfos = null;
        String cellList = notebookInfo.getCellList();
        if (cellList != null && !cellList.isEmpty() && !cellList.equals("[]")) {
            cellIds = JacksonUtils.readJsonArray(cellList, Integer.class);
            // get notebook cells
            cellInfos = this.getCellInfos(notebookId);
        }
        NotebookDTO dto = NotebookDTO.valueOf(notebookInfo, cellIds, cellInfos);

        if (user.equalsIgnoreCase("admin") && isDemo(notebookId)) {
            dto.setIsDemo(true);
        }

        return dto;
    }

    public NotebookDTO getNotebook(Integer notebookId, String user, String commitId) {
        if (Objects.isNull(commitId) || commitId.isEmpty()) {
            return getNotebook(notebookId, user);
        }

        NotebookInfo notebookInfo = this.findById(notebookId);
        checkExecFileAvailable(user, notebookInfo, commitId);

        NotebookCommit notebookCommit = findCommit(notebookId, commitId);

        List<Integer> cellIds = null;
        List<CellCommit> cellInfos = null;
        String cellList = notebookCommit.getCellList();
        if (cellList != null && !cellList.isEmpty() && !cellList.equals("[]")) {
            cellIds = JacksonUtils.readJsonArray(cellList, Integer.class);
            // get notebook cells
            cellInfos = this.getCommittedCellInfos(notebookId, commitId);
        }

        NotebookDTO dto = NotebookDTO.valueOf(notebookInfo, cellIds, notebookCommit, cellInfos);
        if (isDemo(notebookId, commitId)) dto.setIsDemo(true);
        return dto;
    }

    public void checkResourceLimit(String user, Integer newResourceNum) {
        Integer limit = config.getUserNoteBookNumLimit();
        if (limit > 0 && notebookRepository.getUserNotebookCount(user) + newResourceNum > limit) {
            if (config.getIsTrial()) {
                throw new ByzerException(ErrorCodeEnum.NOTEBOOK_NUM_REACH_LIMIT,
                        String.format(
                                ("The online trial version supports up to %1$s notebooks. " +
                                        "Please contact us if you need more help."),
                                limit
                        )
                );
            }
            throw new ByzerException(ErrorCodeEnum.NOTEBOOK_NUM_REACH_LIMIT);
        }
    }

    public List<CodeSuggestDTO> getCodeSuggestion(CodeSuggestionReq params) {
        String result;
        boolean suggestEnable = config.getSuggestEnable();
        if (suggestEnable) {
            try {
                String user = WebUtils.getCurrentLoginUser();
                result = engineService.runAutoSuggest(
                        new EngineService.RunScriptParams()
                                .withSql(params.getSql())
                                .withOwner(user)
                                .with("lineNum", params.getLineNum().toString())
                                .with("columnNum", params.getColumnNum().toString())
                                .with("isDebug", params.getIsDebug().toString())
                                .withAsync("false")
                );
            } catch (ByzerException e) {
                throw new ByzerIgnoreException(e);
            } catch (EngineAccessException e) {
                throw new EngineAccessException(ErrorCodeEnum.ENGINE_ACCESS_EXCEPTION, e);
            }
            List<CodeSuggestDTO> codeSuggests = JacksonUtils.readJsonArray(result, CodeSuggestDTO.class);
            return codeSuggests;
        } else {
            return null;
        }
    }

    public String getNotebookScripts(String user, Integer notebookId, String commitId, Map<String, String> options) {
        NotebookDTO notebook = getNotebook(notebookId, user, commitId);
        List<String> scripts = notebook.getCellList().stream().map(CellInfoDTO::getContent)
                .filter(sql -> Objects.nonNull(sql) && !sql.startsWith("--%markdown"))
                .map(sql -> HintManager.applyAllHintRewrite(sql, options))
                .collect(Collectors.toList());
        return String.join(System.lineSeparator(), scripts);
    }

    public NotebookCommit findCommit(Integer notebookId, String commitId) {
        List<NotebookCommit> notebookCommits = notebookCommitRepository.findByCommit(notebookId, commitId);
        if (notebookCommits.isEmpty()) {
            throw new ByzerException("Commit don't exist");
        }
        return notebookCommits.get(0);

    }
}
