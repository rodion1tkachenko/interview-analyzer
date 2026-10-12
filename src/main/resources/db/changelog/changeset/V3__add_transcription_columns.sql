--liquibase formatted sql

--changeset dev:add-transcription-columns
ALTER TABLE media_files ADD COLUMN audio_path VARCHAR(512);
ALTER TABLE media_files ADD COLUMN transcript TEXT;
ALTER TABLE media_files ADD COLUMN transcript_segments TEXT;
ALTER TABLE media_files ADD COLUMN error_message TEXT;