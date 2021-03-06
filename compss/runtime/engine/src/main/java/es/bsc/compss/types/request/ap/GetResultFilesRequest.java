/*         
 *  Copyright 2002-2018 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package es.bsc.compss.types.request.ap;

import es.bsc.compss.components.impl.AccessProcessor;
import es.bsc.compss.components.impl.DataInfoProvider;
import es.bsc.compss.components.impl.TaskAnalyser;
import es.bsc.compss.components.impl.TaskDispatcher;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import es.bsc.compss.types.data.ResultFile;
import es.bsc.compss.types.data.operation.ResultListener;
import java.util.TreeSet;

public class GetResultFilesRequest extends APRequest {

    private Long appId;
    private Semaphore sem;

    private LinkedList<ResultFile> blockedData;

    public GetResultFilesRequest(Long appId, Semaphore sem) {
        this.appId = appId;
        this.sem = sem;
        blockedData = new LinkedList<>();
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Semaphore getSemaphore() {
        return sem;
    }

    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    public LinkedList<ResultFile> getBlockedData() {
        return blockedData;
    }

    @Override
    public void process(AccessProcessor ap, TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        ResultListener listener = new ResultListener(sem);
        TreeSet<Integer> writtenDataIds = ta.getAndRemoveWrittenFiles(this.appId);
        if (writtenDataIds != null) {
            for (int dataId : writtenDataIds) {
                ResultFile rf;
                rf = dip.blockDataAndGetResultFile(dataId, listener);
                if (rf == null) {
                    continue;
                }
                blockedData.add(rf);
            }
            listener.enable();
        } else {
            sem.release();
        }

    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.BLOCK_AND_GET_RESULT_FILES;
    }

}
