-- Dump all data for a municipality id in "topicrouter".
-- by Hans Schou <hsc@miracle.dk> 2016-10-31
--
-- Initial SQL script "topicrouter.crontab.sql" generated by: /home/hsc/work/pg_dump_by_id/pg_dump_by_id
-- Date: 2016-10-31

-- To make a dump send this script to "psql" with some options. Example in crontab:
--    cat topicrouter.crontab.sql | psql --no-align --tuples-only --dbname=topicrouter --set=MuId=20001 | gzip -c > topicrouter.20001.$(date +%F).sql.gz

\echo
\echo -- Import this script to restore data for municipality id=:MuId;
SELECT '-- municipality: ' || name FROM municipality WHERE id=:MuId;
\echo
\echo -- Syntax:
\echo --   <make a backup of all data>
\echo --   cat <this-file> | psql --dbname=<my-database>
\echo
\echo -- Example:
\echo --   pg_dump --dbname=:DBNAME > backup.sql
\echo --   zcat :DBNAME.:MuId.2016-10-31.sql.gz | psql --dbname=:DBNAME
\echo

\echo '\\set AUTOCOMMIT off'
\echo LOCK TABLE "distributionrule", "distributionrulefiltername", "orgunit", "employment", "distributionrulefilter", "role", "distributionrule_distributionrulefilter" IN EXCLUSIVE MODE;
\echo

\echo -- Delete data
\echo UPDATE orgunit SET isactive=FALSE WHERE municipality_id=:MuId;
\echo UPDATE employment SET isactive=FALSE WHERE municipality_id=:MuId;

\echo DELETE FROM distributionrule_distributionrulefilter WHERE filters_id IN (SELECT id FROM distributionrulefilter drf WHERE drf.assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE AND municipality_id=:MuId));
\echo DELETE FROM distributionrule_distributionrulefilter WHERE distributionrule_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId)));
\echo DELETE FROM distributionrule_distributionrulefilter WHERE distributionrule_id IN (SELECT id FROM distributionrule WHERE parent_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId))));
\echo DELETE FROM distributionrule_distributionrulefilter WHERE filters_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE AND municipality_id=:MuId));
\echo DELETE FROM distributionrule_distributionrulefilter WHERE distributionrule_id IN (SELECT id FROM distributionrule WHERE assignedorg_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId));

\echo UPDATE distributionrule SET parent_id = NULL WHERE responsibleorg_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId) OR assignedorg_id IN (SELECT id FROM employment WHERE isactive = FALSE AND municipality_id=:MuId);
\echo DELETE FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE AND municipality_id=:MuId);
\echo DELETE FROM distributionrule WHERE responsibleorg_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId) OR assignedorg_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId);

\echo DELETE FROM distributionrulefilter drf WHERE drf.distributionrule_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE AND municipality_id=:MuId));
\echo DELETE FROM distributionrulefilter drf WHERE drf.assignedemp IN (SELECT id FROM employment WHERE isactive = FALSE AND municipality_id=:MuId);
\echo DELETE FROM distributionrulefilter drf WHERE drf.assignedorg_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId);

\echo DELETE FROM role WHERE employment_id IN (SELECT id FROM employment WHERE isactive = FALSE AND municipality_id=:MuId);
\echo UPDATE orgunit SET manager_id = NULL WHERE manager_id IN (SELECT id FROM employment WHERE isactive = FALSE AND municipality_id=:MuId);

\echo UPDATE orgunit SET manager_id = NULL WHERE manager_id IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId));

\echo DELETE FROM role WHERE employment_id IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId));

\echo UPDATE distributionrule SET parent_id = NULL WHERE assignedorg_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId) OR responsibleorg_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId);
\echo DELETE FROM distributionrule WHERE parent_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId)));
\echo DELETE FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId));

\echo DELETE FROM distributionrulefilter WHERE assignedemp IN (SELECT id FROM employment WHERE employedin_id IN (SELECT id FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId));

\echo DELETE FROM employment WHERE isactive = FALSE AND municipality_id=:MuId;
\echo DELETE FROM orgunit WHERE isactive = FALSE AND municipality_id=:MuId;

\echo DELETE FROM distributionrulefiltername WHERE municipality_id=:MuId;
\echo DELETE FROM distributionrule WHERE municipality_id=:MuId;
\echo

\echo -- Remove CONSTRAINT to use "COPY FROM stdin"
\echo ALTER TABLE distributionrule DROP CONSTRAINT distributionrule2orgunit;
\echo ALTER TABLE distributionrule DROP CONSTRAINT distributionrule2orgunit4assigned;
\echo ALTER TABLE distributionrule_distributionrulefilter DROP CONSTRAINT "distributionrule_distributionrulefilter2DistributionRule";
\echo ALTER TABLE distributionrule_distributionrulefilter DROP CONSTRAINT "distributionrule_distributionrulefilter2filters";
\echo ALTER TABLE distributionrulefilter DROP CONSTRAINT "distributionrulefilter2assignedOrg";
\echo ALTER TABLE distributionrulefilter DROP CONSTRAINT distributionrulefilter2assignedemp;
\echo ALTER TABLE employment DROP CONSTRAINT employment2orgunit;
\echo ALTER TABLE orgunit DROP CONSTRAINT orgunit2employment;
\echo ALTER TABLE orgunit DROP CONSTRAINT orgunit2orgunit;
\echo ALTER TABLE role DROP CONSTRAINT role2employment;
\echo

-- TABLE:  employment
-- FIELDS: id, businesskey, email, esdhid, esdhlabel, initials, isactive, jobtitle, name, phone, employedin_id, municipality_id
-- WHERE:  municipality_id=:MuId
\echo
\echo COPY "employment" (id, businesskey, email, esdhid, esdhlabel, initials, isactive, jobtitle, name, phone, employedin_id, municipality_id) FROM stdin;
COPY (SELECT id, businesskey, email, esdhid, esdhlabel, initials, isactive, jobtitle, name, phone, employedin_id, municipality_id FROM "employment" WHERE municipality_id=:MuId ORDER BY id) TO stdout;
\echo '\\.'

-- TABLE:  orgunit
-- FIELDS: id, businesskey, email, esdhid, esdhlabel, isactive, name, phone, manager_id, municipality_id, parent_id
-- WHERE:  municipality_id=:MuId
\echo
\echo COPY "orgunit" (id, businesskey, email, esdhid, esdhlabel, isactive, name, phone, manager_id, municipality_id, parent_id) FROM stdin;
COPY (SELECT id, businesskey, email, esdhid, esdhlabel, isactive, name, phone, manager_id, municipality_id, parent_id FROM "orgunit" WHERE municipality_id=:MuId ORDER BY id) TO stdout;
\echo '\\.'

-- TABLE:  distributionrule
-- FIELDS: id, assignedemp, assignedorg_id, kle_id, municipality_id, parent_id, responsibleorg_id
-- WHERE:  municipality_id=:MuId
\echo
\echo COPY "distributionrule" (id, assignedemp, assignedorg_id, kle_id, municipality_id, parent_id, responsibleorg_id) FROM stdin;
COPY (SELECT id, assignedemp, assignedorg_id, kle_id, municipality_id, parent_id, responsibleorg_id FROM "distributionrule" WHERE municipality_id=:MuId ORDER BY id) TO stdout;
\echo '\\.'

-- TABLE:  distributionrulefiltername
-- FIELDS: id, name, type, default_name, municipality_id
-- WHERE:  municipality_id=:MuId
\echo
\echo COPY "distributionrulefiltername" (id, name, type, default_name, municipality_id) FROM stdin;
COPY (SELECT id, name, type, default_name, municipality_id FROM "distributionrulefiltername" WHERE municipality_id=:MuId ORDER BY id) TO stdout;
\echo '\\.'

-- TABLE:  distributionrulefilter
-- FIELDS: id, name, distributionrule_id, assignedorg_id, assignedemp, dtype, days, months, text
-- WHERE:  assignedemp IN (SELECT id FROM employment WHERE municipality_id=:MuId)
\echo
\echo COPY "distributionrulefilter" (id, name, distributionrule_id, assignedorg_id, assignedemp, dtype, days, months, text) FROM stdin;
COPY (SELECT id, name, distributionrule_id, assignedorg_id, assignedemp, dtype, days, months, text FROM "distributionrulefilter"
	WHERE assignedemp IN (SELECT id FROM employment WHERE municipality_id=:MuId)
		OR distributionrule_id IN (SELECT id FROM distributionrule WHERE assignedemp IN (SELECT id FROM employment WHERE municipality_id=:MuId))
		OR assignedorg_id IN (SELECT id FROM orgunit WHERE municipality_id=:MuId)
	ORDER BY id
) TO stdout;
\echo '\\.'

-- TABLE:  role
-- FIELDS: id, admin, manager, municipalityadmin, name, substitute, employment_id, owner_id
-- WHERE:  employment_id IN (SELECT id FROM employment WHERE municipality_id=:MuId)
\echo
\echo COPY "role" (id, admin, manager, municipalityadmin, name, substitute, employment_id, owner_id) FROM stdin;
COPY (SELECT id, admin, manager, municipalityadmin, name, substitute, employment_id, owner_id FROM "role" WHERE employment_id IN (SELECT id FROM employment WHERE municipality_id=:MuId) ORDER BY id) TO stdout;
\echo '\\.'

-- TABLE:  distributionrule_distributionrulefilter
-- FIELDS: distributionrule_id, filters_id
-- WHERE:  distributionrule_id IN (SELECT id FROM distributionrule WHERE municipality_id=:MuId)
\echo
\echo COPY "distributionrule_distributionrulefilter" (distributionrule_id, filters_id) FROM stdin;
COPY (SELECT distributionrule_id, filters_id FROM "distributionrule_distributionrulefilter" WHERE distributionrule_id IN (SELECT id FROM distributionrule WHERE municipality_id=:MuId) ORDER BY distributionrule_id, filters_id) TO stdout;
\echo '\\.'

\echo INSERT INTO auditlog (eventtime, kle, username, operation, eventtype, eventdata, orgunit, employment, municipality_id) VALUES (current_timestamp, NULL, '\''System User'\'', '\''System'\'', '\''Restore'\'', '\''Database restore'\'', NULL, NULL, :MuId);

\echo
\echo ALTER TABLE ONLY distributionrule ADD CONSTRAINT distributionrule2orgunit FOREIGN KEY (responsibleorg_id) REFERENCES orgunit(id);
\echo ALTER TABLE ONLY distributionrule ADD CONSTRAINT distributionrule2orgunit4assigned FOREIGN KEY (assignedorg_id) REFERENCES orgunit(id);
\echo ALTER TABLE ONLY distributionrule_distributionrulefilter ADD CONSTRAINT "distributionrule_distributionrulefilter2DistributionRule" FOREIGN KEY (distributionrule_id) REFERENCES distributionrule(id);
\echo ALTER TABLE ONLY distributionrule_distributionrulefilter ADD CONSTRAINT "distributionrule_distributionrulefilter2filters" FOREIGN KEY (filters_id) REFERENCES distributionrulefilter(id);
\echo ALTER TABLE ONLY distributionrulefilter ADD CONSTRAINT "distributionrulefilter2assignedOrg" FOREIGN KEY (assignedorg_id) REFERENCES orgunit(id);
\echo ALTER TABLE ONLY distributionrulefilter ADD CONSTRAINT distributionrulefilter2assignedemp FOREIGN KEY (assignedemp) REFERENCES employment(id);
\echo ALTER TABLE ONLY employment ADD CONSTRAINT employment2orgunit FOREIGN KEY (employedin_id) REFERENCES orgunit(id);
\echo ALTER TABLE ONLY orgunit ADD CONSTRAINT orgunit2employment FOREIGN KEY (manager_id) REFERENCES employment(id);
\echo ALTER TABLE ONLY orgunit ADD CONSTRAINT orgunit2orgunit FOREIGN KEY (parent_id) REFERENCES orgunit(id);
\echo ALTER TABLE ONLY role ADD CONSTRAINT role2employment FOREIGN KEY (employment_id) REFERENCES employment(id);

\echo
\echo COMMIT;
