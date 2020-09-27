/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//
// A simple accordion panel
//
class AccordionPanel extends JPanel implements ActionListener {
    // Data
    String[]     _title      = null;
    JComponent[] _pane       = null;
    ImageIcon    _iconOpen   = null;
    ImageIcon    _iconClosed = null;

    // Controls
    JButton[]    _buttons = null;

    // Create button
    private JButton _newButton(String title, boolean open)
    {
        JButton button = new JButton(title);

        button.setBorder(BorderFactory.createEtchedBorder());
        button.setIcon(open ? _iconOpen : _iconClosed);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setRolloverEnabled(false);
        button.setFocusable(false);

        if(open) button.setForeground(Color.BLUE);

        button.addActionListener(this);

        return button;
    }

    // Create layout
    private void _createLayout(int activeIndex)
    {
        // Array of buttons
        _buttons = new JButton[_pane.length];

        // Add the buttons before the active component
        JPanel beforePanel = new JPanel(null, true);
        beforePanel.setLayout(new BoxLayout(beforePanel, BoxLayout.Y_AXIS));
        for(int i = 0; i < activeIndex; ++i) {
            JPanel container = new JPanel(new BorderLayout(), true);
            _buttons[i] = _newButton(_title[i], false);
            container.add(_buttons[i], BorderLayout.CENTER);
            beforePanel.add(container);
        }
        add(beforePanel, BorderLayout.NORTH);

        // Add the button for the active component and the active component itself
        JPanel      currentPanel = new JPanel(new BorderLayout(), true);
        JScrollPane scrollPane   = new JScrollPane(_pane[activeIndex], ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        currentPanel.add(_newButton(_title[activeIndex], true), BorderLayout.NORTH);
        currentPanel.add(scrollPane, BorderLayout.CENTER);
            scrollPane.setBorder(BorderFactory.createEtchedBorder());

        _buttons[activeIndex] = null;
        add(currentPanel, BorderLayout.CENTER);
        
        // Add the buttons after the active component
        JPanel afterPanel = new JPanel(null, true);
        afterPanel.setLayout(new BoxLayout(afterPanel, BoxLayout.Y_AXIS));
        for(int i = activeIndex + 1; i < _pane.length; ++i) {
            JPanel  container = new JPanel(new BorderLayout(), true);
            _buttons[i] = _newButton(_title[i], false);
            container.add(_buttons[i], BorderLayout.CENTER);
            afterPanel.add(container);
        }
        add(afterPanel, BorderLayout.SOUTH);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a panel with a spreadsheet
    public AccordionPanel(String[] title, JComponent[] pane, ImageIcon iconOpen, ImageIcon iconClosed)
    {
        super(new BorderLayout(), true);
        _title      = title;
        _pane       = pane;
        _iconOpen   = iconOpen;
        _iconClosed = iconClosed;

        _createLayout(0);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for the buttons
    public void actionPerformed(ActionEvent event)
    {
        // Get the button that fired the event
        JButton button = (JButton) event.getSource();

        // Find which button it is
        for(int i = 0; i < _buttons.length; ++i) {
            if(_buttons[i] == button) {
                removeAll();
                _createLayout(i);
                revalidate();
                return;
            }
        }
    }
}

