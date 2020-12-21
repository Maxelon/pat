------------------------
-- IMPORTING
------------------------

-- :name insert-mail! :! :n
-- :doc Inserts a record of an mail item that has been copied
INSERT INTO mail
(mid, file)
VALUES (:mid, :file);

-- :name synced? :? :1
-- :doc returns the mail record, if it exists
SELECT * FROM mail
WHERE mid = :mid;

-- :name _insert-batch! :i! :raw
-- :doc inserts a batch file record, returning the raw response.
INSERT INTO batch
(file, lid, isodate)
VALUES (:file, :lid, :isodate);

-- :name batch? :? :1
-- :doc returns the batch id if the batch exists
SELECT bid FROM batch
WHERE file = :file;

-- :name batches :? :*
-- :doc returns a list of batch descriptions, with id
SELECT bid, file, lid, isodate, count
FROM batch
WHERE  count > 0

-- :name _insert-result! :i! :raw
-- :doc inserts a new result record, and returns a map: [{:last_insert_rowid() 6}].
INSERT INTO result
(tag, description, type, status, isodate, image, earth,
insulation, leakage, wiring, comment, inspector, bid)
VALUES (:tag, :description, :type, :status, :isodate, :image, :earth,
:insulation, :leakage, :wiring, :comment, :inspector, :bid);

-- :name _matches :? :*
-- :doc retrieves all results record given the mandatory data.
SELECT * FROM result
WHERE tag = :tag
AND description = :description
AND type = :type
AND status = :status
AND isodate = :isodate
AND inspector = :inspector;

-- :name _add-bid! :! :n
-- :doc updates an existing result record with a bid
UPDATE result
SET bid=:bid
WHERE rid = :rid;

-- :name _upd-batch-count! :! :n
-- :doc updates an existing batch record with a true result count
UPDATE batch
SET count = (SELECT COUNT (*) FROM result WHERE result.bid = :bid)
WHERE batch.bid = :bid;

-- :name _get-lids :? :*
-- :doc returns a list of location ids for results matching the tag
SELECT lid FROM batch
WHERE bid in
(SELECT bid FROM result
WHERE tag = :tag)

------------------------
-- RESULT related
------------------------

-- :name get-result :? :1
-- :doc retrieves a result record given the rid
SELECT * FROM result
WHERE rid = :rid;

-- :name update-assessment! :! :n
-- :doc updates an existing result record with an assessment and reason
UPDATE result
SET assessment=:assessment, reason=:reason
WHERE rid = :rid;

-- :name update-link! :! :n
-- :doc updates an existing result record with an asset id
UPDATE result
SET aid = :aid
WHERE rid = :rid;

-- :name orphan! :! :n
-- :doc makes an existing result record an orphan by removing :aid, :assessment and :reason
UPDATE result
SET aid =NULL, assessment=NULL, reason=NULL
WHERE rid = :rid;

-- :name missing-links :? :*
-- :doc returns a list of results with missing link to asset ids
SELECT * FROM result
WHERE aid IS NULL;

-- :name get-results :? :*
-- :doc return all test results for the asset
SELECT * FROM result
WHERE aid = :aid;

---------------------------
-- SCHEDULE related
---------------------------

-- :name list-locations :? :*
-- :doc retrieves a list of valid location descriptions and ids
SELECT * FROM location;

-- :name list-schedule :? :*
-- :doc retrieves all schedule events for a given year
SELECT * FROM schedule;

--------------------------
-- ASSET-STUB related
--------------------------

-- :name delete-stub! :! :n
-- :doc Deletes the matching asset stub
DELETE FROM asset
WHERE aid = :aid;

-- :name _get-stubs :? :*
-- :doc returns a sequence of all asset stubs in the database
SELECT * FROM asset

-- :name _get-stubs-from-tag :? :*
-- :doc retrieves all assets linked to a tag via a result
SELECT * FROM asset
WHERE aid IN (SELECT aid FROM result
WHERE tag = :tag);

-- :name _asset-exists? :? :1
-- :doc retrieves an asset to see if it exists.
SELECT * FROM asset
WHERE aid = :aid;

-- :name _update-asset! :! :n
-- :doc updates an existing asset record with new supplied data
UPDATE asset
SET description = :description
WHERE aid = :aid;

-- :name _insert-asset! :i! :raw
-- :doc inserts a new asset record, and returns a map: [{:last_insert_rowid() 6}].
INSERT INTO asset
(description)
VALUES (:description);

------------------------
-- ADMINISTRATION
------------------------

-- :name _environment :? :1
-- :doc returns the specified environment variable
SELECT val FROM environment
WHERE ref = :ref;

