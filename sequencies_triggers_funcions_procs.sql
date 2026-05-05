-- ============================================================
-- SCRIPT SQL: Joc del Pingu - PL/SQL FUNCIONALITAT COMPLETA (NOTA 10)
-- Autor: Antigravity (AI Assistant)
-- Descripció: Implementació de seqüències, triggers, funcions i procediments 
--              per a la gestió avançada de partides i estadístiques.
-- ============================================================

-- 1. GENERAR NÚMEROS SEQÜENCIALS (S)
-- Creem una seqüència per assignar IDs a les partides automàticament.
BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE SEC_ID_PARTIDA';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE SEQUENCE SEC_ID_PARTIDA
START WITH 1
INCREMENT BY 1
NOCACHE;

-- 2. ASSIGNAR AUTOMÀTICAMENT EL Nº SEQÜENCIAL (T)
-- Assignar el nº seqüencial a la clau primària de la taula partida si arriba NULL.
CREATE OR REPLACE TRIGGER TRG_ID_PARTIDA
BEFORE INSERT ON PARTIDA
FOR EACH ROW
BEGIN
  IF :NEW.ID_PARTIDA IS NULL THEN
    SELECT SEC_ID_PARTIDA.NEXTVAL INTO :NEW.ID_PARTIDA FROM DUAL;
  END IF;
END;
/

-- 3. INCREMENTAR AUTOMÀTICAMENT VICTÒRIES (T)
-- Incrementa el camp 'victories' del jugador guanyador quan una partida es marca com finalitzada.
CREATE OR REPLACE TRIGGER TRG_ACTUALITZA_VICTORIES
AFTER UPDATE OF FINALITZADA ON PARTIDA
FOR EACH ROW
WHEN (NEW.FINALITZADA = 1 AND OLD.FINALITZADA = 0 AND NEW.ID_GUANYADOR IS NOT NULL)
BEGIN
    UPDATE JUGADOR 
    SET VICTORIES = NVL(VICTORIES, 0) + 1 
    WHERE ID_JUGADOR = :NEW.ID_GUANYADOR;
END;
/

-- 4. OBTENIR MÀXIM Nº DE VICTÒRIES (RÈCORD) (F)
-- Retorna el número màxim de victòries registrat a la base de dades.
CREATE OR REPLACE FUNCTION GET_MAX_VICTORIES_RECORD 
RETURN NUMBER IS
    v_max NUMBER;
BEGIN
    SELECT MAX(VICTORIES) INTO v_max FROM JUGADOR;
    RETURN NVL(v_max, 0);
END;
/

-- 5. OBTENIR JUGADORS AMB EL RÈCORD (P)
-- Retorna un cursor amb els noms i victòries dels jugadors que tenen el rècord actual.
CREATE OR REPLACE PROCEDURE GET_JUGADORS_RECORD(p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT NOM_JUGADOR, VICTORIES
        FROM JUGADOR
        WHERE VICTORIES = (SELECT MAX(VICTORIES) FROM JUGADOR)
        AND VICTORIES > 0;
END;
/

-- 6. MITJA DE PARTIDES GUANYADES (F)
-- Calcula la mitja de victòries entre tots els jugadors registrats.
CREATE OR REPLACE FUNCTION GET_MITJA_VICTORIES 
RETURN NUMBER IS
    v_mitja NUMBER;
BEGIN
    SELECT AVG(VICTORIES) INTO v_mitja FROM JUGADOR;
    RETURN NVL(v_mitja, 0);
END;
/

-- 7. JUGADORS AMB MÉS VICTÒRIES QUE LA MITJA (P)
-- Retorna un cursor amb els jugadors que superen la mitja global de victòries.
CREATE OR REPLACE PROCEDURE GET_JUGADORS_SOBRE_MITJA(p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT NOM_JUGADOR, VICTORIES
        FROM JUGADOR
        WHERE VICTORIES > (SELECT AVG(VICTORIES) FROM JUGADOR)
        ORDER BY VICTORIES DESC;
END;
/

-- 8. PERCENTATGE DE JUGADORS AMB MENYS VICTÒRIES (F)
-- Calcula el percentatge de jugadors que tenen menys victòries que el valor passat.
CREATE OR REPLACE FUNCTION PERCENTATGE_MENYS_VICTORIES(p_vics IN NUMBER) 
RETURN NUMBER IS
    v_total NUMBER;
    v_menys NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_total FROM JUGADOR;
    IF v_total = 0 THEN RETURN 0; END IF;
    
    SELECT COUNT(*) INTO v_menys FROM JUGADOR WHERE VICTORIES < p_vics;
    RETURN (v_menys / v_total) * 100;
END;
/

-- 9. AVIS AUTOMÀTIC DE PERCENTATGE (T)
-- Trigger que mostra per consola el nou percentatge de superació en augmentar victòries.
CREATE OR REPLACE TRIGGER TRG_AVIS_RANKING
AFTER UPDATE OF VICTORIES ON JUGADOR
FOR EACH ROW
DECLARE
    v_perc NUMBER;
BEGIN
    v_perc := PERCENTATGE_MENYS_VICTORIES(:NEW.VICTORIES);
    DBMS_OUTPUT.PUT_LINE('RANKING: El jugador ' || :NEW.NOM_JUGADOR || ' ara supera al ' || ROUND(v_perc, 2) || '% dels jugadors.');
END;
/

-- 10. RANKING PER TOTAL DE PARTIDES JUGADES (P)
-- Retorna un cursor amb els jugadors i el seu recompte total de partides (de més a menys).
CREATE OR REPLACE PROCEDURE RANKING_PARTIDES_TOTALS(p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT J.NOM_JUGADOR, COUNT(JP.ID_PARTIDA) AS TOTAL
        FROM JUGADOR J
        LEFT JOIN JUGADOR_PARTIDA JP ON J.ID_JUGADOR = JP.ID_JUGADOR
        GROUP BY J.NOM_JUGADOR
        ORDER BY TOTAL DESC;
END;
/

-- 11. PROCEDIMENT DE CONSULTA AMB CONTROL D'ERRORS (P)
-- Consulta les dades d'un jugador amb validació d'existència i de partides guardades.
CREATE OR REPLACE PROCEDURE CONSULTAR_ESTADISTIQUES_JUGADOR(
    p_nom IN VARCHAR2, 
    p_vics OUT NUMBER,
    p_total_partides OUT NUMBER
) IS
    v_count NUMBER;
BEGIN
    -- Error: Si no existeix aquest jugador a la taula de jugadors
    SELECT COUNT(*) INTO v_count FROM JUGADOR WHERE NOM_JUGADOR = p_nom;
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'ERROR: El jugador ' || p_nom || ' no existeix.');
    END IF;

    -- Error: Si el jugador especificat no ha guardat encara cap partida
    SELECT COUNT(*) INTO p_total_partides 
    FROM JUGADOR_PARTIDA JP 
    JOIN JUGADOR J ON J.ID_JUGADOR = JP.ID_JUGADOR 
    WHERE J.NOM_JUGADOR = p_nom;
    
    IF p_total_partides = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'ERROR: El jugador ' || p_nom || ' encara no ha guardat cap partida.');
    END IF;

    SELECT VICTORIES INTO p_vics FROM JUGADOR WHERE NOM_JUGADOR = p_nom;
END;
/
