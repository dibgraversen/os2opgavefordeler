#!/usr/bin/env bash
echo "Input password for production:"
ssh 10.7.4.78 -l miracle -C 'pg_dump -h 127.0.0.1 -U topicrouter -F c --data-only -T databasechangelog -T databasechangeloglock --password topicrouter' > data

echo "Promoting user: topicrouter to superuser"
ssh localhost -p 2223 -l vagrant -C "sudo su -l postgres -c \"echo ALTER USER topicrouter WITH SUPERUSER | psql\""
ssh localhost -p 2223 -l vagrant -C "psql -h localhost -U topicrouter -d topicrouter --password -c 'TRUNCATE databasechangeloglock'"


echo "Input password for development (SuperSaltFisk):"
ssh localhost -p 2223 -l vagrant -C "pg_restore -1 --data-only -h localhost -U topicrouter -d topicrouter --password --disable-triggers sync/data"

echo "Demoting user: topicrouter as superuser"
ssh localhost -p 2223 -l vagrant -C "sudo su -l postgres -c \"echo ALTER USER topicrouter WITH SUPERUSER | psql\""