; FILE:         browser/core.clj
; PROJECT:      Micro-Manager Data Browser Plugin
; ----------------------------------------------------------------------------
; AUTHOR:       Arthur Edelstein, arthuredelstein@gmail.com, April 19, 2011
; COPYRIGHT:    University of California, San Francisco, 2011
; LICENSE:      This file is distributed under the BSD license.
;               License text is included with the source distribution.
;               This file is distributed in the hope that it will be useful,
;               but WITHOUT ANY WARRANTY; without even the implied warranty
;               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
;               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
;               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

(ns org.micromanager.browser.core
  (:import [javax.swing BorderFactory JButton JComboBox JFrame JLabel JOptionPane
                        JList JPanel JScrollPane JSplitPane SortOrder
                        JTable JTextField RowFilter SpringLayout]
           [javax.swing.table AbstractTableModel DefaultTableModel
                              TableColumn TableRowSorter]
           [javax.swing.event DocumentListener TableModelListener]
           [java.io BufferedReader File FileReader PrintWriter]
           [java.util Vector]
           [java.util.prefs Preferences]
           [java.util.concurrent LinkedBlockingQueue]
           [java.awt Color Dimension Font Insets]
           [java.awt.event ItemEvent ItemListener KeyAdapter MouseAdapter
                           WindowAdapter WindowListener]
           [com.swtdesigner SwingResourceManager]
           [org.micromanager.acquisition ImageStorageListener MMImageCache])
  (:use [org.micromanager.browser.utils
            :only (gen-map constrain-to-parent create-button create-icon-button
                   attach-action-key remove-borders choose-directory
                   read-value-from-prefs write-value-to-prefs 
                   remove-value-from-prefs remove-nth
                   awt-event persist-window-shape close-window)]
        [clojure.contrib.json :only (read-json write-json)]
        [org.micromanager.mm :only (load-mm gui)]))

(def browser (atom nil))

(def browser-status (atom "Idle"))

(def settings-window (atom nil))

(def collections (atom nil))

(def current-data (atom nil))

(def current-locations (atom (sorted-set)))

(def pending-locations (LinkedBlockingQueue.))

(def pending-data-sets (LinkedBlockingQueue.))

(def stop (atom false))

(def prefs (.. Preferences userRoot
      (node "MMDataBrowser") (node "b3d184b1-c580-4f06-a1d9-b9cc00f12641")))

(def tags [
  "ChColors" "ChContrastMax" "ChContrastMin" "ChNames" "Channels" "Comment"
  "ComputerName" "Date" "Depth" "Directory" "FrameComments" "Frames" "GridColumn"
  "GridRow" "Height" "IJType" "Interval_ms" "KeepShutterOpenChannels"
  "KeepShutterOpenSlices" "Location" "MetadataVersion" "MicroManagerVersion"
  "Name" "Path" "PixelAspect" "PixelSize_um" "PixelType" "PositionIndex"
  "Positions" "Prefix" "Slices" "SlicesFirst" "Source" "Time" "TimeFirst"
  "UUID" "UserName" "Width" "z-step_um"
   ])

(defn clear-queues []
  (.clear pending-locations)
  (.clear pending-data-sets)
  (while (not= @browser-status "Idle") (Thread/sleep 10)))

(defn set-browser-status [text]
  (-> @browser :frame
      (.setTitle (str "Micro-Manager Data Set Browser (" text ")")))
  (reset! browser-status text))

(defn get-icon [name]
  (SwingResourceManager/getIcon
    org.micromanager.MMStudioMainFrame (str "icons/" name)))

(defn get-table-columns [table]
  (when-let [col-vector (.. table getColumnModel getColumns)]
    (enumeration-seq col-vector)))

(defn remove-all-columns [table]
  (let [column-model (. table getColumnModel)]
    (dorun (->> table get-table-columns reverse
                      (map #(.removeColumn column-model %))))))

(defn set-filter [table text]
  (let [sorter (.getRowSorter table)
        column-indices
          (int-array (map #(.getModelIndex %) (get-table-columns table)))]
    (do (.setRowFilter sorter (RowFilter/regexFilter text column-indices)))))

(defn connect-search [search-field table]
  (let [d (.getDocument search-field)
        f #(awt-event
            (set-filter table (.getText d 0 (.getLength d)))
            (.setBackground search-field
              (if (zero? (.getRowCount table))
                Color/PINK Color/WHITE)))]
    (.addDocumentListener d
      (reify DocumentListener
        (insertUpdate [_ _] (f))
        (changedUpdate [_ _] (f))
        (removeUpdate [_ _] (f))))))

(defn open-selected-files [table]
  (let [path-column (.indexOf tags "Path")]
    (doseq [i (.getSelectedRows table)]
      (.openAcquisitionData gui
        (.. table getModel
            (getValueAt (.convertRowIndexToModel table i) path-column))))))

(defn listen-to-open [table]
  (.addMouseListener table
    (proxy [MouseAdapter] []
      (mouseClicked [e]
        (when (= 2 (.getClickCount e)) ; double click
          (open-selected-files table))))))

(defn create-browser-table-model [headings]
  (proxy [AbstractTableModel] []
    (getRowCount [] (count @current-data))
    (getColumnCount [] (count (first @current-data)))
    (getValueAt [row column] (nth (nth @current-data row) column))
    (getColumnName [column] (nth headings column))))

(defn create-locations-table-model []
  (proxy [AbstractTableModel] []
    (getRowCount [] (count @current-locations))
    (getColumnCount [] 1)
    (getValueAt [row _] (nth (vec @current-locations) row))
    (getColumnName [_] "Locations")))

(defn get-row-path [row]
  (nth row (.indexOf tags "Path")))

(defn refresh-row [rows new-row]
  (let [changed (atom false)
        new-path (get-row-path new-row)
        data
          (vec
            (for [row rows]
              (if (= (get-row-path row) new-path)
                (do (reset! changed true) new-row)
                row)))]
    (if @changed
      data
      (conj data new-row))))

(defn add-browser-table-row [new-row]
  (swap! current-data refresh-row (vec new-row))
  (awt-event (.fireTableDataChanged (.getModel (@browser :table)))))

(defn remove-location [loc]
  (swap! current-locations disj loc)
  (let [location-column (.indexOf tags "Location")]
    (swap! current-data
           (fn [coll] (remove #(= (nth % location-column) loc) coll)))
    (-> @browser :table .getModel .fireTableDataChanged)))

(defn remove-selected-locations [] 
  (let [location-table (-> @settings-window :locations :table)
        location-model (.getModel location-table)
        selected-rows (.getSelectedRows location-table)]
    (awt-event
      (dorun
        (map remove-location
          (remove nil?
            (for [location-row selected-rows]
              (.getValueAt location-model location-row 0))))))
      (.fireTableDataChanged location-model)))
    
(defn clear-history []
  (remove-location ""))

(defn find-data-sets [root-dir]
  (map #(.getParent %)
    (->> (File. root-dir)
      file-seq
      (filter #(= (.getName %) "metadata.txt")))))

(defn get-frame-index [file-name]
  (try (Integer/parseInt (second (.split file-name "_")))
     (catch Exception e nil)))

(defn count-frames [data-set]
  (inc
    (apply max -1
      (filter identity
        (map #(get-frame-index (.getName %))
             (.listFiles (File. data-set)))))))

(defn get-display-and-comments [data-set]
  (let [f (File. data-set "display_and_comments.txt")]
    (if (.exists f) (read-json (slurp f) false) nil)))

(defn read-summary-map [data-set]
  (-> (->> (File. data-set "metadata.txt")
           FileReader. BufferedReader. line-seq
           (take-while #(not (.startsWith % "},")))
           (apply str))
      (.concat "}}") (read-json false) (get "Summary"))) 

(defn get-summary-map [data-set location]
  (merge (read-summary-map data-set)
    (if-let [frames (count-frames data-set)]
      {"Frames" frames})
    (if-let [d+c (get-display-and-comments data-set)]
      {"Comment" (get-in d+c ["Comments" "Summary"])
       "FrameComments" (dissoc (get d+c "Comments") "Summary")})
    (let [data-dir (File. data-set)]
      {"Path"     (.getAbsolutePath data-dir)
       "Name"     (.getName data-dir)
       "Location" location})))

(def default-headings ["Path" "Time" "Frames" "Comment" "Location"])
  
(defn start-scanning-thread []
  (doto (Thread.
            (fn []
              (dorun (loop []
                (try
                  (Thread/sleep 5)
                  (let [location (.take pending-locations)]
                    (if-not (= location pending-locations)
                      (do (doseq [data-set (find-data-sets location)]
                           ; (println "data-set:" data-set)
                            (.put pending-data-sets [data-set location]))
                          (recur))
                    nil))
                  (catch Exception e nil)))))
          "data browser scanning thread") .start))

(defn start-reading-thread []
  (doto (Thread.
            (fn []
              (dorun (loop []
                (Thread/sleep 5)
                (when (empty? pending-data-sets) (set-browser-status "Idle"))
                (try
                  (let [data-set (.take pending-data-sets)]
                    (if-not (= data-set pending-data-sets)
                      (let [m (apply get-summary-map data-set)]
                        (add-browser-table-row (map #(get m %) tags))
                        (recur))
                      nil))
                  (catch Exception e nil)))))
          "data browser reading thread") .start))

(defn add-location [location]
  (swap! current-locations conj location)
  (.put pending-locations location)
  (set-browser-status "Scanning")
  (awt-event (-> @settings-window :locations :table .getModel .fireTableDataChanged)))

(defn user-add-location []
  (when-let [loc (choose-directory nil
                     "Please add a location to scan for Micro-Manager image sets.")]
    (add-location (.getAbsolutePath loc))))

(defn get-display-index [table index]
  (let [column-model (.getColumnModel table)]
    (first
      (for [i (range (.getColumnCount column-model))
        :when (= index (.getModelIndex (.getColumn column-model i)))]
          i))))

(defn update-browser-column [])

(defn add-browser-column [tag]
  (let [column (doto (TableColumn. (.indexOf tags tag))
                 (.setHeaderValue tag))]
    (.addColumn (@browser :table) column)
    column))

(defn column-visible? [tag]
  (true?
    (some #{true}
      (for [col (get-table-columns (@browser :table))]
        (= (.getIdentifier col) tag)))))

(defn set-column-visible [tag visible]
      (let [table (@browser :table)]
        (if (and visible (not (column-visible? tag)))
          (add-browser-column tag))
        (if (and (not visible) (column-visible? tag))
          (.removeColumn (.getColumnModel table)
            (.getColumn table tag)))))

(defn create-column-model []
   (proxy [AbstractTableModel] []
                (getRowCount [] (count tags))
                (getColumnCount [] 2)
                (getValueAt [row column]
                  (let [tag (nth tags row)]
                    (condp = column
                      0 (column-visible? tag)
                      1 tag)))
                (setValueAt [val row column]
                  (when (zero? column)
                    (let [tag (nth tags row)]
                      (set-column-visible tag val))))
                (getColumnClass [column]
                  (get [Boolean String] column))))

(defn create-column-table []
  (let [table (proxy [JTable] []
                (isCellEditable [_ i] (get [true false] i)))
        model (create-column-model)]
    (doto table
      (.setModel model)
      (.setRowSelectionAllowed false)
      (.setFocusable false)
      (.. getColumnModel (getColumn 0) (setMinWidth 20))
      (.. getColumnModel (getColumn 0) (setMaxWidth 20)))
    table))

(defn update-collection-menu [name]
  (awt-event
    (let [menu (@browser :collection-menu)
          names (sort (keys @collections))
          listeners (.getItemListeners menu)]
      (dorun (map #(.removeItemListener menu %) listeners))
      (.removeAllItems menu)
      (dorun (map #(.addItem menu %) names))
      (.addItem menu "New...")
      (.setSelectedItem (@browser :collection-menu) name)
      (dorun (map #(.addItemListener menu %) listeners)))))

;; collection files


(defn set-last-collection-name [name]
  (write-value-to-prefs prefs "last-collection" name))

(defn get-last-collection-name []
  (or (read-value-from-prefs prefs "last-collection")
    (System/getProperty "user.name")))

(defn read-collection-map []
  (reset! collections
    (or (read-value-from-prefs prefs "collection-files")
        (let [name (System/getProperty "user.name")]
          {name (.getAbsolutePath (File. (str name ".mmdb.txt")))}))))

(defn save-collection-map []
  (write-value-to-prefs prefs "collection-files" @collections))

;; "data and settings"

(defn fresh-data-and-settings []
  {:browser-model-data nil
   :browser-model-headings tags
   :window-size nil
   :display-columns
     [{:width 0.3 :title "Path"}
      {:width 0.3 :title "Time"}
      {:width 0.1 :title "Frames"}
      {:width 0.3 :title "Comment"}]
   :locations nil
   :sorted-column {:order 1 :model-column "Time"}})

(defn save-data-and-settings [collection-name settings]
  (with-open [pr (PrintWriter. (get @collections collection-name))]
    (write-json settings pr)))

(defn load-data-and-settings [name]
  (or 
    (when-let [f (get @collections name)]
      (when (.exists (File. f))
        (read-json (slurp f))))
    (fresh-data-and-settings)))

;; data and settings <--> gui

(defn get-current-data-and-settings []
  (let [table (@browser :table)
        model (.getModel table)]
    {:browser-model-data (map #(zipmap tags %) @current-data)
     :browser-model-headings tags
     :window-size (let [f (@browser :frame)] [(.getWidth f) (.getHeight f)])
     :display-columns
       (let [total-width (float (.getWidth table))]
         (when (pos? total-width)
           (map #(hash-map :width (/ (.getWidth %) total-width)
                                  :title (.getIdentifier %))
                (get-table-columns table))))
     :locations @current-locations
     :sorted-column
       (when-let [sort-key (->> table .getRowSorter .getSortKeys seq first)]
         {:order ({SortOrder/ASCENDING 1 SortOrder/DESCENDING -1}
                   (.getSortOrder sort-key))
          :model-column (.getColumnName model (.getColumn sort-key))})}))


(defn apply-data-and-settings [collection-name settings]
  (clear-queues)
  (update-collection-menu collection-name)
  (set-last-collection-name collection-name)
  (let [table (@browser :table)
        model (.getModel table)
        {:keys [browser-model-data
                browser-model-headings
                window-size display-columns
                locations sorted-column]} settings]
  ;  (awt-event
      (def dc display-columns)
      (reset! current-data (map (fn [r] (map #(get r (keyword %)) tags)) browser-model-data))
      (.fireTableDataChanged model);)
  ;  (awt-event
      (reset! current-locations (apply sorted-set locations))
      (-> @settings-window :locations :table .getModel .fireTableDataChanged)
      (remove-all-columns table)
      (let [total-width (.getWidth table)]
        (doseq [col display-columns]
          (doto (add-browser-column (:title col))
            (.setPreferredWidth (* total-width (:width col))))))
      (-> @settings-window :columns :table
                           .getModel .fireTableDataChanged)));)

;; creating a new collection

(defn user-specifies-collection-name []
  (let [prompt-msg "Please enter a name for the new collection."]
    (loop [msg prompt-msg]
       (let [collection-name (JOptionPane/showInputDialog msg)]
         (cond
           (nil? collection-name)
             nil
           (empty? (.trim collection-name))
             (recur (str "Name must contain at least one character.\n"
                         prompt-msg))
           (contains? @collections collection-name)
             (recur (str "There is already a collection named " collection-name "!\n"
                         prompt-msg))
           :else collection-name)))))

(defn create-new-collection []
  (let [collection-name (user-specifies-collection-name)]
    (if collection-name
      (do
        (swap! collections assoc collection-name
          (.getAbsolutePath (File. (str collection-name ".mmdb.txt"))))
        (save-collection-map)
        (awt-event
          (apply-data-and-settings collection-name (fresh-data-and-settings))))
      (update-collection-menu (get-last-collection-name)))))

(defn create-image-storage-listener []
  (reify ImageStorageListener
    (imageStorageFinished [_ path]
     ; (println "image storage:" path)
      (.put pending-data-sets [path ""]))))

(defn refresh-collection []
  (clear-queues)
  (dorun (map add-location @current-locations)))

;; windows

(defn create-settings-window []
  (let [label-table
          (fn [table label-text parent]
            (let [label (JLabel. label-text)
                  panel (JPanel.)
                  scroll-pane (JScrollPane. table)]
              (.setBorder table (BorderFactory/createLineBorder (Color/GRAY)))
              (doto panel (.add label) (.add scroll-pane)
                          (.setLayout (SpringLayout.)))
              (constrain-to-parent label :n 0 :w 0 :n 20 :e 0
                                   scroll-pane :n 20 :w 0 :s 0 :e 0)
              (.add parent panel)
              (remove-borders table)
              (.setTableHeader table nil)
              (gen-map table panel)))
        split-pane (JSplitPane. JSplitPane/HORIZONTAL_SPLIT true)
        locations (label-table (proxy [JTable] [0 1] (isCellEditable [_ _] false))
                               "Locations" split-pane)
        add-location-button
          (create-icon-button (get-icon "plus.png") user-add-location)
        remove-location-button
          (create-icon-button (get-icon "minus.png") remove-selected-locations)
        columns (label-table (create-column-table) "Columns" split-pane)
        frame (JFrame. "Micro-Manager Data Set Browser Settings")
        main-panel (.getContentPane frame)
        clear-history-button (create-button "Clear history" clear-history)]
    (apply remove-borders (.getComponents split-pane))
    (doto split-pane
      (.setResizeWeight 0.5)
      (.setDividerLocation 0.5))
    (remove-borders split-pane)
    (doto main-panel (.add split-pane) (.add clear-history-button)) 
    (doto (:panel locations)
      (.add add-location-button) (.add remove-location-button))
    (.setLayout main-panel (SpringLayout.))
    (constrain-to-parent
      split-pane :n 30 :w 5 :s -5 :e -5
      add-location-button :n 0 :e -38 :n 18 :e -20
      remove-location-button :n 0 :e -18 :n 18 :e 0
      clear-history-button :n 5 :w 5 :n 30 :w 125)
    (.setBounds frame 50 50 600 600)
    (-> locations :table (.setModel (create-locations-table-model)))
    (persist-window-shape prefs "settings-window-shape" frame)
    (gen-map frame locations columns)))

(defn create-collection-menu-listener []
  (reify ItemListener
    (itemStateChanged [_ e]
      (let [item (.getItem e)]
        (if (= (.getStateChange e) ItemEvent/SELECTED)
          (condp = item
            "New..." (create-new-collection)
            (awt-event (apply-data-and-settings item (load-data-and-settings item))))
          (save-data-and-settings item (get-current-data-and-settings)))))))

(defn handle-exit []
;  (println "Shutting down Data Browser.")
  (clear-queues)
  (.put pending-data-sets pending-data-sets)
  (.put pending-locations pending-locations)
  (close-window (@browser :frame))
  (close-window (@settings-window :frame))
  true)

(defn create-browser []
  (let [frame (JFrame.)
        panel (.getContentPane frame)
        table (proxy [JTable] [] (isCellEditable [_ _] false))
        scroll-pane (JScrollPane. table)
        search-field (JTextField.)
        search-label (JLabel. (get-icon "zoom.png"))
        refresh-button (create-button "Refresh" refresh-collection)
        settings-button (create-button "Settings..."
                          #(.show (:frame @settings-window)))
        collection-label (JLabel. "Collection:")
        collection-menu (JComboBox.)]
    (doto panel
       (.add scroll-pane) (.add search-field) (.add refresh-button)
       (.add settings-button) (.add search-label)
       (.add collection-label) (.add collection-menu))
    (doto table
      (.setAutoCreateRowSorter true)
      (.setShowGrid false)
      (.setGridColor Color/LIGHT_GRAY)
      (.setShowVerticalLines true))
    (.addItemListener collection-menu (create-collection-menu-listener))
    (attach-action-key table "ENTER" #(open-selected-files table))
    (.setFont search-field (.getFont table))
    (.setLayout panel (SpringLayout.))
    (constrain-to-parent scroll-pane :n 32 :w 5 :s -5 :e -5
                         search-field :n 5 :w 25 :n 28 :w 200
                         settings-button :n 5 :w 500 :n 28 :w 600
                         refresh-button :n 5 :w 405 :n 28 :w 500
                         search-label :n 5 :w 5 :n 28 :w 25
                         collection-label :n 5 :w 205 :n 28 :w 275
                         collection-menu :n 5 :w 275 :n 28 :w 405)
    (connect-search search-field table)
    (.setSortsOnUpdates (.getRowSorter table) true)
    (listen-to-open table)
    (attach-action-key search-field "ESCAPE" #(.setText search-field ""))
    (doto frame
      (.setBounds 50 50 620 500)
      (.addWindowListener
        (proxy [WindowAdapter] []
          (windowClosing [e]
            (clear-queues)
            (save-data-and-settings
              (get-last-collection-name)
                (get-current-data-and-settings))
            (close-window (@settings-window :frame))
            (.setVisible frame false)))))
    (persist-window-shape prefs "browser-shape" frame)
    (gen-map frame table scroll-pane settings-button search-field
             collection-menu refresh-button)))

(defn init-columns []
  (vec (map #(vec (list % false)) tags)))

(defn start-browser []
  (load-mm)
  (read-collection-map)
  (reset! settings-window (create-settings-window))
  (reset! browser (create-browser))
  (.addExitHandler gui handle-exit)
  (set-browser-status "Idle")
  (start-scanning-thread)
  (start-reading-thread)
  (MMImageCache/addImageStorageListener (create-image-storage-listener))
  (awt-event
    (.show (@browser :frame))
    (.setModel (:table @browser) (create-browser-table-model tags))
    (let [collection-name (get-last-collection-name)]
      (apply-data-and-settings collection-name (load-data-and-settings collection-name))))
  browser)
