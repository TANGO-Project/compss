package integratedtoolkit.nio.master;

import integratedtoolkit.ITConstants;

import java.util.LinkedList;

import integratedtoolkit.api.ITExecution.ParamType;
import integratedtoolkit.nio.NIOParam;
import integratedtoolkit.nio.NIOTask;
import integratedtoolkit.nio.commands.Data;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.parameter.BasicTypeParameter;
import integratedtoolkit.types.parameter.DependencyParameter;
import integratedtoolkit.types.MethodImplementation;
import integratedtoolkit.types.parameter.Parameter;
import integratedtoolkit.types.TaskParams;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataAccessId.RAccessId;
import integratedtoolkit.types.data.DataAccessId.RWAccessId;
import integratedtoolkit.types.job.Job.JobListener.JobEndStatus;
import integratedtoolkit.types.resources.Resource;

public class NIOJob extends integratedtoolkit.types.job.Job<NIOWorkerNode> {

    private static final String workerClasspath = (System.getProperty(ITConstants.IT_WORKER_CP) != null
            && !System.getProperty(ITConstants.IT_WORKER_CP).equals(""))
            ? System.getProperty(ITConstants.IT_WORKER_CP) : "\"\"";
            
    private static final String workerPythonpath = (System.getProperty(ITConstants.IT_WORKER_PP) != null
            && !System.getProperty(ITConstants.IT_WORKER_PP).equals(""))
            ? System.getProperty(ITConstants.IT_WORKER_PP) : "\"\"";
            
    public NIOJob(int taskId, TaskParams taskParams, Implementation<?> impl, Resource res, JobListener listener) {
        super(taskId, taskParams, impl, res, listener);
    }

    @Override
    public String getHostName() {
        return worker.getName();
    }

    @Override
    public String toString() {
        MethodImplementation method = (MethodImplementation) this.impl;

        String className = method.getDeclaringClass();
        String methodName = taskParams.getName();

        return "NIOJob JobId" + this.jobId + " for method " + methodName + " at class " + className;
    }

    @Override
    public void submit() throws Exception {
        // Prepare the job
        logger.info("Submit NIOJob with ID " + jobId);

        NIOAdaptor.submitTask(this);
    }

    public NIOTask prepareJob() {
        MethodImplementation method = (MethodImplementation) this.impl;

        String className = method.getDeclaringClass();
        String methodName = taskParams.getName();
        boolean hasTarget = taskParams.hasTargetObject();

        LinkedList<NIOParam> params = addParams();

        int numParams = params.size();
        if (taskParams.hasReturnValue()) {
            numParams--;
        }
        
        // Merge command classpath and worker defined classpath
        String resourceClasspath = getResourceNode().getClasspath();
        String finalClasspath = workerClasspath;
        if (!resourceClasspath.equals("")) {
        	if (!finalClasspath.equals("")) {
        		finalClasspath += ":" + resourceClasspath;
        	} else {
        		finalClasspath = resourceClasspath;
        	}
        }
        
        // Merge pythonpath and worker defined pythonpath
        String resourcePythonpath = getResourceNode().getPythonpath();
        String finalPythonpath = workerPythonpath;
        if (!resourcePythonpath.equals("")) {
        	if (!finalPythonpath.equals("")) {
        		finalPythonpath += ":" + resourcePythonpath;
        	} else {
        		finalPythonpath = resourcePythonpath;
        	}
        }

        // Create NIOTask
        NIOTask nt = new NIOTask(lang, 
        						getResourceNode().getInstallDir(), 
        						getResourceNode().getLibPath(), 
        						getResourceNode().getAppDir(), 
        						finalClasspath,
        						finalPythonpath,
        						debug, 
        						className, 
        						methodName, 
        						hasTarget, 
        						params, 
        						numParams, 
        						taskId, 
        						this.taskParams.getId(), 
        						jobId, 
        						history, 
        						transferId
        					);

        return nt;
    }

    private LinkedList<NIOParam> addParams() {
        LinkedList<NIOParam> params = new LinkedList<NIOParam>();
        for (Parameter param : taskParams.getParameters()) {
            ParamType type = param.getType();
            NIOParam np;
            switch (type) {
                case FILE_T:
                case OBJECT_T:
                case SCO_T:
                case PSCO_T:
                    DependencyParameter dPar = (DependencyParameter) param;
                    DataAccessId dAccId = dPar.getDataAccessId();
                    Object value = dPar.getDataTarget();
                    boolean preserveSourceData = true;
                    if (dAccId instanceof RAccessId) {
                        // Parameter is a R, has sources
                        preserveSourceData = ((RAccessId) dAccId).isPreserveSourceData();
                    } else if (dAccId instanceof RWAccessId) {
                        // Parameter is a RW, has sources
                        preserveSourceData = ((RWAccessId) dAccId).isPreserveSourceData();
                    } else {
                        // Parameter is a W, it has no sources
                        preserveSourceData = false;
                    }

                    boolean writeFinalValue = !(dAccId instanceof RAccessId);	// Only store W and RW	                
                    np = new NIOParam(type, preserveSourceData, writeFinalValue, value, (Data) dPar.getDataSource());
                    break;
                default:
                    BasicTypeParameter btParB = (BasicTypeParameter) param;
                    value = btParB.getValue();
                    preserveSourceData = false;	// Basic parameters are not preserved on Worker
                    writeFinalValue = false;	// Basic parameters are not stored on Worker	                
                    np = new NIOParam(type, preserveSourceData, writeFinalValue, value, null);
            }

            params.add(np);
        }
        return params;
    }

    public JobKind getKind() {
        return JobKind.METHOD;
    }

    public void taskFinished(boolean successful) {
        if (successful) {
            listener.jobCompleted(this);
        } else {
            listener.jobFailed(this, JobEndStatus.EXECUTION_FAILED);
        }
    }

    @Override
    public void stop() throws Exception {
        //Do nothing
    }
    
}
