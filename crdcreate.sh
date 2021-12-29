#!/bin/bash
rm create_crdb.sh
tee -a create_crdb.sh << EOF
/opt/redislabs/bin/crdb-cli crdb create --name sample-crdb --memory-size 50mb --default-db-config '{ "port": 12000, "replication": false}' --instance fqdn=cluster1.local,username=r@r.com,password=test --instance fqdn=cluster2.local,username=r@r.com,password=test
EOF
chmod 755 create_crdb.sh
docker cp create_crdb.sh rp1:/opt/create_crdb.sh
docker exec -it rp1 bash -c "/opt/create_crdb.sh"
docker exec -it rp1 bash -c "rladmin status databases"
