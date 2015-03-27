
/*
 * CRUD2Test.java
 *
 * Created on Jul 10, 2012, 12:12:53 PM
 */
package CRUDsm;

import eu.hayde.box.gui.CRUDsm;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListModel;

/**
 *
 * @author cansenturk
 */
public class CRUDsmTest extends javax.swing.JFrame {

    // Variable declaration
    DefaultListModel model = new DefaultListModel();

    //<editor-fold defaultstate="collapsed" desc="what we do for our crud test is here !! ">
    /**
     * our testclass
     */
    private class Contact {

        public Long id = 0l;
        public String firstname;
        public String lastname;
        public String email;

        public String toString() {
            return lastname + ", " + firstname;
        }
    }
    private CRUDsm theCRUDOBjectHandler = new CRUDsm<Long, Contact>() {
        @Override
        public HashMap<Long, Contact> actionInit() {
            // set to autosave
            //this.setAutomaticSave(true);

            // fill the objects
            return new HashMap<Long, Contact>();
            
        }

        @Override
        public void actionChanged() {
            btnDelete.setEnabled(true);
            btnSave.setEnabled(true);
            btnNew.setEnabled(false);
        }

        @Override
        public Contact actionNewObject() {

            return new Contact();
        }

        @Override
        public boolean actionDelete(Contact object) {
            if (chkSimulateDBError.isSelected()) {
                return false;
            }
            else {
                return true;
            }
        }

        @Override
        public CRUDTuple actionSave(Contact object) {
            CRUDTuple returnValue = null;
            if (chkSimulateDBError.isSelected()) {
                return null;
            }
            else {
                if (object.id == null || object.id == 0l) {
                    object.id = new Date().getTime();
                    returnValue = new CRUDTuple(object.id, object);
                } else {
                    returnValue = new CRUDTuple( object.id, object);
                }
                return returnValue;
            }
        }

        @Override
        public boolean actionValidate(Contact object) {
            String errorMsg = "";
            if (txtFirstname.getText().length() == 0) {
                errorMsg += "Firstname empty!<br/>";
            }
            if (txtLastname.getText().length() == 0) {
                errorMsg += "Lastname empty!<br/>";
            }
            if (txtEmail.getText().length() == 0) {
                errorMsg += "Email empty!<br/>";
            }

            if (errorMsg.length() > 0) {
                lblErrorMsg.setText("<html>" + errorMsg + "</html>");
                return false;
            }
            else {
                lblErrorMsg.setText("");
                object.firstname = txtFirstname.getText();
                object.lastname = txtLastname.getText();
                object.email = txtEmail.getText();
                return true;
            }
        }

        @Override
        public Long actionSelected() {
            Contact selectedUser = (Contact) lstObjects.getSelectedValue();
            return selectedUser.id;
        }

        @Override
        public void actionRefreshView(Contact object) {
            
            if (object == null) {
                txtFirstname.setText("");
                txtFirstname.setEnabled(false);

                txtLastname.setText("");
                txtLastname.setEnabled(false);

                txtEmail.setText("");
                txtEmail.setEnabled(false);

                btnDelete.setEnabled(false);
                btnSave.setEnabled(false);
                btnNew.setEnabled(true);

            }
            else {
                lstObjects.setSelectedValue(object, true);
                txtFirstname.setText(object.firstname);
                txtFirstname.setEnabled(true);

                txtLastname.setText(object.lastname);
                txtLastname.setEnabled(true);

                txtEmail.setText(object.email);
                txtEmail.setEnabled(true);

                btnDelete.setEnabled(true);
                btnSave.setEnabled(false);
                btnNew.setEnabled(true);
            }

        }

        @Override
        public void actionRemovedOrAdded(CRUDsm.CRUDEvents event, Contact crudo) {
            //List<Contact> elements = new ArrayList<Contact>(crudoObjects.values());
            //System.out.println("Size : " + elements.size());
            //List<Contact> listaa = new ArrayList<Contact>(crudoObjects.values()); 
            if (event == CRUDEvents.INITIALIZE) {
                lstObjects.setModel(model);
            }
            else if (event == CRUDEvents.DELETE) {
                model.removeElement(crudo);
            }
            else if (event == CRUDEvents.SAVE) {
                model.addElement(crudo);
                lstObjects.setSelectedValue(crudo, true);
            }
        }
    };
    //</editor-fold>

    /**
     * Creates new form CRUD2Test
     */
    public CRUDsmTest() {
        
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        lstObjects = new javax.swing.JList();
        btnClose = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnNew = new javax.swing.JButton();
        lblFirstname = new javax.swing.JLabel();
        txtFirstname = new javax.swing.JTextField();
        lblLastname = new javax.swing.JLabel();
        txtLastname = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        chkSimulateDBError = new javax.swing.JCheckBox();
        lblExplanation = new javax.swing.JLabel();
        lblErrorMsg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        lstObjects.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstObjects.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstObjectsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstObjects);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnNew.setText("New");
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

        lblFirstname.setText("Firstname");

        txtFirstname.setText("jTextField1");
        txtFirstname.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtKeyTyped(evt);
            }
        });

        lblLastname.setText("Lastname");

        txtLastname.setText("jTextField2");
        txtLastname.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtKeyTyped(evt);
            }
        });

        lblEmail.setText("eMail");

        txtEmail.setText("jTextField3");
        txtEmail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtKeyTyped(evt);
            }
        });

        chkSimulateDBError.setText("Simulate DB Error");
        chkSimulateDBError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSimulateDBErrorActionPerformed(evt);
            }
        });

        lblExplanation.setText("only lowercase letters");

        lblErrorMsg.setBackground(new java.awt.Color(255, 255, 204));
        lblErrorMsg.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblErrorMsg.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblErrorMsg.setOpaque(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(chkSimulateDBError)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 212, Short.MAX_VALUE)
                        .add(btnNew)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnDelete)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnSave)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnClose))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 211, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblFirstname)
                                    .add(lblLastname)
                                    .add(lblEmail))
                                .add(35, 35, 35)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(txtFirstname, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                    .add(txtLastname)
                                    .add(txtEmail))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblExplanation))
                            .add(lblErrorMsg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblFirstname)
                            .add(txtFirstname, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblLastname)
                            .add(txtLastname, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblEmail)
                            .add(txtEmail, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(lblExplanation))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(lblErrorMsg, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnClose)
                    .add(btnSave)
                    .add(btnDelete)
                    .add(btnNew)
                    .add(chkSimulateDBError))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void chkSimulateDBErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSimulateDBErrorActionPerformed
            // TODO add your handling code here:
	}//GEN-LAST:event_chkSimulateDBErrorActionPerformed

	private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
            theCRUDOBjectHandler.event(CRUDsm.CRUDEvents.CLOSING);
	}//GEN-LAST:event_btnCloseActionPerformed

	private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
            theCRUDOBjectHandler.event(CRUDsm.CRUDEvents.SAVE);
	}//GEN-LAST:event_btnSaveActionPerformed

	private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
            theCRUDOBjectHandler.event(CRUDsm.CRUDEvents.INITIALIZE);
	}//GEN-LAST:event_formWindowOpened

	private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
            theCRUDOBjectHandler.event(CRUDsm.CRUDEvents.DELETE);
	}//GEN-LAST:event_btnDeleteActionPerformed

	private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
            theCRUDOBjectHandler.event(CRUDsm.CRUDEvents.NEW);
	}//GEN-LAST:event_btnNewActionPerformed

	private void lstObjectsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstObjectsValueChanged
            theCRUDOBjectHandler.event(CRUDsm.CRUDEvents.SELECT);
	}//GEN-LAST:event_lstObjectsValueChanged

	private void txtKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtKeyTyped
            if (!theCRUDOBjectHandler.isChanged()) {
                theCRUDOBjectHandler.event(CRUDsm.CRUDEvents.CHANGED);
            }
	}//GEN-LAST:event_txtKeyTyped

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CRUDsmTest().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnSave;
    private javax.swing.JCheckBox chkSimulateDBError;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblErrorMsg;
    private javax.swing.JLabel lblExplanation;
    private javax.swing.JLabel lblFirstname;
    private javax.swing.JLabel lblLastname;
    private javax.swing.JList lstObjects;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirstname;
    private javax.swing.JTextField txtLastname;
    // End of variables declaration//GEN-END:variables
}
