# 📘 DOCUMENTACIÓ TÈCNICA — JOC D'EN PINGU
## Part CRUD i PL/SQL (Nivell Mínim + Avançat)

---

## PART 1: SENTÈNCIES SQL INCRUSTADES (CRUD)

### 1.1 Connexió a la Base de Dades

**Fitxer:** `GestorBBDD.java` (línies 58-73)

La connexió s'estableix via JDBC amb fallback automàtic entre la xarxa local i la remota.

```java
private Connection conectarDirecte(String url, String user, String pwd) {
    Connection con = null;
    try {
        Class.forName("oracle.jdbc.OracleDriver");
        DriverManager.setLoginTimeout(5);
        con = DriverManager.getConnection(url, user, pwd);
    } catch (Exception e) {
        logsConnexio.add("ERROR CONNEXIÓ (" + url + "): " + e.getMessage());
    }
    return con;
}
```

---

### 1.2 CREATE — Registre d'Usuaris

**Fitxer:** `GestorBBDD.java` (línies 211-228) · **Crida des de:** `PantallaLogin.java` (línia 108)

**SQL:** `INSERT INTO jugador (id_jugador, nom_jugador, victories, contrasenya) VALUES (...)`

```java
// GestorBBDD.java — registrarUsuario()
public boolean registrarUsuario(String username, String password) {
    // 1. SELECT per comprovar si l'usuari ja existeix
    ArrayList<...> exist = select(conexion,
        "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'");
    if (exist.isEmpty()) {
        // 2. SELECT per obtenir el següent ID disponible
        ArrayList<...> res = select(conexion,
            "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
        int nextId = /* ... */ ;
        // 3. INSERT per crear el nou jugador amb contrasenya encriptada (SHA-256)
        String sql = "INSERT INTO jugador (id_jugador, nom_jugador, victories, contrasenya) "
                   + "VALUES (" + nextId + ", '" + username + "', 0, '" + hashPw + "')";
        ok = executeInsUpDel(conexion, sql, "REGISTRE") > 0;
    }
    return ok;
}
```

**Des del joc:** L'usuari omple el formulari de registre a `PantallaLogin`, on es valida longitud mínima i coincidència de contrasenyes.

---

### 1.3 READ — Login i Consultes

#### 1.3.1 Validació de Credencials

**Fitxer:** `GestorBBDD.java` (línies 258-267) · **Crida des de:** `PantallaLogin.java` (línia 77)

```java
// SQL: SELECT amb filtre per nom i contrasenya hashejada
"SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username
    + "' AND contrasenya = '" + hashPw + "'"
```

#### 1.3.2 Obtenir ID del Jugador

**Fitxer:** `GestorBBDD.java` (línies 246-253)

```java
"SELECT id_jugador FROM jugador WHERE nom_jugador = '" + username + "'"
```

#### 1.3.3 Llistar Partides Guardades

**Fitxer:** `GestorBBDD.java` (línies 359-377) · **Crida des de:** `PantallaLobby.java` (línia 91)

```java
// SELECT principal amb format de data
"SELECT id_partida, nom_partida, TO_CHAR(data_creacio, 'DD/MM/YYYY') as DATA_CREACIO, "
+ "torn_actual, finalitzada FROM partida ORDER BY id_partida DESC"

// SELECT addicional per cada partida: jugadors associats
"SELECT j.nom_jugador FROM jugador j "
+ "JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador "
+ "WHERE jp.id_partida = " + idP
```

#### 1.3.4 Carregar una Partida

**Fitxer:** `GestorBBDD.java` (línies 320-354) · **Crida des de:** `PantallaLobby.java` (línia 195)

```java
// 1. Dades de la partida
"SELECT * FROM partida WHERE id_partida = " + id

// 2. Jugadors i posicions
"SELECT j.nom_jugador, jp.posicio_actual FROM jugador j "
+ "JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador "
+ "WHERE jp.id_partida = " + id
```

---

### 1.4 UPDATE / MERGE — Guardar Partida

**Fitxer:** `GestorBBDD.java` (línies 272-315) · **Crida des de:** `PantallaJuego.java` (línia 688)

Utilitza `MERGE INTO` (UPSERT) per crear o actualitzar en una sola sentència.

```java
// MERGE de la partida
"MERGE INTO partida dst USING (SELECT " + idPartida + " AS id_p FROM dual) src "
+ "ON (dst.id_partida = src.id_p) "
+ "WHEN MATCHED THEN UPDATE SET torn_actual = " + torn + ", nom_partida = '" + nom + "' "
+ "WHEN NOT MATCHED THEN INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual) "
+ "VALUES (" + idPartida + ", 1, '" + nom + "', SYSDATE, " + torn + ")"

// MERGE de cada jugador-partida (posicions)
"MERGE INTO jugador_partida dst USING (SELECT " + idJ + " AS id_j, " + idPartida + " AS id_p FROM dual) src "
+ "ON (dst.id_jugador = src.id_j AND dst.id_partida = src.id_p) "
+ "WHEN MATCHED THEN UPDATE SET posicio_actual = " + pos + " "
+ "WHEN NOT MATCHED THEN INSERT (id_jugador, id_partida, posicio_actual) VALUES (src.id_j, src.id_p, " + pos + ")"
```

---

### 1.5 Estadístiques (SELECT amb funcions d'agregació)

**Fitxer:** `GestorBBDD.java` (línies 382-424) · **Crida des de:** `PantallaEstadistiques.java`

```java
// Victòries d'un jugador
"SELECT victories FROM jugador WHERE id_jugador = " + idJugador

// Mitjana global
"SELECT AVG(victories) as MITJA FROM jugador WHERE " + FILTRE_USUARIS

// Rècord màxim
"SELECT MAX(victories) as MAX_V FROM jugador WHERE " + FILTRE_USUARIS

// Rànquing (Top 10)
"SELECT nom_jugador, victories as TOTAL FROM jugador WHERE " + FILTRE_USUARIS
+ " ORDER BY victories DESC FETCH FIRST 10 ROWS ONLY"

// Jugadors amb el rècord
"SELECT nom_jugador, victories FROM jugador WHERE victories = " + max
+ " AND victories > 0 AND (" + FILTRE_USUARIS + ")"

// Jugadors sobre la mitjana
"SELECT nom_jugador, victories FROM jugador WHERE victories > " + mitja
+ " AND (" + FILTRE_USUARIS + ") ORDER BY victories DESC"

// Percentatge de jugadors superats
"SELECT COUNT(*) as TOTAL FROM jugador WHERE " + FILTRE_USUARIS
"SELECT COUNT(*) as MENYS FROM jugador WHERE victories < " + vics
+ " AND (" + FILTRE_USUARIS + ")"
```

> [!IMPORTANT]
> El filtre `FILTRE_USUARIS` exclou bots i jugadors no registrats:
> `contrasenya IS NOT NULL AND contrasenya != 'BOT_PWD' AND nom_jugador NOT LIKE 'Jugador %' ...`

---

### 1.6 Inicialització de Dades Mestres

**Fitxer:** `GestorBBDD.java` (línies 156-187)

```java
// INSERT de tipus de casella (Normal, Os, Forat, Trineu, Interrogant, SueloQuebradizo)
"INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (1, 'NORMAL', 'SENSE EFECTE')"
// ... (6 inserts per als 6 tipus)

// INSERT del taulell per defecte
"INSERT INTO taulell (id_taulell, mida_taulell) VALUES (1, 50)"

// INSERT dels bots per a persistència
"INSERT INTO jugador (id_jugador, nom_jugador, victories, contrasenya) VALUES (991, 'BOT 1', 0, 'BOT_PWD')"
```

---
---

## PART 2: PL/SQL — DOCUMENTACIÓ TÈCNICA (NIVELL MÍNIM OBLIGATORI)

**Fitxer SQL complet:** `sequencies_triggers_funcions_procs.sql`

---

### 2.1 Seqüència: SEC_ID_PARTIDA (S)

| Camp | Valor |
|---|---|
| **Nom** | `SEC_ID_PARTIDA` |
| **Objectiu** | Generar números seqüencials únics per assignar com a clau primària a les partides |
| **Quan s'usa** | Cada cop que es crea una nova partida sense ID assignat |
| **Paràmetres** | Cap — s'invoca amb `SEC_ID_PARTIDA.NEXTVAL` |
| **Retorna** | Un enter auto-incrementat |
| **Errors** | Cap controlat explícitament |

```sql
CREATE SEQUENCE SEC_ID_PARTIDA
START WITH 1
INCREMENT BY 1
NOCACHE;
```

**Bloc anònim de prova:**
```sql
BEGIN
    DBMS_OUTPUT.PUT_LINE('Proper ID: ' || SEC_ID_PARTIDA.NEXTVAL);
END;
/
```

---

### 2.2 Trigger: TRG_ID_PARTIDA (T)

| Camp | Valor |
|---|---|
| **Nom** | `TRG_ID_PARTIDA` |
| **Objectiu** | Assignar automàticament el nº seqüencial a la clau primària de `PARTIDA` |
| **Quan es dispara** | `BEFORE INSERT ON PARTIDA` — per cada fila |
| **Què el fa disparar** | Un `INSERT` a la taula `PARTIDA` on `ID_PARTIDA` arriba com a `NULL` |
| **Errors** | Cap controlat explícitament |

```sql
CREATE OR REPLACE TRIGGER TRG_ID_PARTIDA
BEFORE INSERT ON PARTIDA
FOR EACH ROW
BEGIN
  IF :NEW.ID_PARTIDA IS NULL THEN
    SELECT SEC_ID_PARTIDA.NEXTVAL INTO :NEW.ID_PARTIDA FROM DUAL;
  END IF;
END;
/
```

**Bloc anònim de prova:**
```sql
BEGIN
    INSERT INTO PARTIDA (id_partida, id_taulell, nom_partida, data_creacio, torn_actual)
    VALUES (NULL, 1, 'TEST_TRIGGER', SYSDATE, 1);
    DBMS_OUTPUT.PUT_LINE('Partida inserida amb ID auto-assignat.');
    ROLLBACK;
END;
/
```

---

### 2.3 Trigger: TRG_ACTUALITZA_VICTORIES (T)

| Camp | Valor |
|---|---|
| **Nom** | `TRG_ACTUALITZA_VICTORIES` |
| **Objectiu** | Incrementar en 1 les victòries del guanyador quan una partida es marca com finalitzada |
| **Quan es dispara** | `AFTER UPDATE OF FINALITZADA ON PARTIDA` — per cada fila |
| **Què el fa disparar** | Un `UPDATE` que canvia `FINALITZADA` de 0 a 1 i `ID_GUANYADOR` no és null |
| **Errors** | Cap controlat explícitament |

```sql
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
```

**Bloc anònim de prova:**
```sql
DECLARE
    v_id_partida NUMBER;
    v_id_jugador NUMBER := 1; -- ID d'un jugador existent
BEGIN
    SELECT MAX(id_partida) INTO v_id_partida FROM PARTIDA WHERE finalitzada = 0;
    UPDATE PARTIDA SET finalitzada = 1, id_guanyador = v_id_jugador
    WHERE id_partida = v_id_partida;
    DBMS_OUTPUT.PUT_LINE('Trigger disparat. Victòries actualitzades.');
    ROLLBACK;
END;
/
```

---

### 2.4 Funció: GET_MAX_VICTORIES_RECORD (F)

| Camp | Valor |
|---|---|
| **Nom** | `GET_MAX_VICTORIES_RECORD` |
| **Objectiu** | Obtenir el màxim nº de partides guanyades (rècord global) |
| **Paràmetres** | Cap |
| **Retorna** | `NUMBER` — el valor màxim de victòries, o 0 si no n'hi ha |

```sql
CREATE OR REPLACE FUNCTION GET_MAX_VICTORIES_RECORD
RETURN NUMBER IS
    v_max NUMBER;
BEGIN
    SELECT MAX(VICTORIES) INTO v_max FROM JUGADOR;
    RETURN NVL(v_max, 0);
END;
/
```

**Bloc anònim:**
```sql
BEGIN
    DBMS_OUTPUT.PUT_LINE('Rècord de victòries: ' || GET_MAX_VICTORIES_RECORD);
END;
/
```

---

### 2.5 Procediment: GET_JUGADORS_RECORD (P)

| Camp | Valor |
|---|---|
| **Nom** | `GET_JUGADORS_RECORD` |
| **Objectiu** | Obtenir els jugadors que tenen el rècord de victòries |
| **Paràmetres** | `p_cursor OUT SYS_REFCURSOR` |
| **Retorna** | Cursor amb `NOM_JUGADOR` i `VICTORIES` |

```sql
CREATE OR REPLACE PROCEDURE GET_JUGADORS_RECORD(p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT NOM_JUGADOR, VICTORIES FROM JUGADOR
        WHERE VICTORIES = (SELECT MAX(VICTORIES) FROM JUGADOR) AND VICTORIES > 0;
END;
/
```

**Bloc anònim:**
```sql
DECLARE
    v_cursor SYS_REFCURSOR;
    v_nom VARCHAR2(100);
    v_vics NUMBER;
BEGIN
    GET_JUGADORS_RECORD(v_cursor);
    LOOP
        FETCH v_cursor INTO v_nom, v_vics;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('Jugador: ' || v_nom || ' — Victòries: ' || v_vics);
    END LOOP;
    CLOSE v_cursor;
END;
/
```

---

### 2.6 Funció: GET_MITJA_VICTORIES (F)

| Camp | Valor |
|---|---|
| **Nom** | `GET_MITJA_VICTORIES` |
| **Objectiu** | Calcular la mitjana de victòries entre tots els jugadors |
| **Paràmetres** | Cap |
| **Retorna** | `NUMBER` — la mitjana, o 0 |

```sql
CREATE OR REPLACE FUNCTION GET_MITJA_VICTORIES
RETURN NUMBER IS
    v_mitja NUMBER;
BEGIN
    SELECT AVG(VICTORIES) INTO v_mitja FROM JUGADOR;
    RETURN NVL(v_mitja, 0);
END;
/
```

**Bloc anònim:**
```sql
BEGIN
    DBMS_OUTPUT.PUT_LINE('Mitjana de victòries: ' || ROUND(GET_MITJA_VICTORIES, 2));
END;
/
```

---

### 2.7 Procediment: GET_JUGADORS_SOBRE_MITJA (P)

| Camp | Valor |
|---|---|
| **Nom** | `GET_JUGADORS_SOBRE_MITJA` |
| **Objectiu** | Mostrar jugadors amb més victòries que la mitjana |
| **Paràmetres** | `p_cursor OUT SYS_REFCURSOR` |

```sql
CREATE OR REPLACE PROCEDURE GET_JUGADORS_SOBRE_MITJA(p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT NOM_JUGADOR, VICTORIES FROM JUGADOR
        WHERE VICTORIES > (SELECT AVG(VICTORIES) FROM JUGADOR)
        ORDER BY VICTORIES DESC;
END;
/
```

**Bloc anònim:**
```sql
DECLARE
    v_cursor SYS_REFCURSOR;
    v_nom VARCHAR2(100);
    v_vics NUMBER;
BEGIN
    GET_JUGADORS_SOBRE_MITJA(v_cursor);
    LOOP
        FETCH v_cursor INTO v_nom, v_vics;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE(v_nom || ': ' || v_vics || ' victòries');
    END LOOP;
    CLOSE v_cursor;
END;
/
```

---

### 2.8 Funció: PERCENTATGE_MENYS_VICTORIES (F)

| Camp | Valor |
|---|---|
| **Nom** | `PERCENTATGE_MENYS_VICTORIES` |
| **Objectiu** | Calcular el % de jugadors amb menys victòries que el valor passat |
| **Paràmetres** | `p_vics IN NUMBER` — nº de victòries de referència |
| **Retorna** | `NUMBER` — percentatge (0-100) |

```sql
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
```

**Bloc anònim:**
```sql
BEGIN
    DBMS_OUTPUT.PUT_LINE('Amb 5 victòries superes al ' ||
        ROUND(PERCENTATGE_MENYS_VICTORIES(5), 2) || '% dels jugadors.');
END;
/
```

---

### 2.9 Trigger: TRG_AVIS_RANKING (T)

| Camp | Valor |
|---|---|
| **Nom** | `TRG_AVIS_RANKING` |
| **Objectiu** | Mostrar automàticament el % de jugadors superats en actualitzar victòries |
| **Quan es dispara** | `AFTER UPDATE OF VICTORIES ON JUGADOR` — per cada fila |
| **Què el fa disparar** | Qualsevol `UPDATE` que modifiqui el camp `VICTORIES` |

```sql
CREATE OR REPLACE TRIGGER TRG_AVIS_RANKING
AFTER UPDATE OF VICTORIES ON JUGADOR
FOR EACH ROW
DECLARE
    v_perc NUMBER;
BEGIN
    v_perc := PERCENTATGE_MENYS_VICTORIES(:NEW.VICTORIES);
    DBMS_OUTPUT.PUT_LINE('RANKING: El jugador ' || :NEW.NOM_JUGADOR
        || ' ara supera al ' || ROUND(v_perc, 2) || '% dels jugadors.');
END;
/
```

---

### 2.10 Procediment: RANKING_PARTIDES_TOTALS (P)

| Camp | Valor |
|---|---|
| **Nom** | `RANKING_PARTIDES_TOTALS` |
| **Objectiu** | Mostrar rànquing de jugadors per total de partides jugades |
| **Paràmetres** | `p_cursor OUT SYS_REFCURSOR` |

```sql
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
```

**Bloc anònim:**
```sql
DECLARE
    v_cursor SYS_REFCURSOR;
    v_nom VARCHAR2(100);
    v_total NUMBER;
BEGIN
    RANKING_PARTIDES_TOTALS(v_cursor);
    DBMS_OUTPUT.PUT_LINE('=== RANKING PARTIDES TOTALS ===');
    LOOP
        FETCH v_cursor INTO v_nom, v_total;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE(v_nom || ': ' || v_total || ' partides');
    END LOOP;
    CLOSE v_cursor;
END;
/
```

---

### 2.11 Procediment: CONSULTAR_ESTADISTIQUES_JUGADOR (P) — Amb control d'errors

| Camp | Valor |
|---|---|
| **Nom** | `CONSULTAR_ESTADISTIQUES_JUGADOR` |
| **Objectiu** | Consultar la posició d'un jugador al rànquing amb validació |
| **Paràmetres** | `p_nom IN VARCHAR2`, `p_vics OUT NUMBER`, `p_total_partides OUT NUMBER` |
| **Errors controlats** | `-20001`: El jugador no existeix · `-20002`: No ha guardat cap partida |

```sql
CREATE OR REPLACE PROCEDURE CONSULTAR_ESTADISTIQUES_JUGADOR(
    p_nom IN VARCHAR2,
    p_vics OUT NUMBER,
    p_total_partides OUT NUMBER
) IS
    v_count NUMBER;
BEGIN
    -- Error 1: jugador inexistent
    SELECT COUNT(*) INTO v_count FROM JUGADOR WHERE NOM_JUGADOR = p_nom;
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'ERROR: El jugador ' || p_nom || ' no existeix.');
    END IF;

    -- Error 2: sense partides guardades
    SELECT COUNT(*) INTO p_total_partides
    FROM JUGADOR_PARTIDA JP JOIN JUGADOR J ON J.ID_JUGADOR = JP.ID_JUGADOR
    WHERE J.NOM_JUGADOR = p_nom;
    IF p_total_partides = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'ERROR: El jugador ' || p_nom
            || ' encara no ha guardat cap partida.');
    END IF;

    SELECT VICTORIES INTO p_vics FROM JUGADOR WHERE NOM_JUGADOR = p_nom;
END;
/
```

**Bloc anònim (cas correcte):**
```sql
DECLARE
    v_vics NUMBER;
    v_total NUMBER;
BEGIN
    CONSULTAR_ESTADISTIQUES_JUGADOR('admin', v_vics, v_total);
    DBMS_OUTPUT.PUT_LINE('Victòries: ' || v_vics || ' | Partides: ' || v_total);
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE(SQLERRM);
END;
/
```

**Bloc anònim (cas error -20001):**
```sql
DECLARE
    v_vics NUMBER; v_total NUMBER;
BEGIN
    CONSULTAR_ESTADISTIQUES_JUGADOR('JUGADOR_INEXISTENT', v_vics, v_total);
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error capturat: ' || SQLERRM);
END;
/
```

---

### Taula Resum de Components PL/SQL

| # | Funcionalitat | Tipus | Component |
|---|---|---|---|
| 1 | Generar números seqüencials | **S** | `SEC_ID_PARTIDA` |
| 2 | Assignar ID automàtic a partides | **T** | `TRG_ID_PARTIDA` |
| 3 | Incrementar victòries del guanyador | **T** | `TRG_ACTUALITZA_VICTORIES` |
| 4 | Obtenir rècord de victòries | **F** | `GET_MAX_VICTORIES_RECORD` |
| 5 | Obtenir jugadors amb el rècord | **P** | `GET_JUGADORS_RECORD` |
| 6 | Mitjana de victòries | **F** | `GET_MITJA_VICTORIES` |
| 7 | Jugadors sobre la mitjana | **P** | `GET_JUGADORS_SOBRE_MITJA` |
| 8 | Percentatge de jugadors superats | **F** | `PERCENTATGE_MENYS_VICTORIES` |
| 9 | Avís automàtic de rànquing | **T** | `TRG_AVIS_RANKING` |
| 10 | Rànquing per partides totals | **P** | `RANKING_PARTIDES_TOTALS` |
| 11 | Estadístiques amb control d'errors | **P** | `CONSULTAR_ESTADISTIQUES_JUGADOR` |
