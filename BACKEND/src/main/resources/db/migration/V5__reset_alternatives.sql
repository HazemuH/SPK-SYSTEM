-- Reset the alternative catalog so the seeder can repopulate it with the A1–A50
-- toy set from DomainSeed. Every stored ranking is derived from the old catalog
-- (frozen toy ids/names), so the calculation snapshots go with it — DomainSeeder
-- recomputes and republishes a fresh session on the next startup.

-- Calculation snapshots first (children before parents; explicit for H2 + PostgreSQL).
DELETE FROM calculation_norms;
DELETE FROM calculation_weights;
DELETE FROM calculation_criteria;
DELETE FROM ranking_entries;
DELETE FROM calculation_results;
DELETE FROM calculation_runs;

-- Then the alternatives themselves.
DELETE FROM toy_scores;
DELETE FROM toy_tags;
DELETE FROM toys;
