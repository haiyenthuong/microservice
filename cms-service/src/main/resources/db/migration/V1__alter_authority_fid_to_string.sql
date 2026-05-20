-- Migration: Change adm_authorities.fid from INT to VARCHAR(50)
-- Purpose: Store parent authority ID instead of numeric function ID
-- Date: 2025-05-19

-- Step 1: Add new column fid_new as VARCHAR(50)
ALTER TABLE adm_authorities ADD COLUMN fid_new VARCHAR(50);

-- Step 2: Migrate data from fid to fid_new
-- Note: For existing data with fid=0 (root level), set to NULL or empty string
-- For other values, we'll need to map them to actual authority IDs
-- This is a placeholder - you may need to adjust based on your actual data
UPDATE adm_authorities
SET fid_new = CASE
    WHEN fid = 0 THEN NULL  -- Root level authorities have no parent
    ELSE CAST(fid AS CHAR(10))  -- Temporary: convert to string
END;

-- Step 3: Drop the old fid column
ALTER TABLE adm_authorities DROP COLUMN fid;

-- Step 4: Rename fid_new to fid
ALTER TABLE adm_authorities CHANGE COLUMN fid_new fid VARCHAR(50) NOT NULL;

-- Step 5: Add foreign key constraint (optional, if you want referential integrity)
-- Uncomment below if you want to enforce parent-child relationship
-- ALTER TABLE adm_authorities
-- ADD CONSTRAINT fk_authority_parent
-- FOREIGN KEY (fid) REFERENCES adm_authorities(id)
-- ON DELETE SET NULL;
