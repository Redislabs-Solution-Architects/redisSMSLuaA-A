guid=$1
docker exec -it rp1 bash -c "/opt/redislabs/bin/crdb-cli crdb flush --crdb-guid $guid"
