package vul.clock.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

class MenuItemFactory {
  // for the pure key without ctrl/alt/shift key mask
  static JMenuItem create(String label, Runnable action, int keyCode) {
    return create(label, action, keyCode, 0);
  }

  @SuppressWarnings("serial")
  static JMenuItem create(String label, Runnable action, int keyCode, int keyMask) {
    JMenuItem item = new JMenuItem(new AbstractAction(label) { @Override public void actionPerformed(ActionEvent evt) { action.run(); } });
    item.setFont(item.getFont().deriveFont(Font.PLAIN));
    if (keyCode > 0) {
      item.setAccelerator(KeyStroke.getKeyStroke(keyCode, keyMask));
    }
    return item;
  }
}
