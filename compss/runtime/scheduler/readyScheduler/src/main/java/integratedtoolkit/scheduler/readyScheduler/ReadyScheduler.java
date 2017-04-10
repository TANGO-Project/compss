package integratedtoolkit.scheduler.readyScheduler;

import integratedtoolkit.components.impl.ResourceScheduler;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.scheduler.exceptions.BlockedActionException;
import integratedtoolkit.scheduler.exceptions.UnassignedActionException;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.scheduler.types.Profile;
import integratedtoolkit.scheduler.types.Score;
import integratedtoolkit.types.implementations.Implementation;
import integratedtoolkit.types.resources.WorkerResourceDescription;
import integratedtoolkit.util.ActionSet;

import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Representation of a Scheduler that considers only ready tasks
 *
 * @param <P>
 * @param <T>
 * @param <I>
 */
public abstract class ReadyScheduler<P extends Profile, T extends WorkerResourceDescription, I extends Implementation<T>>
        extends TaskScheduler<P, T, I> {

    // Logger
    protected static final Logger LOGGER = LogManager.getLogger(Loggers.TS_COMP);

    protected final ActionSet<P, T, I> dependingActions;
    protected final ActionSet<P, T, I> unassignedReadyActions;


    /**
     * Constructs a new Ready Scheduler instance
     * 
     */
    public ReadyScheduler() {
        super();

        this.dependingActions = new ActionSet<>();
        this.unassignedReadyActions = new ActionSet<>();
    }

    /*
     * *********************************************************************************************************
     * *********************************************************************************************************
     * ***************************** UPDATE STRUCTURES OPERATIONS **********************************************
     * *********************************************************************************************************
     * *********************************************************************************************************
     */

    @Override
    public void workerLoadUpdate(ResourceScheduler<P, T, I> resource) {
        /*
        LOGGER.info("[ReadyScheduler] WorkerLoad update on resource " + resource.getName());

        // When a worker's load has been modified we can try to schedule unassigned ready actions
        // Obtain and sort ready actions
        LinkedList<AllocatableAction<P, T, I>> candidates = this.unassignedReadyActions.removeAllCompatibleActions(resource.getResource());
        LOGGER.info("[ReadyScheduler] There are " + candidates.size() + " compatible actions");
        PriorityQueue<ObjectValue<AllocatableAction<P, T, I>>> executionCandidates = new PriorityQueue<>();
        for (AllocatableAction<P, T, I> action : candidates) {
            Score actionScore = generateActionScore(action);
            Score resourceScore = action.schedulingScore(resource, actionScore);
            ObjectValue<AllocatableAction<P, T, I>> obj = new ObjectValue<>(action, resourceScore);
            executionCandidates.add(obj);
        }
        LOGGER.info("[ReadyScheduler] There are " + executionCandidates.size() + " executionCandidates");
        // Try to schedule and launch ready actions
        while (!executionCandidates.isEmpty()) {
            ObjectValue<AllocatableAction<P, T, I>> obj = executionCandidates.poll();
            AllocatableAction<P, T, I> action = obj.getObject();
            Score actionScore = obj.getScore();
            try {
                scheduleAction(action, actionScore);
                tryToLaunch(action);
            } catch (BlockedActionException e) {
                removeFromReady(action);
                addToBlocked(action);
            }
        }
        */
    }

    @Override
    public abstract Score generateActionScore(AllocatableAction<P, T, I> action);

    /*
     * *********************************************************************************************************
     * *********************************************************************************************************
     * ********************************* SCHEDULING OPERATIONS *************************************************
     * *********************************************************************************************************
     * *********************************************************************************************************
     */
    @Override
    protected void scheduleAction(AllocatableAction<P, T, I> action, Score actionScore) throws BlockedActionException {
        if (action.hasDataPredecessors()) {
            this.dependingActions.addAction(action);
        } else {
            try {
                action.schedule(actionScore);
            } catch (UnassignedActionException ex) {
                this.unassignedReadyActions.addAction(action);
            }
        }
    }

    protected void scheduleAction(AllocatableAction<P, T, I> action, ResourceScheduler<P, T, I> targetWorker, Score actionScore)
            throws BlockedActionException, UnassignedActionException {

        if (action.hasDataPredecessors()) {
            this.dependingActions.addAction(action);
        } else {
            action.schedule(targetWorker, actionScore);
        }
    }

    @Override
    public abstract void handleDependencyFreeActions(LinkedList<AllocatableAction<P, T, I>> executionCandidates,
            LinkedList<AllocatableAction<P, T, I>> blockedCandidates, ResourceScheduler<P, T, I> resource);

    @Override
    public LinkedList<AllocatableAction<P, T, I>> getUnassignedActions() {
        return unassignedReadyActions.getAllActions();
    }
}
