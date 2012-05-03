(ns slide-explorer.image
  (:import (ij CompositeImage ImagePlus ImageStack)
           (ij.process ByteProcessor LUT ImageProcessor ShortProcessor)
           (mmcorej TaggedImage)
           (javax.swing JFrame)
           (java.awt Color)
           (org.micromanager.utils ImageUtils))
  (:use [org.micromanager.mm :only (core load-mm gui)]))

; Image Processing Needed:
; 1. Trim the images
; 2. Overlay color channels using a set of LUTs
; 3. Split RGB images into multiple colors
; 4. Maximum intensity projection
; 5. Flat-field correction
; 6. Find stitching vector
; 7. Merge and scale the images

; (defn getPixels [^BufferedImage image] (-> image .getRaster .getDataBuffer .getData))

(defn insert-image [proc-host proc-guest x-host y-host]
  (when proc-guest
    (.insert proc-host proc-guest x-host y-host)))

(defn crop [^ImageProcessor original x y w h]
  (doto (.createProcessor original w h)
    (insert-image original (- x) (- y))))

(defn raw-to-tile [raw-processor
                    [overlap-x overlap-y]
                    [desired-map-x desired-map-y]
                    [found-map-x found-map-y]]
  (let [dx (- found-map-x desired-map-x)
        dy (- found-map-y desired-map-y)]
    (crop proc
          (- (/ overlap-x 2) dx)
          (- (/ overlap-y 2) dy)
          (- (.getWidth proc) overlap-x)
          (- (.getHeight proc) overlap-y))))

(defn processor-to-image [^ImageProcessor proc]
  (.createImage proc))

;; Merge and scale the images

(defn merge-and-scale [img1 img2 img3 img4]
  (let [w (.getWidth img1)
        h (.getHeight img1)
        large (.createProcessor img1 (* 2 w) (* 2 h))]
    (doto large
      (insert-image img1 0 0)
      (insert-image img2 w 0)
      (insert-image img3 0 h)
      (insert-image img4 w h)
      (.setInterpolationMethod ImageProcessor/BILINEAR))
    (.resize large w h)))

;; Channels/LUTs

(defn lut-object [^Color color ^double min max gamma]
  (let [lut (ImageUtils/makeLUT color gamma)]
    (set! (. lut min) min)
    (set! (. lut max) max)
    lut))

(def black-lut (lut-object Color/BLACK 0 255 1.0))

(defn make-stack
  "Produces an ImageJ ImageStack from a collection
   of ImageProcessors."
  [processors]
  (let [proc1 (first processors)
        w (.getWidth proc1)
        h (.getHeight proc1)
        stack (ImageStack. w h)]
    (doseq [processor processors]
      (.addSlice stack processor))
    stack))
        
(defn overlay
  "Takes n ImageProcessors and n lut objects and produces a BufferedImage
   containing the overlay."
  [processors luts]
  (let [luts (if (= 1 (count luts))
               (list (first luts) black-lut)
               luts)
        processors (if (= 1 (count processors))
                     (let [proc (first processors)]
                       (list proc (.createProcessor proc
                                    (.getWidth proc) (.getHeight proc))))
                     processors)
        stack (make-stack processors)
        img+ (ImagePlus. "" stack)]
    (.setDimensions img+ (.getSize stack) 1 1)
    (.getImage
      (doto (CompositeImage. img+ CompositeImage/COMPOSITE)
        (.setLuts (into-array luts))))))


;; test
    
(defn show [img-or-proc]
  (.show (ImagePlus. "" img-or-proc))
  img-or-proc)

