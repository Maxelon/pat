(ns pat.model.import
  (:require [clojure.java.shell :as shell]
            [clojure.xml :as xml]
            [pat.db.core :as db]
            [pat.model.schedule :as ms]))

;;XML parsing
(defn isodate
  "Converts a dd-MMM-yyyy string to ISO format.
  If date string is 10 chars long, assumed to already be ISO"
  [date-string]
  (let [in-sdf (java.text.SimpleDateFormat. "dd-MMM-yyyy")
        out-sdf (java.text.SimpleDateFormat. "yyyy-MM-dd")]
    (if (= 10 (count date-string))
      date-string
      (.format out-sdf (.parse in-sdf date-string)))))

(defn format-result
  "takes a xml map from XML/parse, and re-formats into the required result"
  [xml-map]
  (let [xml-vec (:content xml-map)
        nil-alias #{"N/A" "null" "OPEN"}
        cleaned (map #(if (contains? nil-alias (first (:content %)))
                        (assoc % :content nil)
                        (assoc % :content (first (:content %)))) xml-vec)
        raw (reduce #(assoc %1 (:tag %2) (:content %2)) {} cleaned)]
    {:tag         (:assetId raw)
     :description (:description raw)
     :isodate     (isodate (:lastTestDate raw))
     :type        (:testType raw)
     :status      (:testResult raw)
     :image       (:image raw)
     :earth       (:earthResult raw)
     :insulation  (:insulationResult raw)
     :leakage     (:leakageResult raw)
     :wiring      (:wiringResult raw)
     :inspector   (:userName raw)
     :comment     (:commentDescription raw)}))

;;Batch identification
(defn files
  "Returns a list of files that have not been imported"
  []
  (let [directory (clojure.java.io/file "./inbox/")
        all-files (mapv str (filter
                              #(and (.isFile %) (clojure.string/ends-with? % ".zip"))
                              (file-seq directory)))]
    (sort (filter #(not (db/batch? {:file %})) all-files))))

;;Zip extraction
(defn extract
  "Extracts file from zip-file and places it in destination directory"
  [zip-file file destination]
  (do
    (comment (println (str "Extract " file " from " zip-file)))
    (shell/sh "unzip" "-o" zip-file file "-d" destination)))

(defn get-export
  "Extracts Export.xml and returns a sequence of results"
  [zip-file]
  (do
    (extract zip-file "Export.xml" ".temp")
    (map format-result (:content (xml/parse "./.temp/Export.xml")))))

;;---------------------------------------------------------------------
;; A batch description is a map with :file (zip) :lid and other optional data
;;---------------------------------------------------------------------
(defn batch-valid?
  "Checks to make sure batch has a file and lid"
  [batch-descriptor]
  (and (:file batch-descriptor) (:lid batch-descriptor)))

(defn import-batch
  "Imports batch to database, archives the batch and moves images to image folder.
  As map is lazy, zip-file may have moved when it tries to extract, so doall required."
  [batch-description]
  (if (batch-valid? batch-description)
   (if (db/batch? batch-description)
    nil
    (let [zip-file (:file batch-description)
          lid (:lid batch-description)
          batch (get-export zip-file)
          with-image (filter :image batch)
          bid (db/insert-batch! batch-description)]
      (println "importing bid: " bid)
      (doall (map #(db/add-result! (assoc % :lid lid :bid bid)) batch))
      (db/update-batch-count! bid)
      (doall (map #(extract zip-file (:image %) "./images/") with-image))
      ))))
      
(defn skip-batch
  "Marks a batch without importing its results"
  [batch-description]
  (println "skipping")
  (if (batch-valid? batch-description)
   (db/insert-batch! (assoc batch-description :count 0))))


;;Image extraction
(defn get-image
  "Takes an image filename in the images directory and slurp the bytes from the image.
  Returns nil if the file does not exist."
  [x]
  (if (.exists (clojure.java.io/file (str "./images/" x)))
   (with-open [out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy (clojure.java.io/input-stream (str "./images/" x)) out)
    (.toByteArray out))))

(defn batch-descriptor
  "returns a map of batch data"
  [zip-file]
  (let [results (get-export zip-file)
        count (count results)
        tags (map :tag results)
        lid-frq (last
                  (sort-by val
                           (frequencies
                             (reduce concat (mapv #(db/get-lids {:tag %}) tags)))))]
    {:file zip-file :count count :lid (if (nil? lid-frq) "AC" (key lid-frq)) :isodate (ms/today)}))

(defn get-batches
  "Returns the next batch details, or nil"
  []
  (map #(batch-descriptor %) (files)))


