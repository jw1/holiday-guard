-- Migration: Rename Deviation.Action.SKIP to FORCE_SKIP for consistency
-- This aligns the database values with the RunStatus enum naming convention
-- where deviations always represent "forced" statuses

UPDATE deviation
SET action = 'FORCE_SKIP'
WHERE action = 'SKIP';
