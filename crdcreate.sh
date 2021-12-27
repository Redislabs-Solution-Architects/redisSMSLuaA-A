#!/bin/bash
rm create_crdb.sh
tee -a create_crdb.sh << EOF
/opt/redislabs/bin/crdb-cli crdb create --name sample-crdb --memory-size 500mb --default-db-config '{ "port": 14555, "replication": false}' --instance fqdn=cluster1.local,username=jason.haugland@redislabs.com,password=redis123 --instance fqdn=cluster2.local,username=jason.haugland@redislabs.com,password=jasonrocks
EOF
chmod 755 create_crdb.sh
docker cp create_crdb.sh re-node1:/opt/create_crdb.sh
docker exec -it re-node1 bash -c "/opt/create_crdb.sh"
docker exec -it re-node1 bash -c "rladmin status databases"
