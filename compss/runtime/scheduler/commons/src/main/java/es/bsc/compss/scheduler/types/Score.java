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
package es.bsc.compss.scheduler.types;

import java.util.Set;

import es.bsc.compss.comm.Comm;
import es.bsc.compss.types.TaskDescription;
import es.bsc.compss.types.annotations.parameter.Direction;
import es.bsc.compss.types.data.DataAccessId;
import es.bsc.compss.types.data.DataInstanceId;
import es.bsc.compss.types.data.LogicalData;
import es.bsc.compss.types.parameter.DependencyParameter;
import es.bsc.compss.types.parameter.Parameter;
import es.bsc.compss.types.resources.Resource;
import es.bsc.compss.types.resources.Worker;

/**
 * Action score representation
 *
 */
public class Score implements Comparable<Score> {

    protected long actionScore; // Action Priority
    protected long resourceScore; // Resource Priority
    protected long waitingScore; // Resource Blocked Priority
    protected long implementationScore; // Implementation Priority

    /**
     * Constructor
     *
     * @param actionScore
     * @param waiting
     * @param res
     * @param impl
     */
    public Score(long actionScore, long res, long waiting, long impl) {
        this.actionScore = actionScore;
        this.resourceScore = res;
        this.waitingScore = waiting;
        this.implementationScore = impl;
    }

    /**
     * Clone
     *
     * @param clone
     */
    public Score(Score clone) {
        this.actionScore = clone.actionScore;
        this.resourceScore = clone.resourceScore;
        this.waitingScore = clone.waitingScore;
        this.implementationScore = clone.implementationScore;
    }

    /**
     * Returns the action priority
     *
     * @return
     */
    public long getActionScore() {
        return this.actionScore;
    }

    /**
     * Returns the estimated time of wait in the resource
     *
     * @return
     */
    public long getWaitingScore() {
        return this.waitingScore;
    }

    /**
     * Returns the score of the resource (number of data in that resource)
     *
     * @return
     */
    public long getResourceScore() {
        return this.resourceScore;
    }

    /**
     * Returns the implementation score
     *
     * @return
     */
    public long getImplementationScore() {
        return this.implementationScore;
    }

    /**
     * Checks whether a score is better than another. Returns true if @a is
     * better than @b
     *
     * @param a
     * @param b
     * @return
     */
    public static final boolean isBetter(Score a, Score b) {
        if (a == null) {
            return false;
        }
        if (b == null) {
            return true;
        }
        return a.isBetter(b);
    }

    /**
     * Checks if the current score is better than the given. Returns true if
     *
     * @implicit is better than @other
     *
     * @param other
     * @return
     */
    public boolean isBetter(Score other) {
        if (this.actionScore != other.actionScore) {
            return this.actionScore > other.actionScore;
        }
        if (this.resourceScore != other.resourceScore) {
            return this.resourceScore > other.resourceScore;
        }
        if (this.waitingScore != other.waitingScore) {
            return this.waitingScore > other.waitingScore;
        }
        return this.implementationScore > other.implementationScore;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Long.hashCode(actionScore);
        result = 31 * result + Long.hashCode(resourceScore);
        result = 31 * result + Long.hashCode(waitingScore);
        result = 31 * result + Long.hashCode(implementationScore);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Score) {
            Score other = (Score) obj;
            return (this.actionScore == other.actionScore && this.resourceScore == other.resourceScore
                    && this.waitingScore == other.waitingScore && this.implementationScore == other.implementationScore);
        }

        return false;
    }

    @Override
    public int compareTo(Score other) {
        if (this.equals(other)) {
            return 0;
        } else if (this.isBetter(other)) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Calculates the number of Parameters in @params located in a given worker
     *
     * @w.
     *
     * @param params
     * @param w
     * @return
     */
    public static long calculateDataLocalityScore(TaskDescription params, Worker<?> w) {
        long resourceScore = 0;
        if (params != null) {
            Parameter[] parameters = params.getParameters();

            // Obtain the scores for the host: number of task parameters that
            // are located in the host
            for (Parameter p : parameters) {
                if (p instanceof DependencyParameter && p.getDirection() != Direction.OUT) {
                    DependencyParameter dp = (DependencyParameter) p;
                    DataInstanceId dId = null;
                    switch (dp.getDirection()) {
                        case IN:
                            DataAccessId.RAccessId raId = (DataAccessId.RAccessId) dp.getDataAccessId();
                            dId = raId.getReadDataInstance();
                            break;
                        case INOUT:
                            DataAccessId.RWAccessId rwaId = (DataAccessId.RWAccessId) dp.getDataAccessId();
                            dId = rwaId.getReadDataInstance();
                            break;
                        case OUT:
                            // Cannot happen because of previous if
                            break;
                    }

                    // Get hosts for resource score
                    if (dId != null) {
                        LogicalData dataLD = Comm.getData(dId.getRenaming());
                        if (dataLD != null) {
                            Set<Resource> hosts = dataLD.getAllHosts();
                            for (Resource host : hosts) {
                                if (host == w) {
                                    resourceScore++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return resourceScore;
    }

    @Override
    public String toString() {
        return "[Score = ["
                + "action:" + this.actionScore + ", "
                + "resource:" + this.resourceScore + ", "
                + "load:" + this.waitingScore + ", "
                + "implementation:" + this.implementationScore
                + "]" + "]";
    }

}
