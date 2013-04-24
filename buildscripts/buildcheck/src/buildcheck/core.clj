(ns buildcheck.core
  (:import (java.io File)
           (java.text SimpleDateFormat)
           (java.util Calendar Date))
  (:require [postal.core :as postal]
            [clojure.xml])
  (:gen-class))

(def micromanager (File. "../.."))

(def MS-PER-HOUR (* 60 60 1000))

(defn svn-conflicts []
  (let [mm-path (.getAbsolutePath micromanager)]
    (->> (file-seq micromanager)
         (map #(.getAbsolutePath %))
         (filter #(.endsWith ".mine" %))
         (map #(.replace % ".mine" ""))
         (map #(.replace % mm-path "")))))
  
(def today-token
  (let [format (SimpleDateFormat. "yyyyMMdd")
        one-hour-ago (doto (Calendar/getInstance)
                       (.add Calendar/HOUR -1))]
    (.format format (.getTime one-hour-ago))))

(defn result-file [bits mode]
  (File. micromanager (str "/results" bits ".txt")))

(defn old-file? [file time-limit-hours]
  (let [now (System/currentTimeMillis)
        before (- now (* time-limit-hours MS-PER-HOUR))]
    (< (.lastModified file) before)))

(defn vs-log-files [bits]
  (->> micromanager
       file-seq
       (filter #(and (= "BuildLog.htm" (.getName %))
                     (.contains (.getAbsolutePath %) "Release")
                     (.contains (.getAbsolutePath %) (str bits))
                     (not (old-file? % 24))))))

(defn vs-log-text [f]
  (->> (slurp f :encoding "utf-16")
       (re-seq #"(?s)<pre>(.*?)</pre>")
       (drop 2)
       (map second)
       (apply str)))

(defn contains-errors? [log-text]
  (if (re-find #"\n[^\n]+\b([1-9]|[0-9][1-9]|[0-9][0-9][1-9])\b\serror\(s\)[^\n]+\n"
               log-text)
    true false))

(defn visual-studio-errors [bits]
  (filter contains-errors? (map vs-log-text (vs-log-files bits))))

(defn javac-errors [result-text]
  (map first
    (re-seq #"([0-9]*[1-9])\serrors?" result-text)))

(defn device-adapter-dlls [dir]
  (filter
      (fn [file]
        (let [file-name (.getName file)]
          (and (.endsWith file-name ".dll")
               (.startsWith file-name "mmgr_dal"))))
      (.listFiles dir)))

(defn old-files [files time-limit-hours]
  (filter #(old-file? % time-limit-hours) files))

(defn old-dlls [dir time-limit-hours]
  (old-files (device-adapter-dlls dir) time-limit-hours))

(defn old-jars [dir time-limit-hours]
  (old-files
    (filter
      #(.. % getName (endsWith ".jar"))
      (file-seq dir))
    time-limit-hours))

(defn exe-on-server? [bits date-token]
  (let [txt (slurp "http://valelab.ucsf.edu/~MM/nightlyBuilds/1.4/Windows/")
        pattern (re-pattern (str "MMSetup" bits "BIT_[^\\s]+?_" date-token ".exe"))]
    (re-find pattern txt)))

(def device-adapter-parent-dirs [(File. micromanager "/DeviceAdapters")
                                 (File. micromanager "/SecretDeviceAdapters")])

(defn do-not-build []
  (set (apply concat
         (for [blacklist (map #(File. % "_ADAPTERS_NOT_IN_BUILD.txt") device-adapter-parent-dirs)]
           (let [txt (slurp blacklist)]
             (map #(first (.split % ":")) (.split txt "\n")))))))

(def non-windows-device-adapters #{"dc1394" "SimpleCam" "Video4Linux"})

(defn device-adapter-dirs []
  (filter #(and (.isDirectory %)
                (not (.. % getName (startsWith "."))))
          (mapcat #(.listFiles %) device-adapter-parent-dirs)))

(defn files-of-type [parent-dirs suffix]
  (let [ending (str "." suffix)]
    (filter #(.. % getName (endsWith ending))
            (mapcat file-seq parent-dirs))))

(defn missing-vcproj []
  (let [device-adapter-dirs (device-adapter-dirs)
        directories-without-vcproj
        (filter identity
                (for [device-adapter-dir device-adapter-dirs]
                  (when (empty? (filter #(.. % getName (endsWith ".vcproj"))
                                        (file-seq device-adapter-dir)))
                    device-adapter-dir)))]
        (sort
          (clojure.set/difference
            (set (map #(.getName %) directories-without-vcproj))
            (do-not-build)
            non-windows-device-adapters))))

(defn device-vcproj-files []
    (filter #(.. % getName (endsWith ".vcproj"))
            (mapcat file-seq device-adapter-parent-dirs)))

(defn device-vcxproj-files []
    (filter #(.. % getName (endsWith ".vcxproj"))
            (mapcat file-seq device-adapter-parent-dirs)))

(defn dll-name [file]
  (second (re-find #"mmgr_dal_(.*?).dll" (.getName file))))

(defn project-name [vcproj-file]
  (try
    (-> vcproj-file clojure.xml/parse :attrs :Name)
    (catch Exception e (println vcproj-file))))

(defn bin-dir [bits]
  (File. micromanager
         (condp = bits
           32 "bin_Win32"
           64 "bin_x64")))

(defn get-dll-names [bits]
  (map dll-name (device-adapter-dlls (bin-dir bits))))

(def helper-vcprojs #{"DEClientLib" "DEMessaging"})

(defn missing-device-adapters [bits]
  (let [dll-names (get-dll-names bits)
        project-names (map project-name (filter #(not (.. % getAbsolutePath (contains "_ATTIC")))
                                          (device-vcproj-files)))]
    (sort (clojure.set/difference (set project-names)
                                  #{nil}
                                  (set dll-names)
                                  (do-not-build)
                                  helper-vcprojs))))

(defn device-pages []
  (let [index-txt (slurp "http://valelab.ucsf.edu/~MM/MMwiki/index.php/Device%20Support")]
    (map second (re-seq #"a href=\"/~MM/MMwiki/index.php/(.*?)\"" index-txt))))

(defn missing-device-pages []
  (let [dll-names (get-dll-names 32)
        device-page-names (device-pages)]
    (sort (clojure.set/difference
            (clojure.set/union (set non-windows-device-adapters)
                               (missing-vcproj)
                               (set dll-names))
            (set device-page-names)
            (do-not-build)))))

(defn str-lines [sequence]
  (apply str (interpose "\n" sequence)))

(defn report-segment [title data]
  (str "\n\n" title ":\n"
       (if-not (empty? data)
         (str-lines (flatten (list data)))
         "None.")))

(defn report-build-errors [bits mode test]
  (let [f (result-file bits mode)
        svn-confs (svn-conflicts)
        result-txt (slurp f)
        vs-errors (visual-studio-errors bits)
        outdated-dlls (map #(.getName %) (old-dlls (bin-dir bits) 24))
        javac-errs (javac-errors result-txt)
        outdated-jars (map #(.getName %)
                           (old-jars (File. micromanager "Install_AllPlatforms") 24))
        installer-ok (exe-on-server? bits today-token)
        missing-vcproj-files (missing-vcproj)]
    (when-not (and (not test)
                   (empty? svn-confs)
                   (empty? vs-errors)
                   (empty? outdated-dlls)
                   (empty? javac-errs)
                   (empty? outdated-jars)
                   (empty? missing-vcproj-files)
                   installer-ok)
      (str
        "\n\nMICROMANAGER " bits "-bit "
          ({:inc "INCREMENTAL" :full "FULL"} mode)
          " BUILD ERROR REPORT\n"
        "For the full build output, see " (.getAbsolutePath f)
        (report-segment "Subversion conflicts" svn-confs)
        (report-segment "Visual Studio reported errors" vs-errors)
        (report-segment "Outdated device adapter DLLs" outdated-dlls)
        (report-segment "Errors reported by java compiler" javac-errs)
        (report-segment "Outdated jar files" outdated-jars)
        (when (= 32 bits)
          (report-segment "Missing .vcproj files" missing-vcproj-files))
        (report-segment "Uncompiled device adapters" (missing-device-adapters bits))
        (when (= 32 bits)
          (report-segment "Missing device pages" (missing-device-pages)))
        "\n\nIs installer download available on website?\n"
        (if installer-ok "Yes." "No. (build missing)\n")
      ))))

(defn make-full-report [mode send?]
  (let [report
        (str
          (report-build-errors 32 mode false)
          (report-build-errors 64 mode false))]
    (if-not (empty? report)
      (do 
        (when send?
          (postal/send-message ^{:host "smtp.gmail.com"
                                 :user "mmbuilderrors"
                                 :pass (slurp "C:\\pass.txt")
                                 :ssl :yes}
                               {:from "mmbuilderrors@gmail.com"
                                :to "info@micro-manager.org"
                                :subject "mm build errors!"
                                :body report}))
        (println report))
      (println "Nothing to report."))))

(defn test-report []
  (doseq [bits [32 64]]
    (println (report-build-errors bits :full true))))

(defn -main [mode]
  (make-full-report (get {"inc" :inc "full" :full} mode) true))

;; other windows stuff (manual)


(defn edit-file! [file edit-fn]
  (let [file (clojure.java.io/file file)]
    (spit file (edit-fn (slurp file)))))

(defn replace-in-file!
  "Replace a re-pattern in a file with a new value."
  [file pat new-val]
  (edit-file! file #(clojure.string/replace % pat new-val)))

(defn replace-in-files!
  "Replace a re-pattern in a list of files with a new value."
  [files pat new-val]
  (dorun (map #(replace-in-file! % pat new-val) files)))

(defn fix-output-file-tags!
  "Fix the dll output path specified in all vcproj files."
  []
  (replace-in-files! (device-vcproj-files)
                    #"\$\(OutDir\)/.+?\.dll" "\\$(OutDir)/mmgr_dal_\\$(ProjectName).dll"))
    
(defn find-copy-step [vcproj]
  (re-find #"\"copy .+?\"" (slurp vcproj)))

(defn bad-copy-step [vcproj]
  (not (.contains (or (find-copy-step vcproj) "PlatformName") "PlatformName")))

(defn all-bad-copy-steps
  "Find all vcproj files with a bad post-build copy step"
  []
  (filter bad-copy-step (device-vcproj-files)))

(defn find-pdb [vcproj]
  (re-find #"\".*?\.pdb\"" (slurp vcproj)))

(defn fix-pdb-file-tags!
  "Fix the pdb file path specified in all vcproj files."
  []
  (replace-in-files! (device-vcproj-files)
                    #"\".*?\.pdb\"" "\"\\$(OutDir)/\\$(ProjectName).pdb\""))


;;;; checking mac stuff (manual)

(defn uses-serial-port [file]
  (.contains (slurp file) "g_Keyword_Port"))

(defn devices-using-serial-port []
         (into (sorted-set)
               (map #(.getName (.getParentFile %))
                    (filter uses-serial-port (files-of-type device-adapter-parent-dirs "cpp")))))

(defn unix-built-devices []
  (->
    (into (sorted-set)
          (map #(nth (.split % "_") 2)
               (filter #(.startsWith % "libmmgr")
                       (map #(.getName %)
                            (file-seq (File. "/Users/arthur/Programs/ImageJ"))))))
    (disj "Stradus") (conj "Vortran")
    (disj "MarzhauserLStep") (conj "Marzhauser-LStep")))


(defn missing-unix-adapters []
  (into (sorted-set)
        (clojure.set/difference (set (map #(.toLowerCase %) (devices-using-serial-port)))
                                (set (map #(.toLowerCase %) (unix-built-devices)))
                                #{"pi_gcs" "pi_gcs_2" "xcite120pc_exacte" "skeleton" "crystal"
                                  "imic2000" "polychrome5000" "yokogawa" "ni100x" "twophoton"
                                  "thorlabsdcstage" "lumencorcia" "toptica_ichrome_mle"})))
