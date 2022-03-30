package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.bean.entity.UserAction;
import io.kyligence.notebook.console.bean.entity.UserInfo;
import io.kyligence.notebook.console.bean.model.UploadedFiles;
import io.kyligence.notebook.console.dao.UserActionRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.support.EncryptUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;


public class UserServiceTest extends NotebookLauncherBaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserActionRepository userActionRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAuth() {
        Integer DEFAULT_ADMIN_ID = 1;
        Assert.assertEquals(DEFAULT_ADMIN_ID, userService.auth(DEFAULT_ADMIN_USER, "admin"));

        thrown.expect(ByzerException.class);
        thrown.expectMessage("Access Denied");

        userService.auth(DEFAULT_ADMIN_USER, "wrongpwd");
        userService.auth("not-exist-user", "pwd");
    }

    @Test
    public void testCreateUser() {
        UserInfo user = new UserInfo();

        user.setName("testUser1");
        user.setPassword(EncryptUtils.encrypt("pwdForUser1"));

        userService.createUser(user);

        thrown.expect(ByzerException.class);
        thrown.expectMessage("User Already Exist");

        userService.createUser(user);
    }

    @Test
    public void testUpdateUser() {
        UserInfo user = new UserInfo();

        user.setName("testUser2");
        user.setPassword(EncryptUtils.encrypt("pwdForUser2"));

        user = userService.createUser(user);

        user.setPassword(EncryptUtils.encrypt("pwdChanged"));
        UserInfo updated = userService.updateUser(user);

        Assert.assertEquals("pwdChanged", EncryptUtils.decrypt(updated.getPassword()));

    }

    @Test
    public void testGetUserAction() {
        UserAction userAction = new UserAction();
        userAction.setUser("userForGet");
        userAction.setOpenedNotebooks("[1,2,3]");
        userActionRepository.save(userAction);
        Assert.assertNotNull(userService.getUserAction("userForGet"));

        Assert.assertNull(userService.getUserAction("user-not-exist"));
    }

    @Test
    public void testSaveOpenedExecfiles() {
        userService.saveOpenedExecfiles("userForSave", "[1,2,3,4]");
        Assert.assertNotNull(userService.getUserAction("userForSave"));
    }

    @Test
    public void testUploadedFiles() {
        String user = "userForFile";
        Assert.assertNull(userService.getUploadedFileRecords("not-exist-user"));
        userService.removeUploadedFiles("not-exist-user", "not-exist-file");

        UserAction userAction = new UserAction();
        userAction.setUser(user);
        userActionRepository.save(userAction);

        Assert.assertNull(userService.getUploadedFileRecords(user));
        userService.removeUploadedFiles(user, "not-exist-file");


        userService.saveUploadedFiles(user, "1.csv", 2.);
        UploadedFiles saved = userService.getUploadedFileRecords(user);
        Assert.assertNotNull(saved);
        Assert.assertEquals(2, userService.getUploadedFileRecords(user).getTotalSize().intValue());

        userService.saveUploadedFiles(user, "2.csv", 4.);
        Assert.assertEquals(6, userService.getUploadedFileRecords(user).getTotalSize().intValue());

        userService.removeUploadedFiles(user, "1.csv");
        Assert.assertEquals(4, userService.getUploadedFileRecords(user).getTotalSize().intValue());

        userService.removeUploadedFiles(user, "2.csv");
        Assert.assertEquals(0, userService.getUploadedFileRecords(user).getTotalSize().intValue());

        userService.removeUploadedFiles(user, "3.csv");
    }

}
