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
import es.bsc.compss.log.Loggers;
import es.bsc.compss.types.request.Request;
import es.bsc.compss.types.request.exceptions.ShutdownException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The TPRequest class represents any interaction with the TaskProcessor
 * component.
 */
public abstract class APRequest extends Request {

    protected static final Logger LOGGER = LogManager.getLogger(Loggers.TP_COMP);

    /**
     * Contains the different types of request that the Access Processor can
     * response.
     */
    public enum APRequestType {
        ANALYSE_TASK,
        UPDATE_GRAPH,
        WAIT_FOR_TASK,
        WAIT_FOR_ALL_TASKS,
        END_OF_APP,
        ALREADY_ACCESSED,
        REGISTER_DATA_ACCESS,
        TRANSFER_OPEN_FILE,
        TRANSFER_RAW_FILE,
        TRANSFER_OBJECT,
        NEW_VERSION_SAME_VALUE,
        IS_OBJECT_HERE,
        SET_OBJECT_VERSION_VALUE,
        GET_LAST_RENAMING,
        BLOCK_AND_GET_RESULT_FILES,
        UNBLOCK_RESULT_FILES,
        SHUTDOWN,
        GRAPHSTATE,
        TASKSTATE,
        DELETE_FILE,
        FINISH_ACCESS_FILE,
        DEBUG,
        DEREGISTER_OBJECT
    }

    /**
     * Returns the type of request for this instance
     *
     * @return returns the request type name of this instance
     * @result returns the request type name of this instance
     *
     */
    public abstract APRequestType getRequestType();

    /**
     * Processes the Request
     *
     * @param ap AccessProcessor processing the request
     * @param ta Task Analyser of the processing AccessProcessor
     * @param dip DataInfoProvider of the processing AccessProcessor
     * @param td Task Dispatcher attached to the processing AccessProcessor
     * @throws compss.types.request.exceptions.ShutdownException
     */
    public abstract void process(AccessProcessor ap, TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td)
            throws ShutdownException;

}
