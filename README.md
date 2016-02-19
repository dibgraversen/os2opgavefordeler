Læs mig
====

Demo miljø
---
[https://os2opgavefordeler-test.miracle.dk](https://os2opgavefordeler-test.miracle.dk)

Kræver en gyldig brugerkonto.


Produktions miljø
---
[https://os2opgavefordeler.dk](https://os2opgavefordeler.dk)

Kræver en gyldig brugerkonto.

Oversigt
---
Applikationen består af:

 - Javascript frontend udviklet i angular.js
 - Java backend på en JBoss
 - Postgresql database
 - Liquibase til database migrationer
 - Puppet til miljø styring

Opsætning af udviklingsmiljø
---
1. Stå i roden af dette projekt
2. vagrant up
3. vent
4. mvn clean install
5. bash ./bin/dev_deploy.sh

Miljøet kan tilgås på: [http://localhost:1080](http://localhost:1080) .

Wildfly lytter på: [http://localhost:8080/](http://localhost:8080/) .

 - Bruger: mgmtuser
 - Password: mgmtuser

Loggen ligger i /var/log/wildfly/console.log - du kan bruge:
```
 vagrant ssh -c "tail -f /var/log/wildfly/console.log"
```
Postgres lytter på port 5432 .

 - Bruger: topicrouter
 - Password: SuperSaltFisk


For at replikere frontenden fra din maskine til vagrant maskinen kan du køre:
```
vagrant rsync-auto
```
Så vil koden blive lagt over hver gang du gemmer.

God fornøjelse


