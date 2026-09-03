-- Align the catalog with the thesis: the alternatives keep the codes A1–A50 and
-- the ratings behind Tabel 4.41, but carry the names, categories and age ranges
-- from Tabel 4.1. That splits the old 8 categories into 15, so the category list
-- is replaced too.
--
-- DomainSeeder repopulates both tables and republishes a calculation session.

DELETE FROM toy_scores;
DELETE FROM toy_tags;
DELETE FROM toys;
ALTER TABLE toys ALTER COLUMN id RESTART WITH 1;

DELETE FROM categories;

-- Rankings are frozen against the previous catalog, so none of them still apply.
DELETE FROM calculation_norms;
DELETE FROM calculation_weights;
DELETE FROM calculation_criteria;
DELETE FROM ranking_entries;
DELETE FROM calculation_results;
DELETE FROM calculation_runs;
