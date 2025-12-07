# 5DON4D Project

## Démarrage du Projet

Le projet peut être démarré facilement en utilisant le script `run.sh` ou manuellement.

### Prérequis

Pour exécuter le projet, les outils suivants doivent être installés et accessibles :

* **Docker**
* **Maven** (`mvn`)
* **Angular CLI** (`ng`)

---

### Lancement

Le script `run.sh` automatise le lancement de tous les services backend via Docker et des applications Spring Boot et Angular.

1.  Assurez-vous d'avoir donné les permissions d'exécution au script :
    ```bash
    chmod +x run.sh
    ```
2.  Lancez le script :
    ```bash
    ./run.sh
    ```

### URLs d'Accès
Une fois le projet démarré, vous pouvez accéder aux services aux adresses suivantes :

- API Backend (Spring Boot) : http://localhost:8080/

- Application Frontend (Angular) : http://localhost:4200/

- Interface Web MongoDB (Mongo Express) : http://localhost:8081/

- Interface Web Neo4j Browser : http://localhost:7474/

- API Elasticsearch : http://localhost:9200/


### Informations Techniques
- Backend : Spring Boot

- Frontend : Angular 16

- Bases de données : MongoDB, Neo4j, Redis, Elasticsearch