; FILE:         sequence_generator.clj
; PROJECT:      Micro-Manager
; SUBSYSTEM:    mmstudio acquisition engine
; ----------------------------------------------------------------------------
; AUTHOR:       Arthur Edelstein, arthuredelstein@gmail.com, 2010-2011
;               Developed from the acq eng by Nenad Amodaj and Nico Stuurman
; COPYRIGHT:    University of California, San Francisco, 2006-2011
; LICENSE:      This file is distributed under the BSD license.
;               License text is included with the source distribution.
;               This file is distributed in the hope that it will be useful,
;               but WITHOUT ANY WARRANTY; without even the implied warranty
;               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
;               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
;               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

(ns org.micromanager.sequence-generator
  (:use [org.micromanager.mm :only [select-values-match? core mmc]]))

(def MAX-Z-TRIGGER-DIST 5.0)

(defstruct channel :name :exposure :z-offset :use-z-stack :skip-frames)

(defstruct stage-position :stage-device :axes)

(defstruct acq-settings :frames :positions :channels :slices :slices-first
  :time-first :keep-shutter-open-slices :keep-shuftter-open-channels
  :use-autofocus :autofocus-skip :relative-slices :exposure :interval-ms :custom-intervals-ms)

(defn pairs [x]
  (partition 2 1 (lazy-cat x (list nil))))

(defn pairs-back [x]
  (partition 2 1 (lazy-cat (list nil) x)))

(defn if-assoc [pred m k v]
  (if pred (assoc m k v) m))

(defn make-property-sequences [channel-properties]
  (let [ks (apply sorted-set
                  (apply concat (map keys channel-properties)))]
    (into (sorted-map)
      (for [[d p] ks]
        [[d p] (map #(get % [d p]) channel-properties)]))))

(defn channels-sequenceable [property-sequences channels]
  (and
    (not (some false?
           (for [[[d p] s] property-sequences]
             (or (apply = s)
                 (and (core isPropertySequenceable d p)
                      (<= (count s) (core getPropertySequenceMaxLength d p)))))))
    (apply = (map :exposure channels))))
 
(defn select-triggerable-sequences [property-sequences]
  (into (sorted-map)
    (filter #(let [[[d p] vs] %]
               (and (core isPropertySequenceable d p)
                    (not (apply = vs))))
            property-sequences)))

(defn make-dimensions [settings]
  (let [{:keys [slices channels frames positions
                slices-first time-first]} settings
        a [[slices :slice :slice-index] [channels :channel :channel-index]]
        a (if slices-first a (reverse a))
        b [[frames :frame :frame-index] [positions :position :position-index]]
        b (if time-first b (reverse b))]
    (concat a b)))
        
(defn nest-loop [events dim-vals dim dim-index-kw]
  (if (and dim-vals (pos? (count dim-vals)))
    (for [i (range (count dim-vals)) event events]
      (assoc event
        dim-index-kw i
        dim (if (= dim-index-kw :frame-index) i (get dim-vals i))))
    (map #(assoc % dim-index-kw 0) events)))

(defn create-loops [dimensions]
  (reduce #(apply (partial nest-loop %1) %2) [{}] dimensions))

(defn make-main-loops [settings]
  (create-loops (make-dimensions settings)))

(defn build-event [settings event]
  (assoc event
    :exposure (if (:channel event)
                (get-in event [:channel :exposure])
                (:default-exposure settings))
    :relative-z (:relative-slices settings)))

(defn process-skip-z-stack [events slices]
  (if (pos? (count slices))
    (let [middle-slice (get slices (long (/ (count slices) 2)))]
      (filter
        #(or
           (nil? (% :channel))
           (-> % :channel :use-z-stack)
           (= middle-slice (% :slice)))
        events))
    events))

(defn different [e1 e2 selectors]
  (not= (get-in e1 selectors)
        (get-in e2 selectors)))

(defn manage-shutter [events keep-shutter-open-channels keep-shutter-open-slices]
  (for [[e1 e2] (pairs events)]
    (let [diff #(different e1 e2 %)]
      (assoc e1 :close-shutter
             (if e2 (or
                      (and
                        (not keep-shutter-open-channels)
                        (diff [:channel]))
                      (and
                        (not keep-shutter-open-slices)
                        (diff [:slice]))
                      (and (diff [:frame-index])
                           (not
                             ;; special case where we rapidly cycle through channels:
                             (and (not (diff [:slice]))
                                  (not (diff [:position-index]))
                                  keep-shutter-open-channels
                                  (let [wait (e2 :wait-time-ms)]
                                    (or (nil? wait) (zero? wait))))))
                      (diff [:position-index])
                      (:autofocus e2)
                      (diff [:channel :properties ["Core" "Shutter"]]))
               true)))))

(defn process-channel-skip-frames [events]
  (filter
    #(or
       (nil? (% :channel))
       (-> % :channel :skip-frames zero?)
       (zero? (mod (% :frame-index) (-> % :channel :skip-frames inc))))
    events))

(defn process-new-position [events]
  (for [[e1 e2] (pairs-back events)]
    (assoc e2 :new-position
      (different e1 e2 [:position-index]))))

(defn process-use-autofocus [events use-autofocus autofocus-skip]
  (for [[e1 e2] (pairs-back events)]
    (assoc e2 :autofocus
      (and use-autofocus
        (or (not e1)
            (and (zero? (mod (e2 :frame-index) (inc autofocus-skip)))
                 (or (different e1 e2 [:position-index])
                     (different e1 e2 [:frame-index]))))))))

(defn process-wait-time [events interval]
  (cons
    (assoc (first events) :wait-time-ms (if (vector? interval) (first interval) 0))    ;if supplied first custom time point is delay before acquisition start
    (for [[e1 e2] (pairs events) :when e2]
      (if-assoc (different e1 e2 [:frame-index])
                e2 :wait-time-ms (if (vector? interval)
                                   (nth interval (:frame-index e2))
                                   interval)))))
        
(defn event-triggerable [burst event]
  (let [n (count burst)
        e1 (peek burst)
        e2 event
        channels (map :channel (conj burst event))
        props (map :properties channels)]
    (and
      (channels-sequenceable (make-property-sequences props) channels)
      (or (= (e1 :slice) (e2 :slice))
          (let [z-drive (. mmc getFocusDevice)]
            (and
              (. mmc isStageSequenceable z-drive)
              (< n (core getStageSequenceMaxLength z-drive))
              (<= (Math/abs (- (e1 :slice) (e2 :slice))) MAX-Z-TRIGGER-DIST)
              (<= (e1 :slice-index) (e2 :slice-index))))))))
  
(defn burst-valid [e1 e2]
  (and
    (let [wait-time (:wait-time-ms e2)]
      (or (nil? wait-time) (>= (:exposure e2) wait-time)))
    (select-values-match? e1 e2 [:exposure :position])
    (not (:autofocus e2))))

(defn make-triggers [events]
  (let [props (map #(-> % :channel :properties) events)]
    (merge
      {:properties (-> props make-property-sequences select-triggerable-sequences)}
      (let [slices (map :slice events)]
        (when-not (apply = slices)  
          {:slices (when (-> events first :slice) slices)})))))

(defn accumulate-burst-event [events]
  (loop [remaining-events (next events)
         burst [(first events)]]
    (let [e1 (last burst)
          e2 (first remaining-events)]
      (if (and e1
               (burst-valid e1 e2)
               (event-triggerable burst e2))
        (recur (next remaining-events)
               (conj burst e2))
        [burst remaining-events]))))
      
(defn make-bursts [events]  
  (lazy-seq
    (let [[burst later] (accumulate-burst-event events)]
      (when burst
        (cons
          (if (< 1 (count burst))
            (assoc (first burst)
                   :task :burst
                   :burst-data burst
                   :trigger-sequence (make-triggers burst))
            (assoc (first burst) :task :snap))
          (when later
            (make-bursts later)))))))

(defn add-next-task-tags [events]
  (for [p (pairs events)]
    (do ;(println p)
        (assoc (first p) :next-frame-index (get (second p) :frame-index)))))

(defn selectively-update-tag [events event-template key update-fn]
  (let [ks (keys event-template)]
    (for [event events]
      (if (select-values-match? event event-template ks)
        (update-in event [key] update-fn)
        event))))

(defn selectively-append-runnable [events event-template runnable]
  (selectively-update-tag events event-template :runnables
                          #(conj (vec %) runnable)))

(defn attach-runnables [events runnable-list]
  (if (pos? (count runnable-list))
    (recur (apply selectively-append-runnable events (first runnable-list))
           (next runnable-list))
    events))

(defn generate-default-acq-sequence [settings runnables]
  (let [{:keys [slices keep-shutter-open-channels keep-shutter-open-slices
                use-autofocus autofocus-skip interval-ms custom-intervals-ms relative-slices
                runnable-list]} settings]
    (-> (make-main-loops settings)
        (#(map (partial build-event settings) %))
        (process-skip-z-stack slices)
        (process-channel-skip-frames)
        (process-use-autofocus use-autofocus autofocus-skip)
        (process-new-position)
        (process-wait-time (if (first custom-intervals-ms) custom-intervals-ms interval-ms))
        (attach-runnables runnables)
        (manage-shutter keep-shutter-open-channels keep-shutter-open-slices)
        (make-bursts)
        (add-next-task-tags)   
        )))

(defn make-channel-metadata [channel]
  (when-let [props (:properties channel)]
    (into {}
      (for [[[d p] v] props]
        [(str d "-" p) v]))))

(defn generate-simple-burst-sequence [numFrames use-autofocus
                                      channels default-exposure
                                      triggers]
  (let [numChannels (max 1 (count channels))
        numFrames (max 1 numFrames)
        exposure (if (pos? numChannels)
                   (:exposure (first channels))
                   default-exposure)
        events
        (->> (for [f (range numFrames)
                   c (range numChannels)]
               {:frame-index f
                :channel-index c})
             (map
               #(let [f (:frame-index %)
                      c (:channel-index %)
                      first-plane (and (zero? f) (zero? c))
                      last-plane (and (= numFrames (inc f))
                                      (= numChannels (inc c)))]
                 (assoc %
                    :next-frame-index (inc f)
                    :wait-time-ms 0.0
                    :exposure exposure
                    :position-index 0
                    :autofocus (if first-plane use-autofocus false)
                    :channel-index c
                    :channel (get channels c)
                    :slice-index 0
                    :metadata (make-channel-metadata (get channels c))))))]
    ;(println "events:" events)
    (lazy-seq
      (list
        (assoc (first events)
               :task :burst
               :burst-data events
               :trigger-sequence triggers)))))
  

(defn generate-multiposition-bursts [positions num-frames use-autofocus
                                     channels default-exposure triggers]
  (let [simple (generate-simple-burst-sequence
                 num-frames use-autofocus channels default-exposure triggers)]
    (process-new-position
      (flatten
        (for [pos-index (range (count positions))]
          (map #(assoc % :position-index pos-index
                       :position (nth positions pos-index))
               simple))))))

(defn generate-acq-sequence [settings runnables]
  (let [{:keys [numFrames time-first positions slices channels
                use-autofocus default-exposure interval-ms
                autofocus-skip custom-intervals-ms]} settings
        num-positions (count positions)
        property-sequences (make-property-sequences (map :properties channels))]
    (cond
      (and (or time-first
               (> 2 num-positions))
           (> 2 (count slices))
           (or (> 2 (count channels))
               (and
                 (channels-sequenceable property-sequences channels)
                 (apply == 0 (map :skip-frames channels))
                 (apply = true (map :use-z-stack channels))))
           (or (not use-autofocus)
               (>= autofocus-skip (dec numFrames)))
           (zero? (count runnables))
           (not (first custom-intervals-ms))
           (> default-exposure interval-ms))
             (let [triggers 
                   {:properties (select-triggerable-sequences property-sequences)}]
               (if (< 1 num-positions)
                 (generate-multiposition-bursts
                   positions numFrames use-autofocus channels
                   default-exposure triggers)
               (generate-simple-burst-sequence
                 numFrames use-autofocus channels
                 default-exposure triggers)))
      :else
        (generate-default-acq-sequence settings runnables))))

; Testing:


(defn test-z-sequence []
  (let [cam (core getCameraDevice)
        z (core getFocusDevice)]
    (core setProperty cam "TriggerDevice" "D-DA")
    (core loadStageSequence z (org.micromanager.mm/double-vector (range 0 210 20)))
    (core startStageSequence z)))

(def my-channels
  [(struct channel "Cy3" 100 0 true 0)
   (struct channel "Cy5"  50 0 false 0)
   (struct channel "DAPI" 50 0 true 0)])

(def default-settings
  (struct-map acq-settings
    :frames (range 10) :positions [{:name "a" :x 1 :y 2} {:name "b" :x 4 :y 5}]
    :channels my-channels :slices (range 5)
    :slices-first true :time-first true
    :keep-shutter-open-slices false :keep-shutter-open-channels true
    :use-autofocus true :autofocus-skip 3 :relative-slices true :exposure 100
    :interval-ms 1000))


(def test-settings
  (struct-map acq-settings
    :frames (range 10) :positions [{:name "a" :x 1 :y 2} {:name "b" :x 4 :y 5}]
    :channels my-channels :slices (range 5)
    :slices-first true :time-first true
    :keep-shutter-open-slices false :keep-shutter-open-channels true
    :use-autofocus true :autofocus-skip 3 :relative-slices true :exposure 100
    :interval-ms 100))

(def null-settings
  (struct-map acq-settings
    :frames (range 96)
    :positions (range 1536)
    :channels [(struct channel "Cy3" 100 0 true 0)
               (struct channel "Cy5"  50 0 true 0)]
    :slices (range 5)
    :slices-first true :time-first false
    :keep-shutter-open-slices false :keep-shutter-open-channels true
    :use-autofocus true :autofocus-skip 3 :relative-slices true :exposure 100
    :interval-ms 100))

;(def result (generate-acq-sequence null-settings))

;(count result)
