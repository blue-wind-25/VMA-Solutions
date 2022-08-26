/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package appzgl;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//
// Application for generating license
//
public class GenLicApp {
    // Show/create a modal dialog
    private static Object showModalDialog(JOptionPane opane, String title, int style)
    {
        JDialog dialog = opane.createDialog(null, null);

        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setVisible(true);

        return opane.getValue();
    }

    // Show an input dialog
    private static String showInputDialog(String caption, String message)
    {
        // Generate the question panel
        final JPanel pnlQuestion = new JPanel(new GridLayout(2, 1, 0, 5), true);
            final JTextField txtAnswer = new JTextField();
            pnlQuestion.add(new JLabel(message));
            pnlQuestion.add(txtAnswer);
            txtAnswer.setFont(new Font("Liberation Mono", Font.PLAIN, txtAnswer.getFont().getSize()));

        // Generate the dialog
        final JButton     btnOK     = new JButton("OK");
        final JButton     btnCancel = new JButton("Cancel");
        final Object[]    ans       = { btnOK, btnCancel };
        final JOptionPane op        = new JOptionPane(pnlQuestion, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, ans, txtAnswer);

        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnOK); }
        });

        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnCancel); }
        });

        // Show the dialog
        Object ret = showModalDialog(op, caption, JRootPane.QUESTION_DIALOG);

        // Return true if the user has selected "OK"
        return (ret == ans[0]) ? txtAnswer.getText() : null;
    }

    // Show a text dialog
    private static void showTextDialog(String caption, String message)
    {
        // Generate the text display
        JTextArea txtMsg  = new JTextArea(message, 10, 64);
            txtMsg.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            txtMsg.setLineWrap(true);
            txtMsg.setEditable(false);
            txtMsg.setFont(new Font("Liberation Mono", Font.PLAIN, txtMsg.getFont().getSize()));

        JScrollPane scrMsg = new JScrollPane(txtMsg);
            scrMsg.setBorder(BorderFactory.createLoweredBevelBorder());

        // Generate the dialog
        final JButton     btnOK = new JButton("OK");
        final Object[]    ans   = { btnOK };
        final JOptionPane op    = new JOptionPane(scrMsg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_OPTION, null, ans, ans[0]);

        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnOK); }
        });

        // Show the dialog
        showModalDialog(op, caption, JRootPane.INFORMATION_DIALOG);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Program start-up point
    public static void main(String argv[])
    {
        // Initialize look and feel
        try { UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); }
        catch(Exception e){}

        // Generate key pair?
        if(argv[0].equals("gkp")) {
            try {
                GenLic.genKeyPair("appzgl/key/vma-pub.key", "appzgl/key/vma-prv.key");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        // Generate license data?
        if(argv[0].equals("gld")) {
            try {
                String hostKey = showInputDialog("Generate License Data", "Enter the host key:");
                String licData = GenLic.genLicData(
                    "appzgl/key/vma-prv.key",
                    hostKey.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", "").replaceAll(" ", "")
                );
                System.out.println(licData);
                showTextDialog("Generated License Data", licData);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        // Extract host ID
        if(argv[0].equals("xhi")) {
            try {
                String licData = showInputDialog("Extract Host Information", "Enter the host key or license data:");
                String hostID  = GenLic.extractHostID(
                    licData.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", "").replaceAll(" ", "")
                );
                System.out.println(hostID);
                showTextDialog("Extracted Host Information", hostID);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
}

/*

Example host key:

F4008677917A9154B557BF54B569BF5CA414B355BD00E400B157B40CE4009C53
BE4FA800E314E10AFE0AFD09E20DFE0BE014E114B556E714A802E665E60EEA09
E10CE909E203E40DE90CE800E508E50EE00AE003E102E708EA78E87B957F9478
9609E1799600F46EEF03226B7C748ED51C8717B31D5EC8B951459095D2884F0F
5E7B5FD6D65C0EFFE3FD4CBCC803B1373877038C43AC193B8D368C6A3AD0545C
DD7DC92E79E32F4A4B1C4B35693750037C0BB2576504AEB6E19162300B03EA7A
34A411A35A40F84109FC0703EB3ABC3DF38283B344D425E6C9BEE3FD0E005761
68FE7AF50498126CAC0A1BBA7B4E741C918667E6C32894C206FCE34161B183B9

Example license data:

1A841294657E301DB15E5A5BF5BE6B7BCD829E667057D42B37C4435849BA5402
E2474175675ED30BE66E93B78843C4C89658014D348D7B20338B73A2C8C34297
EFCCA07F8C758B6E92399844A10F414B09D01AB918CEC03054AC56A4E9FE65C2
04517513CE1315B02F1228A0BFD14DE2ADE6A081364DD9F9DCA959C8191FC0C5
A7BBBC753BA594BB610B0E94FCF60E67957C8EEB8FEED838AE03C9866FB3CBF5
7CEBB3D3568B9DBDD3FBB273A54286FE3A0866EA7BA52A9293974CCC534E0D27
D8D1CE8DB7564293560AF168DBEE67707519985EFE21E0482BFCA417C96849F2
1929C15D9187FBA07F65B7DDFE7EC0C7E51AFEF55A0F0DE294655EEC86B76BB5
5A5ED1A7EB8A46B6598A3966C340BF48FE59121A39CDFF3E0A2EECD52750633A
84A1F9CA1A4C3709007F0C6776EAE89A5138F70C2719BED8DB8DFB35C18E3973
B07406032220F0476DE4825A5E25EBCB3F29D3E026B76B2D1A5DF2F6F40F484D
D2975B5534D1349EAD6A422D585B9FC12A44DB2C60E60684F3527B1088050B0F
7C574BF5F078CDCCF83B014FC15810601501C9062234377C8EBCB352A243B50D
561DCB8FB62EB0FE912E347006500C835556EAC529254D2A5C73DFAE1434106B
FB396B94500DB2F53B2F5CBB9C132AEAD619F041A7024A167FB0108B40CD63C4
41998663BED8BC3CEDD42047BA85B4803C427558583B09650484F8A378F473CA
EE8494E3F404A1490409E50F40D7D42769962D33CD57302B8693F754ADBAC851
5C08E975844A320118646EBE6A4E3AC3764CE05981DB9C349B8995C72ECDA89E
0EC049766E766F637B3570444407A445E9DAFABAF9CC2738BED4BEDF7C81F1BA
9258946A5813E1DEC0110ACBC3A5C337B161B7322B1311408DECC95DCB978FCA
F9C0E3A3EDF99A4482F6422834F5BF50AD0B8D67CC42C103233545EC55639FA9
A1967AFD2F68B2F798E7F946CC75D6FD4603D4BD1EA1842472062EFC584DE75D
EC75DF2EED16BAD25FF6F66B30D4DB4D869B1BEDBAF5C5AEE24247EAC7681E93
71D7BBA8951FE9CCD36FAC678530B4DB749C9913992799209299BDADE706E80C

*/
