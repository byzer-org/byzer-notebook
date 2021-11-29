/*
 * Copyright (C) 2021 Kyligence Inc. All rights reserved.
 *
 * http://kyligence.io
 *
 * This software is the confidential and proprietary information of
 * Kyligence Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * Kyligence Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.kyligence.notebook.console.scheduler;

import io.kyligence.notebook.console.bean.dto.TaskInfoDTO;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import io.kyligence.notebook.console.scheduler.dolphin.dto.EntityModification;

import java.util.List;
import java.util.Map;

public interface RemoteSchedulerInterface {
    void createTask(String user, String name, String description, String entityType, Integer entityId, String entityName, ScheduleSetting scheduleSetting, Map<String, String> extraSettings);
    void deleteTask(String user, String projectName, Integer taskId);
    void updateTask(String user, Integer taskId, String name, String description, EntityModification modification, ScheduleSetting scheduleSetting, Map<String, String> extraSettings);
    TaskInfoDTO getTask(String projectName, String user, Integer taskId);
    TaskInfoDTO getTask(String projectName, String user, String entityType, Integer entityId);
    List<TaskInfoDTO> getTasks(String projectName, String user);
    void getTask(String user);
    void getTask();
    String getServiceName();

    TaskInfoDTO searchForEntity(String entityName, String entityType, Integer entityId);
}
