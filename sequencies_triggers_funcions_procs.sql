-- ============================================================
-- SCRIPT SQL: Joc del Pingu - Sequencies, Triggers, Funcions i Procediments
-- Autor: Antigravity (AI Assistant)
-- ============================================================

-- 1. SEQUEÈNCIA (S)
-- Generar números seqüencials per a la taula de partides.
CREATE SEQUENCE SEC_ID_PARTIDA
START WITH 100
INCREMENT BY 1
NOCACHE;

-- 2. TRIGGER (T)
-- Assignar automàticament el nº seqüencial a la clau primària de la taula partida.
CREATE OR REPLACE TRIGGER TRG_ID_PARTIDA
BEFORE INSERT ON PARTIDA
FOR EACH ROW
BEGIN
  IF :NEW.ID_PARTIDA IS NULL THEN
    SELECT SEC_ID_PARTIDA.NEXTVAL INTO :NEW.ID_PARTIDA FROM DUAL;
  END IF;
END;
/

-- 3. FUNCIÓ (F): Quantitat de partides guanyades
-- Obtenir la quantitat de partides que ha guanyat un determinat jugador.
CREATE OR REPLACE FUNCTION GET_VICTORIES_JUGADOR(p_id_jugador IN NUMBER) 
RETURN NUMBER IS
    v_victories NUMBER;
BEGIN
    SELECT victories INTO v_victories FROM jugador WHERE id_jugador = p_id_jugador;
    RETURN v_victories;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN -1; -- Jugador no existeix
END;
/

-- 4. FUNCIÓ (F): Record personal (puntuació màxima)
-- Obtenir la puntuació màxima (peixos) d'un determinat jugador.
CREATE OR REPLACE FUNCTION GET_RECORD_JUGADOR(p_id_jugador IN NUMBER) 
RETURN NUMBER IS
    v_record NUMBER;
BEGIN
    SELECT MAX(peixos) INTO v_record 
    FROM jugador_partida 
    WHERE id_jugador = p_id_jugador;
    
    RETURN NVL(v_record, 0);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN -1;
END;
/

-- 5. FUNCIÓ (F): Mitja de puntuació global
-- Obtenir la mitja de puntuació (peixos) d'entre totes les partides jugades.
CREATE OR REPLACE FUNCTION GET_MITJA_PUNTUACIO_GLOBAL
RETURN NUMBER IS
    v_mitja NUMBER;
BEGIN
    SELECT AVG(peixos) INTO v_mitja FROM jugador_partida;
    RETURN NVL(v_mitja, 0);
END;
/

-- 6. PROCEDIMENT (P): Ranking per partides jugades
-- Mostra el ranking de jugadors ordenats pel total de partides jugades.
-- Control d'errors: jugador no existeix o sense partides.
CREATE OR REPLACE PROCEDURE RANKING_PARTIDES_JUGADES(p_id_jugador_check IN NUMBER DEFAULT NULL) IS
    v_count NUMBER;
BEGIN
    -- Control d'error si es passa un jugador específic per comprovar
    IF p_id_jugador_check IS NOT NULL THEN
        SELECT COUNT(*) INTO v_count FROM jugador WHERE id_jugador = p_id_jugador_check;
        IF v_count = 0 THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: El jugador amb ID ' || p_id_jugador_check || ' no existeix.');
            RETURN;
        END IF;
        
        SELECT COUNT(*) INTO v_count FROM jugador_partida WHERE id_jugador = p_id_jugador_check;
        IF v_count = 0 THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: El jugador ' || p_id_jugador_check || ' encara no ha jugat cap partida.');
            RETURN;
        END IF;
    END IF;

    DBMS_OUTPUT.PUT_LINE('--- RANKING: PARTIDES JUGADES ---');
    FOR r IN (
        SELECT j.nom_jugador, COUNT(jp.id_partida) as total
        FROM jugador j
        LEFT JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador
        GROUP BY j.nom_jugador
        ORDER BY total DESC
    ) LOOP
        DBMS_OUTPUT.PUT_LINE(r.nom_jugador || ': ' || r.total || ' partides');
    END LOOP;
END;
/

-- 7. PROCEDIMENT (P): Ranking per record personal
-- Mostra el ranking de jugadors ordenats pel seu record personal (max peixos).
CREATE OR REPLACE PROCEDURE RANKING_RECORD_PERSONAL(p_id_jugador_check IN NUMBER DEFAULT NULL) IS
    v_count NUMBER;
BEGIN
    IF p_id_jugador_check IS NOT NULL THEN
        SELECT COUNT(*) INTO v_count FROM jugador WHERE id_jugador = p_id_jugador_check;
        IF v_count = 0 THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: El jugador amb ID ' || p_id_jugador_check || ' no existeix.');
            RETURN;
        END IF;
        
        SELECT COUNT(*) INTO v_count FROM jugador_partida WHERE id_jugador = p_id_jugador_check;
        IF v_count = 0 THEN
            DBMS_OUTPUT.PUT_LINE('ERROR: El jugador ' || p_id_jugador_check || ' encara no ha guardat cap partida.');
            RETURN;
        END IF;
    END IF;

    DBMS_OUTPUT.PUT_LINE('--- RANKING: RECORD PERSONAL (PEIXOS) ---');
    FOR r IN (
        SELECT j.nom_jugador, MAX(jp.peixos) as record
        FROM jugador j
        JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador
        GROUP BY j.nom_jugador
        ORDER BY record DESC
    ) LOOP
        DBMS_OUTPUT.PUT_LINE(r.nom_jugador || ': ' || r.record || ' peixos');
    END LOOP;
END;
/

-- 8. PROCEDIMENT (P): Jugadors amb record superior a la mitja
-- Mostra la llista de jugadors que han tret un record superior a la mitja global.
CREATE OR REPLACE PROCEDURE JUGADORS_SUPERIOR_MITJA IS
    v_mitja NUMBER;
BEGIN
    v_mitja := GET_MITJA_PUNTUACIO_GLOBAL();
    DBMS_OUTPUT.PUT_LINE('--- JUGADORS AMB RECORD SUPERIOR A LA MITJA (' || ROUND(v_mitja, 2) || ') ---');
    
    FOR r IN (
        SELECT j.nom_jugador, MAX(jp.peixos) as record
        FROM jugador j
        JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador
        GROUP BY j.nom_jugador
        HAVING MAX(jp.peixos) > v_mitja
        ORDER BY record DESC
    ) LOOP
        DBMS_OUTPUT.PUT_LINE(r.nom_jugador || ': ' || r.record);
    END LOOP;
END;
/

-- 9. FUNCIÓ (F): Percentatge de jugadors amb menys puntuació
-- Passant una puntuació, calcular el percentatge de jugadors que han obtingut menys puntuació (record).
CREATE OR REPLACE FUNCTION PERCENTATGE_MENYS_PUNTUACIO(p_puntuacio IN NUMBER) 
RETURN NUMBER IS
    v_total_jugadors NUMBER;
    v_jugadors_menys NUMBER;
BEGIN
    SELECT COUNT(DISTINCT id_jugador) INTO v_total_jugadors FROM jugador_partida;
    
    IF v_total_jugadors = 0 THEN RETURN 0; END IF;
    
    SELECT COUNT(*) INTO v_jugadors_menys
    FROM (
        SELECT id_jugador, MAX(peixos) as record
        FROM jugador_partida
        GROUP BY id_jugador
    ) WHERE record < p_puntuacio;
    
    RETURN (v_jugadors_menys / v_total_jugadors) * 100;
END;
/
