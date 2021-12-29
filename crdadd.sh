guid=$1
docker exec -it rp1 bash -c "/opt/redislabs/bin/crdb-cli crdb add-instance --crdb-guid $guid --instance fqdn=cluster3.local,username=r@r.com,password=test"
