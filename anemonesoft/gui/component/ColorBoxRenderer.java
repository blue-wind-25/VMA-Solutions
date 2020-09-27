/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.component;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;

//
// A color-box-renderer class
//
public class ColorBoxRenderer extends JPanel implements ListCellRenderer {
    // Controls
    private JPanel _pnlColor = new JPanel();
    private JLabel _lblText  = new JLabel();

    // Colors
    private Color _bgrColor = null;
    private Color _selColor = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Item class
    public static class Item {
        public Color  col;
        public String text;

        public Item(Color c, String t)
        { col = c; text = t; }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a color-box-renderer
    public ColorBoxRenderer(Font font)
    {
        super(null, true);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(true);

        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        JPanel pnlBorder = new JPanel(new GridLayout(), true);
            pnlBorder.setOpaque(false);
            pnlBorder.setMinimumSize  (new Dimension(13, 13));
            pnlBorder.setMaximumSize  (new Dimension(13, 13));
            pnlBorder.setPreferredSize(new Dimension(13, 13));
            pnlBorder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createLineBorder(new Color(0, 0, 0), 1)
            ));
            pnlBorder.add(_pnlColor);
        add(pnlBorder);
        
        _lblText.setFont(font);
        add(_lblText);

        BasicComboPopup pp = (BasicComboPopup) (new JComboBox()).getAccessibleContext().getAccessibleChild(0);
        JList           ls = pp.getList();
        _bgrColor = ls.getBackground();
        _selColor = ls.getSelectionBackground();
    }

    // Render the component
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        ColorBoxRenderer.Item item = (ColorBoxRenderer.Item) value;

        setBackground(isSelected ? _selColor : _bgrColor);

        _pnlColor.setBackground(item.col);
        _lblText.setText(item.text);

        return this;
    }
}
