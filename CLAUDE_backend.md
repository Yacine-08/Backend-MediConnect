# MediConnect — Backend only
# Contexte complet dans ../CLAUDE.md

## Package racine
sn.edu.ept.mediconnect

## Modules clés
- auth/ → /api/auth/**
- users/ → patient, medecin, infirmier
- medical/ → consultation, dossier, examen, ordonnance, rendezvous, transfert, alerte
- dtos/ → tous les Request/Response (référence pour les shapes JSON)
- exceptions/ → GlobalExceptionHandler

## Commande
mvn spring-boot:run → port 8080
