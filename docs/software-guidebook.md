Løsningsbeskrivelse for OS2 OpgaveFordeler
===

1. Introduktion
===
Dette skriv giver et overblik for hvordan det er tænkt at lave OS2 OpgaveFordeler. Det inkluderer følgende:

1. Krav, constraints og principper.
2. Software arkitekturen.

2. Kontekst
===
OS2 OpgaveFordeler laves som java applikation med REST interface samt tilhørende webapplikation. REST interface giver mulighed for opslag for mapningen mellem KLE-numre og ansvarlig entitet for håndtering af denne.
Webapplikationen giver mulighed for at vedligholde de tilgængelige data/relationer.
Følgende konteksdiagram viser applikation og aktører:
![Kontekstdiagram for OS2 OpgaveFordeler](img/context-diagram.png "Kontekstdiagram OS2 OpgaveFordeler")
Formålet med applikationen er at udstille og vedligholde data for mapning mellem en kommunes opgaver og dem der er ansvarlige for at udføre dem.

Brugere
---
Der er tre slags brugere af applikationen

**1. Sysadmin**
Denne bruger kan administrere de forskellige kommuners opsætning.

**2. Kommuneadmin**
Denne administrator kan vedligholde en kommunes organisationsstruktur.

**3. Bruger**
Denne bruger kan lave fordelinger af samt flytte ansvar for opgaver. Dette er det typiske scenarie.

Eksterne systemer
---
Der er flere eksterne systemer som interagerer med OS2 OpgaveFordeler.

**1. KL Mox agent** Dette system leverer KLE strukturen som repræsenterer en kommunernes opgaver.

**2. Kommuners Mox agent** Disse systemer leverer information om kommuners organisationsstruktur.

**3. Kommunale systemer** Disse systemer trækker oplysninger om en kommunes delegering af et KLE-nummer.

3. Funktionelt Overblik
===
OS2 OpgaveFordeler handler om en kommunes forbindelse mellem KLE-numre og den entitet der håndterer det pågældende emne hvad enten det er en afdeling eller en medarbejder.

KLE-numre
---
KLE-numre kommer fra KL og indlæses via mox agent eller en sysadmin. KLE-strukturen er fælles for alle kommuner.

Kommuner og Organisationsstruktur
---
Hver kommune kan oprettes som selvstændig enhed i applikationen. De har administratorer som vedligholder kommunens organisationsstruktur og har derudover brugere der vedligholder mapning mellem KLE-numre og afdelinger eller medarbejdere.

Kommuner oprettes og vedligholdes overordnet set af sysadmin.

REST interface
---
Oplysninger om hvem der håndterer KLE-numre for en kommune, kan trækkes via REST/JSON til brug ved integration.

4. Quality Attributes/Nonfunktionelle krav
===
Performance
---
Alle funktioner i OS2 OpgaveFordeler skal svare på under n sekunder for n samtidige brugere.
REST interface skal kunne håndtere nn/s.

Scalability
---
OS2 OpgaveFordeler skal kunne skalere til n gange forventet anvendelse.

- 10 gange eksisterende KLE-numre (nn).
- nn kommuner med:
  - nnn afdelinger.
  - nnn brugere.
  - nnn fordelinger.

Availability
---
Applikationen skal være tilgængelig ...

Internationalisation
---
Webapplikationen leveres på dansk. I REST interface vil der benyttes engelsk terminologi.

Localisation
---
Webapplikation kører dansk. REST interface engelsk.

Browser kompatabilitet
---
Webapplikationen skal virke på nyeste versioner af følgende browsere med javascript aktiveret:

- Google Chrome
- Internet Explorer
- Firefox
- Safari

5. Constraints
===
OS2 OpgaveFordeler skal udvikles med open source teknologi. Der er valgt Java og js.

Deployment på Apache webserver og JBoss Wildfly/EAP6. 

Der skal benyttes SAML til authentication.

6. Principper
===
Id'er defineres med long.

Continuos integration
---
Der etableres continuos integration til web interface og java applikation.

Automated testing
---
Der etableres automatisk afvikling af unit tests.

Configuration
---
Al konfiguration vil foregå i .properties filer for at holde det ude af applikatonen og styre deployment på tværs af miljøer.

7. Software Arkitektur
===
Her følger et overblik over arkitekturen.

Containers
---
Dette diagram viser de forskellige dele af applikationen fordelt på containere. Opdeling er kun på typer af container og repræsenterer ikke antal.
![Containerdiagram for OS2 OpgaveFordeler](img/container-diagram.png "Containerdiagram OS2 OpgaveFordeler")

**Webapplikationen** hostet på Apache giver mulighed for at vedligholde fordelinger, organisationsstrukturer samt KLE. Det er en applikation lavet i Angular/HTML5 som benytter REST interfacet i java applikationen.

**Java applikationen** hostes på JBoss og sørger for alt omkring authentication, authorization og udstiller via REST, interface til opslag og vedlighold på KLE, orgstrukturer samt regler for fordeling.

**Relationel db** i form af PostgreSQL benyttes til persistens fra Java applikationen.

Komponenter i Java applikation
---
De tre primære komponenter i Java applikationen omhandler KLE, Kommuners organisationsstruktur samt selve fordelingerne.
![Componentdiagram for OS2 OpgaveFordeler](img/component-diagram.png "Componentdiagram OS2 OpgaveFordeler")


