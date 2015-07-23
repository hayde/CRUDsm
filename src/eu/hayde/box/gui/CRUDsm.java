package eu.hayde.box.gui;

import eu.hayde.box.gui.CRUDModelListener.CRUD_ACTION_AUTOMATED_SAVE;
import eu.hayde.box.gui.CRUDModelListener.CRUD_ACTION_DELETE_OR_CANCEL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author can.senturk
 * @version 1.0
 * @date	2012/07/02
 */
public abstract class CRUDsm<CRUDID, CRUDObject> {

    public class CRUDTuple {

        public CRUDID id;
        public CRUDObject object;

        public CRUDTuple(CRUDID id, CRUDObject object) {
            this.id = id;
            this.object = object;
        }
//        public CRUDTuple(){
//            
//        }
    }

    private enum stateMachine {
        // neccessary for the road-map of the internal state machine

        INITIALIZE,
        NOTHING_SELECTED,
        PRESELECTED,
        SELECTED,
        NEW,
        CHANGED,
        SAVE,
        AUTOMATICSAVE,
        DELETE,
        REFRESHVIEW,
        VALIDATING,
        SELECTIONLISTCHANGED,
        INIT_SEARCH,
        SEARCH
    }

    public enum CRUDEvents {

        INITIALIZE, NEW, SELECT, SAVE, DELETE, CHANGED, CLOSING, INIT_SEARCH, SEARCH
    }
    /**
     * here we store the list of objects, we are going to work with.
     */
    protected HashMap<CRUDID, CRUDObject> objects = null;
    /**
     * here we store only the key id of the current element. a null will
     * indicate for all functions, that nothing is selected.
     */
    protected CRUDObject currentElement = null;
    protected CRUDID currentID = null;
    protected CRUDObject deletedOrAddedElement = null;
    protected CRUDID selectedButNotCurrentID = null;
    protected boolean initialized = false;
    protected boolean changeFlag = false;
    protected boolean isInSearch = false;
    protected boolean internalRefreshState = false;
    protected boolean bAutomaticSaveActivated = false;
    protected CRUDModelListener ModelListener = null;
    private CRUDEvents currentEvent = null;

    /**
     * the constructor, which expects the objects in hashmap format to reach the
     * elements quite fast
     *
     * @param objects in the format HashMap< key, object >
     */
    public CRUDsm() {
    }

    public boolean isChanged() {
        return changeFlag;
    }

    public boolean isInSearch() {
        return isInSearch;
    }

    public CRUDObject getCurrentObject() {
        return currentElement;
    }

    /**
     * adds a new element to the internal elements list
     *
     * @param key
     * @param element
     */
    public void addElement(CRUDID key, CRUDObject element) {
        this.objects.put(key, element);
    }

    public CRUDID getCurrentKey() {
        return currentID;
    }

    public HashMap<CRUDID, CRUDObject> getObjects() {
        return objects;
    }

    public Collection<CRUDObject> getElements() {
        return objects.values();
    }

    public Set<CRUDID> getKeys() {
        return objects.keySet();
    }

    public boolean isAutomaticSave() {
        return bAutomaticSaveActivated;
    }

    public void setAutomaticSave(boolean automaticSave) {
        this.bAutomaticSaveActivated = true;
    }

    /*
     * returns the current model listener, which are responsible for the asking
     * the user about to save and delete or not
     * 
     * @return the CRUDModelListener
     */
    public CRUDModelListener getModelListener() {
        return this.ModelListener;
    }

    /**
     * does set the model listener, that is responsible for questioning the user
     * if he wants to delete or save the entry he is currently working with
     *
     * @param modelListener
     */
    public void setModelListener(CRUDModelListener modelListener) {
        this.ModelListener = modelListener;
    }

    /**
     * events
     */
    public boolean event(CRUDEvents newEvent) {
        boolean returnValue = false;

        if (internalRefreshState) {
            // the user fired a new event inside the action handlers. that is not allowed
            // ==> do nothing!
        } else {
            try {
                List<stateMachine> roadmap = new ArrayList<stateMachine>();

                internalRefreshState = true;
                currentEvent = newEvent;
                isInSearch = false;
                deletedOrAddedElement = null;


                switch (newEvent) {

                    case INITIALIZE:
                        if (!initialized) {
                            roadmap.add(stateMachine.INITIALIZE);
                            roadmap.add(stateMachine.SELECTIONLISTCHANGED);
                            roadmap.add(stateMachine.NOTHING_SELECTED);
                            roadmap.add(stateMachine.REFRESHVIEW);
                        }
                        break;

                    case NEW:
                        if (changeFlag) {
                            roadmap.add(stateMachine.AUTOMATICSAVE);
                        }
                        roadmap.add(stateMachine.NEW);
                        roadmap.add(stateMachine.REFRESHVIEW);
                        break;

                    case SELECT:
                        roadmap.add(stateMachine.PRESELECTED);
                        if (changeFlag) {
                            roadmap.add(stateMachine.AUTOMATICSAVE);
                        }
                        roadmap.add(stateMachine.SELECTED);
                        roadmap.add(stateMachine.REFRESHVIEW);
                        break;

                    case SAVE:
                        if (changeFlag) {
                            roadmap.add(stateMachine.VALIDATING);
                            roadmap.add(stateMachine.SAVE);
                            roadmap.add(stateMachine.REFRESHVIEW);
                        }
                        break;

                    case DELETE:
                        roadmap.add(stateMachine.DELETE);
                        roadmap.add(stateMachine.NOTHING_SELECTED);
                        roadmap.add(stateMachine.REFRESHVIEW);
                        break;

                    case CHANGED:
                        if (!changeFlag) {
                            roadmap.add(stateMachine.CHANGED);
                        }

                        break;
                    case CLOSING:
                        if (changeFlag) {
                            roadmap.add(stateMachine.AUTOMATICSAVE);
                            roadmap.add(stateMachine.REFRESHVIEW);
                        }
                        break;
                    case INIT_SEARCH:
                        if (changeFlag) {
                            roadmap.add(stateMachine.AUTOMATICSAVE);
                            roadmap.add(stateMachine.REFRESHVIEW);
                        }
                        roadmap.add(stateMachine.INIT_SEARCH);
                        isInSearch = true;
                        break;
                    case SEARCH:
                        roadmap.add(stateMachine.SEARCH);
                        roadmap.add(stateMachine.SELECTIONLISTCHANGED);
                        roadmap.add(stateMachine.NOTHING_SELECTED);
                        roadmap.add(stateMachine.REFRESHVIEW);
                        break;

                }

                returnValue = process(roadmap);

            } finally {
                internalRefreshState = false;
                currentEvent = newEvent;
                deletedOrAddedElement = null;
            }
        }

        return returnValue;

    }

    private boolean process(List<stateMachine> roadmap) {

        /**
         * we put this variable for true so long, till a "false" is given back
         * from one of the actionFunctions.
         */
        boolean returnValue = true;

        while ((roadmap.size() > 0) && returnValue) {

            switch (roadmap.get(0)) {

                case INITIALIZE:
                    this.objects = actionInit();
                    if (objects == null) {
                        returnValue = false;
                    } else {
                        returnValue = true;
                    }
                    break;

                case NOTHING_SELECTED: {
                    currentElement = null;
                    currentID = null;
                    changeFlag = false;
                    returnValue = true;
                }
                break;

                case PRESELECTED:
                    selectedButNotCurrentID = actionSelected();
                    break;

                case SELECTED:
                    currentID = selectedButNotCurrentID;
                    if (currentID != null && objects.containsKey(currentID)) {
                        currentElement = objects.get(currentID);
                    } else {
                        currentElement = null;
                        currentID = null;
                    }
                    changeFlag = false;
                    break;

                case NEW: {
                    currentElement = actionNewObject();
                    currentID = null;
                    if (currentElement == null) {
                        returnValue = false;
                    } else {
                        returnValue = true;
                    }
                }
                break;

                case CHANGED:
                    changeFlag = true;
                    actionChanged();
                    returnValue = true;
                    break;

                case SAVE: {
                    CRUDTuple newObject = null;
                    newObject = actionSave(this.currentElement);
                    if (newObject == null) {
                        returnValue = false;
                    } else {
                        returnValue = true;
                        changeFlag = false;
                        if (objects.containsKey(newObject.id)) {
                            // already existing in the list, so replace the 
                            // object with the new one
                            objects.put(newObject.id, newObject.object);

                        } else {
                            // this element is new to the system
                            // so put it it
                            objects.put(newObject.id, newObject.object);

                            // this one is important for the selection list change.
                            deletedOrAddedElement = newObject.object;

                            // and now ... put the new state to 
                            // the roadmap
                            roadmap.add(1, stateMachine.SELECTIONLISTCHANGED);
                        }
                        currentID = newObject.id;
                        currentElement = newObject.object;
                    }
                }
                break;

                case AUTOMATICSAVE:
                    if (this.bAutomaticSaveActivated) {
                        currentEvent = CRUDEvents.SAVE;
                        deletedOrAddedElement = null;
                        returnValue = true;
                        roadmap.add(1, stateMachine.SAVE);
                        roadmap.add(1, stateMachine.VALIDATING);

                    } else {
                        CRUD_ACTION_AUTOMATED_SAVE saveIt = actionAutomaticSave();

                        if (saveIt == CRUD_ACTION_AUTOMATED_SAVE.SAVE) {
                            currentEvent = CRUDEvents.SAVE;
                            deletedOrAddedElement = null;
                            roadmap.add(1, stateMachine.SAVE);
                            roadmap.add(1, stateMachine.VALIDATING);
                            returnValue = true;

                        } else {
                            if (saveIt == CRUD_ACTION_AUTOMATED_SAVE.CANCEL) {
                                returnValue = false;

                            } else {
                                if (saveIt == CRUD_ACTION_AUTOMATED_SAVE.DISCARD) {
                                    changeFlag = false;
                                    returnValue = true;
                                }
                            }
                        }
                    }
                    break;
                case DELETE:
                    if (actionDeleteOrCancel() != CRUD_ACTION_DELETE_OR_CANCEL.DELETE) {
                        returnValue = false;
                    } else {
                        if (currentID == null) {
                            // this was a new entry, so you have nothing to delete
                            // realy. just throw the info away!
                            returnValue = true;
                            changeFlag = false;
                        } else {
                            if (actionDelete(this.currentElement)) {
                                objects.remove(currentID);

                                // store the delete element for selection purpose (so the 
                                // remover can find what to delete in its list)
                                deletedOrAddedElement = currentElement;

                                currentID = null;
                                currentElement = null;

                                returnValue = true;
                                changeFlag = false;

                                roadmap.add(1, stateMachine.SELECTIONLISTCHANGED);

                            } else {
                                returnValue = false;
                            }
                        }
                    }
                    break;

                case REFRESHVIEW:
                    actionRefreshView(this.currentElement);
                    break;

                case VALIDATING:
                    if (actionValidate(this.currentElement)) {
                        returnValue = true;
                    } else {
                        returnValue = false;
                    }
                    break;

                case SELECTIONLISTCHANGED:
                    actionRemovedOrAdded(currentEvent, deletedOrAddedElement);
                    break;
                case INIT_SEARCH:
                    actionInitSearch();
                    break;
                case SEARCH:
                    this.objects = actionSearch();
                    if (objects == null) {
                        returnValue = false;
                    } else {
                        returnValue = true;
                    }
                    break;


            }

            // remove the current position
            roadmap.remove(0);
        }

        return returnValue;
    }

    /**
     * internal function to call the CRUD ModelListener or the Default listener
     *
     * @return CRUD_ACTION_AUTOMATED_SAVE
     */
    protected CRUD_ACTION_AUTOMATED_SAVE actionAutomaticSave() {
        if (ModelListener == null) {
            ModelListener = new DefaultCRUDModelListener();
        }
        return ModelListener.actionAutomatedSave();
    }

    /**
     * internal function to call the CRUD ModelListener or the Default listener
     *
     * @return CRUD_ACTION_DELETE_OR_CANCEL
     */
    protected CRUD_ACTION_DELETE_OR_CANCEL actionDeleteOrCancel() {
        if (ModelListener == null) {
            // create a new default listener and use that one!
            ModelListener = new DefaultCRUDModelListener();
        }

        return ModelListener.actionDeleteOrCancel();
    }

    //<editor-fold defaultstate="collapsed" desc="action functions">
    /**
     * this action will be called, if you first initialize this object. So
     *
     * Here you will arrange you components. Fill your lists with the selection
     * parameters, put in the possible selections for you combo box, ...
     */
    public abstract HashMap<CRUDID, CRUDObject> actionInit();

    /**
     * If there are changes reported, this method will be called.
     *
     * In general you will enable here the "save" button (or other indicators)
     */
    public abstract void actionChanged();

    /**
     * Here a new object should be created.
     *
     * In general you create here a new working object, where you put all the
     * information into. You also enable the save buttons and disable the delete
     * button.
     */
    public abstract CRUDObject actionNewObject();

    /**
     * This indicates, that the delete button was pushed. And the user was
     * already asked, if he want to perform the delete or cancel the action. He
     * already decided to delete.
     *
     * So you finally will delete the current working object
     */
    public abstract boolean actionDelete(CRUDObject crudo);

    /**
     * You should here take care, to save the object.
     */
    public abstract CRUDTuple actionSave(CRUDObject crudo);

    /**
     *
     */
    public abstract boolean actionValidate(CRUDObject crudo);

    public abstract CRUDID actionSelected();

    public abstract void actionRefreshView(CRUDObject crudo);

    public abstract void actionRemovedOrAdded(CRUDEvents event, CRUDObject crudo);

    public abstract void actionInitSearch();

    public abstract HashMap<CRUDID, CRUDObject> actionSearch();
    //</editor-fold>
}
