/*
 *
 */
package eu.hayde.box.gui;

/**
 * 
 * @author can.senturk
 */
public abstract class CRUDModelListener {
	
	public enum CRUD_ACTION_DELETE_OR_CANCEL {
		CANCEL, DELETE
	}
	
	public enum CRUD_ACTION_AUTOMATED_SAVE {
		CANCEL, DISCARD, SAVE
	}
	
	public abstract CRUD_ACTION_AUTOMATED_SAVE actionAutomatedSave();
	public abstract CRUD_ACTION_DELETE_OR_CANCEL actionDeleteOrCancel();
	
}
