/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.gui;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author cansenturk
 */
public class DefaultCRUDModelListener extends CRUDModelListener {

	public static class WarningMessages {
		public static String DELETE_ARE_YOU_SURE = "Sind Sie sicher, dass Sie die Daten löschen wollen?";
		public static String DELETE_MESSAGE_TITLE = "Löschen";
		public static String DELETE_BUTTON_TEXT = "Löschen";

		public static String SAVE_SHOULD_SAVE_IT = "Sollen die eingegebenen Daten gespeichert werden?";
		public static String SAVE_MESSAGE_TITLE = "Änderungen speichern";
		public static String SAVE_BUTTON_YES = "Speichern";
		public static String SAVE_BUTTON_NO = "Nein";

		public static String CANCEL_BUTTON_TEXT = "Abbrechen";

	}

	@Override
	public CRUD_ACTION_AUTOMATED_SAVE actionAutomatedSave() {
		        // save first!
        CRUD_ACTION_AUTOMATED_SAVE returnValue = CRUD_ACTION_AUTOMATED_SAVE.CANCEL;

        //Custom button text
        Object[] options =
        {
            WarningMessages.SAVE_BUTTON_YES, WarningMessages.SAVE_BUTTON_NO, WarningMessages.CANCEL_BUTTON_TEXT
        };
        int selectedValue = JOptionPane.showOptionDialog(null,
                                                         WarningMessages.SAVE_SHOULD_SAVE_IT,
                                                         WarningMessages.DELETE_MESSAGE_TITLE,
                                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         null,
                                                         options,
                                                         options[2]);
        if (selectedValue == 0)
        {
			returnValue = CRUD_ACTION_AUTOMATED_SAVE.SAVE;
        }

        if (selectedValue == 1)
        {
			returnValue = CRUD_ACTION_AUTOMATED_SAVE.DISCARD;
        }

        return returnValue;
	}

	@Override
	public CRUD_ACTION_DELETE_OR_CANCEL actionDeleteOrCancel() {
        CRUD_ACTION_DELETE_OR_CANCEL returnValue = CRUD_ACTION_DELETE_OR_CANCEL.CANCEL;

        String originalValue = (String) UIManager.get("OptionPane.okButtonText");
        UIManager.put("OptionPane.okButtonText", WarningMessages.DELETE_BUTTON_TEXT );

        //Custom button text
        Object[] options =
        {
            WarningMessages.DELETE_BUTTON_TEXT, WarningMessages.CANCEL_BUTTON_TEXT
        };
        int i = JOptionPane.showOptionDialog(null,
                                                WarningMessages.DELETE_ARE_YOU_SURE,
                                                WarningMessages.DELETE_MESSAGE_TITLE,
                                                JOptionPane.OK_CANCEL_OPTION,
                                                JOptionPane.QUESTION_MESSAGE,
                                                null,
                                                options,
                                                options[1]);

        if (i == 0)
        {
            returnValue = CRUD_ACTION_DELETE_OR_CANCEL.DELETE;
        }

        UIManager.put("OptionPane.okButtonText", originalValue);
        return returnValue;
	}

}
