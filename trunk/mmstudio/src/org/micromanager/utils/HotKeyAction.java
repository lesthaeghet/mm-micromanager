package org.micromanager.utils;

/**
 * Storage container for HotKeyActions
 *
 * @author nico
 */
public class HotKeyAction {
      public static final int GUICOMMAND = 0;
      public static final int BEANSHELLSCRIPT = 1;
      public static final int SNAP = 0;
      public static final int TOGGLELIVE = 1;
      public static final int TOGGLESHUTTER = 2;
      public static final int ADD_TO_ALBUM = 3;
      public static final int MARK = 4;
      public static final String[] guiItems_ = {"Snap", "Toggle Live", "Toggle Shutter", "->Album", "Mark Position"};
      public static final int NRGUICOMMANDS = guiItems_.length;

      public int type_;  // either GUICOMMAND or BEANSHELLSCRIPT
      public int guiCommand_;
      public java.io.File beanShellScript_;
      private org.micromanager.MMStudio studio_ =
              org.micromanager.MMStudio.getInstance();

      public HotKeyAction(int guiCommand) {
         type_ = GUICOMMAND;
         guiCommand_ = guiCommand;
      }

      public HotKeyAction(java.io.File beanshellScript) {
         type_ = BEANSHELLSCRIPT;
         beanShellScript_ = beanshellScript;
      }

      public boolean ExecuteAction() {
         if (type_ == GUICOMMAND) {
            switch (guiCommand_) {
               case SNAP:
                  studio_.snapSingleImage();
                  return true;
               case TOGGLELIVE:
                  if (studio_.getLiveMode())
                     studio_.enableLiveMode(false);
                  else
                     studio_.enableLiveMode(true);
                  return true;
               case TOGGLESHUTTER:
                  studio_.toggleShutter();
                  return true;
               case ADD_TO_ALBUM:
                  studio_.snapAndAddToImage5D();
                  return true;
               case MARK:
                  studio_.markCurrentPosition();
                  return true;
            }
         } else {
            org.micromanager.ScriptPanel.runFile(beanShellScript_);
            return true;
         }
         return false;
      }


}
