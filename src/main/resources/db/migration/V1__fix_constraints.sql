-- =============================================================================
--  V1 — Alignement schéma ↔ entités Java
--
--  Hibernate ddl-auto=update ajoute colonnes et tables mais ne peut pas :
--    - supprimer / recréer des contraintes CHECK
--    - rendre une colonne nullable quand elle était NOT NULL
--
--  Ce script est idempotent (IF EXISTS / IF NOT EXISTS).
-- =============================================================================

-- ─── rendez_vous.type  (enum : VIDEO, PRESENTIEL, URGENCE) ───────────────────
ALTER TABLE rendez_vous DROP CONSTRAINT IF EXISTS rendez_vous_type_check;
ALTER TABLE rendez_vous ADD CONSTRAINT rendez_vous_type_check
    CHECK (type IN ('VIDEO', 'PRESENTIEL', 'URGENCE'));

-- ─── rendez_vous.statut  (enum : PLANIFIE, CONFIRME, ANNULE, EFFECTUE, DATE_PROPOSEE) ─
ALTER TABLE rendez_vous DROP CONSTRAINT IF EXISTS rendez_vous_statut_check;
ALTER TABLE rendez_vous ADD CONSTRAINT rendez_vous_statut_check
    CHECK (statut IN ('PLANIFIE', 'CONFIRME', 'ANNULE', 'EFFECTUE', 'DATE_PROPOSEE'));

-- ─── rendez_vous : colonnes ajoutées dans l'entité, Hibernate les gère aussi ─
ALTER TABLE rendez_vous ADD COLUMN IF NOT EXISTS date_proposee TIMESTAMP;
ALTER TABLE rendez_vous ADD COLUMN IF NOT EXISTS lien_video    VARCHAR(255);

-- ─── transferts.hopital_source_id : nullable dans l'entité (pas de nullable=false) ─
ALTER TABLE transferts ALTER COLUMN hopital_source_id DROP NOT NULL;

-- ─── transferts.medecin_destination_id : nouvelle relation nullable ───────────
ALTER TABLE transferts ADD COLUMN IF NOT EXISTS medecin_destination_id BIGINT;

-- ─── transferts.type  (enum : INTERNE, EXTERNE, URGENCE) ─────────────────────
ALTER TABLE transferts DROP CONSTRAINT IF EXISTS transferts_type_check;
ALTER TABLE transferts ADD CONSTRAINT transferts_type_check
    CHECK (type IN ('INTERNE', 'EXTERNE', 'URGENCE'));

-- ─── transferts.statut  (enum : EN_ATTENTE, ACCEPTE, REFUSE, EFFECTUE, ANNULE) ─
ALTER TABLE transferts DROP CONSTRAINT IF EXISTS transferts_statut_check;
ALTER TABLE transferts ADD CONSTRAINT transferts_statut_check
    CHECK (statut IN ('EN_ATTENTE', 'ACCEPTE', 'REFUSE', 'EFFECTUE', 'ANNULE'));

-- ─── users.role  (inclut ASSISTANT ajouté comme 4e rôle soignant) ────────────
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('ADMIN', 'MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT', 'PATIENT'));

-- ─── alertes.niveau  (enum : INFO, URGENT, CRITIQUE) ─────────────────────────
ALTER TABLE alertes DROP CONSTRAINT IF EXISTS alertes_niveau_check;
ALTER TABLE alertes ADD CONSTRAINT alertes_niveau_check
    CHECK (niveau IN ('INFO', 'URGENT', 'CRITIQUE'));
