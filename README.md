# familie-ks-mottak
Mottaksapplikasjon for kontantstøtte-søknader

## Bygging lokalt
Appen kjører på Java 11. Bygging gjøres ved å kjøre `mvn clean install`. 

## Kjøring og testing lokalt
For å kjøre opp appen lokalt kan en kjøre `DevLauncher` med Spring-profilen `dev` satt. Dette kan feks gjøres ved å sette
`-Dspring.profiles.active=dev` under Edit Configurations -> VM Options. 

Appen er da tilgjengelig under `localhost:8084`.

Dersom man vil gå mot endepunkter som krever autentisering lokalt, kan man få et testtoken ved å gå mot `localhost:8084/local/cookie`. 


### Postgres
For å kjøre med postgres lokalt, kan man kjøre `docker run --name mottak -p 5432:5432 -e POSTGRES_PASSWORD=mottak -e POSTGRES_USER=mottak -d postgres`. Databasen kan da settes opp i feks IntelliJ med å bruke verdiene 
i `application-postgresql.yaml`. DevLauncher må kjøres opp med profilen `postgresql`, og `dev`-profilen må deaktiveres. 
