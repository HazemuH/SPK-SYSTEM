-- The synthesis no longer does Simple Additive Weighting, so the snapshot column
-- that stores an alternative's result is named for what it holds: the final AHP
-- score, Σ (criterion weight × subcriterion priority).

ALTER TABLE ranking_entries RENAME COLUMN saw_score TO final_score;
