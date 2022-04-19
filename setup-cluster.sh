#!/usr/bin/env bash

CLUSTER_NAME=cluster.redis-enterprise.com
ADMIN_U=admin@redis-enterprise.com
ADMIN_P=redis123

GREEN='\e[32;1m'
NC='\e[0m'

OK=${GREEN}ok${NC}

printf '\nBootstrap endpoint: '

until $(docker exec -it re1 sh -c "curl --output /dev/null --silent --head --fail -k https://localhost:9443/v1/bootstrap"); do
    printf '.'
    sleep 3
done
printf ' ready!\n'

printf '\nnode 1: '
docker exec -it re1 rladmin cluster create name $CLUSTER_NAME username $ADMIN_U password $ADMIN_P

printf '\nnode 2: '
docker exec -it re2 rladmin cluster join nodes 172.22.0.11 username $ADMIN_U password $ADMIN_P

printf '\nnode 3: '
docker exec -it re3 rladmin cluster join nodes 172.22.0.11 username $ADMIN_U password $ADMIN_P

printf '\nCluster setup complete!\n'


sleep 1
printf "\nChanging prompt colors for fun.\n\n"
docker exec re1 bash -c "echo -e \"export PS1='\u@\[\e[35;1m\][Node-N1]\[\e[0m\]:\[\e[35m\]\w\[\e[0m\]$ '\" >> ~/.bashrc"
docker exec re2 bash -c "echo -e \"export PS1='\u@\[\e[33;1m\][Node-N2]\[\e[0m\]:\[\e[33m\]\w\[\e[0m\]$ '\" >> ~/.bashrc"
docker exec re3 bash -c "echo -e \"export PS1='\u@\[\e[32;1m\][Node-N3]\[\e[0m\]:\[\e[32m\]\w\[\e[0m\]$ '\" >> ~/.bashrc"
echo ok

printf "\n\nCreating IP routes - wait for updates..."
docker exec --user root re1 bash -c "iptables -t nat -I PREROUTING -p udp --dport 53 -j REDIRECT --to-ports 5300  >/dev/null"
docker exec --user root re2 bash -c "iptables -t nat -I PREROUTING -p udp --dport 53 -j REDIRECT --to-ports 5300  >/dev/null"
docker exec --user root re3 bash -c "iptables -t nat -I PREROUTING -p udp --dport 53 -j REDIRECT --to-ports 5300  >/dev/null"
sleep 60
echo ok

printf "\n\nTCPDUMP install on each node.\n\n"
docker exec --user root re1 bash -c "apt-get install -y tcpdump dnsutils"
docker exec --user root re2 bash -c "apt-get install -y tcpdump dnsutils"
docker exec --user root re3 bash -c "apt-get install -y tcpdump dnsutils"
echo ok


sleep 1
echo -e "Finished!"
sleep 2
