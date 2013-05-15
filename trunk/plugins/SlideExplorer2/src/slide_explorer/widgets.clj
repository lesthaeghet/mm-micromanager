(ns slide-explorer.widgets
  (:import (java.awt.event ActionListener)
           (java.util UUID)
           (javax.swing AbstractAction JButton JCheckBox JFrame JComponent KeyStroke)
           (org.micromanager.utils JavaUtils)))

;; key binding

(defn bind-key
  "Maps an input-key on a swing component to an action,
  such that action-fn is executed when key is pressed."
  [component input-key action-fn global?]
  (let [im (.getInputMap component (if global?
                                     JComponent/WHEN_IN_FOCUSED_WINDOW
                                     JComponent/WHEN_FOCUSED))
        am (.getActionMap component)
        input-event (KeyStroke/getKeyStroke input-key)
        action
          (proxy [AbstractAction] []
            (actionPerformed [e]
                (action-fn)))
        uuid (.. UUID randomUUID toString)]
    (.put im input-event uuid)
    (.put am uuid action)))

(defn bind-keys
  [component input-keys action-fn global?]
  (dorun (map #(bind-key component % action-fn global?) input-keys)))

(defn bind-window-keys
  [window input-keys action-fn]
  (bind-keys (.getContentPane window) input-keys action-fn true))

;; window and screen utilities

(defn screen-bounds [screen]
  (.. screen getDefaultConfiguration getBounds))

(defn screen-devices []
  (seq (.. java.awt.GraphicsEnvironment
      getLocalGraphicsEnvironment
      getScreenDevices)))

(defn default-screen-device []
  (.. java.awt.GraphicsEnvironment
      getLocalGraphicsEnvironment
      getDefaultScreenDevice))

(defn- overlap-area [rect1 rect2]
  (let [intersection (.intersection rect1 rect2)]
    (* (.height intersection) (.width intersection))))

(defn window-screen [window]
  (when window
    (let [window-bounds (.getBounds window)
          screens (screen-devices)]
      (apply max-key #(overlap-area window-bounds
                                    (screen-bounds %)) screens))))

;; window positioning

(defn show-window-center
  ([window width height parent-window]
    (let [bounds (screen-bounds
                   (or (window-screen parent-window)
                       (first (screen-devices))))
          x (+ (.x bounds) (/ (- (.width bounds) width) 2))
          y (+ (.y bounds) (/ (- (.height bounds) height) 2))]
      (doto window
        (.setBounds x y width height)
        .show)))
  ([window width height]
    (show-window-center window width height nil)))

;; full screen

(def old-bounds (atom {}))

(defn full-screen!
  "Make the given window/frame full-screen."
  [window]
  (when (and window (not (@old-bounds window)))
    (when-not (@old-bounds window)
      (swap! old-bounds assoc window (.getBounds window)))
    (.dispose window)
    (.setUndecorated window true)
    (let [screen (window-screen window)]
      (if (and (JavaUtils/isMac)
               (= screen (default-screen-device)))
        (.setFullScreenWindow screen window)
        (.setBounds window (screen-bounds screen))))
    (.repaint window)
    (.show window)))

(defn exit-full-screen!
  "Restore the given full-screened window to its previous
   (non-full-screen) bounds."
  [window]
  (when (and window (@old-bounds window))
    (.dispose window)
    (.setUndecorated window false)
    (let [screen (window-screen window)]
      (when (= window (.getFullScreenWindow screen))
        (.setFullScreenWindow screen nil)))
    (when-let [bounds (@old-bounds window)]
      (.setBounds window bounds)
      (swap! old-bounds dissoc window))
    (.repaint window)
    (.show window)))

(defn toggle-full-screen!
  "Turn full screen mode on and off for a given window."
  [window]
  (when window
    (if (@old-bounds window)
      (exit-full-screen! window)
      (full-screen! window))))

(defn setup-fullscreen [window]
  (bind-window-keys window ["F"] #(toggle-full-screen! window))
  (bind-window-keys window ["ESCAPE"] #(exit-full-screen! window)))

;; widgets

(defn button [text press-fn]
  (doto (JButton. text)
    (.addActionListener
      (proxy [ActionListener] []
        (actionPerformed [e]
                         (press-fn))))))

(defn checkbox [label state-atom & address]
  (let [checkbox (JCheckBox. label)]
    (.addActionListener checkbox
      (proxy [ActionListener] []
        (actionPerformed [e]
                         (swap! state-atom assoc-in address (.isSelected checkbox)))))
    checkbox))




