/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.component;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// Tab button-component class
//
public class TabButtonComponent extends JPanel {
    // The component of this tab
    private JComponent _comp = null;

    // Controls
    private JTabbedPane _pnnOwner   = null;
    private JLabel      _lblIcon    = null;
    private JLabel      _lblCaption = null;
    
    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a tab-buton-component
    public TabButtonComponent(final JTabbedPane pane, JComponent comp, String caption, ImageIcon icon, String tooltip, boolean noClose)
    {
        super(new BorderLayout(), true);
        setOpaque(false);

        _comp     = comp;
        _pnnOwner = pane;

        if(tooltip != null && tooltip.equals("")) tooltip = null;

        // Generate label for the icon
        if(icon != null) {
            _lblIcon = new JLabel(null, icon, JLabel.LEFT);
                _lblIcon.setToolTipText(tooltip);
                _lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
                _lblIcon.addMouseListener(new MouseListener() {
                    public void mouseEntered (MouseEvent event) {}
                    public void mouseExited  (MouseEvent event) {}
                    public void mousePressed (MouseEvent event) {}
                    public void mouseReleased(MouseEvent event) {}
                    public void mouseClicked (MouseEvent event) { onTabClicked(event); }
                });
        }

        // Generate label for the caption
        if(caption != null) {
            _lblCaption = new JLabel(caption, null, JLabel.LEFT);
                _lblCaption.setToolTipText(tooltip);
                _lblCaption.setFont(_lblCaption.getFont().deriveFont(Font.BOLD));
                _lblCaption.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
                _lblCaption.addMouseListener(new MouseListener() {
                    public void mouseEntered (MouseEvent event) {}
                    public void mouseExited  (MouseEvent event) {}
                    public void mousePressed (MouseEvent event) {}
                    public void mouseReleased(MouseEvent event) {}
                    public void mouseClicked (MouseEvent event) { onTabClicked(event); }
                });
        }

        // Put the controls
        if(_lblIcon    != null) add(_lblIcon, BorderLayout.WEST);
        if(_lblCaption != null) add(_lblCaption, BorderLayout.CENTER);
        if(!noClose           ) add(new TabButton(this, _pnnOwner), BorderLayout.EAST);
    }

    // Get the caption of the tab
    public String getCaption()
    {
        return (_lblCaption == null) ? null : _lblCaption.getText();
    }
    
    // Set the caption of the tab
    public void setCaption(String txt)
    {
        if(_lblCaption != null) _lblCaption.setText(txt);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    private void onTabClicked(MouseEvent event)
    {
        _pnnOwner.setSelectedComponent(_comp);

        if(_lblIcon == null || _lblCaption == null || event.getClickCount() < 2) return;
        
        String txt = GUtil.showInputDialog(GUIMain.instance.getRootFrame(), _S("str_tab_ask_tcapt"), _lblCaption.getText());
        if(txt == null) return;
        _lblCaption.setText(txt);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Internal tab button class
    private class TabButton extends JButton implements ActionListener {
        // Reference to the parent and tabbed-pane
        private TabButtonComponent _parent = null;
        private JTabbedPane        _pane   = null;

        ////////////////////////////////////////////////////////////////////////////////////////////

        // Construct a tab button
        public TabButton(TabButtonComponent parent, JTabbedPane pane)
        {
            _parent = parent;
            _pane   = pane;

            setPreferredSize(new Dimension(8, 17));
            setUI(new BasicButtonUI());
            setBorderPainted(false);
            setContentAreaFilled(false);
            setRolloverEnabled(true);
            setFocusable(false);
            setToolTipText(_S("dlg_close_tab_tooltip"));
            addActionListener(this);
        }
        
        // An implementation if this method is not needed
        public void updateUI()
        {}

        // Draw the cross
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            boolean isActive = (_pane.getTabComponentAt(_pane.getSelectedIndex()) == _parent);

            Graphics2D g2 = (Graphics2D) g.create();

            if(isActive && getModel().isPressed()) {
                // Outline
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.WHITE);
                g2.drawLine(2, 7, getWidth() - 2, getHeight() - 6);
                g2.drawLine(getWidth() - 2, 7, 2, getHeight() - 6);
                // Cross
                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.RED);
                g2.drawLine(2, 7, getWidth() - 2, getHeight() - 6);
                g2.drawLine(getWidth() - 2, 7, 2, getHeight() - 6);
            }
            else {
                g2.setStroke(new BasicStroke(2));
                g2.setColor((isActive &&  getModel().isRollover()) ? Color.RED : Color.BLACK);
                g2.drawLine(2, 7, getWidth() - 2, getHeight() - 6);
                g2.drawLine(getWidth() - 2, 7, 2, getHeight() - 6);
            }
            
            g2.dispose();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        // Event handler for the buttons
        public void actionPerformed(ActionEvent event)
        {
            if(_pane.getTabComponentAt(_pane.getSelectedIndex()) != _parent) {
                _pane.setSelectedComponent(_comp);
                return;
            }
            
            if(!GUtil.showYesNoQuestionDialog((Frame) SwingUtilities.windowForComponent(this), _S("dlg_close_tab_confirm"))) return;
            
            int i = _pane.indexOfTabComponent(_parent);
            if(i != -1) _pane.remove(i);
        }
    }
}
