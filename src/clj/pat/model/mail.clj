(ns pat.model.mail
  (:require [pat.config :refer [env]]
            [postal.core :as postal]
            [clojure.java.shell :as shell]
            [clojure-mail.core :as mail]
            [clojure-mail.message :as msg]
            [clojure.java.io :as io]
            [pat.db.core :as db]))

;;Backup
(defn send-backup!
  "Creates a backup.zip of the images folder and database, and then sends it to me!"
  []
  (shell/sh "zip" "-r" "backup" "images")
  (shell/sh "zip" "-r" "backup" ((first (re-seq #"jdbc:sqlite:(.*)" (:database-url env))) 1))
  (postal/send-message {:host (db/envir :smtp-server)
                        :user (db/envir :user)
                        :pass (db/envir :pass)
                        :port 587
                        :tls  true}
                       {:from    (db/envir :user)
                        :to      "ken@maxelon.co.uk"
                        :subject "Pat Manager backup data"
                        :body    [{:type    "text/html"
                                   :content "<b>This is the zip: </b>"}
                                  {:type    :attachment
                                   :content (java.io.File. "backup.zip")}]}))

(defn copy-zip!
  "Takes a zip from a message and places it in the ./inbox folder.
  Adds a record to the mail table to mark it as having been done.
  If the message has previously been synced, then nothing is done."
  [message]
  (if (not (db/synced? {:mid (msg/id message)}))
    (let [date-string (.format
                        (java.text.SimpleDateFormat. "yyyyMMdd_hhmmss")
                        (msg/date-sent message))
          forwarded? (re-seq #"Fwd:" (msg/subject message))
          body (if forwarded?
                 (second (msg/message-body message))
                 (msg/message-body message))
          content (if (sequential? body)
                    (first (filter #(re-seq #"application/zip" (:content-type %)) body))
                    nil)
          mail-id (msg/id message)
          zip-name (if content
                     (get (first (re-seq #"name=(.*).zip" (:content-type content))) 1)
                     nil)
          file-name (case zip-name
                      nil nil
                      "Export" (str date-string ".zip")
                      (str zip-name ".zip"))]
      #_(println (str "new mail: " mail-id "/" zip-name "/" file-name))
      (if file-name
        (with-open [out (io/output-stream (str "./inbox/" file-name))]
          (io/copy (:body content) out)))
      (db/insert-mail! {:mid mail-id :file file-name}))))

(defn sync-inbox!
  "Opens the email inbox and synchronises zip files with the ./inbox folder.
  Syncronisation is one way, from the email account, and deletions are not
  sychronised."
  []
  (let [store (mail/store (db/envir :imap-server) (db/envir :user) (db/envir :pass))
        messages (mail/all-messages store "inbox")]
    (if (empty? messages)
      "No mail to synchronise"
      (doall
        (doseq [m messages] (copy-zip! m))
        (mail/mark-all-read store "inbox")
        (str (count messages) " messages synchronised")))))

