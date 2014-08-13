package org.micromanager.imagedisplay;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import ij.ImagePlus;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;
import javax.swing.event.MouseInputAdapter;
import net.miginfocom.swing.MigLayout;
import org.micromanager.MMStudio;
import org.micromanager.internalinterfaces.DisplayControls;
import org.micromanager.utils.ReportingUtils;


/**
 * This class is the Frame that handles image viewing: it contains the 
 * canvas and controls for determining which channel, Z-slice, etc. is shown. 
 * HACK: we have overridden getComponents() on this function to "fix" bugs
 * in other bits of code; see that function's comment.
 */
public class DisplayWindow extends StackWindow {

   private boolean closed_ = false;
   private final EventBus bus_;
   
   // store window location in Java Preferences
   private static final int DEFAULTPOSX = 300;
   private static final int DEFAULTPOSY = 100;
   private static Preferences displayPrefs_;
   private static final String WINDOWPOSX = "WindowPosX";
   private static final String WINDOWPOSY = "WindowPosY";
   
   // This class is used to signal that a window is closing.
   public static class RequestToCloseEvent {
      public DisplayWindow window_;
      public RequestToCloseEvent(DisplayWindow window) {
         if (displayPrefs_ != null) {
            displayPrefs_.putInt(WINDOWPOSX, window.getLocation().x);
            displayPrefs_.putInt(WINDOWPOSY, window.getLocation().y);
         }
         window_ = window;
      }
   };

  
   
   public DisplayWindow(final ImagePlus ip, DisplayControls controls, 
         final EventBus bus) {
      super(ip);
      initializePrefs();
      int posX = DEFAULTPOSX, posY = DEFAULTPOSY;
      if (displayPrefs_ != null) {
         posX = displayPrefs_.getInt(WINDOWPOSX, DEFAULTPOSX); 
         posY = displayPrefs_.getInt(WINDOWPOSY, DEFAULTPOSY);
      }
      ip.getWindow().setLocation(posX, posY);
      
      // HACK: hide ImageJ's native scrollbars; we provide our own.
      if (cSelector != null) {
         remove(cSelector);
      }
      if (tSelector != null) {
         remove(tSelector);
      }
      if (zSelector != null) {
         remove(zSelector);
      }
      // Override the default layout with our own, so we can do more 
      // customized controls. 
      // This layout is intended to minimize distances between elements.
      setLayout(new MigLayout("insets 1, fillx"));
      // Re-add the ImageJ canvas.
      add(ic, "align center, wrap");
      add(controls, "align center, wrap, growx");

      pack();

      // Add a listener so we can update the histogram when an ROI is drawn,
      // and to override the title-setting behavior of ImagePlus when the 
      // magnification tool is used.
      ic.addMouseListener(new MouseInputAdapter() {
         @Override
         public void mousePressed(MouseEvent me) {
            if (Toolbar.getToolId() == 11) { // zoom tool selected
               bus.post(new UpdateTitleEvent());
            }
         }

         @Override
         public void mouseReleased(MouseEvent me) {
            if (ip instanceof MMCompositeImage) {
               ((MMCompositeImage) ip).updateAndDraw(true);
            } else {
               ip.updateAndDraw();
            }
         }
      });

      // Repack on window size change, since our controls may have also changed
      // size.
      addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            pack();
         }
      });

      bus_ = bus;

      zoomToPreferredSize();
   }

   /**
    * Keep class specific preferences to store window location
    */
   private void initializePrefs() {
      if (displayPrefs_ == null) {
         try {
            displayPrefs_ = Preferences.userNodeForPackage(getClass());
         } catch (Exception e) {
            ReportingUtils.logError(e);
         }
      }
   }
   
   /**
    * Set our canvas' magnification based on the preferred window magnification.
    * Also sets the position of the window, based on the position of the last
    * closed window.
    * 
    */
   private void zoomToPreferredSize() {
      Point location = getLocation();

      double mag = MMStudio.getInstance().getPreferredWindowMag();

      // Use approximation here because ImageJ has fixed allowed magnification
      // levels and we want to be able to be a bit more approximate and snap
      // to the closest allowed magnification. 
      if (mag < ic.getMagnification()) {
         while (mag < ic.getMagnification() &&
               Math.abs(mag - ic.getMagnification()) > .01) {
            ic.zoomOut(ic.getWidth() / 2, ic.getHeight() / 2);
         }
      } else if (mag > ic.getMagnification()) {
         while (mag > ic.getMagnification() &&
               Math.abs(mag - ic.getMagnification()) > .01) {
            ic.zoomIn(ic.getWidth() / 2, ic.getHeight() / 2);
         }
      }

      //Make sure the window is fully on the screen
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Point newLocation = new Point(location.x,location.y);
      if (newLocation.x + getWidth() > screenSize.width && getWidth() < screenSize.width) {
          newLocation.x = screenSize.width - getWidth();
      }
      if (newLocation.y + getHeight() > screenSize.height && getHeight() < screenSize.height) {
          newLocation.y = screenSize.height - getHeight();
      }

      setLocation(newLocation);
   }

   @Override
   public boolean close() {
      windowClosing(null);
      return closed_;
   }

   @Override
   public void windowClosing(WindowEvent e) {
      if (!closed_) {
         bus_.post(new RequestToCloseEvent(this));
      }
   }

   /**
    * Our controls have changed size; repack the window.
    * @param event
    */
   @Subscribe
   public void onLayoutChange(ScrollerPanel.LayoutChangedEvent event) {
      pack();
   }

   // Force this window to go away.
   public void forceClosed() {
      try {
         super.close();
      } catch (NullPointerException ex) {
         ReportingUtils.showError(ex, "Null pointer error in ImageJ code while closing window");
      }
      MMStudio.getInstance().removeMMBackgroundListener(this);
      closed_ = true;
   }

   @Override
   public void windowClosed(WindowEvent E) {
      try {
         super.windowClosed(E);
      } catch (NullPointerException ex) {
            ReportingUtils.showError(ex, "Null pointer error in ImageJ code while closing window");
      }
   }

   @Override
   public void windowActivated(WindowEvent e) {
      if (!isClosed()) {
         super.windowActivated(e);
      }
   }

   /**
    * HACK HACK HACK HACK HACK HACK ACK HACK HACK HACK HACK HACK ACK HACK
    * We override this function to "fix" the Orthogonal Views plugin, which
    * assumes that item #2 in the array returned by getComponents() is the 
    * cSelector object of an ImageJ StackWindow. Of course, actually changing
    * this behavior for everyone else causes *them* to break horribly, hence
    * why we have to examine the stack trace to determine our caller. Ugh!
    * HACK HACK HACK HACK HACK HACK ACK HACK HACK HACK HACK HACK ACK HACK
    * @return 
    */
   @Override
   public Component[] getComponents() {
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      for (StackTraceElement element : stack) {
         if (element.getClassName().contains("Orthogonal_Views")) {
            return new Component[] {ic, cSelector};
         }
      }
      return super.getComponents();
   }
}