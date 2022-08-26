/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            VMA Consultant
*/

package anemonesoft.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// A class for displaying a reference box
//
public class RefsBox extends JDialog implements ActionListener {
    // Controls
    private JButton _btnOK = new JButton(_S("dlg_ok"));

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Private constructor
    private RefsBox(Frame parentFrame)
    {
        super(parentFrame);

        // Generate the references
        StringBuilder sb = new StringBuilder();
        sb.append("A Practical Guide to Analytical Method Validation, Including Measurement Uncertainty and Accuracy Profiles. A. Gustavo González and M. Ángeles Herrador. TrAC Trends in Analytical Chemistry, volume 26, issue 3, pages 227-238. Elsevier Ltd. 2007.\n\n");
        sb.append("Aligning of Single and Multiple Wavelength Chromatographic Proﬁles for Chemometric Data Analysis Using Correlation Optimised Warping. Niels-Peter Vest Nielsen, Jens Michael Carstensen, Jørn Smedsgaard. Journal of Chromatography A, volume 805, issues 1-2, pages 17–35. Elsevier Science B.V. 1998.\n\n");
        sb.append("Analytical Aspects of High Performance Thin Layer Chromatography. Gunawan Indrayanto. In ManMohan Srivastava (ed.) Fast Identification of Molecules: HPTLC Technique, pages 179-201. Springer-Verlag Berlin Heidelberg. 2011.\n\n");
        sb.append("Assessment of Quality Performance Parameters for Straight Line Calibration Curves Related to the Spread of the Abscissa Values around Their Mean. Jacques O. De Beer, Thomas R. De Beer, and Leo Goeyens. Analytica Chimica Acta, volume 584, issue 1, pages 57-65. Elsevier B.V. 2007.\n\n");
      //sb.append("Detecting Outliers with Grubbs' Test. Available from https://www.graphpad.com/support/faqid/1598 (last accessed August 12, 2021).\n\n");
        sb.append("Dynamic Time Warping (DTW) and Correlation Optimized Warping (COW). Giorgio Tomasi, Thomas Skov and Frans van den Berg. 2006. Available from http://www.models.kvl.dk/dtw_cow (accessed: August 25, 2012).\n\n");
        sb.append("Frame Rate-Independent Low-Pass Filter. Gavin Kistner. 2011. Available from http://phrogz.net/js/framerate-independent-low-pass-filter.html (last accessed August 28, 2012).\n\n");
      //sb.append("Grubbs' Test. Charles Zaiontz. Available from https://www.real-statistics.com/students-t-distribution/identifying-outliers-using-t-distribution/grubbs-test (last accessed August 12, 2021).\n\n");
        sb.append("Guidance for Industry. VICH GL49(R). U.S. Department of Health and Human Services, Food and Drug Administration, Center for Veterinary Medicine. March 2015.\n\n");
        sb.append("Improvement of the Decision Efficiency of the Accuracy Profile by Means of a Desirability Function for Analytical Methods Validation Application to Diacetyl-Monoxime Colorimetric Assay Used for the Determination of Urea in Transdermal Iontophoretic Extracts. E. Rozet, V. Wascotte, N. Lecouturier, V. Préat, W. Dewé, B. Boulanger, Ph. Hubert. Analytica Chimica Acta, volume 591, issue 2, pages 239-247. Elsevier B.V. 2007.\n\n");
        sb.append("International Standard ISO 8466-1. 1990.\n\n");
        sb.append("Intra-Laboratory Assessment of Method Accuracy (Trueness and Precision) by Using Validation Standards. A. Gustavo González, M. Ángeles Herrador, and Agustín G. Asuero. Talanta, volume 82, issue 5, pages 1995–1998. Elsevier B.V. 2010.\n\n");
        sb.append("ISO 8466-1, Part 1, Statistical Evaluation of the Linear Calibration Function. International Organization for Standardization, Geneva, Switzerland. 1999.\n\n");
        sb.append("Key Aspects of Analytical Method Validation and Linearity Evaluation. Pedro Araujo. Journal of Chromatography B, volume 877, issue 23, pages 2224-2234. Elsevier B.V. 2009.\n\n");
        sb.append("Linearity. Joachim Ermer. In Joachim Ermer and John H. McB. Miller (Ed). Method Validation in Pharmaceutical Analysis, pages 80-98. Wiley-VCH. 2005.\n\n");
        sb.append("Method Validation in Pharmaceutical Analysis, Second, Completely Revised and Updated Edition. Joachim Ermer and Phil Nethercote (ed.). Wiley-VCH. 2015.\n\n");
        sb.append("NORMDIST. Oliver Maag. 2009. Available from http://www.alina.ch/oliver/faq-excel-normdist.shtml (last accessed March 12, 2016).\n\n");
        sb.append("PeakFinder. Nathanael C. Yoder. 2011. Available from http://www.mathworks.com/matlabcentral/fileexchange/25500 (last accessed August 27, 2012).\n\n");
        sb.append("Probability Distribution Functions. In Interactive Statistics Pages: Web Pages that Perform Statistical Calculations! In John C. Pezzullo's Home Page. 1996. Available from http://statpages.org/pdfs.html (accessed April 25, 2011).\n\n");
        sb.append("Quality Assurance in Analytical Chemistry. Werner Funk, Vera Dammann, and Gerhild Donnevert. VCH Publishers. 1995.\n\n");
        sb.append("Robustness. Gerd Kleinschmidt. In Joachim Ermer and John H. McB. Miller (Ed). Method Validation in Pharmaceutical Analysis, pages 120-169. Wiley-VCH. 2005.\n\n");
        sb.append("Statistic and Chemometrics for Analytical Chemistry, 6th edition. J.N. Miller and Jane C. Miller. Person Education Limited. Essex. England. 2010.\n\n");
        sb.append("Statistical Calculator Z-Table, T-table, Chi-squared table, C4 table. In Homework Help in Statistics, Accounting, & Finance by Mrs. Anju Pramod Dubey, MBA, M.Phil. In Tutoring & Homework Help Math, Chemistry, Physics, Etc. Available from http://www.tutor-homework.com/statistics_tables/statistics_tables.html (accessed December 10, 2011).\n\n");
        sb.append("Target Selection for Alignment of Chromatographic Signals Obtained Using Monochannel Detectors. M. Daszykowski, B. Walczak. Journal of Chromatography A, volume 1176, issues 1-2, pages 1–11. Elsevier B.V. 2007.\n\n");
        sb.append("The Frustrated Reviewer - Recurrent Failures in Manuscripts Describing Validation of Quantitative TLC/HPTLC Procedures for Analysis of Pharmaceuticals. Katalin Ferenczi-Fodor, Bernd Renger, and Zoltán Végh. Journal of Planar Chromatography - Modern TLC, volume 23, number 3, pages 173-179. Akadémiai Kiadó. 2010.\n\n");
        sb.append("Tolerance Intervals for Normal Distribution. John C. Pezzullo. 2005. Available from http://statpages.info/tolintvl.html (last accessed March 11, 2016).\n\n");
        sb.append("Validation Method of Analysis by Using Chromatography. Mochammad Yuwono and Gunawan Indrayanto. In H.G. Brittain (ed.) Profiles of Drugs Substances, Excipients and Related Methodology, volume 32, pages 243-258. Elsevier Academic Press. 2005.\n\n");
        sb.append("Validation Method of Analysis-Updated 2011. Gunawan Indrayanto. In H.G. Brittain (ed.), G. Indrayanto (Contributing Editor) Profiles of Drugs Substances, Excipients and Related Methodology, volume 37, pages 439-465. Elsevier Academic Press. 2012.\n\n");
        sb.append("Validation of In-vitro Bioassay Methods: Application in Herbal Drug Research. G. Indrayanto, G.S. Putra, F. Suhud. In A. A Almajed (ed.) Profile of Drug Substances, Excipients and Related Methodology, volume 46, pages 273-3101. Academic Press, Elsevier Inc. 2021.\n\n");
        sb.append("Validierung in der Analytik. Stavros Kromidas. Wiley-VCH. 2011.");

        // Generate the label for the introduction text
        JPanel pnlIntro = new JPanel(null, true);
        JLabel lblIntro = new JLabel(_S("dlg_ref_intro"), null, JLabel.LEFT);
        pnlIntro.setLayout(new BoxLayout(pnlIntro, BoxLayout.X_AXIS));
        pnlIntro.add(lblIntro);
        pnlIntro.add(Box.createHorizontalGlue());

        // Generate text area for the reference details
        JTextArea txtRefDet = new JTextArea(sb.toString(), 15, 50);
        txtRefDet.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        txtRefDet.setLineWrap(true);
        txtRefDet.setWrapStyleWord(true);
        txtRefDet.setEnabled(false);
        txtRefDet.setDisabledTextColor(SystemColor.controlText);
        txtRefDet.setBackground(SystemColor.window);

        JScrollPane scrRefDet = new JScrollPane(txtRefDet);
        scrRefDet.setBorder(BorderFactory.createEtchedBorder());

        // Generate panels for the reference dialog
        JPanel mainPanel = new JPanel(null, true);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(pnlIntro);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            mainPanel.add(scrRefDet);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Create the dialog
        JPanel dialogPanel = new JPanel(new BorderLayout(), true);
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            dialogPanel.add(mainPanel, BorderLayout.CENTER);
            dialogPanel.add(_btnOK, BorderLayout.SOUTH);
        add(dialogPanel);

        _btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
        _btnOK.addActionListener(this);

        // Ensure that the dialog size is calculated
        pack();

        // Center the dialog relative to its parent
        Dimension dialogSize = getSize();
        Dimension parentSize = parentFrame.getSize();
        Point     parentPos  = parentFrame.getLocation();
        setLocation((parentSize.width - dialogSize.width) / 2 + parentPos.x, (parentSize.height - dialogSize.height) / 2 + parentPos.y);

        // Allow the dialog to be closed by pressing escape
        final JDialog _THIS = this;
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            { _THIS.setVisible(false); }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show a reference box
    public static void showDialog(Frame parentFrame)
    {
        RefsBox rb = new RefsBox(parentFrame);
        GUtil.showModalDialog(rb, null, _S("dlg_ref_title"), JRootPane.INFORMATION_DIALOG);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for button
    public void actionPerformed(ActionEvent event)
    { this.setVisible(false); }
}
